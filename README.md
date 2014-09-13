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

[Apache Maven](http://maven.apache.org/) 3.0 or higher is required to build teamengine.

* Run `git clone https://github.com/opengeospatial/teamengine.git`
* Go the "teamengine" created folder and run `mvn install`.
* Execute the `mvn site` to generate project documentation;
* A PDF document is created at the target/pdf directory.

### Installing the tests

To install the test read the PDF test look at the PDF created in the previous step
