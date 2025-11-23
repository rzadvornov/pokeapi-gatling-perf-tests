# Builder Stage: Use Gradle 9.2 with JDK 21
FROM gradle:9.2-jdk21 AS builder

WORKDIR /app

# Copy and setup Gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (optional, but speeds up subsequent steps)
RUN gradle dependencies --no-daemon

# Copy source code and build the project
COPY src ./src
# The 'assemble' task is usually faster than 'build' if you only need the JAR/classes,
# but 'build' is used here to ensure tests/other checks are run if required.
RUN gradle build --no-daemon

---

# Runtime Stage: Minimal JRE 21
FROM openjdk:21-jre-slim

# Set up the application directory
WORKDIR /app

# The runtime stage needs very little.
# Install 'curl' only if your application (or Gatling) specifically needs it at runtime.
# If Gatling needs it, keep this section. Otherwise, remove it.
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy only the compiled artifacts and resources needed for execution.

# 1. Copy the executable Gatling files/classes (adjust path if your project structure differs)
#    Gatling often runs from classes or test-output, not a standard JAR.
#    The path below assumes the build directory contains the compiled classes/resources needed by Gradle/Gatling to run tests.
COPY --from=builder /app/build /app/build

# 2. Copy the Gradle Wrapper and essential files needed to run the 'gatlingRun' task
COPY --from=builder /app/gradlew /app/
COPY --from=builder /app/gradle /app/gradle

# 3. Copy the configuration/settings files required by the wrapper
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