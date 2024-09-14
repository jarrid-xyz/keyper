#!/bin/sh
set -e

# Run the Java command and capture its output
output=$(java -jar /home/keyper/lib/build/libs/lib-cli.jar "$@")

# Print the output, replacing newlines with spaces
echo "$output" | tr '\n' ' '

# Add a final newline for better formatting
echo