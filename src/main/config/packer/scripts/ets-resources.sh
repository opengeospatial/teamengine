#!/bin/bash
# Environment variable ETS_RESOURCES_RELEASE specifies ets-resources release version.
# Use tip of master branch if not set.
# See https://github.com/opengeospatial/ets-resources
#
# Note: Maven and Git must be installed and available on the system path.

cd $HOME
# Fix line endings (strip CR chars)
if [ -f ets-releases.csv ]; then
  sed -i 's/\r//' ets-releases.csv
fi

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
if [ -n "$ETS_RESOURCES_RELEASE" ]; then 
  git checkout $ETS_RESOURCES_RELEASE
fi
mvn clean package
cd $HOME; mkdir ets-resources
tar xzf $ETS_SRC/ets-resources/target/ets-resources-*.tar.gz -C ets-resources
cd ets-resources
./bin/unix/setup-tebase.sh $HOME/ets-releases.csv
