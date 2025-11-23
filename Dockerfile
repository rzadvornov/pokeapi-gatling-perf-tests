# Stage 1: Builder
FROM gradle:9.2-jdk21 AS builder
WORKDIR /app
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle build --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Install dependencies
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 1. Copy the compiled artifacts
COPY --from=builder /app/build /app/build

# 2. Copy the Gradle Wrapper
COPY --from=builder /app/gradlew /app/
COPY --from=builder /app/gradle /app/gradle
COPY --from=builder /app/build.gradle /app/
COPY --from=builder /app/settings.gradle /app/

# 3. Copy the source code to the runtime stage.
# Without this, Gradle sees "missing" sources and may clean/delete your compiled classes.
COPY --from=builder /app/src /app/src

# Create results directory
RUN mkdir -p /app/results

ENV TEST_TYPE=load
RUN chmod +x gradlew

# 4. Run the specific custom task (e.g., "loadTest") instead of "gatlingRun".
# This uses your TEST_TYPE variable to construct the task name dynamically.
RUN echo '#!/bin/bash\n\
# Construct the task name (e.g., load -> loadTest)\n\
TASK_NAME="${TEST_TYPE}Test"\n\
echo "Running Gradle task: $TASK_NAME"\n\
\n\
./gradlew $TASK_NAME\n\
EXIT_CODE=$?\n\
\n\
echo "Copying reports to /app/results..."\n\
cp -r build/reports/gatling/* results/ 2>/dev/null || echo "No reports found to copy."\n\
exit $EXIT_CODE' > /app/run-tests.sh && chmod +x /app/run-tests.sh

ENTRYPOINT ["/app/run-tests.sh"]