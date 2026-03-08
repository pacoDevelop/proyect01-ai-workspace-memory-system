# Multi-stage build for User Service

# ==================== Stage 1: Build ====================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q && \
    mvn clean

# ==================== Stage 2: Runtime ====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Copy JAR from builder stage
COPY --from=builder /app/target/user-service-*.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Spring Boot defaults
ENV SPRING_APPLICATION_JSON={}

# Health check using Spring Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Expose port
EXPOSE 8082

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
