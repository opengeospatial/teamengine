#!/bin/sh
base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
for x in $base/../repo/*.jar
do
  cp=$cp:$x
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi
$java -cp $cp $JAVA_OPTS com.occamlab.te.ViewLog -cmd=$0 $*

