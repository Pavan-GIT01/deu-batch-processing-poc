package com.asc.deu_batch_processing.sqslistener;

import com.asc.deu_batch_processing.service.FileProcessingService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DispatcherQueueListener {

  private final FileProcessingService fileProcessingService;

  /** Listen to Dispatcher Queue - small real-time */
  @SqsListener(value = "${app.sqs.dispatcher-queue-url}")
  public void listen(String message) throws Exception {
    log.info("===DISPATCHER QUEUE message received===");
    fileProcessingService.processMessage(message, "DISPATCHER");
  }
}
