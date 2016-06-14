#!/bin/bash
: ${use_oracle_jdk:=false}
yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
yum check-update
yum -y update
yum -y install apache-commons-daemon-jsvc
if [ $use_oracle_jdk = true ]; then
  cd /tmp
  curl --retry 2 -L -b "oraclelicense=accept" -O http://download.oracle.com/otn-pub/java/jdk/8u91-b14/jdk-8u91-linux-x64.tar.gz
  mkdir /opt/oracle.com
  cd /opt/oracle.com
  tar xzf /tmp/jdk-*.tar.gz
  ln -s /opt/oracle.com/jdk1.8.0_91/ /opt/jdk8
else 
  yum -y install java-1.8.0-openjdk-devel
  ln -s /usr/lib/jvm/java-1.8.0-openjdk /opt/jdk8
fi

# Apache Tomcat 7.0
cd /tmp
curl --retry 1 -O http://apache.mirror.gtcomm.net/tomcat/tomcat-7/v7.0.69/bin/apache-tomcat-7.0.69.tar.gz
useradd tomcat
echo "tomcat:t0mc4t" | chpasswd
mkdir /opt/apache.org
cd /opt/apache.org
tar xzf /tmp/apache-tomcat-*.tar.gz
ln -s apache-tomcat-7.0.69/ tomcat7
# Create Tomcat instance
mkdir -p /srv/tomcat7/base-1
cd /srv/tomcat7/base-1
cp -r /opt/apache.org/tomcat7/conf .
mkdir lib logs temp webapps work
chown -R tomcat:tomcat /srv/tomcat7
# Modify server.xml in place
sed -i 's/redirectPort="[0-9]\+"/& enableLookups="false" URIEncoding="UTF-8"/' conf/server.xml
sed -i '/AJP\/1/ i <!--' conf/server.xml
sed -i '/AJP\/1/ a -->' conf/server.xml
# Systemd unit file
cp /tmp/tomcat/tomcat-jsvc.service /etc/systemd/system/
mkdir /etc/opt/apache.org/
cp /tmp/tomcat/tomcat.env /etc/opt/apache.org/
systemctl daemon-reload
# Clean up
rm -f /tmp/*.tar.gz
rm -fr /tmp/tomcat
