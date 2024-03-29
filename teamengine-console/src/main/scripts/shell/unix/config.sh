#!/bin/sh
base=`dirname $0`
TE_HOME=$base/../..

if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi

cp=$TE_HOME/resources
for x in $TE_HOME/lib/*.jar
do
  cp=$cp:$x
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi
$java -cp $cp $JAVA_OPTS com.occamlab.te.config.ConfigFileCreator
