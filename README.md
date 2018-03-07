# gretl

The [Gradle](http://www.gradle.org) gretl plugin extends gradle for use as a sql-centric
(geo)data etl. gretl = gradle etl.

This repository consists of three parts for the GRETL system:
* _GRETL_  jar (Gradle plugin)
    * [gretl](gretl) folder
* _GRETL_  runtime (Docker Image)
    * [runtimeImage](runtimeImage) folder, [README](runtimeImage/README.md)
* _GRETL_  system configuration (OpenShift project)
    * [serviceConfig](serviceConfig) folder, [README](serviceConfig/README.md)

## Licencse

_GRETL_ is licensed under the [MIT License](LICENSE).

## Architecture

The description of the architecture can be found here: [docs/architecture/](docs/architecture/architecture.md)

## Manual

A german user manual can be found here: [docs/user/](docs/user/index.md)

## Devel

Information about _GRETL_ developing can be found here: [docs/devel/](docs/devel/index.md)

## Status

_GRETL_  is in development state.

## System requirements

For the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed
on your system, version 1.8 or later and gradle, version 3.4 or later.
For convenience use the gradle wrapper.

