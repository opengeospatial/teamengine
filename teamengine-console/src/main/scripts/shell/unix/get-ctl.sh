#!/bin/bash
# Reads a CSV file (first argument) where each record contains two fields:
# URL, local path name relative to TE_BASE/scripts
# Example:
# http://svn.example.org/scripts/alpha/1.0.0/tags/r2,alpha/1.0.0
# http://search.maven.org/remotecontent?filepath=org/example/beta/1.0/beta-1.0.zip,beta-1.0.zip
#
# Note: wget or curl is required to download Maven artifacts.

base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
[ -z "$JAVA_HOME" ] && echo "JAVA_HOME must be set." && exit 1

csvfile="$1"

while IFS="," read url etspath
do
  if [[ $url == *filepath=* ]]; then
    wget -O $TE_BASE/scripts/$etspath $url 2>/dev/null || curl -o $TE_BASE/scripts/$etspath $url
  else
    svn export $url $TE_BASE/scripts/$etspath -q 2>/dev/null
  fi
done < "$csvfile"

pushd $TE_BASE/scripts
# filename patterns which match no files will expand to null string
shopt -s nullglob
for f in *.zip
do
  "$JAVA_HOME"/bin/jar xf "$f"
  rm "$f"
done
