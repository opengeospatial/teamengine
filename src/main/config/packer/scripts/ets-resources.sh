#!/bin/bash
# Environment variable ETS_RESOURCES_RELEASE specifies ets-resources release version
# See https://github.com/opengeospatial/ets-resources
#
# Note: Maven and Git must be installed and available on the system path.
: ${ETS_RESOURCES_RELEASE:=16.06.08}

cd $HOME
# Modify ~/.bash_profile in place (login shell)
sed -i '/^PATH=/ i export TE_BASE=/srv/teamengine' .bash_profile
sed -i '/^PATH=/ i export ETS_SRC=/usr/local/src/cite' .bash_profile
sed -i '/^PATH=/ i export JAVA_HOME=/opt/jdk8' .bash_profile
sed -i '/^PATH=/ i export M2_HOME=/opt/apache.org/maven3' .bash_profile
sed -i '/^PATH=/ s/$/:$M2_HOME\/bin/' .bash_profile
. ~/.bash_profile

cd $ETS_SRC
git clone https://github.com/opengeospatial/ets-resources.git ets-resources
cd ets-resources
git checkout $ETS_RESOURCES_RELEASE
mvn clean install
cd $HOME; mkdir ets-resources-$ETS_RESOURCES_RELEASE
tar xzf $ETS_SRC/ets-resources/target/ets-resources-*.tar.gz -C ets-resources-$ETS_RESOURCES_RELEASE
cd ets-resources-$ETS_RESOURCES_RELEASE
./bin/unix/setup-tebase.sh ctl-scripts-release.csv
