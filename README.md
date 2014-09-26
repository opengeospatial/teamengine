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

An example installation can be found at: http://cite.opengeospatial.org/teamengine/ 

### How to build

[Apache Maven](http://maven.apache.org/) 3.0 or higher is required to build teamengine.

* Clone the repository

   `git clone https://github.com/opengeospatial/teamengine.git`

* Go to the folder where the local repository was created

   `cd teamengine`

* install via maven 

   `mvn install`

### More information 
The following sources include documentation about how to install the tests, run TEAM Engine via command line and run TEAM Engine as a web application.

* An extensive tutorial can be found in the [doc folder](https://github.com/opengeospatial/teamengine/blob/master/doc/en/index.rst). 

* A simple set of instructions can be found by building the documentation using running mvn site, which creates a PDF document at the target/pdf directory.

   `mvn site` 

### License

[Apache 2.0 License](LICENSE.txt)

### Bugs

Issue tracker is available at [github](https://github.com/opengeospatial/teamengine/issues)

## Mailing Lists

The [cite-forum](http://cite.opengeospatial.org/forum) is where software developers discuss issues and solutions related to OGC tests and TEAM Engine. 

## More Information

Visit the [CITE website](http://cite.opengeospatial.org/) to get more information about the CITE program and tools.

