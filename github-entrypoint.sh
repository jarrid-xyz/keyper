#!/bin/bash
set -e

IFS=' ' read -r -a ADDR <<< "$@"

LOG_FILE="/home/keyper/keyper.log"

# Capture stdout and stderr from the Keyper command
java -jar /home/keyper/lib/build/libs/lib-cli.jar "${ADDR[@]}" &> $LOG_FILE || true
status=$?

# Set the output using GitHub Actions Workflow Commands
echo "## Keyper Command Output" >> $GITHUB_STEP_SUMMARY
cat $LOG_FILE >> $GITHUB_STEP_SUMMARY

# Output to console as well (this will be visible in the action logs)
cat $LOG_FILE

# Exit with the same status as the Keyper command
exit $status
