#!/bin/bash
set -e

IFS=' ' read -ra ADDR <<< "$1"

# Capture the output of the Java command
output=$(java -jar /home/keyper/lib/build/libs/lib-cli.jar "${ADDR[@]}")

# Set the output using GitHub Actions Workflow Commands
echo "## Keyper Command Output" >> $GITHUB_STEP_SUMMARY
echo "$output" >> $GITHUB_STEP_SUMMARY

# Output to console as well (this will be visible in the action logs)
echo "$output"

# Exit with the same status as the Java command
exit $?