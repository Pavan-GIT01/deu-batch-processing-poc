package com.asc.deu_batch_processing.repository;

import com.asc.deu_batch_processing.entity.EmployeeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeRecord, Long > {
}
