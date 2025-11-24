# Stage 1: Builder
# Switched to Alpine base to avoid persistent apt-get network errors.
FROM gradle:8.5-jdk21-alpine AS builder

# 1. Create a non-root user and set up the home directory
ARG USER_NAME=gradleuser
ARG USER_UID=1010
# FIX: Use apk for Alpine. Install 'shadow' for useradd/groupadd.
RUN apk update \
    && apk add --no-cache shadow \
    && rm -rf /var/cache/apk/*
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
# Switched to JRE Alpine base.
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set initial user to root (default) for package installation
# The user is 'root' at this point.

# Install dependencies (must be run as root)
# Use apk for Alpine. Install 'curl' and 'shadow'.
RUN apk update \
    && apk add --no-cache curl shadow \
    && rm -rf /var/cache/apk/*

# 2. Define the same non-root user and group
ARG USER_NAME=appuser
ARG USER_UID=1010
RUN groupadd --gid $USER_UID $USER_NAME \
    && useradd --uid $USER_UID --gid $USER_UID -m $USER_NAME \
    && chown -R $USER_NAME:$USER_NAME /home/$USER_NAME

# Set the application working directory
WORKDIR /home/$USER_NAME/app

# 3. Copy artifacts and files from the builder stage.
# This COPY runs as root (default user), which allows us to change ownership later.
COPY --from=builder /home/gradleuser/app/build ./build
COPY --from=builder /home/gradleuser/app/gradlew ./
COPY --from=builder /home/gradleuser/app/gradle ./gradle
COPY --from=builder /home/gradleuser/app/build.gradle ./
COPY --from=builder /home/gradleuser/app/settings.gradle ./
COPY --from=builder /home/gradleuser/app/src ./src

# Explicitly ensure the new user owns the application directory (BEST PRACTICE)
# This RUN command now executes as root, resolving the permission denied error.
RUN chown -R $USER_NAME:$USER_NAME /home/$USER_NAME/app

# Switch to the non-root user *after* all root operations are complete.
USER $USER_NAME

# Create results directory (now correctly owned by $USER_NAME)
RUN mkdir -p results

# Set environment variable
ENV TEST_TYPE=load

# Ensure the gradlew script is executable
RUN chmod +x gradlew

# 4. Create and set the entrypoint script
# Using a clean COPY heredoc for the script content.
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
