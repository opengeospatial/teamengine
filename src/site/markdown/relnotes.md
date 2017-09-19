Release Notes
=============

## 4.11 (2017-09-19)
- Fix [#231](https://github.com/opengeospatial/teamengine/issues/231): HTML report is not able to display test details page outside the teamengine.
- [web] Fix [#230](https://github.com/opengeospatial/teamengine/issues/230): Improve message for users if HTML report cannot be created from CTL
- Fix [#224](https://github.com/opengeospatial/teamengine/issues/224): Update documentation of REST API
- [spi] Fix [#239](https://github.com/opengeospatial/teamengine/issues/239): REST API: remove 'html' from path to request resource in html format
- [spi] Fix [#232](https://github.com/opengeospatial/teamengine/issues/232): REST API: Format should be passed via request header
- Merge [#211](https://github.com/opengeospatial/teamengine/pull/211): Earl reporting
- Merge [#225](https://github.com/opengeospatial/teamengine/pull/225): Added Message in result page.
- Merge [#153](https://github.com/opengeospatial/teamengine/pull/153): squid:CommentedOutCodeLine - Sections of code should not be commented out
- Merge [#155](https://github.com/opengeospatial/teamengine/pull/155): Multiple code improvements - squid:S2275, squid:S1197, squid:S1213, squid:S1066
- Merge [#154](https://github.com/opengeospatial/teamengine/pull/154): Multiple code improvements - squid:S1192, squid:S1488, squid:S1213

## 4.10 (2016-11-23)
- [spi-ctl] Fix [#190](https://github.com/opengeospatial/teamengine/issues/190) : Invoke CTL test suite via RESTful API
- [virtualization] Add Packer template for Amazon EC2 image (teamengine-aws)
- Incorporate user guides from OGC Testbed-12 into site content


## 4.9 (2016-10-07)
- [spi] Fix [#172](https://github.com/opengeospatial/teamengine/issues/172): Add test run input arguments to EARL report
- [spi] Fix [#171](https://github.com/opengeospatial/teamengine/issues/171): Ordering of conformance classes in EARL report 
- [web] Merge [PR #192](https://github.com/opengeospatial/teamengine/pull/192) to fix issue [#191](https://github.com/opengeospatial/teamengine/issues/191)
- [spi] Add diagnostic info about a failing test to earl:TestResult
- [spi] EARL results (earl-results.rdf) are now created and serialized by EarlReporter, which implements org.testng.IReporter 
- Add `teamengine-spi-ctl` module to enable execution of CTL suites using RESTful API.


## 4.8.1 (2016-08-15)
- Fix [#185](https://github.com/opengeospatial/teamengine/issues/185) - 4.8 not building due to teamengine-virtualization because of dependencies to ets-wfs20


## 4.8 (2016-08-05)
- [web,core] Merge [PR #182](https://github.com/opengeospatial/teamengine/pull/182): 
  Fix [ets-wms13#32](https://github.com/opengeospatial/ets-wms13/issues/32)
- [web,core] Merge [PR #181](https://github.com/opengeospatial/teamengine/pull/181): 
  Fix #174
- [core] Merge [PR #178](https://github.com/opengeospatial/teamengine/pull/178): 
  Fix NullPointerException if content type is null
- [virtualization] Merge [PR #176](https://github.com/opengeospatial/teamengine/pull/176): 
  Add support for building docker image (dockerfile)
- [web] Merge [PR #175](https://github.com/opengeospatial/teamengine/pull/175): 
  Fix #173


## 4.7.1 (2016-07-11)
- [spi] Create earl:TestRequirement resources in EarlTestListener#onStart method
- [spi] Add cite:TestRun resource to EARL results (summary of test results)
- Update site docs (use Maven 3.2.5 or later)


## 4.7 (2016-07-05)
- [spi:#152] Initial implementation of listeners that generate test results using W3C EARL vocabulary 
  (as application/rdf+xml)
- [virtualization: #167] Create [Packer](https://www.packer.io/) template to generate server image 
  for VirtualBox environment
- [web] Add VirtualWebappLoader to context (TE_BASE/resources/lib/*.jar)
- [core] Remove dependency on `xercesImpl-xsd11` (ets-kml2 now uses `xercesImpl-xsd11-shaded`)
- [core, web] Merge [PR #163](https://github.com/opengeospatial/teamengine/pull/163) to fix 
  [#158](https://github.com/opengeospatial/teamengine/issues/158): Merge security (XXE) fixes
- [realm] [#150](https://github.com/opengeospatial/teamengine/issues/150): Use PBKDF2 function to 
  generate password hashes
- [web] Add context listener to check that stored user passwords are not in clear text (generate hash if so).
- Update site documentation (Virtualization Guide)


## 4.6 (2016-02-29)
- [web] [#142](https://github.com/opengeospatial/teamengine/issues/142) - Simplify the user interface when creating a new session
- [site] Updated documentation related to using Java 8, prerequisites and adding tests (ETS)


## 4.5 (2016-02-03)
- [resources] Merge [PR #145](https://github.com/opengeospatial/teamengine/pull/145): 
  Update style sheet that displays WMS client test results
- [core] Resolve [#138](https://github.com/opengeospatial/teamengine/issues/138): 
  Add support for XML Schema 1.1
- [spi] Resolve [#139](https://github.com/opengeospatial/teamengine/issues/139): 
  Move supporting JAX-RS classes to WMS 1.3 client test suite
- [spi] Resolve [#127](https://github.com/opengeospatial/teamengine/issues/127): 
  Improve TestNG report (test descriptions)
- Update site documentation (TestNG cookbook)


## 4.4 (2015-11-03)
- [spi] Merge [PR #137](https://github.com/opengeospatial/teamengine/pull/137) to fix 
  [#136](https://github.com/opengeospatial/teamengine/issues/136): Exception error message 
  not displayed in HTML report
- [spi] Merge [PR #134](https://github.com/opengeospatial/teamengine/pull/134) to fix 
  [#131](https://github.com/opengeospatial/teamengine/issues/131): Add REST API method 
  to handle multipart request
- [spi] Merge [PR #133](https://github.com/opengeospatial/teamengine/pull/133): Revise 
  HTML representation of TestNG report.
- [web] Merge [PR #132](https://github.com/opengeospatial/teamengine/pull/132) to fix 
  [#98](https://github.com/opengeospatial/teamengine/issues/98): Invalid UTF-8 bytes 
  in test description
- Update site documentation (TestNG guidance)


## 4.3 (2015-09-29)
- [core] Merge [PR #123](https://github.com/opengeospatial/teamengine/pull/123) to fix 
  [#113](https://github.com/opengeospatial/teamengine/issues/113): 'Execute this session again' 
  button  not working
- [web, spi] Merge [PR #97](https://github.com/opengeospatial/teamengine/pull/97): Updated 
  ReportLog and ConfigFileCreator
- [web] Merge [PR #126](https://github.com/opengeospatial/teamengine/pull/126) to fix 
  [#124](https://github.com/opengeospatial/teamengine/issues/124): Unit test 
  `VerifyTestSuite.executeStartingTest` fails in Windows environment
- [core] Merge [PR #86](https://github.com/opengeospatial/teamengine/pull/86): Add 
  XmlValidatingParser.validateSingleResult


4.2 (2015-07-31)
-------------------
- [web] Fix [#96](https://github.com/opengeospatial/teamengine/issues/96) and [#112](https://github.com/opengeospatial/teamengine/issues/112): Sporadic incorrect inherit failures. 
  Also related to [#77](https://github.com/opengeospatial/teamengine/issues/77)  and [#70](https://github.com/opengeospatial/teamengine/issues/70) 
- [core] [spi] Fix [#108](https://github.com/opengeospatial/teamengine/issues/108) Too many open files - persistent storage and SXXP0003: Error reported by XML parser
- [all] Fix [#105](https://github.com/opengeospatial/teamengine/issues/105) Malformed pom.xml prevent single module build and use as dependency
- [core] Fix [#110](https://github.com/opengeospatial/teamengine/issues/110) Run TE via console in headless manner
- [core] Pull [#114](https://github.com/opengeospatial/teamengine/pull/114) Added headless form support, removed unnecessary casts, centralized access to args[i] into a single local variable
- [web] Fix [#95](https://github.com/opengeospatial/teamengine/issues/95) Improve how the sessions are displayed
- [web] Fix [#37](https://github.com/opengeospatial/teamengine/issues/37) Improve message when TE_BASE is not setup properly
- [web] Fix [#119](https://github.com/opengeospatial/teamengine/issues/119) Create a page that allows to get information about the build 


4.1 (2015-06-18)
---------------------------
- [site] All documentation is now written in markdown [#104] (https://github.com/opengeospatial/teamengine/issues/104).

4.1-beta2 (2015-05-15)
---------------------------------
- [web] Fix [#56] (https://github.com/opengeospatial/teamengine/issues/56): Configuration file updates automatically based on the tests inside scripts folder. Improved unit tests on this regard.
- [web] Work folder is cleaned every time TE starts
- [core] Log out better messages in the XML parsers, when null resources are being parsed.
 

4.1-beta1 (2015-04-24)
-------------------------------

This minor update provides the following enhancements and fixes:

- [core] Fix [#88](https://github.com/opengeospatial/teamengine/issues/88):
    XMLValidatingParser for DTD.
- [core, spi, web] Fix [#75](https://github.com/opengeospatial/teamengine/pull/75): Various
    enhancements to facilitate client testing.
- [spi] Fix [#74](https://github.com/opengeospatial/teamengine/issues/74): Return status code 400 if missing or invalid test run argument.
- [web] Fix [#92](https://github.com/opengeospatial/teamengine/issues/92):  
    TE_BASE/resources/site now allows to customize welcome, index and footer html pages
- [web] Fix [#38](https://github.com/opengeospatial/teamengine/issues/38): 
    Session not starting properly when hitting enter from a blank description field    
- [web] Fix [#56](https://github.com/opengeospatial/teamengine/issues/56): 
    Remove the need to update the main config file
- [web] Fix [#42](https://github.com/opengeospatial/teamengine/issues/42): 
    Add capability to remove sessions

4.0.6 (2014-12-09)
--------------------------

This maintenance release includes the following changes:

-   [web] Fix [#69](https://github.com/opengeospatial/teamengine/issues/69): Move
    register link to welcome page.
-   [core] Fix [#68](https://github.com/opengeospatial/teamengine/issues/68):
    Update test result only if a failing subtest has not already done
    so.
-   [core] Fix #64(https://github.com/opengeospatial/teamengine/issues/64):
    XMLValidatingParser will use schema location hints if no schema
    references are supplied.
-   [console] Remove export-ctl shell scripts; see
    [ets-resources](https://github.com/opengeospatial/ets-resources) for
    information about how to set up test suites.
-   Update the *Getting Started Guide*
-   Fix (Javadoc) errors when building with JDK 8.
-   Modify POMs for GitHub

4.0.5 (2014-03-11)
--------------------------

This maintenance release includes the following changes:

-   [web] Fix CITE-937: Service proxy is bypassed.
-   [web] Reintroduce default site content (/site).
-   Update *Getting Started Guide*

4.0.4 (2014-03-07)
--------------------------

This update includes the following fix:

-   [web] Fixed CITE-924: EPSG database is not created in web app
    environment.

4.0.3 (2013-11-14)
--------------------------

This maintenance release includes the following changes:

-   [web] Fixed CITE-895 (Remove site-specific HTML resources from WAR
    file)

4.0.2 (2013-11-01)
--------------------------

This maintenance release includes the following changes:

-   [web] Fixed base URL for web resources.
-   [spi] Don't split class and package names in TestNG reports.
-   [web] Added 'ogc.cite' profile to POM.
-   Updated dependencies to latest release versions: Xerces 2.11.0,
    TestNG 6.8.7, Maven plugins, etc.

4.0.1 (2013-08-23)
--------------------------

This is a maintenance release. It includes the following updates:

-   [spi] Show TestNG verdicts for each test group in text summary.
-   [web] Display sessions sorted in order with name of test suite.
-   Updated documentation.

4.0 (2013-07-04)
------------------------

This is the final release of TEAM-Engine 4.0. The following issues were
resolved:

-   [core] Fixed CITE-822 (SoapParser does not support HTTP code 400
    "Bad request").
-   [core] Fixed CITE-821 (HTTPParser attempts to parse non-XML entity).
-   [core] Fixed CITE-810 (eliminate spurious inherited failures).

4.0-rc
--------------

-   Release candidate.
-   [core] Warning verdict in subtest no longer 'taints' result of
    parent test.
-   [web] Uploaded file is placed in test session directory (fix for
    CITE-808).
-   [web] Removed config/home property in TE\_BASE/config.xml.
-   [web] GetStatus polling interval increased to 4s from 1s.

4.0-beta3
-----------------

-   TestNG reporter (BasicXMLReporter) adds test result attributes to
    report.
-   Added teamengine-console module (assembles binary CLI distribution
    instead of core module).
-   TECore.build\_request() no longer automatically percent-encodes
    request parameters.
-   CTL test report generator now recognizes all test verdicts.
-   Updated teamengine-core dependencies (joda-time-2.2).
-   Updated site documentation.

4.0-beta2
-----------------

-   Allow customization of content on welcome page (teamengine-web
    module)
-   Added test suite revision/status element
    ("Alpha","Beta","Final","Deprecated").
-   Eliminated separate contexts for static web resources.
-   Added TE\_BASE/config.xml as a watched resource (update triggers
    reload).
-   REST API presents Javadoc overview document at
    /rest/suites/{etsCode}/{etsVersion}/.
-   Added support for ctl-msg processing instruction on ctl:test tags to
    enable output of structured messages (via saxon:serialize()
    function).
-   Various updates of the site documentation.

