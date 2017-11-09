#!/bin/bash

echo "=============================================================="
echo "Docker container for test jenkins"
echo "Uses the following container definition:"
echo "https://github.com/openshift/jenkins"
echo "=============================================================="

docker build --force-rm -t gretl-test-jenkins -f jenkins/Dockerfile jenkins


# set permission for jenkins data directory
#chmod 777 `pwd`/jenkins/data

#docker run -it \
#	--name jenkins_my \
#	-e JENKINS_PASSWORD=admin1234 \
#	-p 8080:8080 \
#	-v `pwd`/jenkins/data:/var/lib/jenkins \
#	openshift/jenkins-2-centos7


docker run -it \
	--name jenkins_my \
	-e JENKINS_PASSWORD=admin1234 \
	-p 8080:8080 \
	--rm \
	gretl-test-jenkins
