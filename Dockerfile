FROM docker.io/centos:7
MAINTAINER milkliver
#ARG uid=0
#ARG gid=0
#USER 0

VOLUME /sys/fs/cgroup

RUN yum install -y java
RUN yum install -y curl

RUN mkdir /testfiles
WORKDIR /testfiles

ADD ./target/deploy-test.jar /testfiles/
RUN chmod 777 -Rf /testfiles

RUN mkdir /configs
ADD ./src/main/resources/application.properties /configs/application.properties
RUN chmod 777 -Rf /configs/application.properties

CMD ["/bin/java", "-jar", "-Dspring.config.location=/configs/application.properties", "/testfiles/deploy-test.jar"]
#CMD ["tail","-f","/dev/null"]