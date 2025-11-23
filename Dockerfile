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

# Copy artifacts
COPY --from=builder /app/build /app/build
COPY --from=builder /app/gradlew /app/
COPY --from=builder /app/gradle /app/gradle
COPY --from=builder /app/build.gradle /app/
COPY --from=builder /app/settings.gradle /app/

# Create the results directory (This is where we will mount the volume)
RUN mkdir -p /app/results

ENV TEST_TYPE=load
RUN chmod +x gradlew

# Create a wrapper script to run tests and THEN copy results
# This ensures Gradle can clean its internal build folder without crashing,
# and we still get the reports in the mounted volume.
RUN echo '#!/bin/bash\n\
./gradlew gatlingRun\n\
EXIT_CODE=$?\n\
echo "Copying reports to /app/results..."\n\
cp -r build/reports/gatling/* results/ 2>/dev/null || echo "No reports found to copy."\n\
exit $EXIT_CODE' > /app/run-tests.sh && chmod +x /app/run-tests.sh

ENTRYPOINT ["/app/run-tests.sh"]