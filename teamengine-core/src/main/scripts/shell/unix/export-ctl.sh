#!/bin/sh
# Reads a CSV file (first argument) where each record contains two fields:
# Subversion tag URL, local path name relative to TE_BASE/scripts
# Example:
# http://svn.example.org/scripts/alpha/1.0.0/trunk,alpha/1.0.0

csvfile="$1"
# remove CR (Windows line breaks)
sed -i'.bak' -e 's/\x0D//g' $csvfile

while IFS="," read url etspath
do
  svn -q export $url $TE_BASE/scripts/$etspath
done < "$csvfile"

