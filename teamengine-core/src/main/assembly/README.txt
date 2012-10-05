The value of the TE_BASE system property or environment variable specifies the 
location of the main configuration directory that contains several essential 
sub-directories (see structure below). Unpack the contents of the *-base.zip 
archive into the TE_BASE directory.

TE_BASE
  |-- config.xml
  |-- resources/
  |-- scripts/
  |-- work/
  +-- users/
      |-- {username1}/
      +-- {usernameN}/

The "resources" sub-directory contains libraries and other resources that are 
required to execute a test suite using a command-line shell; it should be 
structured as indicated below.

resources/
  |
  +-- lib/*.jar
 
