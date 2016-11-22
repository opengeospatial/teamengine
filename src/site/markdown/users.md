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

| URI (relative to base) | Resource | Response media type |
|--- | --- | --- |
| /rest/suites | Test suite collection |  application/xhtml+xml |
| /rest/suites/{etsCode}/{etsVersion} | Test suite documentation | application/xhtml+xml |
| /rest/suites/{etsCode}/{etsVersion}/run | Test run controller | application/xml, application/rdf+xml |

In the request URIs the `{etsCode}` and `{etsVersion}` parameters denote the
test suite code and version, respectively, for a particular test suite.

![warning](./images/warn-16px.png) **Warning:** When using the REST API, if any
test run argument includes a URI value that contains an ampersand ('&')
character in the query component, it must be percent-encoded as %26 since
it is a "data octet" in this context (see [RFC 3986, sec.
2.1](http://tools.ietf.org/html/rfc3986#section-2.1)).

The test run parameters are described in the test suite summary document. This 
document can be viewed from the web application by selecting the _Test Suite Revision_
link displayed on the home page; the REST API will also present the document at the
`/rest/suites/{etsCode}/{etsVersion}` endpoint. Each parameter is either mandatory, 
conditional (required under certain circumstances), or optional.

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
