#!/bin/bash
set -e

export IS_GITHUB_ACTION=true

IFS=' ' read -r -a ADDR <<< "$@"

LOG_FILE="/home/keyper/keyper.log"

# NOTE:Maybe we don't need to copy the files to the workspace, giving it a try
cp /home/keyper/cdktf.json /home/keyper/cdktf-app.sh /github/workspace
echo "DEBUG: Github Workspace Directory: $(ls -la /github/workspace)"

# Capture stdout and stderr from the Keyper command
java -jar /home/keyper/lib/build/libs/lib-cli.jar "${ADDR[@]}" &> $LOG_FILE || status=$?

# Set github action output
echo "## Keyper Command Output" >> $GITHUB_STEP_SUMMARY
# Format Keyper log in code block
echo '```' >> $GITHUB_STEP_SUMMARY
cat $LOG_FILE >> $GITHUB_STEP_SUMMARY
echo '```' >> $GITHUB_STEP_SUMMARY

# Output to console as well (this will be visible in the action logs)
cat $LOG_FILE

# Exit with the same status as the Keyper command
exit $status
