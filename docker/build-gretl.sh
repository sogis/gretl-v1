#!/bin/bash

echo "======================================================================="
echo "Build Docker container for GRETL runtime"
echo "Uses the following container definition:"
echo "https://github.com/openshift/jenkins/blob/master/slave-maven/Dockerfile"
echo "======================================================================="

# TODO no version lock
cp ../build/libs/gretl-1.0.4-SNAPSHOT.jar gretl
cp ../dependencies.gradle gretl

docker build --no-cache --force-rm -t gretl-runtime -f gretl/Dockerfile gretl

rm gretl/gretl*.jar
rm gretl/dependencies.gradle

# look into the container:
# docker run -it --entrypoint=/bin/sh gretl-runtime
