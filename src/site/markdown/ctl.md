# CTL scripts

The OGC test harness is capable of running tests implemented using the CTL 
([Compliance Test Language](http://portal.opengeospatial.org/files/?artifact_id=33085)) 
scripting language developed by the OGC. The CTL grammar can be regarded as a 
kind of XML-based domain-specific language for defining test suites. A test 
suite is processed to produce XSLT 2.0 templates that are then executed by the 
Saxon 9.0 XSLT processor as shown in the following figure.

__Figure 1:__ Executing CTL scripts

![Executing CTL scripts](images/ctl-execution.png)

The teamengine-core-\${project.version}-base.zip archive contains a sample test 
suite in `scripts/note/1.0/ctl/note.ctl`. The OGC compliance testing program 
also maintains a set of test suites that are publicly available from GitHub at 
this location: [https://github.com/opengeospatial](https://github.com/opengeospatial).
