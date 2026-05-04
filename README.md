# Batch Processing System (POC)

## Overview
This project demonstrates a scalable batch processing system using AWS and Spring Boot.

## Architecture
S3 → Lambda → SQS → Dispatcher → Batch Service → Database

## Technologies Used
- Java 11
- Spring Boot
- AWS S3
- AWS SQS
- AWS Lambda
- PostgreSQL

## Flow
1. File uploaded to S3
2. Lambda triggers and sends message to SQS
3. Dispatcher service reads message
4. Batch service processes records (100 at a time)
5. Data stored in DB

## Features
- Batch processing (100 records per batch)
- Supports large files (1 lakh+ records)
- Error handling 
- Queue-based scalable design

## Notes
- No sensitive data included
- Configs use placeholders

## How to Run
1. Clone repo
2. Configure application.yml
3. Run Spring Boot app

#Author
Rahul Gaikwad
 
