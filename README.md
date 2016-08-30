## TEAM Engine

### Overview 

TEAM Engine (Test, Evaluation, And Measurement Engine) is a Java-based test 
harness for testing web services and other resources. It executes test scripts 
written in the OGC Compliance Test Language (CTL), TestNG, and possibly other 
JVM-friendly languages. It is lightweight and easy to run from the command-line 
or as a web application. 

TEAM Engine can be used to test any type of service or information resource. 
It is the official test harness used by the Open Geospatial Consortium's 
(OGC) [compliance program](http://cite.opengeospatial.org/). Visit the 
[project documentation website](http://opengeospatial.github.io/teamengine/) 
for more information.

An installation maintained by the OGC is available at http://cite.opengeospatial.org/teamengine/.

### How to build

[Apache Maven](http://maven.apache.org/) 3.2.5 or higher is required to build teamengine. The latest 
release is recommended.

* Clone the repository:

   `git clone https://github.com/opengeospatial/teamengine.git`

* Change to the directory containing the local repository:

   `cd teamengine`

* Execute the Maven install phase to add the build artifacts to the local repository:

   `mvn install`

### More information 
The following sources include documentation about how to install the tests, run TEAM Engine via the 
command line or as a web application in a Java servlet container.

* An extensive tutorial can be found in the [doc folder](https://github.com/opengeospatial/teamengine/blob/master/doc/en/index.rst). 

* A _Getting Started Guide_ is available at the [project documentation website](http://opengeospatial.github.io/teamengine/). 
This can also be created by generating the site documentation (which includes a PDF 
document in the target/pdf directory):

   `mvn site` 

### License

[Apache 2.0 License](LICENSE.txt)

### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/teamengine/issues) such as a defect or an 
enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/teamengine/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add a new feature: Fork the repository, implement (and test) the feature on a new topic 
branch, and then create a pull request

### Mailing Lists

The [cite-forum](http://cite.opengeospatial.org/forum) is where software developers discuss issues 
and solutions related to OGC tests and TEAM Engine. 

### More Information

Visit the [CITE website](http://cite.opengeospatial.org/) for more information about the 
CITE program and tools.
