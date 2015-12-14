# Conformance Testing with TestNG, Part 2: Cookbook

## Recipes

01. [Introduction](#s1)
02. [Add a dependency on a third-party library](#s2)
03. [Process test run arguments](#s3)
04. [Create a reusable test fixture](#s4)
05. [Declare specification-related constants](#s5)
06. [Create a package for each conformance class](#s6)
07. [Add a test method](#s7)
08. [Provide informative error messages](#s8)
09. [Update TestNG configuration file](#s9)
10. [Verify a test method](#s10)
11. [Publish test suite documentation](#s11)
12. [Run the tests](#s12)

-----

## <a name="s1">1</a> Introduction

This guide assumes you have read _Conformance Testing with TestNG, Part 1: Essentials_ 
and have generated a new test suite using the Maven archetype as described in that document. 
This document presents some recipes for modifying a pristine test suite in order to implement 
specific test requirements. The OGC [GeoPackage](http://www.geopackage.org/) specification 
will be used to provide concrete examples.

## <a name="s2">2</a> Add a dependency on a third-party library

It is common to make use of external libraries in order to implement new test methods.
In this case we need a JDBC driver to access a SQLite database. This is accomplished by 
simply adding the following dependency to the project POM file:

```xml
<dependency>
  <groupId>org.xerial</groupId>
  <artifactId>sqlite-jdbc</artifactId>
  <version>3.8.11.2</version>
</dependency>
```

The driver will be available in the classpath as a compile dependency (the default scope).


## <a name="s3">3</a> Process test run arguments

The conformance test suite will accept one or more arguments that identify the 
test subject, or implementation under test (IUT). The value of the _iut_ argument 
is expected to be an absolute URI that refers to the IUT; in our example this 
should be a GeoPackage file. The built-in listener `PrimarySuiteListener` (provided 
by the teamengine-spi module) adds the supplied test run arguments to the collection 
of suite-level parameters. The input arguments can then be validated and processed by
the `SuiteFixtureListener` in the root package. In the `processSuiteParameters` method 
the _iut_ argument value is dereferenced and the resulting entity is saved to a 
local file. The File object is set as the value of the suite attribute named 
"testSubjectFile"; it can then be accessed as needed.

```java
void processSuiteParameters(ISuite suite) {
    Map<String, String> params = suite.getXmlSuite().getParameters();
    TestSuiteLogger.log(Level.CONFIG, "Suite parameters\n" 
        + params.toString());
    String iutParam = params.get(TestRunArg.IUT.toString());
    if ((null == iutParam) || iutParam.isEmpty()) {
        throw new IllegalArgumentException(
            "Required test run parameter not found: " 
            + TestRunArg.IUT.toString());
    }
    URI iutRef = URI.create(iutParam.trim());
    File gpkgFile = null;
    try {
        gpkgFile = URIUtils.dereferenceURI(iutRef);
    } catch (IOException iox) {
        throw new RuntimeException(
            "Failed to dereference resource located at " + iutRef, iox);
    }
    TestSuiteLogger.log(Level.FINE, 
        String.format("Wrote test subject to file: %s (%d bytes)",
        gpkgFile.getAbsolutePath(), gpkgFile.length()));
    suite.setAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName(), gpkgFile);
}
```


## <a name="s4">4</a> Create a reusable test fixture

A test fixture (also known as a test context) establishes a consistent baseline for 
running tests. It includes all the things that must be in place in order to run a test 
and verify a particular outcome. In practice, a fixture includes a set of of reusable 
components that persist for the duration of multiple tests--or even the lifetime of 
the entire test run. Examples of fixture items include: 

* a description of the test subject (e.g. service metadata);
* Pre-compiled schemas used to validate response messages; 
* an HTTP client component used to interact with a web service; 
* sample data that must be loaded in advance of testing;
* a driver used to create a database connection.

It is often convenient to create a shared fixture that provides easy access to 
commonly used objects for the duration of a test run. The `CommonFixture` class in 
the root package may be used for this purpose. In this test suite a shared fixture 
contains the following elements:

* a SQLite database file containing a GeoPackage;
* a JDBC DataSource for accessing the SQLite database.

```java
/** A SQLite database file containing a GeoPackage. */
protected File gpkgFile;
/** A JDBC DataSource for accessing the SQLite database. */
protected DataSource dataSource;

/**
 * Initializes the common test fixture. The fixture includes the following
 * components:
 * <ul>
 * <li>a File representing a GeoPackage;</li>
 * <li>a DataSource for accessing a SQLite database.</li>
 * </ul>
 * 
 * @param testContext
 *            The test context that contains all the information for a test
 *            run, including suite attributes.
 */
@BeforeClass
public void initCommonFixture(ITestContext testContext) {
    Object testFile = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName());
    if (null == testFile || !File.class.isInstance(testFile)) {
        throw new IllegalArgumentException(
                String.format("Suite attribute value is not a File: %s", SuiteAttribute.TEST_SUBJ_FILE.getName()));
    }
    this.gpkgFile = File.class.cast(testFile);
    SQLiteConfig dbConfig = new SQLiteConfig();
    dbConfig.setSynchronous(SynchronousMode.OFF);
    dbConfig.setJournalMode(JournalMode.MEMORY);
    dbConfig.enforceForeignKeys(true);
    SQLiteDataSource sqliteSource = new SQLiteDataSource(dbConfig);
    sqliteSource.setUrl("jdbc:sqlite:" + this.gpkgFile.getPath());
    this.dataSource = sqliteSource;
}
```

Note that the File object is obtained from a suite attribute in the `ITestContext` 
object that is injected into the **initCommonFixture** method. Any @Before or @Test 
method can declare a parameter of type `ITestContext`; when this is done, TestNG 
will perform the dependency injection automatically. The DataSource belongs to 
the test class (as a protected field, so it's accessible to all subclasses).


## <a name="s5">5</a> Declare specification-related constants

Most specifications define constant values that show up in test assertions and error 
messages. Add a class to the root package that declares these constants. The `GPKG10` 
class contains various constants pertaining to GeoPackage content and SQLite database 
files.

```java
/**
 * Provides various constants pertaining to GeoPackage 1.0 data containers.
 */
public class GPKG10 {

    /** Length of SQLite database file header (bytes). */
    public static final int DB_HEADER_LENGTH = 100;
    /** Starting offset of "Application ID" field in file header (4 bytes). */
    public static final int APP_ID_OFFSET = 68;
    /** SQLite v3 header string (terminated with a NULL character). */
    public static final byte[] SQLITE_MAGIC_HEADER = 
        new String("SQLite format 3\0").getBytes(StandardCharsets.US_ASCII);
    /** Application id for OGC GeoPackage 1.0. */
    public static final byte[] APP_GP10 = 
        new String("GP10").getBytes(StandardCharsets.US_ASCII);
    /** GeoPackage file name extension. */
    public static final String GPKG_FILENAME_SUFFIX = ".gpkg";
}
```

## <a name="s6">6</a> Create a package for each conformance class

Almost every OGC specification and ISO geomatics standard (in the 19100 series 
overseen by [TC 211](http://www.isotc211.org/)) defines an abstract test suite (ATS) 
containing one or more conformance classes. A conformance class is a set of logically 
related test cases that cover some functional capability. For example, the GeoPackage 
__Core__ conformance class includes constraints that apply to all packages; the 
__Tiles__ conformance class only applies to packages that contain tile data.

Conformance test suites implemented using TestNG adhere to the convention of putting 
tests that belong to different conformance classes into separate packages. So we'll 
create a new package for the __Core__ conformance class. Don't neglect to include a
package comment file (`package-info.java`) that describes the conformance class and 
identifies the relevant sources of test requirements.

```java
/**
 * This package contains tests covering the <strong>Core</strong> 
 * conformance class. The constraints apply to all GeoPackage files 
 * and fall into three areas:
 * 
 * <ul>
 * <li>SQLite Container</li>
 * <li>Spatial Reference Systems</li>
 * <li>Contents</li>
 * </ul>
 * 
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li><a href="http://www.geopackage.org/spec/#_core" target="_blank">
 * GeoPackage Encoding Standard - Core</a></li>
 * </ul>
 */
package org.opengis.cite.gpkg10.core;
```


## <a name="s7">7</a> Add a test method

In general a test method is traced to an abstract test case (ATC) or a requirement 
in a relevant specification. Test classes are declared as appropriate, but often 
it makes sense to preserve a logical grouping (this also helps to reduce the size 
of test classes). For example, the GeoPackage specification splits the core conformance 
constraints into three groups: the container structure, spatial reference systems, 
and package contents. Declare a test class for checking the general characteristics 
of a GeoPackage as a whole. Note that it extends `CommonFixture` so as to use the 
shared test fixture.

```java
package org.opengis.cite.gpkg10.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.opengis.cite.gpkg10.CommonFixture;
import org.opengis.cite.gpkg10.ErrorMessage;
import org.opengis.cite.gpkg10.ErrorMessageKeys;
import org.opengis.cite.gpkg10.GPKG10;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Defines test methods that apply to an SQLite database file. The 
 * GeoPackage standard defines a SQL database schema designed for use 
 * with the SQLite software library.
 * 
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li><a href="http://www.geopackage.org/spec/#_sqlite_container" 
 * target="_blank">GeoPackage Encoding Standard - SQLite Container</a> 
 * (OGC 12-128r12)
 * </li>
 * <li><a href="http://www.sqlite.org/fileformat2.html" 
 * target= "_blank">SQLite Database File Format</a></li>
 * </ul>
 */
public class SQLiteContainerTests extends CommonFixture {
}
```

Now add a test method to verify requirement 1: "A GeoPackage SHALL be a SQLite 
database file using version 3 of the SQLite file format." Do use Javadoc comments
to describe the applicable constraints and expected outcome.  

```java
/**
 * A GeoPackage shall be a SQLite database file using version 3 
 * of the SQLite file format. The first 16 bytes of a GeoPackage 
 * must contain the (UTF-8/ASCII) string "SQLite format 3", including 
 * the terminating NULL character.
 * 
 * @throws IOException
 *             If an I/O error occurs while trying to read the data file.
 * 
 * @see <a href="http://www.geopackage.org/spec/#_requirement-1" 
 * target="_blank">File Format - Requirement 1</a>
 */
@Test(description = "See OGC 12-128r12: Requirement 1")
public void fileHeaderString() throws IOException {
    final byte[] headerString = 
        new byte[GPKG10.SQLITE_MAGIC_HEADER.length];
    try (FileInputStream fileInputStream = 
        new FileInputStream(this.gpkgFile)) {
            fileInputStream.read(headerString);
    }
    Assert.assertEquals(headerString, GPKG10.SQLITE_MAGIC_HEADER, 
        ErrorMessage.format(ErrorMessageKeys.INVALID_HEADER_STR, 
        new String(headerString, StandardCharsets.US_ASCII)));
}
```

## <a name="s8">8</a> Provide informative error messages

It is very important to provide informative error messages so testers are not baffled 
by the reason for a failing test assertion. It is even possible to localize error 
messages using resource bundles, a long-standing mechanism in Java for isolating
[locale-specific data](https://docs.oracle.com/javase/tutorial/i18n/resbundle/). 
Test developers may add error messages in multiple languages if desired.

The `ErrorMessageKeys` class in the root package defines keys used to access localized 
messages for assertion errors; the messages themselves are stored in Properties files 
on the classpath in the root package (the MessageBundle*.properties files, one per 
supported language). There are several keys already defined for common error messages, 
but it is a simple matter to add more specific ones. For example, to add an error key 
for indicating the presence of an invalid header string in a GeoPackage file:

    public static final String INVALID_HEADER_STR = "InvalidHeaderString";

Supplement this with matching entries in the existing resource bundles:

    # MessageBundle.properties (default), MessageBundle_en.properties
    InvalidHeaderString = Data file has unexpected header string: {0}

Note the use of a message parameter, which is invaluable in providing diagnostic 
information. As shown in recipe 7 the `ErrorMessage` class provides a method for 
creating an assertion error message:

    ErrorMessage.format(ErrorMessageKeys.INVALID_HEADER_STR, 
        new String(headerString, StandardCharsets.US_ASCII))


## <a name="s9">9</a> Update TestNG configuration file

The execution of a test suite is driven by the TestNG configuration file, a classpath 
resource located in the root package (the `testng.xml` file found under src/main/resources 
in the code base). Each &lt;test&gt; element in the file corresponds to a conformance 
class. The &lt;packages&gt; element lists the packages that contain the test methods. 
Test methods that are not included by any reference will not be run. Add an element for 
the __Core__ conformance class:

```xml
<test name="Core">
  <packages>
    <package name="org.opengis.cite.gpkg10.core" />
  </packages>
</test>
```


## <a name="s10">10</a> Verify a test method

Like any other code, we should be confident that the test code behaves as expected 
such that a failing test verdict is due to a faulty IUT and not a buggy test. So we 
define (positive and negative) unit tests in order to verify test methods. The 
[JUnit](http://junit.org/) and [Mockito](http://mockito.org/) frameworks are available 
for this purpose.

The `VerifySQLiteContainerTests` class verifies test methods defined by `SQLiteContainerTests`.
Adopting this naming convention is recommended: the test methods in _ConformanceClassATests_ 
are exercised by _VerifyConformanceClassATests_ (under src/test/java).

```java
package org.opengis.cite.gpkg10.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.gpkg10.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;

public class VerifySQLiteContainerTests {

    private static ITestContext testContext;
    private static ISuite suite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initTestFixture() {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
    }

    @Test
    public void validHeaderString() 
        throws IOException, SQLException, URISyntaxException {
        URL gpkgUrl = getClass().getResource(
            "/gpkg/simple_sewer_features.gpkg");
        File dataFile = new File(gpkgUrl.toURI());
        when(suite.getAttribute(
            SuiteAttribute.TEST_SUBJ_FILE.getName())).thenReturn(dataFile);
        SQLiteContainerTests iut = new SQLiteContainerTests();
        iut.initCommonFixture(testContext);
        iut.fileHeaderString();
    }
}
```

Elements of the test fixture can be mocked or stubbed as appropriate. See the 
[Mockito documentation](http://site.mockito.org/mockito/docs/current/org/mockito/Mockito.html) 
for more information.


## <a name="s11">11</a> Publish test suite documentation

Test suite documentation can be published as a [GitHub Pages](https://pages.github.com/) 
site that is freely hosted in the `github.io` domain. A Maven project site is generated 
when the test suite is built. Simply push the site content to the special _gh-pages_ branch 
in order to make it publicly available.

    git checkout --orphan gh-pages
    git rm -rf .
    jar xf $HOME/ets-gpkg10-0.1-SNAPSHOT-site.jar
    git commit -a -m "Update site content for release 0.1-SNAPSHOT"
    git push origin gh-pages

The site may be accessed at http://opengeospatial.github.io/ets-gpkg10/.

## <a name="s12">12</a> Run the tests


### 12-1. Integrated development environment (IDE)

You can use a Java IDE such as Eclipse, NetBeans, or IntelliJ to build and run the test 
suite. First, clone the repository and build the project. All of these IDEs have built-in 
support for [Apache Maven](https://maven.apache.org/).

**Set the main class to run**: `org.opengis.cite.gpkg10.TestNGController`

**Arguments**: The first argument must refer to an XML properties file containing the 
required test run arguments. If not specified, the default location at `${user.home}/test-run-props.xml` 
will be used. You can modify the sample file in `src/main/config/test-run-props.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties version="1.0">
  <comment>Test run arguments</comment>
  <entry key="iut">http://www.geopackage.org/data/simple_sewer_features.gpkg</entry>
</properties>
```

The TestNG results file (`testng-results.xml`) will be written to a subdirectory
in `${user.home}/testng/` having a UUID value as its name.

### 12-2. Command shell (console)

One of the build artifacts is an "all-in-one" JAR file that includes the test 
suite and all of its dependencies; this makes it very easy to execute the test 
suite in a command shell:

    java -jar ets-gpkg10-0.1-SNAPSHOT-aio.jar [-o|--outputDir $TMPDIR] [test-run-props.xml]

### 12-3. OGC test harness

You may also use [TEAM Engine](https://github.com/opengeospatial/teamengine), the official 
OGC test harness, to execute the test suite. The latest test suite releases are usually 
available at the [beta testing facility](http://cite.opengeospatial.org/te2/). As an 
alternative, you can [build and deploy](https://github.com/opengeospatial/teamengine) the 
test harness yourself and use a local installation. The test suite can be invoked through 
the graphical interface or by using the RESTful API as indicated below.

    /teamengine/rest/suites/gpkg10/0.1-SNAPSHOT/run?iut=http://www.geopackage.org/data/simple_sewer_features.gpkg

