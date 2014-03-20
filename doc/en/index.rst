Introduction
---------------

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
--------------
- JAVA 1.7
- MAVEN 3.0
- GIT 1.8
- SVN 
- APACHE TOMCAT 7.0 (Only for the web application)

Download TE Source
---------------------
Go to a local directory where TE will be downloaded. For example a directory called **repo**::

	% mkdir repo
	% cd repo
	
The TE code is located in GitHub: https://github.com/opengeospatial/teamengine. Clone the repository::

	% git clone https://github.com/opengeospatial/teamengine.git
	
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

	% git tag
		4.0
		4.0.1
		...
		4.0.5

Switch to a specific tag::

	% git checkout 4.0.5

Build TE Source
----------------
Go to the directory of teamengine::

	% cd repo/teamengine
	
Build with MAVEN::

	% mvn install
	
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
    
The build also create under the user directory a **teamengine** folder. This is the TE Base, which 
contains the tests, users sessions and other configuration files::

	teamengine/
	├── scripts
	├── users
	│   └── demo
	└── work
	
Prepare TE BASE
^^^^^^^^^^^^^^^^^^^
When running **MAVEN install** the file ``teamengine-console-4.0.5-base.zip`` was  created under the 
**teamengine-console/target**. Unzip it to the **~/teamengine** directory ::

	% unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-base.zip -d ~/teamengine
	
It creates the following under **~/teamengine**::

    teamengine/
	├── README.txt
	├── config.xml
	├── resources
	│   ├── docs
	│   │   └── index.html
	│   ├── lib
	│   └── site
	│       ├── footer.html
	│       ├── logo.png
	│       └── welcome.html
	├── scripts
	│   └── note.ctl
	├── users
	│   └── demo
	└── work



Run TE in console
----------------------------

When running **MAVEN install** the file ``teamengine-console-4.0.5-bin.zip`` was created under the 
**teamengine-console/target**. Unzip to a directory where you will be installing and invoking TE.
For example unzipping it to a new dir  **~/te-install**::

	& mkdir ~/te-install
	% unzip ~/repo/teamengine/teamengine-console/target/teamengine-console-4.0.5-bin.zip -d ~/te-install

The *te-install** dir now looks like this::

	.
	├── README.txt
	├── bin
	├── lib
	├── resources
	
Run the example tests::

	% ./test.sh -source=note.ctl

A window should appear asking for input. The test should run and failed::

	Testing suite note:note-test in Test Mode with defaultResult of Pass ...
	Testing note:main type Mandatory in Test Mode with defaultResult Pass (s0001)...
		  Assertion: The note is valid.
	Testing note:check-heading type Mandatory in Test Mode with defaultResult Pass (s0001/d1e97_1)...
			 Assertion: The heading contains more than whitespace.
		  Test note:check-heading Passed
	Testing note:check-user type Mandatory in Test Mode with defaultResult Pass (s0001/d1e102_1)...
			 Assertion: The 'to' user is valid.
		  Test note:check-user Passed
	Testing note:check-user type Mandatory in Test Mode with defaultResult Pass (s0001/d1e107_1)...
			 Assertion: The 'from' user is valid.
		  Test note:check-user Failed
	   Test note:main Failed
	Suite note:note-test Failed

Run and OGC Test
----------------------

Locating OGC Tests
^^^^^^^^^^^^^^^^^^^^^^^

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


Installling a CTL test
^^^^^^^^^^^^^^^^^^^^^^^

Located a URL for a test for download. For example for CSW 2.0.2 r10::
	https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/

Install the test under scripts::

	% svn -q export https://svn.opengeospatial.org/ogc-projects/cite/scripts/csw/2.0.2/tags/r10/ ~/teamengine/scripts/csw-2.0.2
	
The previous svn command will do a clean download of the csw 2.0.2 test to the ~/teamengine/scripts/csw-2.0.2 dir

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
^^^^^^^^^^^^^^^^^^^^^

To run a test, run **test.sh** under **~/te-install/bin/unix** with a parameter -source=<source of the ctl file>. 
The source file has the word *main*.

To run the CSW 2.0.2 test do the following::

	% cd ~/teamengine/scripts
	% ~/te-install/bin/unix/test.sh -source=csw-2.0.2/src/main.xml

A form asking to provide more information should appear. For example asking for the getCapabilities URL.
The `OGC Reference Implementations Page <http://cite.opengeospatial.org/reference>`_ provides
examples of services that can be exercised

For example for CSW 2.0.2 PyCSW:

	http://demo.pycsw.org/cite/csw?service=CSW&version=2.0.2&request=GetCapabilities

The result should be a sucessfull pass::

	...
			Test csw:capability-tests Passed
	   Test csw:Main Passed
	Suite csw:csw-2.0.2-compliance-suite Passed

Installing TestNG Tests
^^^^^^^^^^^^^^^^^^^^^^^^^^

The **ets-resources** branch in the OGC SVN (https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/) contains  
all the mvn artifacts required to install TestNG tests. Look at the dates to figure out the correct download.

Checkout ets-resources in a conveniente location::

	% svn -q export https://svn.opengeospatial.org/ogc-projects/cite/ets/ets-resources/tags/14.02.04/ ~/ets
	
This is new directory structure under **ets**::

	/ets
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
	
	% cd ~/ets
	% mvn install
	
Maven generates a zip file: ets-resources-14.02.04.zip	

Unzip it::
	
	% unzip ets-resources-14.02.04.zip

	
The following is the directory under target::

    /ets/target/
	├── archive-tmp
	├── config-approved.xml
	├── config.xml
	├── ctl-scripts-release.csv
	├── ets-resources-14.02.04.tar.gz
	├── ets-resources-14.02.04.zip
	├── lib
	└── surefire
	
Generate the scripts using ctl.csv
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The ctl.csv file includes entries for the latest development versions of several OGC test suites. 
Running the following command will populate the TE_BASE/scripts directory with these test suites:

	$ export TE_BASE=/some/path
	$ ./export-ctl.sh ctl.csv
	
For example::

	$ export TE_BASE=~/teamengine
	$ ~/te-install/bin/unix/export-ctl.sh ~/ets/target/ctl-scripts-release.csv 

This script downloaded all the scripts in the csv file to the **teamengine/scripts** folder::

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
	
Unzipped in zipped files::
	
	$ unzip ets-kml22-2.2-r6-ctl-scripts.zip


Install libraries under resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Under teamengine (the TE_BASE) copy all the libraries in the ETS (ets/target) folder::
	
	cp ~/ets/target/lib/*.jar ~/teamengine/resources/

The **resources** directory should like::
	
	/teamengine/resources
	.
	├── cite1-utils-1.1.0.jar
	├── docs
	├── ets-gml-3.2.1-r13.jar
	├── ets-kml22-2.2-r6.jar
	├── ets-owc-1.0-r4.jar
	├── ets-wfs-2.0-r14.jar
	...
	
Copy config file in TE_BASE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Copy the config.xml file to TE_BASE::
	
	cp ~/ets/target/config.xml ~/teamengine/
	
Run a TestNG Test
^^^^^^^^^^^^^^^^^^^^^

Run as follows::

	~/te-install/bin/unix/test.sh -source=kml22/2.2-r6/kml22-suite.ctl 






