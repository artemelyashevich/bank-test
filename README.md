# Banking System API

## Overview

This is a Spring Boot-based banking system API that provides user account management with features like:
- User profile management
- Contact information (emails and phones) management
- Balance interest accrual
- Money transfers between accounts
- User search functionality

## Features

### User Management
- JWT-based authentication
- User profile with multiple contact methods
- Strict validation rules for all fields

### Account Operations
- Automatic 10% interest accrual every 30 seconds (capped at 207% of initial deposit)
- Secure money transfers between accounts
- Transaction history

### Search Capabilities
- Advanced user search with filtering by:
  - Name (prefix match)
  - Email (exact match)
  - Phone (exact match)
  - Date of birth (greater than)

## Technology Stack

- **Backend**: 
  - Java 21
  - Spring Boot 3.4
  - Spring Data JPA
  - Spring Security

- **Database**:
  - PostgreSQL (main data store)
  - Redis (distributed locks, caching)
  - Elasticsearch (search functionality)

- **Other**:
  - JWT for authentication
  - ShedLock for scheduled task coordination
  - Docker for containerization

## API Documentation

The API is documented using Swagger UI available at:
`http://localhost:8080/swagger-ui.html`

## Installation

### Prerequisites
- Java 21+
- Docker
- Maven

### Running with Docker

1. Clone the repository
2. Run the following command:

```bash
docker-compose up -d
mvn clean install
mvn spring-boot:run
```

This will start:
- PostgreSQL database
- Redis server
- Elasticsearch
- The banking application

### Configuration

Application properties can be configured in `application.yml` (minimal config):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking
    username: postgres
    password: postgres
  elasticsearch:
    uris: http://localhost:9200
  redis:
    host: localhost
    port: 6379

```

## Scheduled Tasks

- **Interest Accrual**: Runs every 30 seconds to add 10% interest to all accounts (capped at 207% of initial deposit)
- **Data Synchronization**: Runs periodically to sync data between PostgreSQL and Elasticsearch with help of Redis events
