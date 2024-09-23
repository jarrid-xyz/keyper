#!/bin/sh
if [ "$IS_DOCKER" = "true" ]; then
  java -jar /home/keyper/lib/build/libs/lib-main.jar
else
  java -jar lib/build/libs/lib-main.jar
fi