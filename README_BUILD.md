GRETL Jenkins Pipeline Setup
============================
Requirements and guide for build and deploy of GRETL platform.

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

OpenShift build project
-----------------------

### GRETL runtime Docker image
Project used to build GRETL runtime Docker image and push it to dockerhub.

Documentation: https://blog.openshift.com/pushing-application-images-to-an-external-registry/


```
new-project gretl-build
oc secrets new dockerhub ~/.docker/config.json
oc describe secret dockerhub
oc edit sa builder
oc new-build --name=gretl --strategy=Docker --binary=true
oc edit bc gretl
```
```
    to:
      kind: DockerImage   
      name: docker.io/chrira/jobrunner:latest
    pushSecret:
      name: dockerhub
```


```
cd docker/gretl
cp ../../dependencies.gradle .
cp ../../build/libs/gretl-1.0.4-SNAPSHOT.jar .
oc start-build gretl --from-dir . --follow

```

```
oc process -f jenkins-s2i-template.json \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/chrira/openshift-jenkins.git" \
  -p GRETL_JOB_REPO_URL="git://github.com/chrira/gretljobs.git" \
  | oc apply -f -
```


```
oc process -f gretl-build-template.yaml \
  -p GRETL_RUNTIME_IMAGE="chrira/jobrunner:latest" \
  | oc apply -f -
```
