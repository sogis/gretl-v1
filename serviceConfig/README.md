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
oc process -f serviceConfig/templates/jenkins-s2i-persistent-template.yaml \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/sogis/openshift-jenkins.git" \
  -p JENKINS_IMAGE_STREAM_TAG="jenkins:2" \
  -p GRETL_JOB_REPO_URL="git://github.com/sogis/gretljobs.git" \
  -p GRETL_JOB_FILE_PATH="**" \
  -p GRETL_JOB_FILE_NAME="gretl-job.groovy" \
  -p VOLUME_CAPACITY="1Gi" \
  | oc apply -f -
```
Parameter:
* JENKINS_CONFIGURATION_REPO_URL: Repo containing the Jenkins configuration.
* JENKINS_IMAGE_STREAM_TAG: Docker base image for the Jenkins. 
* GRETL_JOB_REPO_URL: Repo containing the GRETL jobs.
* GRETL_JOB_FILE_PATH: Base path to the GRETL job definitions (Ant style)
* GRETL_JOB_FILE_NAME: Name of the GRETL job configuration file.
* VOLUME_CAPACITY: Persistent volume size for Jenkins configuration data, e.g. 512Mi, 2Gi.

### GRETL runtime
The GRETL runtime configuration with definition of which Docker image to pull from Docker Hub.

Add gretl imagestream to pull newest GRETL runtime image:
```
oc process -f serviceConfig/templates/gretl-is-template.yaml \
  -p GRETL_RUNTIME_IMAGE="sogis/gretl-runtime:32" \
  | oc apply -f -
```
Parameter:
* GRETL_RUNTIME_IMAGE: Docker image reference of the GRETL runtime.

#### Update GRETL runtime image
There are several ways to change the GRETL runtime image version.

Apply the template from the previous section again with the desired image tag.


Apply a patch update with the desired image tag:
```
oc patch is gretl -p $'spec:\n  tags:\n  - from:\n      kind: DockerImage\n      name: sogis/gretl-runtime:32\n    name: latest'
```

Edit the version manually inside the web console of OpenShift
1. go to the project
1. select Builds -> Images
1. click on the Image Stream with name *gretl*
1. select *Edit YAML* on the Actions button
1. change the image tag name to the desired version and save it. 
