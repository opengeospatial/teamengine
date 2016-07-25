#!/bin/bash
# Environment variable TE_VERSION specifies TEAMengine release version
: ${TE_VERSION:=4.7}
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
curl --retry 1 -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}.war"
curl --retry 1 -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}.war.md5"
curl --retry 1 -O "https://repo1.maven.org/maven2/org/opengis/cite/teamengine/teamengine-web/${TE_VERSION}/teamengine-web-${TE_VERSION}-common-libs.tar.gz"
md5=($(md5sum teamengine-web-$TE_VERSION.war))
if [ $md5 != `cat teamengine-web-$TE_VERSION.war.md5` ]; then
  echo 'Unexpected MD5 checksum for teamengine-web-$TE_VERSION.war' ; exit 1;
fi
mv teamengine-web-$TE_VERSION.war /srv/tomcat7/base-1/webapps/teamengine.war
tar xzf /tmp/teamengine-web-$TE_VERSION-common-libs.tar.gz -C /srv/tomcat7/base-1/lib/
cd /srv/tomcat7/base-1/webapps/
unzip -d teamengine teamengine.war
chown -R tomcat:tomcat teamengine/
# this does not apply to teamengine 4.7 or later
if ! grep -q "VirtualWebappLoader" teamengine/META-INF/context.xml; then
  sed -i '\/scripts<\/Watched/r /tmp/teamengine/context-loader' teamengine/META-INF/context.xml
fi
