#!/bin/bash

# use like this:
# start-gretl.sh --job_directory /home/gretl --task_name gradleTaskName -Pparam1=1 -Pparam2=2

task_parameter=()

while [ $# -gt 0 ]; do
    if [[ $1 == *"--"* ]]; then
        v="${1/--/}"
        declare $v="$2"
   elif [[ "$1" =~ ^"-P" ]]; then
        task_parameter+=($1)
   fi
  shift
done

echo "======================================================="
echo "Starts the GRETL runtime to execute the given GRETL job"
echo "task name: $task_name"
echo "job directory: $job_directory"
echo "task_parameter: ${task_parameter[@]}"
echo "======================================================="

docker run -i --rm \
    -v "$job_directory":/home/gradle/project \
    -w /home/gradle/project \
    gretl-runtime "$task_name" "${task_parameter[@]}" --stacktrace
