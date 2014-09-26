#!/bin/sh

#   This script (for UNIX) builds teamengine from scratch. It does not install any test, 
#   except for a default simple test (note.ctl).   
#
#   It automates most of the work require to build:
#     - TEAM Engine
#     - TE_BASE
#     - catalina_base
#
#    At the end of the script there is an option to start tomcat automatically. You need to        
#     remove an exit command. Use with caution. 
#
#   Requirements:
#     - git (1.9.4)
#     - mvn (3.2)
#     - Java (1.6 or more)
#     - local installation of tomcat (7.X)
#
#  The following values are required:
#  - te_git_url: git url of the repository. You can use the remote one or a local one (e.g. 
#    used for fast testing new features).
#  - te_tag: tag or branch to be tested
#  - folder_to_build: local directory where teamengine and catalina_base will be installed
#  - tomcat: path to the local installation of tomcat
#  - folder_site: Optional: Additionally you can provide a folder that has the body, 
#    header and footer information for the Welcome Page. An example is available at     
#    teamengine/teamengine-console/src/main/resources/site
#
#  More information about how to build and customize TEAM Engine here: 
#  https://github.com/opengeospatial/teamengine/

#te_git_url=https://github.com/opengeospatial/teamengine/
te_git_url=file:///Users/lbermudez/Documents/Dropbox/github/teamengine
#te_tag=4.0.5
te_tag=master
folder_to_build=/Users/lbermudez/Documents/Dropbox/software/te_build-20140925
tomcat=/Applications/apache-tomcat-7.0.53

parent_dir=$(pwd)
## optional: contains body, header and footer for the welcome page
folder_site=$parent_dir/site

## no need to change anything else hereafter
catalina_base=$folder_to_build/catalina_base 
war_name=teamengine
repo_te=$folder_to_build/teamengine

clean=true

##  clean 
if [ $clean ]; then
   rm -rf $folder_to_build
   mkdir -p $folder_to_build
fi  


## download TE 

echo "downloading and installing TE"
cd $folder_to_build
git clone $te_git_url teamengine
cd $folder_to_build/teamengine 
git checkout $te_tag
mvn clean install
   

## create and populate catalina base 
rm $catalina_base
mkdir -p $catalina_base
cd $catalina_base
mkdir bin logs temp webapps work lib

## copy from tomcat bin and base files
cp $tomcat/bin/catalina.sh bin/
cp -r $tomcat/conf $catalina_base

## move tomcat to catalina_base
cp $repo_te/teamengine-web/target/teamengine.war $catalina_base/webapps/$war_name.war
unzip -o $repo_te/teamengine-web/target/teamengine-common-libs.zip -d $catalina_base/lib 

## build TE_BASE

mkdir -p $catalina_base/TE_BASE
export TE_BASE=$catalina_base/TE_BASE 

## get the file that has base zip and copy it to TE_BASE
cd $repo_te/teamengine-console/target/
base_zip=$(ls *base.zip | grep -m 1 "base")
unzip -o $repo_te/teamengine-console/target/$base_zip -d $TE_BASE

## copy test users
# cp -r $parent_dir/users/ $TE_BASE/users

## create setenv with environmental variables
cd $catalina_base/bin
touch setenv.sh
cat <<EOF >setenv.sh
#!/bin/sh
## path to tomcat installation to use
export CATALINA_HOME=$tomcat

## path to server instance to use
export CATALINA_BASE=$catalina_base
export CATALINA_OPTS='-server -Xmx1024m -XX:MaxPermSize=128m -DTE_BASE=$TE_BASE'
EOF

chmod 777 *.sh

## The folder_site contains body, header and footer to customize TE.
if [ -d "$folder_site" ];then

   ## move site folder to TE_BASE
   cd $parent_dir
   rm -r $TE_BASE/resources/site
   cp -r $folder_site/ $TE_BASE/resources/site
  
   ## update site in the war file
   cp -r $TE_BASE/resources/site $catalina_base/webapps/site
   cd $catalina_base/webapps
   jar -uf $war_name.war site

fi 

echo "catalina_base was built at" $catalina_base 
echo 'to start run: '$catalina_base'/bin/catalina.sh start'  
echo 'to stop run: '$catalina_base'/bin/catalina.sh stop'  


## If you want the script to start catalina, remove (or comment) the exit command with caution. It will stop any tomcat process and will start catalina_base where teamengine.war was installed.

exit

## check if there is a tomcat instance running and kill the process
pid=$(ps axuw | grep tomcat | grep -v grep |  awk '{print $2}')
if [ "${pid}" ]; then
  eval "kill ${pid}"
fi
## starts teamengine
$catalina_base/bin/catalina.sh start   
