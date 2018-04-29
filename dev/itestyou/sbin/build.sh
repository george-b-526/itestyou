#!/bin/sh
#

# Force shell to fail on any errors.
set -e

clear
echo "Starting ITestYou Build"

export JAVA_HOME="/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.75.x86_64"

java -version
ant -version

ant -buildfile ../testvisor-deploy/build.xml

rm -rf ./release/
mkdir release
cp -r ../testvisor-deploy/dist/* ./release/