#!/bin/sh
base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
cp=$base/../resources/
for x in $base/../repo/*.jar
do
  cp=$cp:$x
done
for x in $base/../components/*
do
  cp=$cp:$x/resources
  for y in $x/lib/*.jar
  do
    cp=$cp:$y
  done
done
for x in $base/../scripts/*
do
  cp=$cp:$x/resources
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi
$java -cp $cp -Djava.protocol.handler.pkgs=com.occamlab.te.util.protocols $JAVA_OPTS com.occamlab.te.Test -cmd="$0" "$@"

