# Multi-stage Dockerfile for RentFlow AI Spring Boot Backend
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root system user for production security hardening
RUN addgroup -S rentflow && adduser -S rentflow -G rentflow

COPY --from=builder /app/target/backend-*.jar app.jar
RUN chown -R rentflow:rentflow /app

USER rentflow

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
