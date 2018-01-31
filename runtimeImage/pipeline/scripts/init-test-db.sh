#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username postgres <<-EOSQL
    CREATE ROLE ddluser WITH LOGIN PASSWORD 'ddluser';
    GRANT ALL PRIVILEGES ON DATABASE gretl TO ddluser;
    CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';
    CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';
    SELECT version();
    CREATE EXTENSION "uuid-ossp";
EOSQL
