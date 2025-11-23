FROM gradle:8.5-jdk11 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the project
RUN gradle build --no-daemon

# Runtime stage
FROM openjdk:11-jre-slim

WORKDIR /app

# Install necessary tools
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy built artifacts from builder
COPY --from=builder /app/build ./build
COPY --from=builder /app/gradle ./gradle
COPY --from=builder /app/gradlew ./
COPY --from=builder /app/build.gradle ./
COPY --from=builder /app/settings.gradle ./
COPY --from=builder /root/.gradle /root/.gradle

# Copy source for Gatling
COPY src ./src

# Create results directory
RUN mkdir -p /app/results

# Set environment variable for test type
ENV TEST_TYPE=load

# Entry point to run tests
ENTRYPOINT ["./gradlew"]

CMD ["gatlingRun"]