#!/bin/bash
# Environment variable ETS_RESOURCES_RELEASE specifies ets-resources release version
# See https://github.com/opengeospatial/ets-resources
#
# Note: Maven and Git must be installed and available on the system path.
: ${ETS_RESOURCES_RELEASE:=16.02.23}
ETS_SRC=/usr/local/src/cite
# Read settings for interactive shell
if [ -r ~/.bashrc ]; then
  . ~/.bashrc
fi

cd $ETS_SRC
git clone https://github.com/opengeospatial/ets-resources.git ets-resources
cd ets-resources
git checkout $ETS_RESOURCES_RELEASE
/opt/apache.org/maven3/bin/mvn clean install
cd $HOME; mkdir ets-resources-$ETS_RESOURCES_RELEASE
tar xzf $ETS_SRC/ets-resources/target/ets-resources-*.tar.gz -C ets-resources-$ETS_RESOURCES_RELEASE
