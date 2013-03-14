#!/bin/sh
# Reads a CSV file (first argument) where each record contains two fields:
# Subversion tag URL, local path name relative to TE_BASE/scripts
# Example:
# http://svn.example.org/scripts/alpha/1.0.0/trunk,alpha/1.0.0
base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi

csvfile="$1"

while IFS="," read url etspath
do
  svn -q export $url $TE_BASE/scripts/$etspath
done < "$csvfile"
