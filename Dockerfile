#======================== build container ========================
FROM docker.io/maven:3.9.2-eclipse-temurin-11 AS builder

MAINTAINER milkliver
#ARG uid=0
#ARG gid=0
USER 0



#======================== add configs and jar ========================
RUN mkdir /workdir
WORKDIR /workdir

COPY ./src /workdir/src
COPY ./pom.xml /workdir/

RUN mvn clean package -DskipTests


#======================== runtime container ========================
FROM docker.io/library/rockylinux:8

MAINTAINER milkliver

ENV PROPERTIES_PATH=/workdir/configs/application.properties


#======================== yum install ========================
RUN yum -y install findutils
RUN yum -y install java


#======================== rpms install ========================
#RUN mkdir /rpms
#WORKDIR /rpms
#ADD ./rpms /rpms
#RUN rpm -ivh --nodigest --nofiledigest /rpms/*
#RUN java -version


#======================== configure environment ========================
RUN mkdir -p /workdir
RUN mkdir -p /workdir/configs

WORKDIR /workdir

COPY --from=builder /workdir/target/*.jar /workdir/
COPY --from=builder /workdir/src/main/resources/*.properties /workdir/configs/

RUN chmod 777 -Rf /workdir
RUN chmod 744 -Rf /workdir/configs/*


#======================== run ========================
USER 1000

ENTRYPOINT exec ls /workdir/*.jar | xargs -i /bin/java -jar -Dspring.config.location=$PROPERTIES_PATH {}

