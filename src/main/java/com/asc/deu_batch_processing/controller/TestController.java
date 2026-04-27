package com.asc.deu_batch_processing.controller;

import com.asc.deu_batch_processing.service.FileProcessingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final FileProcessingService service;

    public TestController(FileProcessingService service) {
        this.service = service;
    }

    @PostMapping
    public String test(@RequestBody String message, String queType) throws Exception {
        service.processMessage(message,queType);
        return "Processed!";
    }
}
