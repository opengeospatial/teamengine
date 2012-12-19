
TEAM-Engine v4
==============

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

The main build artifacts are listed below.

teamengine-core-${version}-distribution.zip
    The core binary distribution (CLI usage)
teamengine-core-${version}-base.[zip|tar.gz]
    Content of main configuration directory (TE_BASE)
teamengine.war
    The JEE web application
teamengine-common-libs.[zip|tar.gz]
    Common runtime dependencies (e.g. JAX-RS 1.1, Derby)

The value of the TE_BASE system property or environment variable specifies 
the location of the main configuration directory that contains several 
essential sub-directories. Unpack the contents of the *-base archive into 
the TE_BASE directory.


How to deploy
-------------
Apache Tomcat 7.0 (with JDK 6 or later) is a supported servlet container. 
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

Unpack the contents of the teamengine-common-libs archive into the 
CATALINA_BASE/lib directory. Deploy the teamengine.war component and start 
the Tomcat instance.

The following URIs provide starting points for discovering and executing test 
suites:

* /teamengine  - Home page for selecting and running CTL test suites
* /teamengine/rest/suites  - Shows a listing of available (TestNG) test suites
