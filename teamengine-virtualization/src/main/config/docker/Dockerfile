FROM tomcat:7.0-jre8

MAINTAINER Torsten Friebe <friebe@lat-lon.de>

# set TEAM engine version
ENV TEAMENGINE_VERSION 4.8-SNAPSHOT

# add TEAM engine webapp
ADD teamengine-web-${TEAMENGINE_VERSION}.war /root/
RUN cd /root/ && unzip -q teamengine-web-${TEAMENGINE_VERSION}.war -d /usr/local/tomcat/webapps/teamengine

# add common libs
ADD teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip /root/
RUN cd /root/ && unzip -q teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip -d /usr/local/tomcat/lib

# add TEAM engine console
ADD teamengine-console-${TEAMENGINE_VERSION}-base.zip /root/
RUN cd /root/ && unzip -q teamengine-console-${TEAMENGINE_VERSION}-base.zip -d /root/te_base

# set TE_BASE
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/root/te_base"

# run tomcat
CMD ["catalina.sh", "run"]