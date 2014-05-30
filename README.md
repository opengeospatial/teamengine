## TEAM ENGINE

### Overview 

TEAM Engine (Test, Evaluation, And Measurement Engine) is a Java-based test 
harness for testing web services and other resources. It executes test scripts 
written in the OGC Compliance Test Language (CTL), TestNG, and other languages. 
It is lightweight and easy to run as a command-line or web application. 

TEAM Engine can be used to test any type of service or (meta)data resource. 
It is the official test harness used by the Open Geospatial Consortium's 
(OGC) [compliance program](http://cite.opengeospatial.org/). Visit the 
[project documentation website](http://opengeospatial.github.io/teamengine/) 
for more information.

### How to build

[Apache Maven](http://maven.apache.org/) 3.0 or higher is required to build 
the teamengine code base, which consists of the following modules:

* teamengine-core: Main CTL script processor
* teamengine-console: Console (CLI) application
* teamengine-web: Web application
* teamengine-resources: Shared resources (stylesheets, schemas, etc.)
* teamengine-realm: Custom Tomcat user realm
* teamengine-spi: Extensibility framework and REST-like API

Simply run `mvn package` in the root project directory to generate all build 
artifacts (using JDK 7 or later). Execute the `mvn site` phase with the top-
level POM to generate project documentation; this will also create an aggregate 
PDF document in the target/pdf directory.

**Note**

Some dependencies may not be not available in the central repository. To obtain 
them, add the following remote repository to a profile in the Maven settings 
file (${user.home}/.m2/settings.xml).

    <profile>
      <id>ogc.cite</id>
      <!-- activate profile by default or explicitly -->
      <repositories>
        <repository>
          <id>opengeospatial-cite</id>
          <name>OGC CITE Repository</name>
          <url>https://svn.opengeospatial.org/ogc-projects/cite/maven</url>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
