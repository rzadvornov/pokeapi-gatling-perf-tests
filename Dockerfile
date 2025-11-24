# Stage 1: Builder
# Use the official image
FROM gradle:9.2-jdk21 AS builder

# 1. Create a non-root user and set up the home directory
ARG USER_NAME=gradleuser
ARG USER_UID=1010
RUN groupadd --gid $USER_UID $USER_NAME \
    && useradd --uid $USER_UID --gid $USER_UID -m $USER_NAME \
    && chown -R $USER_NAME:$USER_NAME /home/$USER_NAME

# Set the non-root user for this stage
USER $USER_NAME

# Set the working directory
WORKDIR /home/$USER_NAME/app

# Copy files and run gradle commands as the non-root user
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle build --no-daemon

# Stage 2: Runtime
# Use a lean base image for the final runtime
FROM eclipse-temurin:21-jdk

# 2. Define the same non-root user and group
ARG USER_NAME=appuser
ARG USER_UID=1010
RUN groupadd --gid $USER_UID $USER_NAME \
    && useradd --uid $USER_UID --gid $USER_UID -m $USER_NAME \
    && chown -R $USER_NAME:$USER_NAME /home/$USER_NAME

# Install dependencies (must be run as root)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /home/$USER_NAME/app

# Set the non-root user **before** copying files
USER $USER_NAME

# 3. Copy artifacts and files from the builder stage as the new user.
# The files copied here will automatically be owned by $USER_NAME.
COPY --from=builder /home/gradleuser/app/build ./build
COPY --from=builder /home/gradleuser/app/gradlew ./
COPY --from=builder /home/gradleuser/app/gradle ./gradle
COPY --from=builder /home/gradleuser/app/build.gradle ./
COPY --from=builder /home/gradleuser/app/settings.gradle ./
COPY --from=builder /home/gradleuser/app/src ./src

# Create results directory (will be owned by $USER_NAME)
RUN mkdir -p results

# Set environment variable
ENV TEST_TYPE=load

# Ensure the gradlew script is executable
RUN chmod +x gradlew

# 4. Create and set the entrypoint script
# The script will be created and run as the non-root user $USER_NAME.
RUN echo '#!/bin/bash\n\
# Construct the task name (e.g., load -> loadTest)\n\
TASK_NAME="${TEST_TYPE}Test"\n\
echo "Running Gradle task: $TASK_NAME"\n\
\n\
./gradlew $TASK_NAME\n\
EXIT_CODE=$?\n\
\n\
echo "Copying reports to /home/appuser/app/results..."\n\
# Reports will be generated in build/reports/gatling, copy to /home/appuser/app/results\n\
cp -r build/reports/gatling/* results/ 2>/dev/null || echo "No reports found to copy."\n\
exit $EXIT_CODE' > run-tests.sh && chmod +x run-tests.sh

# The final user is $USER_NAME and the entrypoint will run as this user
ENTRYPOINT ["./run-tests.sh"]
