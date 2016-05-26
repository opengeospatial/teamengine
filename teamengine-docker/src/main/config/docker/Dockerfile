FROM tomcat:7.0-jre8

MAINTAINER Torsten Friebe <friebe@lat-lon.de>

# set teamengine version
ENV TEAMENGINE_VERSION 4.6

# add teamengine webapp
ADD teamengine-web-${TEAMENGINE_VERSION}.war /root/
RUN cd /root/ && unzip -q teamengine-web-${TEAMENGINE_VERSION}.war -d /usr/local/tomcat/webapps/teamengine

# add common libs
ADD teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip /root/
RUN cd /root/ && unzip -q teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip -d /usr/local/tomcat/lib

# add teamengine console
ADD teamengine-console-${TEAMENGINE_VERSION}-base.zip /root/
RUN cd /root/ && unzip -q teamengine-console-${TEAMENGINE_VERSION}-base.zip -d /root/te_base

# set TE_BASE
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/root/te_base"

# set WFS 2.0 ETS version
ENV ETS_WFS_20_VERSION 1.22

# add ETS for WFS 2.0
ADD ets-wfs20-${ETS_WFS_20_VERSION}-ctl.zip /root/
RUN cd /root/ && unzip -q ets-wfs20-${ETS_WFS_20_VERSION}-ctl.zip -d /root/te_base/scripts
ADD ets-wfs20-${ETS_WFS_20_VERSION}-deps.zip /root/
RUN cd /root/ && unzip -q -o ets-wfs20-${ETS_WFS_20_VERSION}-deps.zip -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib

# run tomcat
CMD ["catalina.sh", "run"]