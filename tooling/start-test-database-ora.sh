#!/bin/bash

echo "=========================================================="
echo "Docker container for test Oracle database."
echo "Uses the following Docker image:"
echo "https://hub.docker.com/r/sath89/oracle-12c/"
echo "=========================================================="

docker run -it \
    --name test-oracle \
    -p 1521:1521 \
    pengbai/docker-oracle-12c-r1

# CHANGE TO ORACLE!!!!
# command to run tests against this database:
# ./gradlew -Ddburl=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build dbTest
