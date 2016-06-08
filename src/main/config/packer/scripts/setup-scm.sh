#!/bin/bash
# Install Git from WANdisco repo
mv /tmp/teamengine/WANdisco-Git.repo /etc/yum.repos.d/
curl -s -O http://opensource.wandisco.com/RPM-GPG-KEY-WANdisco
rpm --import RPM-GPG-KEY-WANdisco
rm RPM-GPG-KEY-WANdisco
yum check-update
yum -y install git

# Install Maven 3.3
cd /tmp
curl -O http://apache.mirror.gtcomm.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
cd /opt/apache.org
tar xzf /tmp/apache-maven-*.tar.gz
ln -s apache-maven-3.3.9/ maven3
mv /tmp/teamengine/mavenrc /etc/

mkdir /usr/local/src/cite
chown teamengine:teamengine /usr/local/src/cite
