#!/bin/bash

# requires GRETL jobs to be cloned beside the gretl repository.
# requires test postgres db to be running on the host with port: 5432
#  -> test db can be started with: start-test-database.sh


hostIP=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`

job_directory=$(pwd)/../gretl/inttest/jobs/db2dbTaskRelPath
task_name=relativePath

task_parameter=(
-Pgretltest_dburi="jdbc:postgresql://$hostIP:5432/gretl"
)


# run GRETL job by GRETL runtime
./start-gretl.sh --job_directory $job_directory --task_name $task_name "${task_parameter[@]}"
