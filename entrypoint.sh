#!/bin/sh
set -e

# Capture the output of the Java command
output=$(java -jar /home/keyper/lib/build/libs/lib-cli.jar "$@")

# Set the output using GitHub Actions Workflow Commands
echo "stdout<<EOF" >> $GITHUB_OUTPUT
echo "$output" >> $GITHUB_OUTPUT
echo "EOF" >> $GITHUB_OUTPUT

# Output to console as well (this will be visible in the action logs)
echo "$output"

# Exit with the same status as the Java command
exit $?