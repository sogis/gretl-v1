#!/bin/bash

# requires GRETL jobs to be cloned beside the gretl repository.
# requires test postgres db to be running on the host with port: 5432
#  -> test db can be started with: start-test-database.sh


hostIP=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`

job_directory=$(pwd)/../../gretljobs/afu_altlasten_pub
task_name=transferAfuAltlasten

task_parameter=(
-PsourceDbUrl="jdbc:postgresql://$hostIP:5432/gretl" \
-PsourceDbUser="ddluser" \
-PsourceDbPass="ddluser" \
-PtargetDbUrl="jdbc:postgresql://$hostIP:5432/gretl" \
-PtargetDbUser="dmluser" \
-PtargetDbPass="dmluser" \
)


# run GRETL job by GRETL runtime
./start-gretl-local.sh --job_directory $job_directory --task_name $task_name "${task_parameter[@]}"
