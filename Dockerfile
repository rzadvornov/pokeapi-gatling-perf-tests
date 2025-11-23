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
# Runtime Stage: Eclipse Temurin JDK 21
# We need the full JDK (not JRE) because we are running via ./gradlew
# ---------------------------------------------------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install curl if needed (optional)
# eclipse-temurin is based on Ubuntu/Debian, so apt-get works
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 1. Copy the build artifacts
COPY --from=builder /app/build /app/build

# 2. Copy the Gradle Wrapper
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