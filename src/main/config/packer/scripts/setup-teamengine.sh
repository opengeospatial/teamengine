#!/bin/bash
# Environment variable TE_VERSION specifies TEAMengine release version
: ${TE_VERSION:=4.5}
yum -y install unzip
# Derby system directory (derby.system.home)
mkdir /srv/derby
chown -R tomcat:tomcat /srv/derby
# TEAMengine instance directory
mkdir /srv/teamengine
cd /srv/teamengine
unzip -a /tmp/teamengine/teamengine-base.zip
chown -R tomcat:cite /srv/teamengine
chmod -R g+w /srv/teamengine
# set group ID on all subdirectories
find /srv/teamengine -type d -exec chmod g+s '{}' \;
cd /tmp
curl -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}.war"
curl -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}-common-libs.tar.gz"
if [ -e teamengine-web-$TE_VERSION.war ]; then
  mv teamengine-web-$TE_VERSION.war /srv/tomcat7/base-1/webapps/teamengine.war
  tar xzf /tmp/teamengine-web-$TE_VERSION-common-libs.tar.gz -C /srv/tomcat7/base-1/lib/
fi
cd /srv/tomcat7/base-1/webapps/
unzip -d teamengine teamengine.war
chown -R tomcat:tomcat teamengine/
# does not apply to teamengine 4.7 or later
if ! grep -q "VirtualWebappLoader" teamengine/META-INF/context.xml; then
  sed -i '\/scripts<\/Watched/r /tmp/teamengine/context-loader' teamengine/META-INF/context.xml
fi
