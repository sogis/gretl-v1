# Tooling

Dockerfiles and convenience scripts for local development.

## test database
PostgreSQL with postgis Docker image to run test tasks against a local database.

### run
The script ```start-test-database.sh``` builds and runs the database locally.

### test
Run the *dbTest* task of the root build.gradle file:
```
./gradlew -Ddburl=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build dbTest
```

Run the *testIntegration* task of the gretl/inttest build.gradle file:
```
cd gretl/inttest
./gradlew build testIntegration
```
