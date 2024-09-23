#!/bin/bash
set -e

IFS=' ' read -r -a ADDR <<< "$@"

LOG_FILE="/home/keyper/keyper.log"
CDKTF_JSON_FILE="/home/keyper/cdktf.json"

# Update the "app" line in /home/keyper/cdktf.json
sed -i 's#"app": "java -jar lib/build/libs/lib-main.jar"#"app": "java -jar /home/keyper/lib/build/libs/lib-main.jar"#' $CDKTF_JSON_FILE

# Copy the updated file to /github/workspace/cdktf.json
cp $CDKTF_JSON_FILE /github/workspace/cdktf.json

echo "Updated and copied cdktf.json files"

# Debug: Print contents of cdktf.json
echo "DEBUG: Contents of $CDKTF_JSON_FILE:"; cat $CDKTF_JSON_FILE
echo "DEBUG: Contents of /github/workspace/cdktf.json:"; cat /github/workspace/cdktf.json

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
