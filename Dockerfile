FROM centos:7
MAINTAINER milkliver
#ARG uid=0
#ARG gid=0
#USER 0

RUN yum install -y java
RUN yum install -y curl

RUN mkdir /testfiles
WORKDIR /testfiles

ADD deploy-test.jar /testfiles/
RUN chmod 777 -Rf /testfiles

#CMD ["/bin/java", "-jar","/testfiles/deploy-test.jar"]
#CMD ["tail","-f","/dev/null"]