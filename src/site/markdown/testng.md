# Conformance Testing with TestNG

## Contents

1. [Getting started](#s1)
2. [Defining a test suite](#s2)
3. [Test fixtures](#s3)
4. [Test listeners](#s4)
5. [Test methods](#s5)
6. [Test preconditions](#s6)
7. [Further resources](#s7)

-----

## <a name="s1">1</a> Getting started

The purpose of conformance testing is to determine the extent to which a product satisfies 
the requirements stipulated in the relevant specifications. In the context of the Open 
Geospatial Consortium's (OGC) [compliance program](http://cite.opengeospatial.org/), it is 
a kind of "black box" testing that examines only the externally visible characteristics or 
behaviors of the implementation under test (IUT).

The OGC maintains a [testing facility](http://cite.opengeospatial.org/te2/) for its members
that currently provides access to test suites for more than 20 OGC specifications, some of 
which are international standards that have been jointly produced with ISO ([TC 211](http://www.isotc211.org/), 
Geographic information/Geomatics). Every OGC specification includes an _abstract test suite_ 
(ATS) that describes the conformance requirements. The ATS&#8212;which generally appears in 
Annex A&#8212;establishes a basis for developing an _executable test suite_ (ETS) that is 
run to perform a conformity assessment.

The official OGC test harness ([TEAM Engine](https://github.com/opengeospatial/teamengine)) 
can execute test suites written using [TestNG](http://testng.org/), a popular Java-based 
testing framework. A Maven [archetype](http://maven.apache.org/archetype/) is available to 
serve as a template for creating a new ETS; it quickly generates a rudimentary test suite that 
can be run immediately. The [source code](https://github.com/opengeospatial/ets-archetype-testng) 
is hosted at GitHub. Project releases are available in the [central Maven repository](http://search.maven.org/) 
with these coordinates (using the [latest version](https://repo1.maven.org/maven2/org/opengis/cite/ets-archetype-testng/) 
is always recommended):

    groupId: org.opengis.cite
    artifactId: ets-archetype-testng
    version: 2.4

In order to use the archetype and build the resulting test suite a [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/) 
(JDK) and [Apache Maven](https://maven.apache.org/) must be installed:

* JDK 7 or higher
* Apache Maven 3.0 or higher

To create a new test suite open a command shell (terminal window) and invoke the interactive 
__archetype:generate__ goal. It is necessary to specify the `ets-code` property at this point, 
the value of which must be a legal Java package name. For OGC test suites the convention is 
to identify the principal specification by major and minor version: wfs20, cat30, and the 
like. See Listing 1 for a sample invocation where the ets-code value is "alpha10"; note 
that a '\' (backslash) character indicates that a line is continued.

**Listing 1: Using the archetype interactively**

    mvn archetype:generate \
        -Dfilter=org.opengis.cite:ets-archetype-testng \
        -Dets-code=alpha10

Several prompts will ask for various property values in order to generate the test suite 
project. The suggested default values may be modified if desired. It is also possible to 
create a test suite non-interactively in "batch" mode by using the -B flag as shown in 
Listing 2.

**Listing 2: Using the archetype in batch mode**

    mvn archetype:generate -B \
        -DarchetypeGroupId=org.opengis.cite \
        -DarchetypeArtifactId=ets-archetype-testng \
        -DarchetypeVersion=2.4 \
        -Dets-code=alpha10 \
        -DartifactId=ets-alpha10 \
        -Dpackage=org.opengis.cite.alpha10

The main class, `TestNGController`, resides in the root package; it implements the 
`TestSuiteController` interface declared in the [teamengine-spi](https://github.com/opengeospatial/teamengine/tree/master/teamengine-spi) 
module. The actual test run arguments are supplied in an XML representation of a Java 
properties file. Specify the location of this file as a command-line argument or simply 
put it in your user home directory (\$HOME or %USERPROFILE%) and name it “test-run-props.xml”. 
An example is shown in Listing 3 (see also src/main/config/test-run-props.xml).

**Listing 3: Passing test run arguments in a properties file**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties version="1.0">
  <comment>Test run arguments</comment>
  <entry key="iut">http://www.w3schools.com/xml/note.xml</entry>
</properties>
```

After an initial test suite has been created, several files must be updated in order to 
describe the test suite and provide guidance to users:

- README (/)
- Javadoc overview (/src/main/javadoc/overview.html)
- Site content (/src/site/)
- CTL wrapper script (/src/main/scripts/ctl/)
- TEAM-Engine configuration file (/src/main/config/teamengine/config.xml)


## <a name="s2">2</a> Defining a test suite

The TestNG framework offers considerable flexibility regarding the overall organization 
of a test suite. A test suite can be conveniently defined by an [XML file](http://testng.org/doc/documentation-main.html#testng-xml). 
Listing 4 shows the test suite definition produced when creating a new test suite from 
the Maven archetype (it's a resource file located at `src/main/resources/org/opengis/cite/${ets-code}/testng.xml`).

**Listing 4: A test suite definition file**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suite name="${ets-code}-${version}" verbose="0" configfailurepolicy="continue">
  <parameter name="iut"  value=""/>

  <listeners>
    <listener class-name="org.opengis.cite.alpha10.TestRunListener" />
    <listener class-name="org.opengis.cite.alpha10.SuiteFixtureListener" />
    <listener class-name="org.opengis.cite.alpha10.TestFailureListener" />
  </listeners>

  <test name="Conformance Level 1">
    <packages>
      <package name="org.opengis.cite.alpha10.level1" />
    </packages>
  </test>
  <test name="Conformance Level 2">
    <packages>
      <package name="org.opengis.cite.alpha10.level2" />
    </packages>
  </test>
</suite>
```

Each &lt;test&gt; element occurring within a top-level &lt;suite&gt; element denotes 
a collection of tests that corresponds to a conformance class or level. Each collection 
contains one or more test classes that focus on a functional area. In a test report 
the results are typically summarized by conformance class as shown in Figure 1.

**Figure 1: Test results summary**

[[images/results-overview.png]]

A test class contains test methods that implement the actual test cases. In Listing 4 
the test classes are identified implicitly by package name. It is strongly recommended 
that each conformance class have a corresponding package that contains its constituent 
test classes. Keep in mind that the test sets (&lt;test&gt; elements) comprising 
a suite are executed in document order as they occur in the XML suite definition.



It is possible to incorporate tests implemented in other test suites. This is 
accomplished by simply referring to the external packages or classes in the test 
suite definition. For example, the WFS 2.0 test suite includes several test classes 
from the GML 3.2 suite (see Listing 5).

**Listing 5: Incorporating tests from other suites**

```xml
<test name="All GML application schemas">
  <classes>
    <class name="org.opengis.cite.iso19136.general.XMLSchemaTests" />
    <class name="org.opengis.cite.iso19136.general.GeneralSchemaTests" />
    <class name="org.opengis.cite.iso19136.general.ModelAndSyntaxTests" />
    <class name="org.opengis.cite.iso19136.general.ComplexPropertyTests" />
  </classes>
</test>

<test name="Simple WFS">
  <packages>
    <package name="org.opengis.cite.iso19142.simple" />
  </packages>
</test>
```

Each test suite is packaged as a standard JAR file. All classes must be available 
on the classpath when a test suite is executed.


## <a name="s3">3</a> Test fixtures

A test fixture is a set of resources that must be in place in order to run a test 
and verify the outcome. A fixture might include items such as metadata about the 
test subject, schemas used to validate response messages, input data, or specialized 
client components. Test fixtures can be created at multiple levels depending on how 
widely they should be shared. A class fixture is shared by all test methods defined in 
the class (or a subclass). A suite fixture contains items that are broadly accessible.

In the TestNG framework the [ITestContext](http://testng.org/javadocs/org/testng/ITestContext.html) 
interface represents a high-level test fixture that can be augmented with user-defined 
attributes (of any type) that are set or retrieved as needed. One common testing scenario 
involves compiling an XML Schema resource using the JAXP Validation API and then storing the 
resulting thread-safe Schema object as a test suite ([ISuite](http://testng.org/javadocs/org/testng/ISuite.html)) 
attribute that can be readily accessed by test methods.

A test suite generated using the Maven archetype includes the `org.opengis.cite.${ets-code}.SuiteFixtureListener` 
class. The <code>processSuiteParameters</code> method demonstrates how to parse a 
test run input argument and store the resulting DOM Document object as a suite 
attribute (Listing 6).

**Listing 6: Assembling a test suite fixture**

    ISuite suite;  // injected by TestNG
    Map<String, String> params = suite.getXmlSuite().getParameters();
    String iutParam = params.get(TestRunArg.IUT.toString());    
    URI iutRef = URI.create(iutParam.trim());
    Document iutDoc = URIUtils.parseURI(iutRef);
    suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);

Note that in most cases the SuiteFixtureListener class will need to be modified in 
order to accommodate particular testing requirements. For example, if the main 
test input is not an XML resource it would be more appropriate to store the 
content of the resource in a local file or database for the duration of the 
test run.


## <a name="s4">4</a> Test listeners 

Several listener interfaces are provided by the TestNG framework in order to modify 
or extend default behaviors. The `ISuiteListener` and `ITestListener` interfaces are 
of particular interest. The former includes methods that are invoked before and after 
a test suite is executed; the latter declares several callback methods pertaining to 
the life cycles of test classes and test methods.

A test suite generated using the Maven archetype contains several predefined listeners 
referenced in the definition file (see Listing 4). These listeners are all contained 
in the root package; they are summarized in Table 1.

<table>
  <caption>Table 1: Predefined listeners</caption>
  <thead>
    <tr style="text-align: left; background-color: LightCyan">
      <th>Name</th>
      <th>Purpose</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TestRunListener (IExecutionListener)</td>
      <td>A listener that is invoked before and after a test run; it is typically used 
      to configure components of the test environment for the duration of the entire 
      test run.</td>
    </tr>
    <tr>
      <td>SuiteFixtureListener (ISuiteListener)</td>
      <td>A listener that performs various initialization and clean-up tasks, such as: 
      processing input arguments, compiling application schemas, and deleting any temporary 
      files created during a test run.</td>
    </tr>
    <tr>
      <td>TestFailureListener (ITestListener)</td>
      <td>A listener that augments a test result with diagnostic information in the event 
      that a test method failed; this information will appear in the XML report when the 
      test run is completed.</td>
    </tr>
  </tbody>
</table>

The predefined listeners may be modified if desired, and test developers can introduce 
additional listeners that perform various pre- and post-processing tasks or modify 
TestNG behavior. See the TestNG documentation for more information about the 
[listener interfaces](http://testng.org/doc/documentation-main.html#testng-listeners).


## <a name="s5">5</a> Test methods

In general, an abstract test case (ATC) gives rise to one or more test methods that 
realize it. A test method bears the `@Test` annotation, which is automatically detected
by the test runner (SuiteRunner). In general, a test method includes at least one assertion 
in order to verify the expected outcome: the content of a response message, the state of 
the test subject, and so on. Listing 7 displays a test method that validates an XML resource 
against the RELAX NG grammar for [Atom feeds](https://tools.ietf.org/html/rfc4287).

**Listing 7: Sample test method**

```java
/**
 * Verify that the entity is a valid Atom feed in accord with RFC 4287.
 */
@Test(description = "ATC 1-3")
public void validAtomFeed() throws SAXException, IOException {
    URL schemaRef = getClass().getResource(
            "/org/opengis/cite/alpha10/rnc/atom.rnc");
    RelaxNGValidator rngValidator = new RelaxNGValidator(schemaRef);
    rngValidator.validate(new DOMSource(testSubject));
    ValidationErrorHandler err = rngValidator.getErrorHandler();
    Assert.assertFalse(err.errorsDetected(),
            ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
            err.getErrorCount(), err.toString()));
}
```

Note that the `@Test` annotation in Listing 7 contains a _description_ attribute; 
this refers to the test requirement(s) or abstract test case that form the test basis. 
The Javadoc comments for the method should provide more information about the expected 
result.

Test methods may accept parameters, which is a very useful capability in some circumstances. 
For example, when testing a web service a particular test may apply to every supported protocol 
binding listed in the service description. Instead of implementing one test for each binding, 
a single test with a parameter that identifies a specific binding will suffice. There are 
[two main mechanisms](http://testng.org/doc/documentation-main.html#parameters) for passing 
parameters:

* specify simple values in the test suite definition (testng.xml)
* supply arbitrary values&#8212;even Java objects&#8212;with data providers

While TestNG does provide a basic [assertion facility](http://testng.org/javadocs/org/testng/Assert.html), 
many test suites benefit from the use of custom assertions. The `ETSAssert` class in the 
root package is provided for this purpose, and test developers are free to add more as 
needed.

Informative error messages greatly aid the diagnosis of test failures. The test method shown 
in Listing 7 builds an assertion error message using a format string that includes several 
arguments supplying details and contextual information. The `ErrorMessageKeys` class in the 
root package defines keys that identify format strings in locale-specific resource bundles. For 
example, the `ErrorMessageKeys.NOT_SCHEMA_VALID` key value is "NotSchemaValid"; this corresponds 
to an entry in the properties file at src/main/resources/org/opengis/cite/${ets-code}/MessageBundle.properties: 

    NotSchemaValid = {0} schema validation error(s) detected.\n {1}

Test developers are strongly encouraged to follow this practice. As an additional benefit,
error messages can be provided in multiple languages using locale-specific data. This is 
primarily accomplished by creating a new properties file that contains the translated 
values (MessageBundle_fr.properties, for example). For more information, see the 
[Java internationalization tutorial](https://docs.oracle.com/javase/tutorial/i18n/).

A test suite can make use of third-party libraries as needed; these dependencies are 
explicitly identified in the project's POM file. The utility libraries listed in Table 
2 may also be of interest if conformance testing calls for more specialized assertion 
checking. The source code can be obtained from GitHub, and the release artifacts are available 
from the [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.opengis.cite%22) 
repository.

<table>
  <caption>Table 2: OGC/CITE utility libraries</caption>
  <thead>
    <tr style="text-align: left; background-color: LightCyan">
      <th>Library</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><a target="_blank" href="https://github.com/opengeospatial/schema-utils">schema-utils</a></td>
      <td>Provides support for validating XML representations using the following standard 
      schema languages: W3C XML Schema 1.0, Schematron (ISO/IEC 19757-3:2006), and RELAX NG 
      (ISO/IEC 19757-2:2008).</td>
    </tr>
    <tr>
      <td><a target="_blank" href="https://github.com/opengeospatial/geomatics-geotk">geomatics-geotk</a></td>
      <td>Provides support for processing spatial data and associated metadata using 
      various <a target="_blank" href="http://www.geotoolkit.org/">Geotk modules</a>.</td>
    </tr>
  </tbody>
</table>

In order to provide some assurance that a test suite is implemented correctly it is necessary 
to develop unit tests that verify the conformance test methods. The [JUnit framework](http://junit.org/) 
is used for this purpose. Several sample unit tests are can be found in the src/test/java 
directory.

Some conformance tests may use quite a few objects in the course of accessing a fixture, interacting 
with the IUT, or verifying a result. When it is desirable to mock or stub other objects in a unit 
test, the [Mockito framework](http://mockito.org/) can be very useful.


## <a name="s6">6</a> Test preconditions

A test, conformance class, or even an entire test suite may be subject to various preconditions 
that must be satisfied before it can be executed. For example, a web service must be available 
before a test run is initiated. Tests covering optional capabilities are generally not run if 
they are not implemented. If any precondition is not met, the affected tests are assigned a 
"SKIP" result.

TestNG provides several annotations for configuration methods that check preconditions:

- @BeforeSuite: The method will be run before any test methods in the suite are run.
- @BeforeTest: The method will be run before any test methods in the affiliated &lt;test&gt; 
set are run (usually applied to conformance classes).
- @BeforeClass: The method will be run before any test method defined in the current class 
is invoked.

As a convenience, a test suite generated with the Maven archetype contains a `SuitePreconditions` 
class wherein BeforeSuite methods can be defined. Be aware that if a class containing any 
configuration methods is not already included in the test suite definition, it must be added 
so the test runner will find it.


## <a name="s7">7</a> Further resources

### Documentation
* [TestNG documentation](http://testng.org/doc/documentation-main.html)
* [TestNG Beginner's Guide](https://books.google.com/books?id=9CuP8S2glWQC) (Google Books)
* [Next Generation Java Testing: TestNG and Advanced Concepts](https://books.google.com/books?id=bCvcMcLZwV4C) (Google Books)

### Mailing lists
* [TestNG users](https://groups.google.com/forum/#!forum/testng-users)
* [OGC/CITE developers](https://lists.opengeospatial.org/mailman/listinfo/cite-dev)
* [OGC/CITE users](https://lists.opengeospatial.org/mailman/listinfo/cite-forum)
