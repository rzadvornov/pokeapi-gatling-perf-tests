# ==========================================
# Stage 1: Builder
# ==========================================
FROM gradle:9.2-jdk21 AS builder

WORKDIR /app

COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src
RUN gradle build --no-daemon

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install curl (optional, useful for debugging)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 1. Copy the compiled build artifacts (classes/resources)
COPY --from=builder /app/build /app/build

# 2. Copy the Gradle Wrapper scripts (NOW they exist in builder)
COPY --from=builder /app/gradlew /app/
COPY --from=builder /app/gradle /app/gradle

# 3. Copy build settings
COPY --from=builder /app/build.gradle /app/
COPY --from=builder /app/settings.gradle /app/

# Create results directory
RUN mkdir -p /app/results

# Set environment variable default
ENV TEST_TYPE=load

# Make the wrapper executable
RUN chmod +x gradlew

# Entry point
ENTRYPOINT ["/app/gradlew"]

# Default command
CMD ["gatlingRun"]