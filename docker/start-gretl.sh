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

# special run configuration for jenkins-slave based image:
# 1. use a shell as entry point
# 2. mount job directory as volume
# 3. run as current user to avoid permission problems on generated .gradle directory
# 4. gretl-runtime image with tag latest
# 5. executed commands seperated by semicolon:
#    a. jenkins jnlp client
#    b. change to project directory
#    c. run gradle with given task and parameter using init script from image

docker run -i --rm \
    --entrypoint="/bin/sh" \
    -v "$job_directory":/home/gradle/project \
    -e USERID=$UID \
    gretl-runtime "-c" "/usr/local/bin/run-jnlp-client;cd /home/gradle/project;gradle $task_name ${task_parameter[@]} --init-script /home/gradle/init.gradle --stacktrace"
