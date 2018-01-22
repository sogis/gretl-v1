#!/bin/bash

job_directory=$(pwd)/../inttest/jobs/iliValidator
task_name=validate


# run GRETL job by GRETL runtime
./start-gretl.sh --job_directory $job_directory --task_name $task_name
