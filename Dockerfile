# Stage 1: Builder
# Switched to Alpine base to avoid persistent apt-get network errors.
FROM gradle:9.2-jdk21-alpine AS builder

# 1. Create a non-root user and set up the home directory
ARG USER_NAME=gradleuser
ARG USER_UID=1010

# Use apk for Alpine. Install 'shadow' for useradd/groupadd.
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
FROM eclipse-temurin:21-jdk-alpine AS runtime

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
# This COPY runs as root (default user).
COPY --from=builder /home/gradleuser/app/build ./build
COPY --from=builder /home/gradleuser/app/gradlew ./
COPY --from=builder /home/gradleuser/app/gradle ./gradle
COPY --from=builder /home/gradleuser/app/build.gradle ./
COPY --from=builder /home/gradleuser/app/settings.gradle ./
COPY --from=builder /home/gradleuser/app/src ./src

# Explicitly ensure the new user owns the application directory (BEST PRACTICE)
# This RUN command executes as root, resolving previous ownership issues.
RUN chown -R $USER_NAME:$USER_NAME /home/$USER_NAME/app

# Create results directory (run as root)
RUN mkdir -p results
# FIX: Change ownership of the results directory to the non-root user ($USER_NAME = appuser)
RUN chown -R $USER_NAME:$USER_NAME results

# Set environment variable
ENV TEST_TYPE=load

# 4. Create and set the entrypoint script (run as root)
COPY <<EOF run-tests.sh
#!/bin/sh

TASK_NAME="${TEST_TYPE}Test"
echo "Running Gradle task: \$TASK_NAME"

./gradlew "\$TASK_NAME" --warn
EXIT_CODE=\$?

if [ \$EXIT_CODE -ne 0 ]; then
    echo "WARNING: Test run failed with exit code \$EXIT_CODE. Attempting to copy any partial reports."
fi

# Use find to locate the LATEST, FULL PATH to the dated report directory.
LATEST_REPORT_FULL_PATH=\$(find build/reports/gatling -mindepth 1 -maxdepth 1 -type d | sort -r | head -n 1)

echo "Checking for latest report in: \$LATEST_REPORT_FULL_PATH"

if [ -d "\$LATEST_REPORT_FULL_PATH" ]; then
    # The copy operation will look like: cp -r build/reports/gatling/date_stamp results/
    echo "Copying dated report directory to results/..."
    
    # We copy the FULL PATH found by find. This is the correct fix.
    cp -r "\$LATEST_REPORT_FULL_PATH" results/
    
    # We should confirm the file was copied using the original reported path:
    REPORT_NAME=\$(basename "\$LATEST_REPORT_FULL_PATH")
    echo "Report files successfully copied to results/\$REPORT_NAME."
else
    echo "ERROR: Report directory not found at build/reports/gatling. No reports copied."
fi

exit \$EXIT_CODE
EOF
# Ensure the gradlew script is executable (Runs as root to resolve "Operation not permitted")
RUN chmod +x run-tests.sh

# Switch to the non-root user *just* before the entrypoint.
USER $USER_NAME

# The final user is $USER_NAME and the entrypoint will run as this user
ENTRYPOINT ["./run-tests.sh"]
