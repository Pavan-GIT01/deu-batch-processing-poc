package com.asc.deu_batch_processing.service;

import com.asc.deu_batch_processing.constant.BatchProcessConstant;
import com.asc.deu_batch_processing.entity.EmployeeRecord;
import com.asc.deu_batch_processing.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileProcessingService {

  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  private final EmployeeRepository repository;

  @Value("${app.s3.bucket}")
  private String bucket;

  public void processMessage(String sqsMessage, String queueType) {

    log.info("[{}] Received SQS message", queueType);
    try {

      JsonNode event = objectMapper.readTree(sqsMessage);

      // Skip s3 test events
      if (isTestEvent(event)) {
        log.info("Ignoring S3 TestEvent");
        return;
      }

      String key =
          extractS3Key(event)
              .orElseThrow(() -> new IllegalArgumentException("Missing S3 key in event"));
      log.info("Processing file from S3: bucket={}, key={}", bucket, key);

      // Download the JSON file from S3
      String json = downloadFromS3(key);
      log.info("Downloaded JSON: {}", json);

      List<EmployeeRecord> records = parseRecords(json, key, queueType);

      if (records.isEmpty()) {
        log.warn("No valid records found in file: {}", key);
        return;
      }

      int batchSize = 100;
      if ("BATCH".equalsIgnoreCase(queueType)) {
        for (int startIndex = 0; startIndex < records.size(); startIndex += batchSize) {
          int endIndex = Math.min(startIndex + batchSize, records.size());

          List<EmployeeRecord> employeeRecords = records.subList(startIndex, endIndex);
          repository.saveAll(employeeRecords);
          log.info("saved batch from {} to {} for {} a file", startIndex, endIndex, key);
        }
        log.info("total saved {} records from {}", records.size(), key);

      } else {
        repository.saveAll(records);
        log.info("Saved all {} records(dispatcher flow) from {}", records.size(), key);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed processing SQS message", e);
    }
  }

  private boolean isTestEvent(JsonNode event) {
    return "s3:TestEvent".equals(event.path(BatchProcessConstant.EVENT).asText());
  }

  // Extracts the S3 object key from an SQS message containing an S3 event notification.
  private Optional<String> extractS3Key(JsonNode event) {
    return Optional.ofNullable(event.path(BatchProcessConstant.RECORDS))
        .filter(JsonNode::isArray)
        .filter(arr -> !arr.isEmpty())
        .map(arr -> arr.get(0))
        .map(r -> r.path(BatchProcessConstant.S3).path("object").path("key").asText(null));
  }

  // Download S3 object and read entire content as UTF-8 string (loads fully into memory)
  private String downloadFromS3(String key) throws IOException {
    try (InputStream is =
        s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  // parse JSON array
  private List<EmployeeRecord> parseRecords(String json, String sourceFile, String queueType)
      throws IOException {
    // 3. Parse JSON and map to entity
    var root = objectMapper.readTree(json);
    if (!root.isArray()) {
      log.warn("Expected JSON array but got {}", root.getNodeType());
      return List.of();
    }
    return StreamSupport.stream(root.spliterator(), false)
        .map(node -> toBatchRecord(node, sourceFile, queueType))
        .flatMap(Optional::stream)
        .toList(); // Java 16+
  }

  private Optional<EmployeeRecord> toBatchRecord(
      JsonNode node, String sourceFile, String queueType) {
    var firstName = getText(node, BatchProcessConstant.FIRSTNAME);
    var lastName = getText(node, BatchProcessConstant.LASTNAME);
    var email = getText(node, BatchProcessConstant.EMAIL);
    var salary = getText(node, BatchProcessConstant.SALARY);

    if (firstName == null && lastName == null && email == null && salary == null) {
      return Optional.empty();
    }
    var record = new EmployeeRecord();
    record.setFirstName(firstName);
    record.setLastName(lastName);
    record.setEmail(email);
    record.setSalary(salary != null ? Double.parseDouble(salary) : null);

    record.setProcessedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    record.setSourceFile(sourceFile);
    record.setQueueType(queueType);
    return Optional.of(record);
  }

  private String getText(JsonNode node, String field) {
    var valueNode = node.get(field);
    return (valueNode == null || valueNode.isNull()) ? null : valueNode.asText();
  }
}
