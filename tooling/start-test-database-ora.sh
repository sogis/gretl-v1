#!/bin/bash

echo "=========================================================="
echo "Docker container for test Oracle database."
echo "Uses the following Docker image:"
echo "https://hub.docker.com/r/pengbai/docker-oracle-12c-r1/"
echo "=========================================================="


if [ "x$1" = "x" ]; then

    docker run -it \
        --name test-oracle \
        -p 1521:1521 \
        pengbai/docker-oracle-12c-r1

else #run image in background

    echo 'Pulling image in foreground, running in background. Oracle listening on localhost:1521'

    docker pull pengbai/docker-oracle-12c-r1

    docker run -d \
        --name test-oracle \
        -p 1521:1521 \
        pengbai/docker-oracle-12c-r1
fi



# hostname: localhost
# port: 1521
# sid: xe
# username: system
# password: oracle
# jdbc-url: jdbc:oracle:thin:@localhost:1521:xe