FROM gradle:4.3.0-jdk8-alpine

COPY init.gradle /home/gradle/
COPY build.gradle /home/gradle/

RUN mkdir -p /home/gradle/libs
COPY gretl-1.0.4-SNAPSHOT.jar /home/gradle/libs

# TODO fix dependency in gretl/init.gradle
COPY postgresql-42.1.4.jar /home/gradle/libs

RUN gradle copyToLibs
RUN ls -la libs

ENTRYPOINT ["gradle", "--offline", "--init-script", "/home/gradle/init.gradle"]
CMD ["showRepos"]
