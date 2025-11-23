# Builder Stage: Use Gradle 9.2 with JDK 21
FROM gradle:9.2-jdk21 AS builder

WORKDIR /app

# Copy and setup Gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src
RUN gradle build --no-daemon

# ---------------------------------------------------
# Runtime Stage: Minimal JRE 21
# (Note: Standard comments start with #, not dashes)
# ---------------------------------------------------
FROM openjdk:21-jre-slim

WORKDIR /app

# Install curl if needed (optional)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 1. Copy the build artifacts
COPY --from=builder /app/build /app/build

# 2. Copy the Gradle Wrapper (Make sure to copy the wrapper script specifically)
COPY --from=builder /app/gradlew /app/
COPY --from=builder /app/gradle /app/gradle

# 3. Copy build configuration
COPY --from=builder /app/build.gradle /app/
COPY --from=builder /app/settings.gradle /app/

# Create results directory
RUN mkdir -p /app/results

# Set environment variable for test type
ENV TEST_TYPE=load

# Ensure the wrapper is executable
RUN chmod +x gradlew

# Entry point to run tests
ENTRYPOINT ["/app/gradlew"]

# Command to execute Gatling tests
CMD ["gatlingRun"]