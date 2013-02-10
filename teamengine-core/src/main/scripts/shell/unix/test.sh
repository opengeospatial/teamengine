#!/bin/sh
base=`dirname $0`
TE_HOME=$base/../..

if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi

cp=$TE_HOME/resources
cp=$cp:$TE_BASE/resources

for x in $TE_HOME/lib/*.jar
do
  cp=$cp:$x
done

for x in $TE_BASE/resources/*
do
  for y in $x/*.jar
  do
    cp=$cp:$y
  done
done

for x in $TE_BASE/scripts/*
do
  cp=$cp:$x/resources
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi

$java -cp $cp -Djava.protocol.handler.pkgs=com.occamlab.te.util.protocols $JAVA_OPTS com.occamlab.te.Test -cmd="$0" "$@"
