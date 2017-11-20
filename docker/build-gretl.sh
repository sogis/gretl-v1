#!/bin/bash

echo "=============================================================="
echo "Build Docker container for GRETL runtime"
echo "Uses the following container definition:"
echo "https://github.com/keeganwitt/docker-gradle/blob/master/"
echo "                                  jdk8-alpine/Dockerfile"
echo "=============================================================="

cp ../build/libs/gretl-1.0.4-SNAPSHOT.jar gretl

# TODO fix dependency in gretl/init.gradle
wget -O postgresql-42.1.4.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.1.4/postgresql-42.1.4.jar
mv postgresql-42.1.4.jar gretl

docker build --no-cache --force-rm -t gretl-runtime -f gretl/Dockerfile gretl

rm gretl/gretl*.jar
rm gretl/postgresql*.jar

# look into the container:
# docker run -it --entrypoint=/bin/sh gretl-runtime
