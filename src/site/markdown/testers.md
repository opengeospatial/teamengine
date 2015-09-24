# Tester Guide


This document provides some basic guidance about how to develop a test
suite that can be executed using TEAM Engine. The test harness is
capable of running tests implemented using the OGC CTL (Compliance Test
Language) scripting language and the TestNG framework.

## CTL scripts


The OGC CTL grammar can be regarded as a kind of XML-based
domain-specific language for defining test suites. A test suite is
processed to produce XSLT 2.0 templates that are then executed by the
Saxon processor. See the OGC specification [OGC
06-126r2](http://portal.opengeospatial.org/files/?artifact_id=33085) for
details about developing test suites using CTL elements.

The teamengine-core-\${project.version}-base.zip archive contains a
sample test suite defined in `scripts/note.ctl`. The OGC compliance
testing program also maintains a set of test suites that are publicly
available from GitHub at this location:
[https://github.com/opengeospatial](https://github.com/opengeospatial).

## TestNG framework


### Creating a new test suite

The TEAM Engine test harness concentrates on functional or "black box"
testing that aims to verify the behavior or structure of a test subject
against the relevant specifications. It can execute test suites
implemented using [TestNG](http://testng.org), a popular Java-based
testing framework that is used to perform testing at multiple levels,
from unit to system testing.

A Maven
[archetype](http://maven.apache.org/archetype/maven-archetype-plugin/)
is available that provides a template for seeding a new test suite; it
quickly generates a rudimentary implementation of an executable test
suite (ETS) that can be run immediately. The [source
code](https://github.com/opengeospatial/ets-archetype-testng) is hosted
at GitHub. Project releases are available in the central Maven
repository with these coordinates (using the latest version is always
recommended):

        groupId: org.opengis.cite
        artifactId: ets-archetype-testng
        version: 2.2

To create a new test suite, invoke the interactive 'generate' goal of
the archetype plugin and supply the `ets-code` parameter to specify the
identifier for the test suite; this value must be a legal Java package
name (for example, 'alpha10' as shown below).

        mvn archetype:generate -Dets-code=alpha10 -DarchetypeGroupId=org.opengis.cite
        

Several prompts will collect various property values in order to
generate the test suite project. The suggested default values may be
modified if desired. The new project is created in a subdirectory named
using the value of the artifactId property.

The main class, `TestNGController`, resides in the root package; it
implements the `TestSuiteController` interface declared in the
teamengine-spi module (see Figure 2). The test suite configuration file
(testng.xml, also located in the root package under src/main/resources)
imposes a high-level structure on a test suite by organizing the test
classes into test sets, each of which corresponds to a conformance
class. For more information see the following section, *Basic anatomy of
a test suite*.

### Basic anatomy of a test suite

The TestNG framework offers considerable flexibility regarding the
overall organization of a test suite. A test run executes one or more
test suites, each of which may include a collection of child test
suites. A child test suite encapsulates tests for a specific functional
area. For example, a so-called "class 2" profile (see ISO 19106) that
introduces new capabilities still requires conformance to the base
standard(s); there may be a child test suite defined for each applicable
base standard. The essential structure of a test suite is illustrated in
the following figure.

**Structure of a TestNG test suite**

![Structure of a TestNG test
suite](./images/testng-suite.png)

Each \<test\> element occurring within a top-level \<suite\> element
denotes a set of tests that typically corresponds to a conformance class
or level. The set contains one or more test classes that focus on
particular capabilities or feature sets (e.g., WFS GetFeature). A test
class contains test methods that implement the actual test cases; these
methods are annotated with `@Test`. The listing shown below includes the
test suite definition produced when creating a new test suite from the
ETS archetype (see
src/main/resources/org/opengis/cite/\${ets-code}/testng.xml).

    <suite name="alpha-1.0-SNAPSHOT" verbose="0" configfailurepolicy="skip">
      <parameter name="iut"  value=""/>
      <parameter name="ics"  value=""/>

      <listeners>
        <listener class-name="org.opengis.cite.alpha.TestRunListener" />
        <listener class-name="org.opengis.cite.alpha.SuiteFixtureListener" />
      </listeners>

      <test name="Conformance Level 1">
        <classes>
          <class name="org.opengis.cite.alpha.level1.Capability1Tests" />
        </classes>
      </test>
      <test name="Conformance Level 2">
        <classes>
          <class name="org.opengis.cite.alpha.level2.Capability2Tests" />
        </classes>
      </test>
    </suite>

The order of test execution can be controlled to some extent, although
this should rarely be necessary. The structure of the XML suite
definition is the most important determinant. Keep the following default
behaviors in mind when putting it together:

-   Test groups (\<test\> elements) comprising a suite are executed in
    document order (as specified in the XML suite definition).
-   Within a given test group, constituent test classes are executed in
    document order.
-   A child test suite is run before its parent suite.
-   Test methods are run after their dependencies, which are declared
    using the `dependsOnMethods` or `dependsOnGroups` attributes on the
    @Test annotation.

See the [TestNG
documentation](http://testng.org/doc/documentation-main.html) for more
information. The books listed below may also be of interest:

-   [TestNG Beginner's
    Guide](http://books.google.ca/books?id=9CuP8S2glWQC)
-   [Next Generation Java Testing: TestNG and Advanced
    Concepts](http://books.google.ca/books?id=bCvcMcLZwV4C)

### Test suite development

Most IDEs have integrated support for working with Maven projects (e.g.
Eclipse, NetBeans, IntelliJ), so test developers may use their IDE of
choice. Some tips for using Eclipse are provided below.

The following Eclipse plug-ins are strongly recommended; install them
from the indicated update sites.

-   TestNG: Update from [http://beust.com/eclipse]
-   Subclipse (Subversion 1.7 client): Update from
    [http://subclipse.tigris.org/update\_1.8.x]
-   m2e (Maven integration): From the main update site for the Eclipse
    release, select "General Purpose Tools" \> m2e
-   EGit (Git SCM client): Available from the main update site.

**Note:** Some Eclipse distributions (such as Eclipse 4.4 for Java EE
Developers) may already include some of these plug-ins by default.

The main class is `TestNGController`, located in the root package. The
test run arguments are supplied as an XML representation of a Java
properties file. Specify the location of this file as the argument in a
Java run configuration or put it in your user home directory (\$HOME or
%USERPROFILE%) and name it "test-run-props.xml". An example is shown in
the listing below (see also src/main/config/test-run-props.xml).

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
    <properties version="1.0">
      <comment>Sample test run arguments</comment>
      <entry key="iut">http://www.w3schools.com/xml/note.xml</entry>
      <entry key="ics">level-2</entry>
    </properties>

![info](./images/info-16px.png) **Note:** The value of the 'iut' argument in
the listing above must be an absolute URI that conforms to the `http` or
`file` schemes.

The test results will be written to a subdirectory (named by a UUID
value) created in the user home directory specified by the system
property "user.home". By default this is the XML report generated by
TestNG. In order to activate the full set of default TestNG listeners
(which will also produce an HTML report), modify the last line of the
`TestNGController(String)` constructor and change the third argument to
'true'.

As an alternative to invoking the main test controller, it is also
possible to create a TestNG run configuration if the [TestNG Eclipse
plugin](http://testng.org/doc/eclipse.html) has been installed. Select
Run \> Run Configurations... and choose **TestNG**. Create a new launch
configuration from a test suite definition file by clicking the "Suite"
option and browsing to the `testng.xml` file located within the
src/main/resources directory. Specify a value for all required test
suite parameters. Note that any values appearing here are superseded by
those given in the properties file when the suite is run using the
TestNGController class. When the configuration is run the results are
displayed in the TestNG view.

Each test suite should contain unit tests to verify that all test
methods (and supporting utility methods) behave as expected. The
[JUnit](http://junit.org/) and
[Mockito](http://code.google.com/p/mockito/) frameworks are commonly
used for this purpose. A new test suite generated from the Maven
archetype includes several sample unit tests.

A test run requires one or more arguments that supply information about
the test subject and how thoroughly it will be tested. The arguments are
typically processed by a `SuiteFixtureListener` at the beginning of the
test run; this listener implements the TestNG interface
[ISuiteListener](http://testng.org/javadocs/org/testng/ISuiteListener.html).
The result of processing an argument will generally be set as a
suite-level attribute that can then be accessed by test methods during
the test run. For example, a web service description is parsed and the
resulting XML document is then set as an attribute of the ISuite
instance:

    ISuite suite; // passed in via ISuiteListener#onStart(ISuite suite) 
    Document iutDoc = URIUtils.parseURI(iutRef);
    suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);

### Utility libraries

A test suite may require some additional libraries in order to perform
more specialized assertion checking. Currently there are a couple of
utility libraries available that facilitate schema validation and
geospatial data processing; these are available at GitHub, and releases
are published in the [Maven Central
repository](http://search.maven.org/#search|ga|1|g%3A%22org.opengis.cite%22)


groupId | artifactId | description
--- | --- | ---
org.opengis.cite | [schema-utils](https://github.com/opengeospatial/schema-utils) | Provides support for validating XML representations using the following schema languages: W3C XML Schema 1.0, Schematron (ISO/IEC 19757-3:2006), and RELAX NG (ISO/IEC 19757-2:2008).
org.opengis.cite | [geomatics-geotk](https://github.com/opengeospatial/geomatics-geotk) | Provides support for processing spatial data and associated metadata using various Geotk modules (see http://www.geotoolkit.org/).



