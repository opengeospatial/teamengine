TEAM Engine Tutorial
####################

:author: Luis Bermudez <lbermudez@opengeospatial.org>, Richard Martell
:version: 4.0.5
:date: March 22, 2014
:questions: http://cite.opengeospatial.org/forum

.. contents::

.. footer::

  .. class:: right

    Page ###Page###

.. section-numbering::

.. raw:: pdf
   
   PageBreak oneColumn
   

Introduction
==============


TEAMEngine (TE) is a test harness that executes test suites written using the OGC CTL test grammar or the TestNG framework.
It is typically used to verify specification compliance and is the official test harness of
the OGC Compliance Testing Program (CITE), where it is used to certify implementations of
OGC and ISO geomatics standards.

OGC hosts an official stable deployment of TEAM Engine with the approved test suites at::

    http://cite.opengeospatial.org/teamengine/

OGC hosts a Beta TEAM Engine with the tests in Beta and with new TEAM Engine functionality::

    http://cite.opengeospatial.org/te2
    
The current license is Mozilla Public License 1.1 (MPL 1.1).    

Prerequisites
==============
- JAVA 1.7
- MAVEN 3.0
- GIT 1.8
- SVN 
- APACHE TOMCAT 7.0 (Only for the web application)

Download TE Source
=======================
Go to a local directory where TE will be downloaded. For example a directory called **repo**::

	$ mkdir repo
	$ cd repo
	
The TE code is located in GitHub: https://github.com/opengeospatial/teamengine. Clone the repository::

	$ git clone https://github.com/opengeospatial/teamengine.git
	
The directory structure should now be as follows::

		/teamengine/
		├── LICENSE.txt
		├── README.md
		├── README.txt
		├── pom.xml
		├── src
		├── target
		├── teamengine-console
		├── teamengine-core
		├── teamengine-realm
		├── teamengine-resources
		├── teamengine-spi
		└── teamengine-web

List available tags::

	$ git tag
		4.0
		4.0.1
		...
		4.0.5

Switch to a specific tag::

	$ git checkout 4.0.5

Build TE Source
=======================
Go to the directory of teamengine::

	$ cd repo/teamengine
	
Build with MAVEN::

	$ mvn install
	
It can take few minutes to install. It will download all the code dependencies to **.m2/** folder. 
A success message should appear after the install::
	
   ...
   [INFO] ------------------------------------------------------------------------
   [INFO] Reactor Summary:
   [INFO] 
   [INFO] TEAM Engine ....................................... SUCCESS [15.912s]
   [INFO] TEAM Engine - Tomcat Realm ........................ SUCCESS [0.617s]
   [INFO] TEAM Engine - Shared Resources .................... SUCCESS [0.317s]
   [INFO] TEAM Engine - Service Providers ................... SUCCESS [0.901s]
   [INFO] TEAM Engine - Core Module ......................... SUCCESS [0.666s]
   [INFO] TEAM Engine - Web Module .......................... SUCCESS [0.731s]
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   [INFO] Total time: 20.151s
   [INFO] Finished at: Wed Apr 17 06:42:15 EDT 2013
   [INFO] Final Memory: 20M/81M
   [INFO] ------------------------------------------------------------------------
   

Under each directory  a **target** folder was created, which contains the build for each artifact. So for example,
the folder **teamengine-console** contains the build invoking TE via console::
	
	└── target
		├── teamengine-console-4.0.5-base.tar.gz
		├── teamengine-console-4.0.5-base.zip
		├── teamengine-console-4.0.5-bin.tar.gz
    	└── teamengine-console-4.0.5-bin.zip

	
Prepare TE BASE
---------------------

The TE_BASE contains the tests, users sessions and other configuration files. The structure of
the TE_BASE directory was created under the 
**teamengine-console/target**.

TE_BASE directory is structured as follows::

	TE_BASE
	  |-- config.xml             # main configuration file (web app)
	  |-- resources/             # Contains test suite resources (CLI)
	  |-- scripts/               # Contains CTL test suites
	  |   |--- ets.ctl           # Stand-alone script
	  |   +--- {ets}/            # A test suite package
	  |
	  |-- work/                  # teamengine work directory
	  +-- users/
		  +-- {username}/        # user credentials & test runs (web app)


The "resources" sub-directory contains libraries and other resources that are
required to execute a test suite using a command-line shell; it should be::
structured as indicated below::

	resources/
	  |
	  +-- lib/*.jar

Select a local directory for TE_BASE::

	$ mkdir ~/TE_BASE

You can configure TE_BASE system property or environment variable. For example::

	$ export TE_BASE=~/TE_BASE
	
Unzip teamengine-console-4.0.5-base.zip in the TE_BASE directory::	
	
	$ unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-base.zip -d $TE_BASE
	

Run TE in console
=======================

When running **MAVEN install** the file ``teamengine-console-4.0.5-bin.zip`` was created under the 
**teamengine-console/target**. Unzip to a directory where you will be installing and invoking TE.
For example unzipping it to a new dir  **~/te-install**::

	& mkdir ~/te-install
	$ unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-bin.zip -d ~/te-install

The **te-install** dir now looks like this::

	.
	├── README.txt
	├── bin
	├── lib
	├── resources
	
Run the example tests::
	
	$ cd $TE_BASE/scripts/
	$ ~/te-install/bin/unix/test.sh -source=note.ctl

A window should appear asking for input. The test should run and failed::

	Testing suite note:note-test in Test Mode with defaultResult of Pass ...
	...
	   Test note:main Failed
	Suite note:note-test Failed

Run and OGC Test
=======================

Locating OGC Tests
-----------------------

OGC Tests can be written either in CTL (Compliance Test Language) or TestNG. Tests are located at the public OGC SVN Repository:

CTL tests are located at:
   https://svn.opengeospatial.org/ogc-projects/cite/scripts/
   
TestNG test are located at:
   https://svn.opengeospatial.org/ogc-projects/cite/ets

This is the list of the current test ant the language they are built in:

	* Catalogue Service - Web (CSW)	2.0.2	- CTL
	* Geography Markup Language (GML)	3.2.1	- TestNG
	* OGC KML	2.2	- TestNG
	* OWS Context (OWC)	1.0 - TestNG
	* Sensor Model Language (SensorML)	1.0.1	- CTL
	* Sensor Observation Service (SOS)	1.0.0	- CTL
	* Sensor Observation Service (SOS)	2.0	r6	- CTL
	* Sensor Planning Service (SPS)	1.0		- CTL
	* Sensor Planning Service (SPS)	2.0	- CTL
	* Simple Feature Access - SQL (SFS)	1.1		- CTL
	* Simple Feature Access - SQL (SFS)	1.2.1	- CTL
	* Web Coverage Service (WCS)	1.0.0	- CTL
	* Web Coverage Service (WCS)	1.1.1	- CTL
	* Web Coverage Service (WCS)	2.0.1	- CTL
	* Web Coverage Service - Earth Observation Profile	1.0 	- CTL
	* Web Feature Service (WFS)	1.0.0	- CTL
	* Web Feature Service (WFS)	1.1.0	- CTL
	* Web Feature Service (WFS)	2.0	- TestNG
	* Web Map Server (WMS) - Client	1.3.0	- CTL
	* Web Map Service (WMS)	1.1.1	- CTL
	* Web Map Service (WMS)	1.3.0	- CTL
	* Web Map Service - SLD Profile (WMS-SLD)	1.1.0	- CTL
	* Web Map Tile Service (WMTS)	1.0.0	- CTL
	* Web Processing Service (WPS)	1.0.0	- CTL 


Installing a CTL test
-----------------------

Located a URL for a test for download. For example for CSW 2.0.2 r10:
	https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/

Install the test under scripts::

	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/ $TE_BASE/scripts/csw-2.0.2
	
The previous svn command will do a clean download of the csw 2.0.2 test to the ~/$TE_BASE/scripts/csw-2.0.2 dir

The scripts directory should look as follows::

	scripts/
	├── csw-2.0.2
	│   ├── config.xml
	│   ├── data
	│   ├── resources
	│   ├── src
	│   └── web
	└── note.ctl



Executing a CTL test
---------------------

To run a test, run **test.sh** under **~/te-install/bin/unix** with a parameter **-source=<source of the ctl file>**. 
The source file has the word *main*.

To run the CSW 2.0.2 test do the following::

	$ cd $TE_BASE/scripts
	$ ~/te-install/bin/unix/test.sh -source=csw-2.0.2/src/main.xml

A form asking to provide more information should appear. For example asking for the getCapabilities URL.
The `OGC Reference Implementations Page <http://cite.opengeospatial.org/reference>`_ provides
examples of services that can be exercised

For example for CSW 2.0.2 PyCSW:

	http://demo.pycsw.org/cite/csw?service=CSW&version=2.0.2&request=GetCapabilities

The result should be a successful pass::

	...
			Test csw:capability-tests Passed
	   Test csw:Main Passed
	Suite csw:csw-2.0.2-compliance-suite Passed
	
Installing a TestNG Tests	
---------------------------

Checkout the test from the OGC SVN repository:
	https://svn.opengeospatial.org/ogc-projects/cite/ets/testng/

For example to checkout KML 2.2 in an svn directory::
	$ cd ~/
	$ svn mkdir svn
	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/testng/ets-kml22/tags/2.2-r8/ ~/svn/kml22
	
This is the structure under the svn directory::

	svn/
	└── kml22
		├── LICENSE.txt
		├── pom.xml
		└── src

Do mvn Install::
	
	$ mvn install
	
The directory should now contain a **target** folder with the build::

	/kml22/target/
	├── ets-kml22-2.2-r8-ctl-scripts.zip
	├── ets-kml22-2.2-r8-deps.zip
	...

Unzip the ctl-scripts to TE_BASE::

	$ cd ~/svn/kml22/target
	$ unzip ets-kml22-2.2-r8-ctl-scripts.zip -d $TE_BASE/scripts/kml22

Put the libraries in $TE_BASE/resources/lib/::

	$ cd ~/svn/kml22/target
	$ unzip ets-kml22-2.2-r8-deps.zip -d jars
	$ cp jars/*.jar $TE_BASE/resources/lib/

Run the test::

	$ cd $TE_BASE/scripts/
	$ ~/te-install/bin/unix/test.sh -source=kml22/kml22/2.2/kml22-suite.ctl
	
	


Installing TestNG Tests
--------------------------

The **ets-resources** branch in the OGC SVN (https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/) contains  
all the mvn artifacts required to install TestNG tests. Look at the dates to figure out the correct download.

Checkout ets-resources in a convenient location (for example ~/svn/ets-resources)::

	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/14.03.20/ ~/svn/ets-resources
	
This is new directory structure under **ets**::

	/svn/ets-resources
	├── pom.xml
	└── src
		└── main
			├── assembly
			│   └── dist.xml
			└── config
				├── ctl-scripts-release.csv
				└── teamengine
					├── config-approved.xml
					└── config.xml

	
Go to the directory and build::
	
	$ mvn install
	
Maven generates a zip file: ets-resources-14.03.20.zip	

The following is the directory under target::

    ~/svn/ets-resources/target/
	├── archive-tmp
	├── config-approved.xml
	├── config.xml
	├── ctl-scripts-release.csv
	├── ets-resources-14.03.20.tar.gz
	├── ets-resources-14.03.20.zip
	├── lib
	└── surefire

Unzip it::
	
	$ unzip ets-resources-14.03.20.zip

It creates the following directory::

	.
	├── archive-tmp
	├── config-approved.xml
	├── config.xml
	├── ctl-scripts-release.csv
	├── ets-resources-14.03.20.tar.gz
	├── ets-resources-14.03.20.zip
	├── lib
	└── surefire
	

	
Generate the scripts using ctl-scripts-release.csv
------------------------------------------------------
The ctl.csv file (ctl-scripts-release.csv) includes entries for the latest development versions of several OGC test suites. 
Running the following command will populate the **TE_BASE/scripts** directory with these test suites:

	$ ~/te-install/bin/unix/export-ctl.sh ~/svn/ets-resources/target/ctl-scripts-release.csv

This script downloaded all the scripts in the csv file to the **$TE_BASE/scripts** folder::

	scripts/
	├── csw
	├── csw-2.0.2
	├── ets-gml-3.2.1-r13-ctl-scripts.zip
	├── ets-kml22-2.2-r6-ctl-scripts.zip
	├── ets-owc-1.0-r4-ctl-scripts.zip
	├── ets-wfs-2.0-r14-ctl-scripts.zip
	├── note.ctl
	├── sensorml
	├── sfs
	├── sos
	├── sps
	├── wcs
	├── wcseo
	├── wfs
	├── wms
	├── wms-client
	├── wms-sld
	├── wmts
	└── wps
	
Unzip all the zipped files::
	
	$ unzip '*.zip'

Install libraries under resources/lib
-------------------------------------
Copy all the libraries genereated to the **resources/lib** directory under TE_BASE::
	
	cp ~/svn/ets-resources/target/lib/*.jar $TE_BASE/resources/lib

The **resources** directory should like::
	
	/teamengine/resources
	.
	├── cite1-utils-1.1.0.jar
	├── commons-io-2.2.jar
	├── ets-gml-3.2.1-r13.jar
	├── ets-kml22-2.2-r6.jar
	├── ets-kml22-2.2-r8.jar
	├── ets-owc-1.0-r4.jar
	...
	
Copy config file in TE_BASE
-----------------------------

Copy the config.xml file to TE_BASE::
	
	cp ~/svn/ets-resources/target/config.xml $TE_BASE
	
Run a TestNG Test
---------------------

Run tests as follows::

For KML 2.2::

	$ ~/te-install/bin/unix/test.sh -source=kml22/2.2/kml22-suite.ctl 

For GML 3.2.1::	
	
	$ ~/te-install/bin/unix/test.sh -source=gml/3.2.1/gml-suite.ctl 
	
Use the following URL to test a GML schema:
	http://cite.lat-lon.de/deegree-compliance-tests-3.3.1/services/gml321?service=WFS&request=DescribeFeatureType&Version=2.0.0

The result should be pass:

	  Test suite: gml-3.2.1-r14
      ======== Test groups ========
      All GML application schemas
          Passed: 7 | Failed: 0 | Skipped: 0
      GML application schemas defining features and feature collections
          Passed: 2 | Failed: 0 | Skipped: 0
      GML application schemas defining spatial geometries
          Passed: 0 | Failed: 0 | Skipped: 2
      GML application schemas defining time
          Passed: 0 | Failed: 0 | Skipped: 2
      GML application schemas defining spatial topologies
          Passed: 0 | Failed: 0 | Skipped: 2
      GML Documents
          Passed: 0 | Failed: 0 | Skipped: 16
      
      
         See detailed test report in the TE_BASE/users/demo/s0005/html/ directory.
      Test tns:Main Passed

Build TEAM Engine as Web application
=======================================

Download and Install Tomcat
-------------------------------

1. Go to Tomcat Download page::
 
   http://tomcat.apache.org/download-70.cgi
   
2. For windows you need to install the ".zip" but not the installer.

   - Download the Core 7.0.52 64-bit Windows.zip from 
   - Copy it in top level C directory
   - Go to the bin directory and do a startup.bat
   - If problems with the JRE_HOME go to Catalina.bat and declare the JRE_HOME variable.


3. Unzip and put it somewhere. For example under ~/tomcat::

	$ mv %/Downloads/apache-tomcat-7.0.52/ ~/tomcat
	
4. Check that the .sh or .bat files can be executed (rw**x**)::
	
	$ ls -
	$ -rwxrwxrwx@ 1 demo  staff    2046 Feb 13 09:29 startup.bat
	
	
	
5. Start Tomcat::
	
	$ ./startup.sh
	

6. Open localhost:8080 and you should be able to see ApacheTomcat/7.0.52 Welcome page.


Create a dedicated Tomcat instance
-----------------------------------
The example here shows the process for a GNU/Linux environment. 

Select a folder for CATALINA_BASE::

	$ mkdir ~/CATALINA_BASE
	
Create structure::
	
	$ cd ~/CATALINA_BASE
	$ mkdir bin conf logs temp webapps work
	
Copy catalina.sh from $CATALINA_HOME/bin (this is tomcat/bin)::

	$ cp ~/tomcat/bin/catalina.sh bin/
	
Copy configuration files from $CATALINA_HOME/conf (this is tomcat/conf)::	

	$ cp -r ~/tomcat/conf ~/CATALINA_BASE/
	
Create a setenv.sh in bin::

	$ touch setenv.sh
	
And copy the following in setenv.sh::

	cat bin/setenv.sh
	!/bin/sh
	## path to java jdk
	## JAVA_HOME=/usr/local/java/jdk7
	## export JAVA_HOME
	 
	 ## path to tomcat installation to use
	CATALINA_HOME=~/tomcat
	export CATALINA_HOME
	 
	 ## path to server instance to use
	CATALINA_BASE=~/CATALINA_BASE
	export CATALINA_BASE
	
CATALINA_BASE directory should like the following::

	CATALINA_BASE/
	├── bin
	│   ├── catalina.sh
	│   └── setenv.sh
	├── conf
	│   ├── Catalina
	│   │   └── localhost
	│   ├── catalina.policy
	│   ├── catalina.properties
	│   ├── context.xml
	│   ├── logging.properties
	│   ├── server.xml
	│   ├── tomcat-users.xml
	│   └── web.xml
	├── logs
	│   └── catalina.out
	├── temp
	├── webapps
	└── work

Build a WAR file
---------------------
A war file with all the libraries can be build by running a maven profile. 

Copy or edit the maven settings in ~/.m2/settings.xml and put the correct ets-resources version. For example 04.03.20::


	<?xml version="1.0" encoding="UTF-8"?>
	<!-- ${user.home}/.m2/settings.xml -->
	<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0">
	  <!-- other elements omitted -->
	  <profiles>
		<profile>
		  <id>ogc.cite</id>
		  <properties>
			<ets-resources-version>14.03.20</ets-resources-version>
		  </properties>
		</profile>
	  </profiles>
	</settings>


Go to the teamengine local source code repository ::
	
	$ cd ~/repo/teamengine/

Run the maven profile::
	
	$ mvn -P ogc.cite package
	
You should get a build success message::

	INFO] ------------------------------------------------------------------------
	...
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	...

The war file should be available at::

	/repo/teamengine/teamengine-web/target/teamengine.war

Move the war file to CATALINA_BASE/webapps::
	
	$ cp ~/repo/teamengine/teamengine-web/target/teamengine.war ~/CATALINA_BASE/webapps/
	
Move needed common libs to 	~/CATALINA_BASE/libs/::
	
	$ cd ~/repo/teamengine/teamengine-web/target
	$ unzip teamengine-common-libs.zip  -d libs
	$ cp *.jar ~/CATALINA_BASE/lib/

Start TEAM Engine::

	$ cd demo/CATALINA_BASE/bin
	$ ./catalina.sh start
	
TEAM Engine should appear when you type::

	http://localhost:8080/teamengine/test.jsp
	
To stop TEAM Engine type::

	$ cd demo/CATALINA_BASE/bin
	$ ./catalina.sh start
	

