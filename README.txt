
TEAM-Engine v4
==============

How to build
------------

Apache Maven 3.0 <http://maven.apache.org/> is required to build the teamengine 
code base, which consists of the following modules:

* teamengine-core: Main CTL script processor
* teamengine-web: Web interface
* teamengine-resources: Shared resources (stylesheets, schemas, etc.)
* teamengine-realm: Custom Tomcat user realm
* teamengine-spi: Extensibility framework and REST-like API

Simply run 'mvn package' in the root project directory to generate all build 
artifacts (using JDK 6 or later). Execute the 'mvn site' phase with the top-
level POM to generate project documentation; this will also create an aggregate 
PDF document in the target/pdf directory.

The main build artifacts are listed below.

teamengine-core-${version}-distribution.zip
    The core binary distribution (CLI usage)
teamengine-core-${version}-base.[zip|tar.gz]
    Contents of the main configuration directory (TE_BASE)
teamengine.war
    The JEE web (servlet) application
teamengine-common-libs.[zip|tar.gz]
    Common runtime dependencies (e.g. JAX-RS 1.1, Apache Derby)


The value of the TE_BASE system property or environment variable specifies 
the location of the main configuration directory that contains several 
essential sub-directories. Unpack the contents of the *-base archive into 
the TE_BASE directory.


How to deploy
-------------

Apache Tomcat 7.0 (with JDK 6 or later) is a supported servlet container. It 
is strongly recommended that a dedicated Tomcat instance be created to host 
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


These JVM options are recommended for the Tomcat instance:
----
CATALINA_OPTS="-server -Xmx1024m -XX:MaxPermSize=128m -DTE_BASE=$TE_BASE"
----

Unpack the contents of the teamengine-common-libs archive into the 
CATALINA_BASE/lib directory. Deploy the teamengine.war component and start 
the Tomcat instance.

The following URIs provide starting points for discovering and executing test 
suites:

* /teamengine  - Home page for selecting and running CTL test suites
* /teamengine/rest/suites  - Shows a listing of available (TestNG) test suites
