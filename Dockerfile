# ==============================================================================
# TeamMatch Backend - Multi-stage Dockerfile for Render / Linux Cloud Deployment
# ==============================================================================

# Stage 1: Build JAR artifact using Maven & Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy Maven project descriptor and source files
COPY pom.xml .
COPY src ./src

# Compile and package executable jar
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image using Eclipse Temurin JRE 17
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Ensure curl and bash/sh utilities are present (already included in alpine)
# Create a non-root group and user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the compiled JAR artifact from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Copy public CA certificate and runtime entrypoint script
COPY aiven-ca.pem .
COPY entrypoint.sh .

# Ensure entrypoint is executable and owned by appuser
RUN chmod +x entrypoint.sh && chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose standard container port
EXPOSE 8081

# Launch container via entrypoint
ENTRYPOINT ["./entrypoint.sh"]
