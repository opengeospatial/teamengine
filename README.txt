
TEAM-Engine
===========

How to build
------------
Apache Maven 3.0 <http://maven.apache.org/> is required to build the teamengine 
code base, which consists of the following modules:

* teamengine-core: Main CTL script processor
* teamengine-web: Web interface
* teamengine-resources: Shared resources (stylesheets, schemas, etc.)
* teamengine-config: Configuration utility
* teamengine-realm: Custom Tomcat user realm
* teamengine-spi: Extensibility framework and REST API

Simply run `mvn package` in the root project directory to generate all build 
artifacts (using JDK 6 or later). 

IMPORTANT: In order to download the Saxon 9.0 libraries from the OGC CITE 
repository (id = 'opengeospatial-cite') it is necessary to add appropriate 
user credentials to the matching server entry in $HOME/.m2/settings.xml.

<servers>
  <server>
    <id>opengeospatial-cite</id>
    <username>USERNAME</username>
    <password>PASSWORD</password>
  </server>
</servers>

NOTE: This setting will no longer be necessary when the libraries are available
from a public Maven repository.

The main build artifacts include:

teamengine-core-${version}-distribution.zip::
    The core binary distribution (CLI usage)
teamengine.war::
    The JEE web application
teamengine-jaxrs-libs.zip::
    Runtime JAX-RS 1.1 dependencies (Jersey)


How to deploy
-------------
Apache Tomcat 6.0 or 7.0 (with JDK 6 or later) are supported servlet containers. 
It is strongly recommended that a dedicated Tomcat instance be created to host 
the teamengine application. Create one as suggested below.

.Create a CATALINA_BASE directory (Windows)
----
> mkdir base-1 & cd base-1
> xcopy %CATALINA_HOME%\conf conf\
> mkdir lib logs temp webapps work
----

.Create a CATALINA_BASE directory (GNU/Linux)
----
$ sudo mkdir -p /srv/tomcat/base-1; cd /srv/tomcat/base-1
$ sudo cp -r $CATALINA_HOME/conf .
$ sudo mkdir lib logs temp webapps work
----

Copy teamengine-realm-${version}.jar to the CATALINA_BASE/lib directory, and 
also unpack the contents of teamengine-jaxrs-libs.zip in this directory. Deploy 
the teamengine.war component and start the Tomcat instance.

The following URIs provide starting points for discovering and executing test 
suites:

* /teamengine  - Home page for selecting and running CTL test suites
* /teamengine/rest/suites  - Shows a listing of available (TestNG) test suites
