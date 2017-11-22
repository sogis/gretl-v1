#!/bin/bash

echo "=========================================================="
echo "Docker container for test PostgreSQL database with postgis"
echo "Uses the following Docker image:"
echo "https://hub.docker.com/r/mdillon/postgis"
echo "=========================================================="

docker build --no-cache --force-rm -t test-database -f test-database/Dockerfile test-database

docker run -it \
    --name test-postgis \
    -e POSTGRES_PASSWORD=admin1234 \
	-p 5432:5432 \
	--rm \
	test-database

# command to run tests against this database:
# ./gradlew -Ddburl=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build dbTest
