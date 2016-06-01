#!/bin/bash
# Environment variable TE_VERSION specifies TEAMengine release version
: ${TE_VERSION:=4.0.6}
yum -y install unzip
# Derby system directory (derby.system.home)
mkdir /srv/derby
chown -R tomcat:tomcat /srv/derby
# TEAMengine instance directory
mkdir /srv/teamengine
cd /srv/teamengine
unzip -a /tmp/teamengine-base.zip
chown -R tomcat:tomcat /srv/teamengine
cd /tmp
curl -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}.war"
curl -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-realm/${TE_VERSION}/teamengine-realm-${TE_VERSION}.jar"
if [ -e teamengine-web-$TE_VERSION.war ]; then
  mv teamengine-web-$TE_VERSION.war /srv/tomcat7/base-1/webapps/teamengine.war
  mv teamengine-realm-$TE_VERSION.jar /srv/tomcat7/base-1/lib/
fi
rm -f /tmp/*.zip
