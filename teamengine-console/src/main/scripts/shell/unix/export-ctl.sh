#!/bin/bash
# Reads a CSV file (first argument) where each record contains two fields:
# Subversion tag URL, local path name relative to TE_BASE/scripts
# Example:
# http://svn.example.org/scripts/alpha/1.0.0/tags/r2,alpha/1.0.0

base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
[ -z "$JAVA_HOME" ] && echo "JAVA_HOME must be set." && exit 1

csvfile="$1"

while IFS="," read url etspath
do
  svn -q export $url $TE_BASE/scripts/$etspath
done < "$csvfile"

pushd $TE_BASE/scripts
# filename patterns which match no files will expand to null string
shopt -s nullglob
for f in *.zip
do
  "$JAVA_HOME"/bin/jar xf "$f"
  rm "$f"
done

