# User Guide

This document provides some general guidance about how to use TEAM Engine to 
execute a test run and view the results. There are three ways to interact with 
the test harness: a command shell, the web application, or a REST-like API.

## Command shell

When a test is executed a Java applet will popup and display a form for the user 
to fill in with the details of the implementation to be tested. The user completes 
the form and in some cases will need to perform visual inspection and fill in other 
forms. There are two ways to run a test: interactively or non-interactively by 
providing the form responses in a file.


### Running a test suite interactively

The console application (teamengine-console-\${project.version}-bin.zip)
includes shell scripts for running test suites in Windows and Unix-like
(Bash shell) environments. Unpack the archive into a convenient location
(TE_HOME); the contents are as shown below.

    TE_HOME
      |-- bin/        # shell scripts (windows, unix)
      |-- lib/        # supporting libraries
      |-- resources/  # classpath resources (schemas, etc.)


If desired, set the value of the `TE_BASE` environment variable in the
`setenv` script; otherwise set it for the user environment according to
the particulars of the operating system in use. Once this is done,
change to the appropriate script directory (TE_HOME/bin/{unix,windows})
or add it to the system path.

![warning](./images/warn-16px.png) **Warning:** If a test suite requires any
supporting libraries that are not included with the core distribution,
these must be added to the TE_BASE/resources/lib directory.

To view a brief summary of a test suite, run the `listsuites` script and
specify the location of the root CTL script (relative to
TE\_BASE/scripts).

    > listsuites -source=note.ctl

    Suite note:note-test ({http://example.net/note-test}note-test)
    Sample test suite
    Checks the content of a note.

To execute a test suite, run the `test` script and specify the location
of the root CTL script (this contains the ctl:suite element); the file
reference may be an absolute filesystem location or relative to
TE\_BASE/scripts. The test results will be written to a subdirectory
*sNNNN* created in TE\_BASE/users/*username*/.

    > test -source=note.ctl

    Testing suite note:note-test in Test Mode with defaultResult of Pass ...
    Testing note:main type Mandatory in Test Mode with defaultResult Pass (s0002)
    ...
    
### Running a test suite non-interactively

It is possible to run the tests in a headless, unattended manner by providing 
files containing the form responses. Form files are specified via the ``-form`` 
parameter, more than one form can be provided using multiple ``-form`` parameters. 
For example, the WMS 1.1.1 tests can be run with the following command:
    
     $ test.sh -source=wms/1.1.1/ctl/ \
       -form=$forms/wms-1.1.1.xml \
       -form=forms/yes.xml

Where ``forms/wms-1.1.1.xml`` is:

     <?xml version="1.0" encoding="UTF-8"?>
     <values>
       <value key="VAR_WMS_CAPABILITIES_URL">
       http://host1/wms?service=WMS&amp;version=1.1.1&amp;request=GetCapabilities
       </value>
       <value key="updatesequence">auto_updatesequence</value>
       <value key="VAR_HIGH_UPDATESEQUENCE">100</value>
       <value key="VAR_LOW_UPDATESEQUENCE">0</value>
       <value key="CERT_PROFILE">queryable_profile</value>
       <value key="recommended">recommended</value>
       <value key="testgml">testgml</value>
       <value key="free">free</value>
       <value key="B_BOX_CONSTRAINT">eitherbboxconstraint</value>
     </values>

and ``forms/yes.xml`` is:
 
     <?xml version="1.0" encoding="UTF-8"?>
     <values>
       <value key="submit">yes</value>
       <value key="answer">yes</value>
     </values>

The form files are used by TEAM Engine in the same order as provided on the command line. 
In case that the test requires filling more forms than provided on the command line, the last provided form is
going to be used multiple times: for example, in the WMS 1.1.1 case, the test will ask the user to visually
confirm visual relationships between two maps, the ``yes.xml`` form will be used for all those
requests.

After the test is invoked via command line, the console output will retrieve the information 
from the forms before providing the test results.

For example:

      INFO: Setting form results:
       <?xml version="1.0" encoding="UTF-8"?>
      <values>
         <value key="VAR_WMS_CAPABILITIES_URL">
         http://host1/wms?service=WMS&amp;version=1.1.1&amp;request=GetCapabilities
         </value>
         <value key="updatesequence">auto_updatesequence</value>
         <value key="VAR_HIGH_UPDATESEQUENCE">100</value>
         <value key="VAR_LOW_UPDATESEQUENCE"></value>
         <value key="CERT_PROFILE">queryable_profile</value>
         <value key="testgml">testgml</value>
         <value key="free">free</value>
         <value key="B_BOX_CONSTRAINT">eitherbboxconstraint</value>
      </values>
      
      Testing suite wms:main_wms in Test Mode with defaultResult of Pass ...
      ...
      Testing wms:wmsops-getmap-params-bbox-2 type Mandatory in Test 
      Mode with defaultResult Pass (s0004/d275e678_1)...
         Assertion: When a GetMap request uses decimal values for the 
         BBOX parameter, then the response is valid.

      Jul 12, 2015 2:44:20 PM com.occamlab.te.TECore setFormResults
      INFO: Setting form results:
       <?xml version="1.0" encoding="UTF-8"?>
      <values>
        <value key="submit">yes</value>
        <value key="answer">yes</value>
      </values>
            Test wms:wmsops-getmap-params-bbox-2 Passed
      

## Web application

### Using the Web Browser Interface

The web application (teamengine.war) provides a user interface for selecting 
test suites, browsing test documentation, and launching test runs. The welcome 
page (e.g. http://localhost:8080/teamengine) displays a list of all available 
test suites. In a new installation a sample suite for the fictitious "XML Note" 
specification should appear. The listed test suites correspond to entries in 
the main configuration file located at TE\_BASE/config.xml.

Select "Login" and then supply the appropriate credentials or register
to create a new user account. After logging in, previous test sessions
are displayed. Select "Create new session" and choose a test suite to
execute.


### Using the REST API

A simple REST API (based on [JAX-RS 1.1](http://jcp.org/en/jsr/detail?id=311)) 
enables programmatic execution of many test suites; it is comprised of the following 
end points:

| URI (relative to base) | Resource | Method(s) | Response media type |
|--- | --- | --- | --- |
| /rest/suites | List of available test suites | GET |  application/xhtml+xml |
| /rest/suites/{etsCode}/{etsVersion} | Test suite documentation | GET | application/xhtml+xml |
| /rest/suites/{etsCode}/{etsVersion}/run | Test run controller | GET, POST | application/rdf+xml, application/xml, application/zip |

In the request URIs the `{etsCode}` and `{etsVersion}` parameters denote the
test suite code (example: "wfs20") and version (example: "1.25"), respectively, for a particular test suite.

![warning](./images/warn-16px.png) **Warning:** When using the REST API, if any
test run argument includes a URI value that contains an ampersand ('&')
character in the query component, it must be percent-encoded as %26 since
it is a "data octet" in this context (see [RFC 3986, sec.
2.1](http://tools.ietf.org/html/rfc3986#section-2.1)).

The list of available test suites is presented as a brief HTML document (XHTML syntax) that contains 
links to the deployed test suites. While the document can be displayed in a web browser for human 
viewers, it can also be consumed and parsed by other software applications in order to facilitate 
test execution. For example, a service description in a registry could be automatically annotated 
with information about its conformance status by running a test suite and inspecting the results.

List of deployed test suites (XHTML syntax):

    <ul>
      <li><a href="suites/wfs20/1.25/" id="wfs20-1.25" type="text/html">WFS 2.0 (ISO 19142:2010) Conformance Test Suite</a></li>       
      <li><a href="suites/gml32/1.24/" id="gml32-1.24" type="text/html">GML (ISO 19136:2007) Conformance Test Suite, Version 3.2.1</a></li>
      <!-- other available test suites -->
    </ul>

When the link for a particular test suite is dereferenced a summary document is obtained. This 
document briefly describes the test suite and contains a table of test run arguments. Each 
input argument is a separate entry in the body of an HTML table as shown in the listing below.

Test run arguments for the WFS 2.0 test suite (raw HTML):

    <tbody>
      <tr id="wfs">
        <td>wfs</td>
        <td>URI</td>
        <td>M</td>
        <td>A URI that refers to a representation of the service capabilities document. 
        This document does not need to be obtained from the service under test (SUT),
        but it must describe the SUT. Ampersand ('&amp;') characters appearing within 
        a query parameter value must be percent-encoded as %26.</td>
      </tr>
      <tr id="fid">
        <td>fid</td>
        <td>NCName</td>
        <td>O</td>
        <td>An identifier that matches the @gml:id attribute value of an available feature 
        instance (may be omitted for "Basic WFS" implementations).</td>
      </tr>
    </tbody>

Description of test run arguments presented in a web browser: 

![Test run arguments in browser,align=center](.images/test-run-args.png)

A test run is initiated by submitting a request to the test run controller. The summary 
description lists the test run arguments (mandatory, conditional, optional) that are 
recognized by the controller. A test suite can be invoked using a simple GET request in most cases. 
For example, to test a WFS 2.0 implementation the target URI is constructed as follows (replace 
localhost:8080 with the actual host name and port number of an available teamengine installation):

    http://localhost:8080/teamengine/rest/suites/wfs20/1.25/run?wfs={wfs-capabilities-url}

where `{wfs-capabilities-url}` is the URL to retrieve the capabilities document for the 
implementation under test (IUT). Note that this need not be obtained directly from the IUT--it could be fetched from elsewhere (e.g. a service registry), as long as it describes the 
same service.

TEAM Engine provides three different types of result formats for test runs. The requested content type is set via HTTP request header:

| Format of resource | HTTP request header |
|--- | --- |
| EARL (RDF/XML) | Accept: application/rdf+xml |
| XML | Accept: application/xml |
| ZIP containing HTML files | Accept: application/zip |

With TEAM Engine 4.9 or later it is also possible to invoke a CTL test suite in this manner.
However, a controller must be available in order to do this. The https://github.com/opengeospatial/ets-wms13[WMS 1.3] 
test suite contains a https://github.com/opengeospatial/ets-wms13/blob/master/src/main/java/org/opengis/cite/wms13/CtlController.java[CtlController class] 
that serves as an example of how to enable this capability in other CTL test suites.

If a test suite is being deployed manually, a couple of steps are required to set 
up an executable test suite (ETS) that was implemented using the TestNG framework. 
Note that the first step is **not** required in order to use the REST API.

1.  Unpack the \*-ctl.zip archive into the TE\_BASE/scripts directory;
    it includes test suite documentation and a simple CTL wrapper script 
    that invokes the main controller.
2.  Put the ETS component (a binary JAR file) and its dependencies into
    the `TE_BASE/resources/lib` directory.

The `*-deps` archive assembles the ETS and its dependencies into a single bundle that 
is unpacked into the lib directory in the last step. In some cases it may be necessary 
to add other (transitive) dependencies. The Tomcat instance does not need to be restarted 
to enable the test suite--it should be available immediately.

#### Output formats

##### EARL (RDF/XML)

The REST API supports the W3C Evaluation and Report Language (EARL, 1.0 Schema) as output format. The 
specification (currently a late stage working draft) defines an RDF vocabulary for 
describing test results:

* http://www.w3.org/TR/EARL10-Schema/[Evaluation and Report Language (EARL) 1.0 Schema]
* http://www.w3.org/TR/EARL10-Guide/[Developer Guide for EARL 1.0]
* https://www.w3.org/TR/HTTP-in-RDF10/[HTTP Vocabulary in RDF 1.0]
* https://www.w3.org/TR/Content-in-RDF10/[Representing Content in RDF 1.0]

The following listing shows how conformance classes are described using the EARL vocabulary.
An `earl:TestRequirement` instance represents a conformance class; it has one or more 
constituent tests (`earl:TestCase`). Furthermore, a dependency may be expressed using 
the _dct:requires_ property. In this example, *Conformance level 2* is based on 
*Conformance level 1* and thus establishes a higher level of conformance.

Conformance classes in EARL results (RDF/XML):

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:earl="http://www.w3.org/ns/earl#"      
         xmlns:dct="http://purl.org/dc/terms/">
      <earl:TestRequirement rdf:about="http://www.opengis.net/spec/KML/2.3/conf/level-1">
        <dct:title xml:lang="en">KML 2.3 - Conformance Level 1</dct:title>
        <dct:description xml:lang="en">Conformance Level 1 includes test cases that address 
          absolute requirements. A KML document must satisfy all assertions at this level to 
          achieve minimal conformance</dct:description>
        <dct:isPartOf rdf:resource="http://docs.opengeospatial.org/ts/14-068r2/14-068r2.html"/>
        <dct:hasPart>
          <earl:TestCase rdf:about="http://www.opengis.net/spec/KML/2.3/conf/level-1/atc-101">
          <dct:description>Verify that the root element of the document has [local name] = "kml" 
            and [namespace name] = "http://www.opengis.net/kml/2.3".</dct:description>
          <dct:title>Document element</dct:title>
          </earl:TestCase>
        </dct:hasPart>
      <!-- other constituent test cases omitted -->
      </earl:TestRequirement>
      <earl:TestRequirement rdf:about="http://www.opengis.net/spec/KML/2.3/conf/level-2">
        <dct:title xml:lang="en">KML 2.3 - Conformance Level 2</dct:title>
        <dct:description xml:lang="en">Includes all tests in Level 1, plus test cases covering 
          requirements that should be satisfied by a KML document. Non-conformance at this 
          level may hinder the utility, portability, or interoperability of the document.</dct:description>
        <dct:requires rdf:resource="http://www.opengis.net/spec/KML/2.3/conf/level-1"/>
        <!-- constituent test cases omitted -->
      </earl:TestRequirement>
    </rd:RDF>


The EARL vocabulary does not define any terms that pertain to a test run by itself. A custom 
vocabulary was introduced for this purpose. A `cite:TestRun` resource provides basic summary 
information about a test run, including the input arguments and an overall tally of test 
verdicts. Standard http://dublincore.org/documents/dcmi-terms/[Dublin Core metadata terms] 
are employed where appropriate. For example, the dct:extent property reports the temporal 
extent of the test run; that is, its total duration represented using the XML Schema
https://www.w3.org/TR/xmlschema-2/#duration[duration datatype].

A TestRun resource:

    <cite:TestRun xmlns:cite="http://cite.opengeospatial.org/">
      <dct:extent rdf:datatype="http://www.w3.org/2001/XMLSchema#duration">PT6M30.204S</dct:extent>
      <dct:title>wfs20-1.25</dct:title>
      <cite:testsSkipped rdf:datatype="http://www.w3.org/2001/XMLSchema#int">1</cite:testsSkipped>
      <cite:testsPassed rdf:datatype="http://www.w3.org/2001/XMLSchema#int">298</cite:testsPassed>
      <cite:testsFailed rdf:datatype="http://www.w3.org/2001/XMLSchema#int">46</cite:testsFailed>
      <dct:created>2016-10-25T17:33:31.290Z</dct:created>
      <cite:inputs>
        <rdf:Bag>
          <rdf:li rdf:parseType="Resource">
            <dct:title>wfs</dct:title>
            <dct:description>http://example.org/services/wfs?service=WFS&amp;request=GetCapabilities</dct:description>
          </rdf:li>
          <rdf:li rdf:parseType="Resource">
            <dct:title>xsd</dct:title>
            <dct:description>http://example.org/services/wfs?service=WFS&amp;version=2.0.0&amp;request=DescribeFeatureType</dct:description>
          </rdf:li>
        </rdf:Bag>
      </cite:inputs>
      <dct:identifier>8ed93bd8-b366-4d4f-b868-c8e5aeccfbaa</dct:identifier>
    </cite:TestRun>

##### XML

The output format XML of the test results is framework-specific: for TestNG, this is an XML
representation having _testng-results_ as the document element. The results of running a
CTL test suite also produce XML output, with _execution_ as the document element. 

###### CTL XML
A successful response contains an XML entity that represents the test results. The root 
element contains a _log_ child element for each test that was run. The first log entry
indicates the overall verdict; the value of the endtest/@result attribute is an integer 
code that signifies a test verdict (see table below).

|Code |Result |
|--- | --- |
|1    |Passed | 
|2    |Not Tested | 
|3    |Skipped | 
|4    |Warning | 
|5    |Inherited Failure |  
|6    |Failed | 

If a constituent test failed, the overall verdict is set as *Inherited Failure* (5).
In general, a failed subtest will "taint" all of its ancestor tests in this manner.
