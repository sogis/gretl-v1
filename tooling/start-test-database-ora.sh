#!/bin/bash

echo "=========================================================="
echo "Docker container for test Oracle database."
echo "Uses the following Docker image:"
echo "https://hub.docker.com/r/pengbai/docker-oracle-12c-r1/"
echo "=========================================================="

docker run -it \
    --name test-oracle \
    -p 1521:1521 \
    pengbai/docker-oracle-12c-r1

# hostname: localhost
# port: 1521
# sid: xe
# username: system
# password: oracle
# jdbc-url: jdbc:oracle:thin:@localhost:1521:xe