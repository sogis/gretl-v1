#!/bin/bash

read -p 'job directory: ' jobDirectory
read -p 'task name: ' taskName

# TODO: no fix parameter amount and names
read -p 'sourceDbUrl: ' sourceDbUrl
read -p 'sourceDbUser: ' sourceDbUser
read -sp 'sourceDbPass: ' sourceDbPass
read -p 'targetDbUrl: ' targetDbUrl
read -p 'targetDbUser: ' targetDbUser
read -sp 'targetDbPass: ' targetDbPass

docker run -i --rm \
    -v "$jobDirectory":/home/gradle/project \
    -w /home/gradle/project \
    gretl-runtime "$taskName" \
    -PsourceDbUrl="$sourceDbUrl" \
    -PsourceDbUser="$sourceDbUser" \
    -PsourceDbPass="$sourceDbPass" \
    -PtargetDbUrl="$targetDbUrl" \
    -PtargetDbUser="$targetDbUser" \
    -PtargetDbPass="$targetDbPass"


