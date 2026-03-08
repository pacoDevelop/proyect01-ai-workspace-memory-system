# Job Service - Quick Start Guide

## Prerequisites

- Docker & Docker Compose
- Java 21+ (for local development)
- Maven 3.9+
- PostgreSQL 15+ (if running locally)
- RabbitMQ 3.13+ (if running locally)

## Docker Compose Setup (Recommended)

### Start all services
```bash
docker-compose up -d
```

This will start:
- **Job Service** on http://localhost:8080
- **PostgreSQL** on localhost:5432
- **RabbitMQ** on localhost:5672 (management UI on http://localhost:15672)
- **Prometheus** on http://localhost:9090
- **Grafana** on http://localhost:3000 (admin/admin)

### Check service health
```bash
# Job Service health
curl http://localhost:8080/actuator/health

# Prometheus targets
http://localhost:9090/targets

# RabbitMQ management
http://localhost:15672 (guest/guest)
```

### Stop all services
```bash
docker-compose down
```

### View logs
```bash
docker-compose logs -f job-service
docker-compose logs -f postgres
docker-compose logs -f rabbitmq
```

---

## Local Development Setup

### Build the project
```bash
mvn clean install
```

### Run with development profile
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Run with test profile
```bash
mvn test
```

### Database initialization
The `scripts/init-db.sql` script is automatically executed by PostgreSQL container on startup.

To manually initialize:
```bash
psql -h localhost -U job_service -d jrecruiter -f scripts/init-db.sql
```

---

## Access Points

| Service | URL | Notes |
|---------|-----|-------|
| Job Service | http://localhost:8080 | REST API |
| Health Check | http://localhost:8080/actuator/health | Service health |
| Metrics | http://localhost:8080/actuator/metrics | Prometheus metrics |
| PostgreSQL | localhost:5432 | DB: jrecruiter |
| RabbitMQ | localhost:5672 | AMQP |
| RabbitMQ UI | http://localhost:15672 | Management (guest/guest) |
| Prometheus | http://localhost:9090 | Metrics scraper |
| Grafana | http://localhost:3000 | Dashboards (admin/admin) |

---

## Environment Variables

### Development (docker-compose)
```
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/jrecruiter
SPRING_DATASOURCE_USERNAME=job_service
SPRING_DATASOURCE_PASSWORD=password
SPRING_RABBITMQ_HOST=rabbitmq
```

### Production (Kubernetes/Cloud)
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<RDS_HOST>:5432/jrecruiter
SPRING_DATASOURCE_USERNAME=<DB_USER>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
SPRING_RABBITMQ_HOST=<RABBITMQ_HOST>
JWT_SECRET=<SECURE_SECRET>
```

---

## Troubleshooting

### Port already in use
```bash
# Find and kill process on port 8080
lsof -i :8080
kill -9 <PID>
```

### PostgreSQL connection issues
```bash
# Check postgres is running
docker-compose ps postgres

# Test connection
psql -h localhost -U job_service -d jrecruiter -c "SELECT 1"
```

### RabbitMQ connection issues
```bash
# Check RabbitMQ is running
docker-compose ps rabbitmq

# Access management UI
http://localhost:15672
```

---

## Development Workflow

1. Make code changes
2. Run tests: `mvn test`
3. Build: `mvn clean install`
4. Run: `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"`
5. Test endpoints: `curl http://localhost:8080/actuator/health`

---

## Production Deployment

See [DEPLOYMENT.md](docs/DEPLOYMENT.md) for Kubernetes/Cloud deployment instructions.
