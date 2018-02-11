# Tooling

Dockerfiles and convenience scripts for local development.

## Test database
Images are for running test tasks against local databases.

### Vendors
#### PostgreSQL
PostgreSQL with PostGIS Docker image with specific (`gretl`) database and users. See [Dockerfile](test-database-pg/Dockerfile) and [init-test-db.sh](test-database-pg/init-test-db.sh).
 
#### Oracle 
Plain-vanilla Oracle 12c image from hub.docker.com. No specific database and/or users. There are a lots of Oracle images on hub.docker.com. Following images do have an reasonable start up time:

* https://hub.docker.com/r/pengbai/docker-oracle-12c-r1/ (which is used here)
* https://hub.docker.com/r/alexeiled/docker-oracle-xe-11g/ 

The credentials seem to be same for all images:

- hostname: localhost
- port: 1521
- sid: xe
- username: system
- password: oracle

### Run
#### PostgreSQL
The script ```start-test-database-pg.sh``` builds and runs the PostgreSQL database locally.

#### Oracle
The script `start-test-database-ora.sh`runs the Oracle database locally.

### Test

*TODO!!!! for ora*

Run the *dbTest* task of the root build.gradle file:
```
./gradlew -Ddburl=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build dbTest
```

Run the *testIntegration* task of the gretl/inttest build.gradle file:
```
cd gretl/inttest
./gradlew build testIntegration
```
