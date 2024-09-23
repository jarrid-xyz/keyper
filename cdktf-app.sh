#!/bin/sh
if [ "$IS_GITHUB_ACTION" = "true" ]; then
  # NOTE: Maybe we don't need to copy the files to the workspace, giving it a try
  cp /home/keyper/cdktf.json /home/keyper/cdktf-app.sh /github/workspace/
  java -jar /home/keyper/lib/build/libs/lib-main.jar
else
  java -jar lib/build/libs/lib-main.jar
fi