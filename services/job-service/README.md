# Job Service

Microservice for Job management in JRecruiter platform.

## Overview

This service handles all job-related operations including job creation, management, and lifecycle. It follows Domain-Driven Design (DDD) principles with a clean architecture.

## Architecture

```
job-service/
├── domain/           # Business logic and aggregates
│   ├── aggregates/   # Aggregate roots
│   ├── entities/     # Entities
│   ├── valueobjects/ # Value objects
│   └── events/       # Domain events
├── infrastructure/   # External concerns
│   ├── persistence/  # Database adapters
│   ├── rest/         # REST controllers
│   └── messaging/    # Message brokers
├── application/      # Application services
└── config/           # Configuration classes
```

## Features

- Job creation and management
- Job lifecycle management (publish, close, update)
- Employer management
- Job search and filtering
- Event-driven architecture
- CQRS pattern for search functionality

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Docker (optional)

### Installation

1. Clone the repository
2. Navigate to the job-service directory
3. Build the project:

```bash
mvn clean install
```

### Running the Service

#### Using Maven

```bash
mvn spring-boot:run
```

#### Using Docker

```bash
mvn jib:dockerBuild
docker run -p 8080:8080 jrecruiter/job-service
```

### Configuration

The service can be configured using environment variables or application.yml:

```yaml
# Environment variables
DATABASE_URL=jdbc:postgresql://localhost:5432/jrecruiter
DATABASE_USERNAME=job_service
DATABASE_PASSWORD=password
```

## API Endpoints

### Job Management

- `POST /api/jobs` - Create a new job
- `GET /api/jobs/{id}` - Get job details
- `PUT /api/jobs/{id}` - Update job
- `DELETE /api/jobs/{id}` - Close job

### Search

- `GET /api/jobs/search` - Search jobs with filters
- `GET /api/jobs/facets` - Get search facets

## Domain Model

### Aggregates

- **Job**: Main aggregate root
- **Employer**: Employer information

### Value Objects

- **JobTitle**: Job title with validation
- **JobDescription**: Job description with validation
- **Salary**: Salary range with currency
- **Location**: Job location with coordinates
- **JobStatus**: Job status (OPEN, CLOSED, DRAFT)

## Events

The service publishes and consumes the following events:

- `JobCreatedEvent`: When a new job is created
- `JobUpdatedEvent`: When a job is updated
- `JobDeletedEvent`: When a job is closed
- `JobStatusChangedEvent`: When job status changes

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Test Coverage

The project aims for at least 80% test coverage.

## Deployment

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: job-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: job-service
  template:
    metadata:
      labels:
        app: job-service
    spec:
      containers:
      - name: job-service
        image: jrecruiter/job-service:latest
        ports:
        - containerPort: 8080
```

### Docker Compose

```yaml
services:
  job-service:
    image: jrecruiter/job-service:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/jrecruiter
    depends_on:
      - postgres
```

## Monitoring

The service includes built-in monitoring with:

- Health checks
- Metrics (Prometheus)
- Logging (Structured JSON)
- Distributed tracing (OpenTelemetry)

## Security

- Input validation
- SQL injection prevention
- XSS protection
- Rate limiting (planned)
- Authentication (via User Service)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please refer to the project documentation or open an issue.