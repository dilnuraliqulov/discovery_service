# Multi-stage build for Discovery Service
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Add labels
LABEL maintainer="Discovery Service Team"
LABEL version="1.0.0"
LABEL description="Eureka Service Discovery for Microservices Architecture"

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Eureka port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

