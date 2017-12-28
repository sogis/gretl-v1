#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE ddluser WITH LOGIN PASSWORD 'ddluser';
    GRANT CREATE ON DATABASE gretl TO ddluser;
    CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';
    CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';
    select version();
    create extension postgis;
    create extension "uuid-ossp";
    select postgis_full_version();
EOSQL
