#!/bin/bash

job_directory=$(pwd)/../inttest/jobs/iliValidatorFail
task_name=validate


# run GRETL job by GRETL runtime
./start-gretl.sh --docker_image chrira/jobrunner:19 --job_directory $job_directory --task_name $task_name
