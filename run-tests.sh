#!/bin/sh
# Construct the task name (e.g., load -> loadTest)
TASK_NAME="${TEST_TYPE}Test"
echo "Running Gradle task: $TASK_NAME"

./gradlew "$TASK_NAME"
EXIT_CODE=$?

echo "Copying reports to /home/$USER_NAME/app/results..."
# Reports will be generated in build/reports/gatling, copy to results/
cp -r build/reports/gatling/* results/ 2>/dev/null || echo "No reports found to copy."
exit $EXIT_CODE
