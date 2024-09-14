#!/bin/sh
set -e

# Run the Java command and output its result, wrapping each line with GitHub Actions syntax
java -jar /home/keyper/lib/build/libs/lib-cli.jar "$@" | while IFS= read -r line; do
    echo "::echo::$line"
done