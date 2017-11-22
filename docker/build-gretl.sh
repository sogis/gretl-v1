#!/bin/bash

echo "=============================================================="
echo "Build Docker container for GRETL runtime"
echo "Uses the following container definition:"
echo "https://github.com/keeganwitt/docker-gradle/blob/master/"
echo "                                  jdk8-alpine/Dockerfile"
echo "=============================================================="

# TODO no version lock
cp ../build/libs/gretl-1.0.4-SNAPSHOT.jar gretl

docker build --no-cache --force-rm -t gretl-runtime -f gretl/Dockerfile gretl

rm gretl/gretl*.jar

# look into the container:
# docker run -it --entrypoint=/bin/sh gretl-runtime
