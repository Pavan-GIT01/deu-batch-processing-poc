package com.asc.deu_batch_processing.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchQueueListener {

    private final FileProcessingService fileProcessingService;

    /**
     * Listen to Batch Queue - large/bulk files
     *
     */
    @SqsListener(value = "${app.sqs.batch-queue-url}")
    public void listen(String message) throws Exception {
        log.info("===BATCH QUEUE message received===");
        fileProcessingService.processMessage(message,"BATCH");
    }
}
