FROM openshift/jenkins-slave-maven-centos7

USER root

COPY gretl /usr/local/bin

COPY init.gradle /home/gradle/
COPY build.gradle /home/gradle/
COPY dependencies.gradle /home/gradle/

RUN mkdir -p /home/gradle/libs
RUN cd /home/gradle && \
    gradle copyToLibs
COPY gretl-1.0.4-SNAPSHOT.jar /home/gradle/libs
RUN ls -la /home/gradle/libs

RUN rm /home/gradle/build.gradle && \
    rm /home/gradle/dependencies.gradle

RUN chown -R 1001:0 /home/gradle && \
    chmod -R g+rw /home/gradle

RUN ls -la /home/gradle

USER 1001

#ENTRYPOINT ["gradle", "--offline", "--init-script", "/home/gradle/init.gradle"]
#CMD ["showRepos"]
