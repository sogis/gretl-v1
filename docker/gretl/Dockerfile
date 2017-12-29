FROM openshift/jenkins-slave-maven-centos7

USER root

COPY gretl /usr/local/bin/
COPY gretl-*.jar /home/gradle/libs/
COPY init.gradle build.gradle dependencies.gradle build.info /home/gradle/

RUN cd /home/gradle && \
    gradle copyToLibs && \
    rm /home/gradle/build.gradle && \
    rm /home/gradle/dependencies.gradle && \
    chown -R 1001:0 /home/gradle && \
    chmod -R g+rw /home/gradle && \
    ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs

USER 1001
