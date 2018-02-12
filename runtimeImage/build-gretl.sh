#!/bin/bash

echo "======================================================================="
echo "Build Docker container for GRETL runtime"
echo "Uses the following container definition:"
echo "https://github.com/openshift/jenkins/blob/master/slave-maven/Dockerfile"
echo "======================================================================="

# TODO no version lock
cp ../gretl/build/libs/gretl-1.0.4-SNAPSHOT.jar gretl
cp ../gretl/lib/ojdbc7-12.1.0.1.jar gretl
cp ../dependencies.gradle gretl

# build infos
echo "local build" > gretl/build.info
echo date: `date '+%Y-%m-%d %H:%M:%S'` >> gretl/build.info

docker build --no-cache --force-rm -t gretl-runtime -f gretl/Dockerfile gretl

rm gretl/gretl*.jar
rm gretl/ojdbc7-*.jar
rm gretl/dependencies.gradle
rm gretl/build.info

# look into the container:
# docker run -it --entrypoint=/bin/sh gretl-runtime
