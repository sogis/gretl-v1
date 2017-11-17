# GRETL Docker environment

Dockerfiles and convenience scripts.

## Jenkins
Jenkins Docker image provided from OpenShift: https://github.com/openshift/jenkins

### run
The script ```start-jenkins.sh``` builds and runs the Jenkins on the local Docker instance.

## GRETL runtime
Docker image based on JDK8 with gradle 4.3.

The GRETL plugin with all dependencies is also packed into the image.

### build
The script ```build-gretl.sh``` builds the runtime as Docker image with the name **gretl-runtime**.

### run
The script ```start-gretl.sh``` runs the image *gretl-runtime*.
Therefore  the image has to be built before, see the build section.
It also asks for the GRETL job to be executed and the parameter to be passed to the job execution.

### test
The script ```test-gretl.sh``` holds job name and parameter to test the runtime with the *afu_altlasten_pub* job.

Be sure to change the path of the job files according to the location on your machine.
