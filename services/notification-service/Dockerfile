# ======================== BUILD STAGE ========================
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app

# Copy Maven POM and install dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code and build
COPY src/ src/
RUN mvn clean package -DskipTests -q

# ======================== PRODUCTION STAGE ========================
FROM eclipse-temurin:21-jre-alpine

# Install curl for healthchecks
RUN apk add --no-cache curl tzdata

# Create non-root user
RUN adduser -D -u 1000 notificationservice

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R notificationservice:notificationservice /app

# Switch to non-root user
USER notificationservice

# Set default timezone
ENV TZ=UTC

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
