package com.asc.deu_batch_processing.repository;

import com.asc.deu_batch_processing.entity.BatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<BatchRecord, Long > {
}
