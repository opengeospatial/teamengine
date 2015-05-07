This document provides some general guidance about how to use TEAM
Engine to execute a test run and view the results. There are three ways
to interact with the test harness: a command shell, the web application,
or a REST-like API.

Command shell
-------------

The console application (teamengine-console-\${project.version}-bin.zip)
includes shell scripts for running test suites in Windows and Unix-like
(Bash shell) environments. Unpack the archive into a convenient location
(TE\_HOME); the contents are as shown below.

    TE_HOME
      |-- bin/              # shell scripts (windows, unix)
      |-- lib/              # supporting libraries
      |-- resources/        # classpath resources (stylesheets, schemas, etc.)
        

If desired, set the value of the TE\_BASE environment variable in the
`setenv` script; otherwise set it for the user evironment according to
the particulars of the operating system in use. Once this is done,
change to the appropriate script directory (TE\_HOME/bin/{unix,windows})
or add it to the system path.

![](./images/warn-16px.png) **Warning:** If a test suite requires any
supporting libraries that are not included with the core distribution,
these must be added to the TE\_BASE/resources/lib directory.

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

Web application
---------------

The web application (teamengine.war) provides a user interface for
selecting test suites, browsing test documentation, and launching test
runs. The welcome page (e.g. http://localhost:8080/teamengine) displays
a table listing all available test suites. In a new installation a
sample suite for the fictitious "XML Note" specification should appear.
The listed test suites correspond to entries in the main configuration
file located at TE\_BASE/config.xml.

Select "Login" and then supply the appropriate credentials or register
to create a new user account. After logging in, previous test sessions
are displayed. Select "Create new session" and choose a test suite to
execute.

The TE\_BASE/config.xml file is a "watched" resource--the web
application will be reloaded whenever this file is modified. This means
that test suites can be added or updated and become available for use
without having to restart the Tomcat instance. Simply put the CTL
scripts in TE\_BASE/scripts and insert or update an entry in the main
configuration file. Supporting libraries, if any, must be placed in the
WEB-INF/lib directory.

As a convenience, the `ets-resources` project may be [cloned from
GitHub](https://github.com/opengeospatial/ets-resources) and built (run
'mvn package') to generate an archive containing all supporting
libraries for the latest OGC test suites. The contents of the archive
are described below.

`config.xml`
:   TEAM-engine instance configuration file (TE\_BASE/config.xml).
`config-approved.xml`
:   TEAMengine configuration file that includes only test suite releases
    that have been formally approved for compliance certification by the
    OGC Technical Committee.
`ctl-scripts-release.csv`
:   A CSV file that contains a list of test suite releases. Each line
    has two fields: Git repository URL, tag name.
`lib/*.jar`
:   A directory containing the required Java libraries that must be
    available on the class path; that is, WEB-INF/lib for the web
    application or TE\_BASE/resources/lib for command-line execution.
`bin/`
:   A directory containing shell scripts for Windows- and UNIX-based
    (Linux/Mac) hosts. The \`setup-tebase\` script will set up a
    TEAM-engine instance (TE\_BASE) with the test suites identified in a
    referenced CSV file.

REST API
--------

A simple REST-like API (based on [JAX-RS
1.1](http://jcp.org/en/jsr/detail?id=311)) enables programmatic
execution of some test suites; currently only TestNG-based suites can be
run in this manner. It exposes the endpoints listed in Table 2. In the
request URIs the `{etsCode}` and `{etsVersion}` parameters denote the
test suite code and version, respectively, for a particular test suite.

  URI (relative to base)                    Resource                   Representation
  ----------------------------------------- -------------------------- -----------------------
  /rest/suites                              Test suite collection      application/xhtml+xml
  /rest/suites/{etsCode}/{etsVersion}       Test suite documentation   text/html
  /rest/suites/{etsCode}/{etsVersion}/run   Test run controller        application/xml

  : Table 2 - REST endpoints

![](./images/warn-16px.png) **Warning:** When using the REST API, if any
test run argument includes a URI value that contains an ampersand ('&')
character in a query component, it must be percent-encoded as %26 since
it is a "data octet" in this context (see [RFC 3986, sec.
2.1](http://tools.ietf.org/html/rfc3986#section-2.1)).

The test run parameters are described in the test suite summary
document. This document can be viewed from the web application by
selecting the *Test Suite Revision* link displayed on the home page; the
REST API will also present the document at the
`/rest/suites/{etsCode}/{etsVersion}` endpoint. Each parameter is either
mandatory, conditional (required under certain circumstances), or
optional.

If a test suite is being deployed manually, several steps are required
to set up an executable test suite (ETS) implemented using the TestNG
framework. Note that the first two steps are **not** required in order
to use the REST API.

1.  Unpack the \*-ctl.zip archive into the TE\_BASE/scripts directory;
    it includes test suite documentation and a simple CTL script that
    invokes the main controller.
2.  Update the TE\_BASE/config.xml file by adding or editing the
    \<standard\> element for the test suite.
3.  Put the ETS component (a binary JAR file) and any dependencies into
    the `WEB-INF/lib` directory of the web application.

The `*-deps` archive assembles the ETS and its dependencies into a
single bundle that is unpacked into the lib directory in the last step.
In some cases it may be necessary to add other (transitive)
dependencies. The Tomcat instance does not need to be restarted to
enable the test suite--it should be available immediately.
