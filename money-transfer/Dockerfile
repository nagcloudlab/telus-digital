# Multi-stage Dockerfile for Money Transfer Application

# Stage 1: Build (Optional - if you want to build inside Docker)
# FROM maven:3.9-openjdk-17-slim AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="nag@example.com"
LABEL application="money-transfer"
LABEL description="Money Transfer Spring Boot Application"
LABEL version="1.0"

# Create app user (security best practice - don't run as root)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Set working directory
WORKDIR /app

# Copy JAR file from Jenkins build
# Jenkins builds the JAR, we just package it in Docker
COPY target/*.jar app.jar

# Change ownership to app user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]