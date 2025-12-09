# ==========================================
# Multi-Stage Docker Build for FHIR Middleware
# ==========================================
# Stage 1: Build the application with Gradle
# Stage 2: Run the application with minimal JRE

# ==========================================
# Stage 1: Build Stage
# ==========================================
FROM gradle:8.10-jdk21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files first for better caching
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Download dependencies (this layer will be cached if build files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the fat JAR
RUN ./gradlew buildFatJar --no-daemon

# ==========================================
# Stage 2: Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Add labels for better documentation
LABEL maintainer="FHIR Middleware Team"
LABEL description="FHIR Middleware Service - Secure authentication layer for FHIR applications"
LABEL version="0.0.1"

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S ktor && adduser -S ktor -G ktor

# Set working directory
WORKDIR /app

# Copy the fat JAR from builder stage
COPY --from=builder /app/build/libs/fhir-middleware-fat.jar /app/application.jar

# Copy application configuration
COPY src/main/resources/application.conf /app/application.conf

# Change ownership to non-root user
RUN chown -R ktor:ktor /app

# Switch to non-root user
USER ktor

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]

