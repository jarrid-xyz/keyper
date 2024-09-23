#!/bin/bash
set -e

IFS=' ' read -r -a ADDR <<< "$@"

LOG_FILE="/home/keyper/keyper.log"


# Create cdktf.json file, this is only needed in github action
cat << EOF > /home/keyper/cdktf.json
{
  "language": "java",
  "app": "java -jar /home/keyper/lib/build/libs/lib-main.jar",
  "projectId": "25a6a20e-00d6-4ac9-9388-7f3954f7db71",
  "sendCrashReports": "true",
  "codeMakerOutput": "imports",
  "terraformProviders": [],
  "terraformModules": [],
  "context": {
  }
}
EOF

echo "Created cdktf.json file"

# Capture stdout and stderr from the Keyper command
java -jar /home/keyper/lib/build/libs/lib-cli.jar "${ADDR[@]}" &> $LOG_FILE || status=$?

# Set github action output
echo "## Keyper Command Output" >> $GITHUB_STEP_SUMMARY
cat $LOG_FILE >> $GITHUB_STEP_SUMMARY

# Output to console as well (this will be visible in the action logs)
cat $LOG_FILE

# Exit with the same status as the Keyper command
exit $status
