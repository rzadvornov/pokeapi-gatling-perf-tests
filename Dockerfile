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

# --- End of Builder Stage ---

# Stage 2: Runtime
# Use a lean base image for the final runtime
FROM eclipse-temurin:21-jre-slim # 💡 FIX 1: Use JRE-slim since we only need to run Java, not build.

# Set initial user to root (default) for package installation
# User is implicitly 'root' here.

# Install dependencies (must be run as root)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 2. Define the same non-root user and group
ARG USER_NAME=appuser
ARG USER_UID=1010
RUN groupadd --gid $USER_UID $USER_NAME \
    && useradd --uid $USER_UID --gid $USER_UID -m $USER_NAME \
    && chown -R $USER_NAME:$USER_NAME /home/$USER_NAME

# Set the working directory (optional, but good practice)
WORKDIR /home/$USER_NAME/app

# 💡 FIX 2: Switch to the non-root user *immediately* before copying artifacts.
# This is the crucial fix: The USER instruction must be re-run after the root-based RUN apt-get install...
USER $USER_NAME

# 3. Copy artifacts and files from the builder stage as the new user.
# The files copied here will automatically be owned by $USER_NAME.
COPY --from=builder /home/gradleuser/app/build ./build
COPY --from=builder /home/gradleuser/app/gradlew ./
COPY --from=builder /home/gradleuser/app/gradle ./gradle
COPY --from=builder /home/gradleuser/app/build.gradle ./
COPY --from=builder /home/gradleuser/app/settings.gradle ./
COPY --from=builder /home/gradleuser/app/src ./src

# Create results directory (will be owned by $USER_NAME, since USER is set)
RUN mkdir -p results

# Set environment variable
ENV TEST_TYPE=load

# Ensure the gradlew script is executable
RUN chmod +x gradlew

# We assume 'appuser' is the name of the runtime user (ARG USER_NAME=appuser)
RUN chown -R $USER_NAME:$USER_NAME /home/$USER_NAME/app

# Using a heredoc is cleaner for multi-line shell scripts
COPY <<EOF run-tests.sh
#!/bin/bash
# Construct the task name (e.g., load -> loadTest)
TASK_NAME="${TEST_TYPE}Test"
echo "Running Gradle task: \$TASK_NAME"

./gradlew "\$TASK_NAME"
EXIT_CODE=\$?

echo "Copying reports to /home/\$USER_NAME/app/results..."
# Reports will be generated in build/reports/gatling, copy to results/
cp -r build/reports/gatling/* results/ 2>/dev/null || echo "No reports found to copy."
exit \$EXIT_CODE
EOF
RUN chmod +x run-tests.sh

# The final user is $USER_NAME and the entrypoint will run as this user
ENTRYPOINT ["./run-tests.sh"]
