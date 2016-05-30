#!/bin/bash
yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
yum check-update
yum -y update
# Oracle JDK 8u91
cd /tmp
curl -L -b "oraclelicense=accept" -O http://download.oracle.com/otn-pub/java/jdk/8u91-b14/jdk-8u91-linux-x64.tar.gz
mkdir /opt/oracle.com
cd /opt/oracle.com
tar xzf /tmp/jdk-*.tar.gz
ln -s jdk1.8.0_91/ jdk8
# Apache Tomcat 7.0
cd /tmp
curl -O http://apache.mirror.gtcomm.net/tomcat/tomcat-7/v7.0.69/bin/apache-tomcat-7.0.69.tar.gz
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
# Clean up
rm -f /tmp/*.tar.gz
