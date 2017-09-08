# gretl

The [Gradle](http://www.gradle.org) gretl plugin extends gradle for use as a sql-centric (geo)data etl. gretl = gradle etl.

## Features

* Database
  * Db2Db
  * SQL executor

## Licencse

gretl is licensed under the [MIT License](LICENSE).

## Status

gretl is in development state.

## System requirements

For the current version of gretl, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later and gradle, version 3.4 or later.

## How to use?

Add this to your `build.gradle`:

```
buildscript {
  repositories {
    mavenCentral()
    maven { url "https://plugins.gradle.org/m2/" }
  }
  dependencies {
    classpath "ch.so.agi.gretl:gradle-gretl.plugin:1.0.0"
  }
}

apply plugin: 'ch.so.agi.gretl'
```

### Supported databases

The database related tasks (`db2db` and `sqlexecutor`) should work with any database with an jdbc driver available. At the moment only PostgreSQL and SQLite are supported (because of the dependency definined in the project's `build.gradle`) and tested:

 * PostgreSQL: `jdbc:postgresql://192.168.50.4:5432/xanadu`
 * SQLite: `jdbc:sqlite:/road/to/mandalay.sqlite`


### Db2Db

```
import ch.so.agi.gretl.steps.*

task transferSomeData(type: Db2DbTask) {
    sourceDb = ['jdbc:postgresql://192.168.50.4:5432/xanadu','login1','password1']
    targetDb = ['jdbc:sqlite:/road/to/mandalay.sqlite',null,null]
    transferSets = [
            new TransferSet('../../can/be/a/relative/path/some.sql', 'albums_dest', true)
    ];
}
```
Copies data from a source database (`sourceDb`) to target database (`targetDb`). It can handle an arbitrary count of transfers in one task.

 `sourceDB` / `targetDB`: A list with a valid jdbc database url, the login name and the password. 

`transferSets`: A list of `TransferSet`. A transfer set consists of the path to the file containing the sql select statement, the destination table and the option to delete the destination table contents before adding the new data.

### SQL executor

```
import ch.so.agi.gretl.steps.*

task executeSomeSql(type: SqlExecutorTask){
    database = ['jdbc:postgresql://192.168.50.4:5432/xanadu','login1','password1']
    sqlFiles = ['/can/be/an/absolute/path/some.sql']
}
```

This task can be used to execute _any_ sql statement in one database.

`database`: A list with a valid jdbc database url, the login name and the password. 

`sqlFiles`: A list with files names containing _any_ sql statements.

