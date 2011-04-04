#!/bin/sh
base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
cp=$base/../apps/engine/resources
for x in $base/../apps/engine/dist/*.jar
do
  cp=$cp:$x
done
for x in $base/../apps/engine/lib/*.jar
do
  cp=$cp:$x
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi
$java -cp $cp $JAVA_OPTS com.occamlab.te.ViewLog -cmd=$0 $*

