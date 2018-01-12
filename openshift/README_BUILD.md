GRETL Jenkins Pipeline Setup
============================
Requirements and guide for build and deploy of the GRETL platform.

Plugins
-------

### OpenShift Client Jenkins Plugin
OpenShift platform connector, used to build and deploy on OpenShift.
Needs the oc tool available on the Jenkins build node (server). Can be installed with the [OpenShift Client Tool configuration](#openShift-client-tool).

https://plugins.jenkins.io/openshift-client

https://github.com/jenkinsci/openshift-client-plugin

### Copy Artifact Plugin
Used to copy gretl jar from master build pipeline project.

https://plugins.jenkins.io/copyartifact


Configuration
-------------

### OpenShift Client Tool
OpenShift client binary to interact with OpenShift platform.

https://github.com/jenkinsci/openshift-client-plugin#setting-up-jenkins-nodes

Manage Jenkins -> Global Tool Configuration -> and find the "OpenShift Client Tools" section

OpenShift Client Tools installations

Name: oc3.7.0

Install automatically

Download URL for binary archive: https://github.com/openshift/origin/releases/download/v3.7.0/openshift-origin-client-tools-v3.7.0-7ed6862-linux-64bit.tar.gz

Subdirectory of extracted archive: openshift-origin-client-tools-v3.7.0-7ed6862-linux-64bit

### OpenShift Cluster

Manage Jenkins -> Configure System -> and find the OpenShift Plugin section.

Cluster Name: OpenShiftProduction

API Server URL: [OpenShift container platform url with port, if needed] 

Disable TLS Verify: true

### OpenShift Login Token
Configure Jenkins secret for OpenShift login.
Service account has to be created first in the OpenShift project.
You can use a domain for all OpenShift related credentials: "OpenShift"

Add a secret text credential

Token: [from service account]

ID: [OpenShift project name]_deploy_token

Description: oc tool login token for project [OpenShift project name]

#### Service account creation
Add service account to the OpenShift project.

* Login to the server with the oc tool and go to your project.
* *oc project* to check that you are in the right project.
* Add service account by this script: **scripts/create-service-account.sh**
* Read the token:
* *oc describe sa jenkins*
* Copy name of first Token.
* *oc describe secret JENKINS-TOKEN-NAME*
* this will display the token
* Create a secret text credential on jenkins

OpenShift build project
-----------------------
Prepare an OpenShift build project and use this pipeline: **openshift/pipeline/gretl-runtime-build.groovy**

### GRETL runtime Docker image
Project used to build GRETL runtime Docker image and push it to dockerhub.

Documentation: https://blog.openshift.com/pushing-application-images-to-an-external-registry/

#### OpenShift project setup
Create project and give access to push to Docker Hub.

```
new-project gretl-build
oc secrets new dockerhub ~/.docker/config.json
oc describe secret dockerhub
oc edit sa builder
```

##### Create build configuration by template
```
oc process -f openshift/templates/gretl-build-template.yaml \
  -p GRETL_RUNTIME_IMAGE="chrira/jobrunner:latest" \
  | oc apply -f -
```

##### Manual build configuration
Not used, when template was applied.
```
oc new-build --name=gretl --strategy=Docker --binary=true
```
```
oc edit bc gretl
```
Change destination to your Docker Hub repository.
```
    to:
      kind: DockerImage   
      name: docker.io/chrira/jobrunner:latest
    pushSecret:
      name: dockerhub
```

###### Build from local machine
Login to OpenShift and use the build project.

```
cd docker/gretl
cp ../../dependencies.gradle .
cp ../../build/libs/gretl-1.0.4-SNAPSHOT.jar .
oc start-build gretl --from-dir . --follow

```


OpenShift integration test project
----------------------------------
Prepare an OpenShift test project and use this pipeline: **openshift/pipeline/gretl-integration-test.groovy**

### OpenShift project setup
Login to OpenShift and use the integration-test project.

Create the needed database:
```
oc process postgresql-ephemeral -n openshift \
  -p POSTGRESQL_DATABASE='gretl' \
  -p DATABASE_SERVICE_NAME='postgresql' \
  | oc create -f -
```
Does not have GIS extension.

#### Crunchy DB with GIS extension
Needs to be configured for int-test use.

Template taken from [CrunchyData](https://github.com/CrunchyData/crunchy-containers).
```
oc process -f openshift/templates/postgres-gis.json \
  -p CCP_IMAGE_PREFIX=crunchydata \
  -p CCP_IMAGE_TAG=centos7-10.1-1.7.0 \
  -p POSTGRESQL_DATABASE='gretl' \
  -p DATABASE_SERVICE_NAME='postgresql' \
  -p PRIMARY_USER='postgresql' \
  | oc apply -f -
```

Cleanup:
```
oc delete deployments,pods,service -l name=postgresql
```


OpenShift system test project
-----------------------------
Prepare an OpenShift test project.

### OpenShift project setup
Create project
```
oc new-project gretl-system-test
```
Setup runtime
```
oc process -f openshift/templates/jenkins-s2i-template.json \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/chrira/openshift-jenkins.git" \
  -p JENKINS_IMAGE_STREAM_TAG="jenkins:2" \
  -p GRETL_JOB_REPO_URL="git://github.com/sogis/gretl.git" \
  -p GRETL_JOB_FILE_PATH="inttest/jobs/**" \
  -p GRETL_JOB_FILE_NAME="gretl-job.groovy" \
  | oc apply -f -
```


OpenShift Jenkins project
-------------------------

```
oc process -f jenkins-s2i-template.json \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/chrira/openshift-jenkins.git" \
  -p GRETL_JOB_REPO_URL="git://github.com/chrira/gretljobs.git" \
  | oc apply -f -
```
