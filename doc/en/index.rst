TEAM Engine Tutorial
####################

:author: Luis Bermudez <lbermudez@opengeospatial.org>, Richard Martell, Chris Coppock
:version: 4.0.5
:date: August 14, 2014
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


The Test, Evaluation, And Measurement (TEAM) Engine (TE) is a test harness that executes test suites written using the Open Geospatial Consortium (OGC) Compliance Test Language (CTL) test grammar or the TestNG framework.
It is typically used to verify specification compliance and is the official test harness of
the OGC Compliance Testing Program (CITE), where it is used to certify implementations of
OGC and ISO geomatics standards.

OGC hosts an official stable deployment of TEAM Engine with the approved test suites at::

    http://cite.opengeospatial.org/teamengine/

OGC hosts a Beta TEAM Engine with the tests in Beta and with new TEAM Engine functionality::

    http://cite.opengeospatial.org/te2
    
The current license is Mozilla Public License 1.1 (MPL 1.1).    


Download Pre-requisite Software:
==============
In order to build Team Engine and the OGC tests you will need the following software:
- Java 1.7.0_67
- Maven 3.2.2
- Git 1.8
- SVN 1.8
- Apache Tomcat 7.0.52

Download Java JDK version 7u67 (Java Development Kit) from: 
	http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880267.html

Download Maven version 3.2.2 from:
	http://apache.mesi.com.ar/maven/maven-3/3.2.2/binaries/apache-maven-3.2.2-bin.zip

Download Git-SCM version 1.9.4 from:
	http://git-scm.com/download/win

Download TortoiseSVN (Subversion) version 1.8.7 from:
	http://downloads.sourceforge.net/project/tortoisesvn/1.8.7/Application/TortoiseSVN-1.8.7.25475-x64-svn-1.8.9.msi?r=http%3A%2F%2Ftortoisesvn.net%2Fdownloads.html&ts=1406298629&use_mirror=iweb

Download Apache Tomcat version 7.0.52 from:
	http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.52/bin/


Configuring the Prerequisite Software
==============
Configure Java
	1. Browse to the downloaded file location and locate the installation file
	2. Execute installation file from download location on the local workstation
	3. Complete the installation with the default options selected, click 'yes', then click 'next' twice
	4. The JDK and JRE will install to 'C:\Program Files\Java\jdk1.7.0_67' and 'C:\Program Files\Java\jre7' respectively.
	5. Click 'Finish' to complete the installation and close the installer.

Configure Maven
	1. Browse to the downloaded file location and locate the Maven zip archive file
	2. Open the zip archived 'apache-maven-3.2.2' file folder by double-clicking zip file
	3. Right click on the folder that is presented in Windows Explorer, and select the 'copy' option in the dropdown menu.
	4. Open a second Windows explorer folder window, browse to 'C:\Program Files\', right click within the file folder in any blank space, and then select the 'paste' option in order to paste the folder into this location.
	5. Click 'continue' on any security prompts 
	6. Finish the file transfer process and close the zip folder and second windows explorer folder window.

Configure Git
	1. Browse to the download location and locate the installation executable file.
	2. Execute installation file from the download location.
	3. As security warnings prompt the user, click 'Run' to accept.
	4. Accept any UAC warnings by clicking 'Yes'.
	5. Click 'Next' twice.
	6. Click 'Next' to install to the default location (C:\Program Files (x86)\Git\).
	7. Review the select components panel and click next to accept the defaults
	8. Click 'Next' to set the start menu folder option as "Git" (the default location).
	9. During installation process, when presented with the window labeled 'Adjusting your PATH environment' select the option to "Use Git from the Windows Command Prompt".
	10. Click 'Next' to select "Checkout windows-style, commit UNIX-style line endings" option (the default option).
	11. Once the installation is completed, de-select the 'view ReleaseNotes.rtf' option and then click 'Finish' to close the installer.

Configure SVN
	1. Browse to the download location and locate the installation executable file.
	2. Execute installation file from the download location.
	3. Click 'Run' at security prompt
	4. Click 'Next', then accept the licensing terms by selecting the radial option and then click 'Next'.
	5. During the installation process, select the 'Entire feature will be installed on the Local Hard Drive' option which enables the full installation to take place. This option is found by right clicking on the top icon in the packages list, and is labeled as "Entire Feature Will be Installed on Local Hard Drive."
	6. Click the 'Install' option and click the 'Yes' button for any UAC prompt.
	7. Click 'Finish' to complete the installation.

Configure Tomcat
	1. Browse to the download location and locate the Apache Tomcat Zip archive file.
	2. Extract file folder by double-clicking on the zip file and right click  on the folder that is presented in Windows Explorer, and selecting the 'copy' option in the dropdown menu.
	3. Open a second Windows Explorer File Folder window, browse to 'C:\' and past the folder into this directory location.
	4. Finish the file transfer and close the zip folder.

Set Environment Variables in Windows:
==============
	1. Click on the Windows 'Start' button,, right-click on 'Computer' and select the 'Properties' option. Select the 'Advanced System Settings' option in the left-side panel of the presented window.
	2. Within the 'System Properties' window, select the 'Advanced' tab and click on the 'Environment Variables' button.
	3. Select the "PATH" variable within the list by clicking on it, then select the 'Edit...' button.
	4. Within the 'Edit System Variable' window, add the full path of the JDK directory, JRE directory, and Maven directory to the end of the existing contents of the PATH variable value. Note: Please ensure that the end of the list and new additions are separated via a semi-colon. (For example: ...;Variable_a;..)
	5. The program paths for these installed software programs, should be added to the path:
		- ;C:\Program Files\Java\jdk1.7.0_67;
		- ;C:\Program Files\Java\jre7;
		- ;C:\Program Files\apache-maven-3.2.2\bin;
		(Verify that the environment variable paths for Git and SVN are already installed) 
	6. Select the 'OK' button within the 'Edit System Variable' window
	7. Within the 'Environment Variables' window, select the 'New...' button below the 'System Variables' list.
	8. Within the 'New System Variable' window, type "JAVA_HOME" (without quotation marks) within the 'Variable Name:' field. Within the 'Variable Value' field, type the full path to the JDK directory (C:\Program Files\Java\jdk1.7.0_67)
	9. Select the 'Ok' button within the 'New System Variable' window.
	10. Within the 'Environment Variables' window, select the 'New' button below the 'System Variables' list. 
	11. Within the 'New System Variable' window, type "JRE_HOME" (without quotation marks) within the 'Variable Name:' field. Within the 'Variable Value:' field, type the full path to the Java JRE directory (C:\Program Files\Java\jre7)
	12. Select the 'Ok' button within the 'New System Variable Window'.
	13. Within the 'Environment Variables' window, select the 'New...' button below the 'System Variables' list.
	14. Within the 'New System Variable' window, type "TE_BASE" (without quotation marks) within the 'Variable Name:' field. Within the 'Variable Value:' field, type the full path to the TE_BASE directory ('C:\TE_BASE' is the default).
	15. Select the 'Ok' button within the 'New System Variable' window.
	16. This process completes the configuration process for setting environment variables in Windows. At the end of this process, the following items should be accounted for in the PATH section of the system's Environment Variables. Please note that depending on the system, these may be slightly different, and that they are only being included as a reference. 
		- ;C:\Program Files\TortoiseSVN\bin;
		- ;C:\Program Files\Java\jdk1.7.0_67;
		- ;C:\Program Files\Java\jre7;
		- ;C:\Program Files\apache-maven-3.2.2\bin;
		- ;C:\Program Files (x86)\Git\cmd;

Completing the Installation of Pre-requisite Software	
==============
	1. Select the 'Ok' button within the 'Environment Variables' window.
	2. Select the 'Ok' button within the 'System Properties' window.
	3. Close any open programs and restart the workstation.
	4. Open a command prompt on the workstation
	5. Run the following commands at the command prompt:
		(Note: in order to run the necessary commands, you need to enter the command via this syntax: 'Program_Name+[space] hyphen [space] hyphen version')
		- java -version (one hyphen) : which will print the version of the your install, and ensure your system can access the program
		- mvn --version : this will print the version of the your install, and ensure your system can access the program
		- svn --version : this command will print the version of the your install, and ensure your system can access the program
		- git --version : this will print the version of the your install, and ensure your system can access the program
		- echo %TE_BASE% : this will print the full path to TE_BASE
		- echo %JAVA_HOME% : this will print the full path to the JDK installation location
		- echo %JRE_HOME% : this will print the full path to the JRE installation location
	6. Change directory (cd) to the folder: 'C:\apache-tomcat-7.0.52\bin' and then launch the Tomcat service by entering the following command: 'startup.bat'.
	7. Open web browser window, and type the following URL (Uniform Resource Locator): http://localhost:8080 or http://127.0.0.1:8080 and you should be able to see the Apache Tomcat/7.0.52 Welcome Page.
	8. Note: If there are any problems with the JRE_HOME shown in the command prompt, double check the System Environment Variables for the JRE_HOME entry declared in the System Environment Variable settings.
	9. Close the web browser window.
	10. In the command prompt, ensuring you are in the working directory 'C:\apache-tomcat-7.0.52\bin', shutdown Tomcat by entering the following command: 'shutdown.bat'.
	11. Running these commands will ensure that all of the pre-requisite software is installed correctly, and will allow you to verify that the Java JDK and JRE were installed to the correct directory.
	12. Now that the configuration is complete, close any open programs and restart the workstation.


Download TE Source
=======================
Change Directory (cd) or browse to a local directory where TE will be downloaded. For example a directory called **repo**::

In Unix:
	$ mkdir repo
	$ cd repo

In Windows:
	c:\> mkdir repo
	Then change directory to repo (c:\> cd repo)


The TE code is located in GitHub: https://github.com/opengeospatial/teamengine. Clone the repository::

	In Unix:
	$ > git clone https://github.com/opengeospatial/teamengine.git
	
	In Windows:
	c:\repo> git clone https://github.com/opengeospatial/teamengine.git

Change directory to c:\repo\teamengine and verify the directory structure by issuing the list directory command (Windows: 'dir', Unix: 'ls')

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

At the command prompt type the command 'git tag', which will display the available tags within the Git repository

The tag listing should look similar to this:

	$ git tag
		4.0
		4.0.1
		...
		4.0.5

Switch to a specific tag by typing::

	$ git checkout 4.0.5

Build TE Source
=======================
Ensure you are in the working directory of teamengine::

	$ cd repo/teamengine
	
Build with MAVEN::

	In Unix: $ mvn install
	In Windows: c:\repo\teamengine\> mvn install

It will take few minutes to install, and then a success message will appear after the install::
	
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
   

Under each directory  a **target** folder was created, which contains the build folder for each artifact.
The folder **teamengine-console** contains the directory::
	
	└── target
		├── teamengine-console-4.0.5-base.tar.gz
		├── teamengine-console-4.0.5-base.zip
		├── teamengine-console-4.0.5-bin.tar.gz
    	└── teamengine-console-4.0.5-bin.zip

	
Prepare TE BASE
---------------------

Unzip teamengine-console-4.0.5-base.zip in the TE_BASE directory (Note: If previous content exists, click yes to prompts to replace Folders and Files)

In Unix: $ > unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-base.zip -d $TE_BASE

In Windows: Browse in Windows Explorer to c:\repo\teamengine\teamengine-console-4.0.5-base.zip and copy the contents to c:\TE_BASE


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
required to execute a test suite using a command-line shell; it should be structured as indicated below::

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
**teamengine-console/target**. 

Unzip the zip archive to a new directory **~$/te-install** by conducting the following actions::
(Note: Be aware of the difference in TE_BASE and te-install and the use of uppercase and underscore versus lowercase and hyphens, as the directions are case-sensitive) 

	In Unix:
	$ mkdir ~/te-install
	$ unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-bin.zip -d ~/te-install

	In Windows:
	c:\> mkdir te-install
	Browse in Windows Explorer to: c:\repo\teamengine\teamengine-console-4.0.5-bin.zip and copy the contents of the zip archive into c:\te-install


The **te-install** dir now looks like this::

	.
	├── README.txt
	├── bin
	├── lib
	├── resources
	
Run the example tests::
	
	In Unix:
	$ cd $TE_BASE/scripts/
	$ ~/te-install/bin/unix/test.sh -source=note.ctl

	In Windows:
	c:\> te-install\bin\windows\test.bat -source=c:\TE_BASE\scripts\note.ctl


A window should appear asking for input. Click start to run the test and the test should run and fail, which is the intended result::

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

This is the list of the current test and the language they are built in:

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

Browse to a URL for a test for download. For example for CSW 2.0.2 r10:
	https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/

Install the test under scripts::
(Note: The example svn command will do a clean download of the csw 2.0.2 test to the ~/$TE_BASE/scripts/csw-2.0.2 directory)
	
	In Unix:
	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/ $TE_BASE/scripts/csw-2.0.2
	
	In Windows:
	In a command prompt window, change directory to the c:\repo location, before executing the svn command in the next step.
	c:\> svn -q export https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10 (insert one space here) c::\TE_BASE\scripts\csw-2.0.2


The scripts directory should look as follows::

	scripts/
	├── csw-2.0.2
	│   ├── config.xml
	│   ├── data
	│   ├── resources
	│   ├── src
	│   └── web
	└── note.ctl



Executing a CTL test
---------------------

To run the CSW 2.0.2 test do the following::
	In Unix:
	$ cd $TE_BASE/scripts
	$ ~/te-install/bin/unix/test.sh -source=csw-2.0.2/src/main.xml

	In Windows:
	c:\> te-install\bin\windows\test.bat -source=c:\TE_BASE\scripts\csw-2.0.2\src\main.xml


A window form asking the user to provide more information should appear. For example asking for the getCapabilities URL.

The `OGC Reference Implementations Page <http://cite.opengeospatial.org/reference>`_ provides
examples of services that can be exercised.

For example for CSW 2.0.2 pycsw:

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
	In Unix:
	$ cd ~/
	$ svn mkdir svn
	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/testng/ets-kml22/tags/2.2-r9/ ~/svn/kml22
	
	In Windows:
	c:\> cd repo
	c:\repo> mkdir svn
	c:\repo\> cd svn
	c:\repo\svn> svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/testng/ets-kml22/tags/2.2-r9/


This is the structure under the svn directory::

	svn/
	└── kml22
		├── LICENSE.txt
		├── pom.xml
		└── src

Execute mvn Install::
	
	$ mvn install
	
The directory should now contain a **target** folder with the build::

	/kml22/target/
	├── ets-kml22-2.2-r9-ctl-scripts.zip
	├── ets-kml22-2.2-r9-deps.zip
	...

Unzip the ctl-scripts to TE_BASE::
	
	In Unix:
	$ cd ~/svn/kml22/target
	$ unzip ets-kml22-2.2-r9-ctl-scripts.zip -d $TE_BASE/scripts/kml22

	In Windows:
	Browse in Windows Explorer to c:\repo\svn\2.2-r9\target>
	Extract the files from ets-kml22-2.2-r9-ctl-scripts.zip into c:\TE_BASE\scripts\kml22


Copy the libraries into TE_BASE/resources/lib/::
	
	In Unix:
	$ cd ~/svn/kml22/target
	$ unzip ets-kml22-2.2-r9-deps.zip -d jars
	$ cp jars/*.jar $TE_BASE/resources/lib/

	In Windows:
	Browse in Windows Explorer to c:\repo\svn\2.2-r9\target>
	Extract the files from ets-kml22-2.2-r9-deps.zip into c:\TE_BASE\resources\lib\


Run the test::
	
	In Unix:
	$ cd $TE_BASE/scripts/
	$ ~/te-install/bin/unix/test.sh -source=kml22/kml22/2.2/kml22-suite.ctl
	
	In Windows:
	At the command prompt, type the command: c:\> te-install\bin\windows\test.bat -source=c:\TE_BASE\scripts\kml22\2.2\kml22-suite.ctl

When the windows appears, the user provides the test URL they want to evaluate or can close the windows to cancel the test.
	


Installing TestNG Tests
--------------------------

The **ets-resources** branch in the OGC SVN (https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/) contains  
all the mvn artifacts required to install TestNG tests. Check the dates to ensure you are downloading the desired version.

Checkout ets-resources::
	
	In Unix:
	$ svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/14.03.20/ -d ~/svn/ets-resources

	In Windows:
	Change directory to c:\repo\svn>
	c:\repo\svn> mkdir ets-resources
	c:\repo\svn> cd ets-resources
	c:\repo\svn\ets-resources> svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/14.04.16/
	
This is new directory structure under **ets**::

	/svn/ets-resources
	├── pom.xml
	└── src
		└── main
			├── assembly
			│   └── dist.xml
			└── config
				├── ctl-scripts-release.csv
				└── teamengine
					├── config-approved.xml
					└── config.xml

	
Navigate to the **\repo\svn\ets-resources\14.04.16\** directory and execute the install command::
	
	$ mvn install

Maven generates a zip file: ets-resources-14.04.16.zip	

The following is the directory structure under target::

    ~repo/svn/ets-resources/target/
	├── archive-tmp
	├── config-approved.xml
	├── config.xml
	├── ctl-scripts-release.csv
	├── ets-resources-14.03.20.tar.gz
	├── ets-resources-14.03.20.zip
	├── lib
	└── surefire

Unzip it::
	
	In Unix:
	$ unzip ets-resources-14.03.20.zip

	In Windows:
	Browse in Windows Explorer to c:\repo\svn\ets-resources\14.04.16\target\ folder
	Extract the contents of the ets-resources-14.04.16.zip file into c:\repo\svn\ets-resources\14.04.16\target\ folder (current folder directory)


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
The ctl.csv file (ctl-scripts-release.csv) includes entries for the latest developmental versions of several OGC testing suites. 
(Note: Before running the script listed below, delete all the contents of the TE_BASE\scripts directory)

Running the following command will populate the **TE_BASE/scripts** directory with these test suites::
	
	In Unix:
	$ ~/te-install/bin/unix/export-ctl.sh ~/svn/ets-resources/14.04.16/target/ctl-scripts-release.csv

	In Windows:
	Change directory to c:\> and issue the command:
	c:\> te-install\bin\windows\export-ctl.bat  c:\repo\svn\ets-resources\14.04.16\target\ctl-scripts-release.csv

This script downloads all the scripts in the csv file to the **$TE_BASE/scripts** folder::

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
	

Unzip all the zipped files within the 'TE_BASE\scripts' directory::
	
	In Unix:
	Change directory to $ TE_BASE/scripts and issue the command:
	$ unzip '*.zip'

	In Windows:
	Browse to c:\TE_BASE\scripts directory and unzip the four compressed file directories into their respective directory sub-folders::
	
	For GML and KML files, the user will need to generate these sub-folders in this directory. For WFS, you will need to manually unzip the zip archive and copy the folder labeled '2.0' (which is the version of the test) into the pre-existing WFS folder. 


Install libraries under resources/lib
-------------------------------------
Copy all the libraries generated to the **resources/lib** directory under TE_BASE::
	
	In Unix:
	cp ~/svn/ets-resources/14.04.16/target/lib/*.jar $TE_BASE/resources/lib

	In Windows:
	Browse to the c:\repo\svn\ets-resources\14.04.16\target\lib directory and copy all of the files with a *.jar extension into the c:\TE_BASE\resources\lib directory


The **resources** directory should like::
	
	/teamengine/resources/lib
	.
	├── cite1-utils-1.1.0.jar
	├── commons-io-2.2.jar
	├── ets-gml-3.2.1-r13.jar
	├── ets-kml22-2.2-r6.jar
	├── ets-kml22-2.2-r9.jar
	├── ets-owc-1.0-r4.jar
	...
	
Copy the configuration file in TE_BASE
-----------------------------

	In Unix:
	cp ~/svn/ets-resources/target/config.xml $TE_BASE
	
	In Windows:
	Browse to the c:\repo\svn\ets-resources\14.04.16\target directory and copy the config.xml file into the c:\TE_BASE directory. If the file already exists in the c:\TE_BASE directory, replace it with the newer version.

Run a TestNG Test
---------------------

Run tests as follows::

For KML 2.2::
	
	In Unix:
	$ ~/te-install/bin/unix/test.sh -source=kml22/2.2/kml22-suite.ctl 

	In Windows:
	Change directory to c:\ and type the following command:
	c:\> te-install\bin\windows\test.bat -source=c:\TE_BASE\scripts\kml22\2.2\kml22-suite.ctl
	Click Start in order to execute the test

For GML 3.2.1::	
	
	In Unix:
	$ ~/te-install/bin/unix/test.sh -source=gml/3.2.1/gml-suite.ctl 
	
	In Windows:
	Change directory to c:\ and type the following command:
	c:\> te-install\bin\windows\test.bat -source=c:\TE_BASE\scripts\gml\3.2.1\gml-suite.ctl

Input the following URL to test a GML schema:
	http://cite.lat-lon.de/deegree-compliance-tests-3.3.1/services/gml321?service=WFS&request=DescribeFeatureType&Version=2.0.0

Click start in order to execute the test.

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

The example listed here shows the process for the MS Windows Environment:

Select a folder for CATALINA_BASE::

At the command prompt, change directory to c:\>
c:\> mkdir CATALINA_BASE

Create the Directory Structure::

Change directory to CATALINA_BASE
c:\> mkdir bin conf lib logs temp webapps work

Populating File Directories::

Copy catalina.bat file from c:\apache-tomcat-7.0.52\bin into c:\CATALINA_BASE\bin

Copy all of the files from c:\apache-tomcat-7.0.52\conf and then paste them into c:\CATALINA_BASE\conf

Create Set Environment File::

Create a plaintext file using a text editor
Name the file setenv.bat and save in the c:\CATALINA_BASE\bin folder

Create the contents of the file by copying the following text into the setenv.bat file within the text editor

------------------------------------------------------
------------------------------------------------------

rem path to java jdk
set JAVA_HOME=c:\Program Files\Java\jdk1.7.0_67

rem path to tomcat install to use
set CATALINA_HOME=c:\apache-tomcat-7.0.52

rem path to server instance to use
set CATALINA_BASE=c:\CATALINA_BASE

rem sets the catalina options setting to a specific window size, memory limits, and sets DTE_BASE locally
set CATALINA_OPTS=-server -Xmx1024m -XX:MaxPermSize=128m -DTE_BASE=c:\TE_BASE

------------------------------------------------------
------------------------------------------------------

Click on the save icon


	
CATALINA_BASE directory should like the following::

	CATALINA_BASE/
	├── bin
	│   ├── catalina.sh
	│   └── setenv.sh
	├── conf
	│   ├── Catalina
	│   │   └── localhost
	│   ├── catalina.policy
	│   ├── catalina.properties
	│   ├── context.xml
	│   ├── logging.properties
	│   ├── server.xml
	│   ├── tomcat-users.xml
	│   └── web.xml
	├── logs
	│   └── catalina.out
	├── temp
	├── webapps
	└── work

Build a WAR file
---------------------
A war file with all the libraries can be build by running a modified maven profile. 

Copy or edit the maven settings in '\apache-maven-3.2.1\conf\settings.xml' and input the correct ets-resources version. For example 04.04.16::

In Unix it is located at: /usr/local/apache-maven-3.2.1/conf
In Windows it is located at c:\Program Files\apache-maven-3.2.1\conf

Please note the bracketing within the XML file and nest the code snippet appropriately. Additional profile and data entry sections exist, so the user only needs to add this profile as well.

	<?xml version="1.0" encoding="UTF-8"?>
	<!-- ${user.home}/.m2/settings.xml -->
	<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0">
	  <!-- other elements omitted -->
	  <profiles>
		<profile>
		  <id>ogc.cite</id>
		  <properties>
			'''''<ets-resources-version>14.04.16</ets-resources-version>'''''
		  </properties>
		</profile>
	  </profiles>
	</settings>

Save the updated file to the user desktop, and then copy into the 'apache-maven-3.2.1\conf' directory. (This is required due to system permission levels)


Browse to the teamengine local source code repository ::
	
	In Unix:
	$ cd ~/repo/teamengine/

	In Windows:
	Change directory to c:\repo\teamengine

Run the maven profile::
	
	In Unix:
	$ mvn -P ogc.cite package
	
	In Windows:
	c:\> mvn -P ogc.cite package

You should get a build success message::

	INFO] ------------------------------------------------------------------------
	...
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	...

The war file should be available at::

	/repo/teamengine/teamengine-web/target/teamengine.war

Move the war file to CATALINA_BASE/webapps::
	
	In Unix:
	$ cp ~/repo/teamengine/teamengine-web/target/teamengine.war ~/CATALINA_BASE/webapps/
	
	In Windows:
	Browse using Windows Explorer to c:\repo\teamengine\teamengine-web\target
	Copy 'teamengine.war' file into c:\CATALINA_BASE\webapps

Move needed common libs to 	~/CATALINA_BASE/libs/::
	
	In Unix:
	$ cd ~/repo/teamengine/teamengine-web/target
	$ unzip teamengine-common-libs.zip  -d libs
	$ cp *.jar ~/CATALINA_BASE/lib/

	In Windows:
	Browse to c:\repo\teamengine\teamengine-web\target
	Extract contents of 'teamengine-common-libs.zip' into c:\CATALINA_BASE\lib


Start TEAM Engine::
	
	In Unix:
	$ cd demo/CATALINA_BASE/bin
	$ ./catalina.sh start
	
	In Windows:
	Change directory to c:\CATALINA_BASE\bin
	Enter the following command at the prompt
	c:\> catalina.bat start

TEAM Engine should appear when you type::

	http://localhost:8080/teamengine/test.jsp

Register a username and password if you have not done so previously. Be advised that the username and password are stored in plaintext in TE_BASE\User\ subfolders, and it is strongly advised
not to use previous or currently utilized usernames or passwords.

Once you are running Team Engine, the URL should change to http://localhost:8080/teamengine/viewsessions.jsp 
This URL should be used after logging in, or the backend system could crash.


To stop TEAM Engine type::
	
	In Unix:
	$ cd demo/CATALINA_BASE/bin
	$ ./catalina.sh start
	
	In Windows:
	Change directory to c:\CATALINA_BASE\bin
	Enter the following command at the prompt
	c:\> catalina.bat stop
