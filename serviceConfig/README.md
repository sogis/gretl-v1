OpenShift GRETL system project
------------------------------
Create the GRETL system on the OpenShift container platform.


## Setup runtime with Jenkins

### Create project
```
oc new-project gretl-system
```

### GRETL-Jenkins
Apply project template with the GRETL-Jenkins configuration.
```
oc process -f serviceConfig/templates/jenkins-s2i-template.json \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/sogis/openshift-jenkins.git" \
  -p JENKINS_IMAGE_STREAM_TAG="jenkins:2" \
  -p GRETL_JOB_REPO_URL="git://github.com/sogis/gretljobs.git" \
  -p GRETL_JOB_FILE_PATH="**" \
  -p GRETL_JOB_FILE_NAME="gretl-job.groovy" \
  | oc apply -f -
```
Parameter:
* JENKINS_CONFIGURATION_REPO_URL: Repo containing the Jenkins configuration.
* JENKINS_IMAGE_STREAM_TAG: Docker base image for the Jenkins. 
* GRETL_JOB_REPO_URL: Repo containing the GRETL jobs.
* GRETL_JOB_FILE_PATH: Base path to the GRETL job definitions (Ant style)
* GRETL_JOB_FILE_NAME: Name of the GRETL job configuration file.

### GRETL runtime
The GRETL runtime configuration with definition of which Docker image to pull from Docker Hub.

Add gretl imagestream to pull newest GRETL runtime image:
```
oc process -f serviceConfig/templates/gretl-is-template.json \
  -p GRETL_RUNTIME_IMAGE="sogis/gretl-runtime:26" \
  | oc apply -f -
```
Parameter:
* GRETL_RUNTIME_IMAGE: Docker image reference of the GRETL runtime.

This can also be used to update the GRETL runtime image after creation.
