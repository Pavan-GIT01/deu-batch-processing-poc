package com.asc.deu_batch_processing.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents an employee record with basic details and processing metadata. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
public class EmployeeRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstName;
  private String lastName;
  private String email;
  private Double salary;

  // for tracking purpose
  private String sourceFile;
  private String queueType;
  private Instant processedAt;
}
