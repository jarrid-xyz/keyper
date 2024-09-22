#!/bin/bash
set -e

ADDR=("$@")

echo "## Keyper Command Output" >> $GITHUB_STEP_SUMMARY

# Capture stdout and stderr from the Keyper command
# Set the output using GitHub Actions Workflow Commands
java -jar /home/keyper/lib/build/libs/lib-cli.jar "${ADDR[@]}" &> $GITHUB_STEP_SUMMARY
status=$?

# Output to console as well (this will be visible in the action logs)
cat $GITHUB_STEP_SUMMARY

# Exit with the same status as the Keyper command
exit $status
