FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift

MAINTAINER milkliver
#ARG uid=0
#ARG gid=0
USER 0

#========================install rpms========================
#RUN mkdir /rpms
#WORKDIR /rpms
#ADD ./rpms /rpms
#RUN rpm -ivh --nodigest --nofiledigest /rpms/*

#RUN java -version


#========================add scdf executor and jobs========================
RUN mkdir /workdir
WORKDIR /workdir

ADD ./scdf-task01.jar /workdir/
ADD ./externalProgramFiles/* /workdir/
RUN chmod 777 -Rf /workdir

RUN mkdir /configs
ADD ./*.properties /configs/
RUN chmod 777 -Rf /configs/*



#========================run scdf========================
USER 1001

ENTRYPOINT ["/bin/java","-jar","-Dspring.config.location=/configs/application.properties","/workdir/*.jar"]
#CMD ["/bin/java","-jar","-Dspring.config.location=/configs/execution.properties","/workdir/scdf-task01.jar"]


# For Test
#CMD ["tail","-f","/dev/null"]