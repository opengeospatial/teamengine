Release Notes
=============

## 6.0.0-RC2 (2024-11-07)
- [#624](https://github.com/opengeospatial/teamengine/pull/624): Update versions of dependencies and plugins

## 6.0.0-RC1 (2024-02-14)

Attention: Java 17 and Tomcat 10.1 are required.

- [#511](https://github.com/opengeospatial/teamengine/issues/511): Update to Java 17
- [#556](https://github.com/opengeospatial/teamengine/issues/556): TEAM Engine Tomcat 10.1 update
- [#579](https://github.com/opengeospatial/teamengine/issues/579): Update Maven dependencies to latest versions
- [#574](https://github.com/opengeospatial/teamengine/issues/574): Update Maven plugins to latest versions
- [#598](https://github.com/opengeospatial/teamengine/issues/598): Modernize unit tests
- [#599](https://github.com/opengeospatial/teamengine/issues/599): Upgrade TestNG to latest version
- [#587](https://github.com/opengeospatial/teamengine/issues/587): Update documentation for version 6.0
- [#591](https://github.com/opengeospatial/teamengine/issues/591): Clean up code
- [#608](https://github.com/opengeospatial/teamengine/issues/608): aio JAR CLI does not work with acceptMediaType application/zip
- [#614](https://github.com/opengeospatial/teamengine/pull/614): Bump org.apache.tomcat:tomcat-catalina from 10.1.11 to 10.1.16
- [#602](https://github.com/opengeospatial/teamengine/issues/602): Introduce spring-javaformat-maven-plugin and execute formatting
- [#576](https://github.com/opengeospatial/teamengine/issues/576): Resolve errors and warnings of maven-javadoc-plugin when using Java 17
- [#590](https://github.com/opengeospatial/teamengine/issues/590): Report details link points to broken locations
- [#589](https://github.com/opengeospatial/teamengine/issues/589): Fix REST interface for version 6.0
- [#601](https://github.com/opengeospatial/teamengine/issues/601): Introduce Maven Enforcer Plugin
- [#575](https://github.com/opengeospatial/teamengine/issues/575): Unit test ImageParserTest.parsePNG_noAlphaChannel fails with Java 17
- [#578](https://github.com/opengeospatial/teamengine/issues/578): Analyse warnings logged by maven-pdf-plugin when using Java 17

## 5.7 (2023-12-18)
- [#603](https://github.com/opengeospatial/teamengine/pull/603): Add CORS header in process method of MonitorServlet
- [#593](https://github.com/opengeospatial/teamengine/pull/593): Fix test result logic for results other than PASS/FAIL.

## 5.6.1 (2023-04-14)
- [#572](https://github.com/opengeospatial/teamengine/pull/572): Fix a bug resulting from loading order of xml-resolver and schema-utils

## 5.6 (2023-03-31)
- [#552](https://github.com/opengeospatial/teamengine/pull/552): Console application updates
- [#558](https://github.com/opengeospatial/teamengine/issues/558): Some test suites display java.lang.AssertionError in HTML report
- [#559](https://github.com/opengeospatial/teamengine/issues/559): Enhance EARL/HTML report to be generated when iut is not reachable
- [#560](https://github.com/opengeospatial/teamengine/issues/560): Create best practice documentation for failures caused by nested CTL tests
- [#561](https://github.com/opengeospatial/teamengine/issues/561): Implement consideration of config.xml on classpath
- [#555](https://github.com/opengeospatial/teamengine/issues/555): Wrong URL in HTML reports for test using POST requests
- [#569](https://github.com/opengeospatial/teamengine/pull/569): Bump commons-fileupload from 1.3.3 to 1.5 in /teamengine-web
- [#557](https://github.com/opengeospatial/teamengine/pull/557): [SECURITY] Fix Zip Slip Vulnerability

## 5.5.2 (2022-08-26)
- [#553](https://github.com/opengeospatial/teamengine/issues/553): WFS 1.0 and WCS 2.0 test suites fail with IllegalStateException: Already connected

## 5.5.1 (2022-08-12)
- [#548](https://github.com/opengeospatial/teamengine/issues/548): REST API: Test run against CTL based test suite does not return test result
- [#544](https://github.com/opengeospatial/teamengine/issues/544): TEAM Engine cannot be started without setting java property javax.xml.parsers.DocumentBuilderFactory
- [#545](https://github.com/opengeospatial/teamengine/issues/545): View Sessions button appears at unexpected position
- [#551](https://github.com/opengeospatial/teamengine/pull/551): Set commons-codec to v1.11
- [#546](https://github.com/opengeospatial/teamengine/pull/546): Add credentials to SoapUI project

## 5.5 (2022-07-07)
- [#541](https://github.com/opengeospatial/teamengine/issues/541): REST API: Test run with Accept application/zip returns 404
- [#542](https://github.com/opengeospatial/teamengine/issues/542): REST API: Test runs with CTL based test suites return 500
- [#488](https://github.com/opengeospatial/teamengine/issues/488): Replace old with new logo
- [#498](https://github.com/opengeospatial/teamengine/issues/498): Delete Session leads to DocumentBuilderFactoryImpl not found
- [#538](https://github.com/opengeospatial/teamengine/pull/538): Bump tomcat-catalina from 7.0.69 to 7.0.81 in /teamengine-realm
- [#483](https://github.com/opengeospatial/teamengine/issues/483): Replace previous OGC logo with the new OGC logo
- [#533](https://github.com/opengeospatial/teamengine/issues/533): REST interface cannot be used with CTL test suites
- [#530](https://github.com/opengeospatial/teamengine/issues/530): When user is logged in to TEAM Engine and returns to langing page, there is no button to start further test runs
- [#532](https://github.com/opengeospatial/teamengine/issues/532): Executing ets-wms-client13 test suite with master branch leads to unexpected password request
- [#528](https://github.com/opengeospatial/teamengine/issues/528): Unit tests TECoreTest.testNestedFailure and TECoreTest.testNestedWarning fail
- [#522](https://github.com/opengeospatial/teamengine/issues/522): Unit test TEPathTest fails
- [#523](https://github.com/opengeospatial/teamengine/issues/523): Session id is not displayed correctly
- [#476](https://github.com/opengeospatial/teamengine/issues/476): HTTPParser doesn't follow 302 redirect
- [#493](https://github.com/opengeospatial/teamengine/issues/493): HTML report contains obsolete \<br\>
- [#310](https://github.com/opengeospatial/teamengine/issues/310): Validate Run-time options
- [#294](https://github.com/opengeospatial/teamengine/issues/294): Fortify Issue: Path Manipulation
- [#518](https://github.com/opengeospatial/teamengine/pull/518): Fix inheritance
- [#346](https://github.com/opengeospatial/teamengine/issues/346): Refactor folder structure of Maven project
- [#502](https://github.com/opengeospatial/teamengine/issues/502): StackTrace is displayed in web browser when session is broken
- [#500](https://github.com/opengeospatial/teamengine/issues/500): viewOldSessionLog page returns 500 NullPointerException when not logged in
- [#480](https://github.com/opengeospatial/teamengine/issues/480): Empty Earl-report
- [#66](https://github.com/opengeospatial/teamengine/issues/66): Prohibit anonymous test execution using the REST API
- [#439](https://github.com/opengeospatial/teamengine/pull/439): Added API to generate statistics regularly.
- [#420](https://github.com/opengeospatial/teamengine/issues/420): Add hint to legal terms when registering to TEAM Engine
- [#354](https://github.com/opengeospatial/teamengine/issues/354): Add ETS form validation
- [#487](https://github.com/opengeospatial/teamengine/issues/487): NullPointerException when running from command line
- [#414](https://github.com/opengeospatial/teamengine/issues/414): Remove Java code failure semantics
- [#428](https://github.com/opengeospatial/teamengine/pull/428): Add support for headers when using GET requests
- [#495](https://github.com/opengeospatial/teamengine/pull/495): Bump commons-io and xercesImpl versions
- [#494](https://github.com/opengeospatial/teamengine/pull/494): Move depencency versions
- [#449](https://github.com/opengeospatial/teamengine/issues/449): Re-executing session test sometimes does not update results
- [#448](https://github.com/opengeospatial/teamengine/issues/448): Null Pointer Exceptions 500 Server Errors
- [#465](https://github.com/opengeospatial/teamengine/issues/465): Fail to generate report because of invalid character
- [#422](https://github.com/opengeospatial/teamengine/pull/422): [SECURITY] Use HTTPS to resolve dependencies in Maven Build
- [#423](https://github.com/opengeospatial/teamengine/issues/423): Official Website of Teamengine error!
- [#457](https://github.com/opengeospatial/teamengine/issues/457): Restrict access to TestNG reports
- [#450](https://github.com/opengeospatial/teamengine/issues/450): The test Entry Point / Sign In page is very confusing
- [#460](https://github.com/opengeospatial/teamengine/issues/460): Teamengine new HTML report is generating the details html pages multiple time
- [#469](https://github.com/opengeospatial/teamengine/issues/469): Incorrect response on beta instance TestSuites list
- [#464](https://github.com/opengeospatial/teamengine/pull/464): Bump guava from 26.0-jre to 29.0-jre in /teamengine-core
- [#426](https://github.com/opengeospatial/teamengine/issues/426): Documentation refers to Oracle 8 JDK that is not available
- [#394](https://github.com/opengeospatial/teamengine/issues/394): Replace OSGEO logo
- [#440](https://github.com/opengeospatial/teamengine/issues/440): Cleanup dependencies
- [#463](https://github.com/opengeospatial/teamengine/pull/463): Added header for soapui test.

## 5.4.1 (2021-02-19)
- [#447](https://github.com/opengeospatial/teamengine/issues/447): TestSuite version on API
- [#397](https://github.com/opengeospatial/teamengine/issues/397): Enhance REST API by a technical response with information about the ets
- [#435](https://github.com/opengeospatial/teamengine/issues/435): Test INPUT in HTML report displays wrong information
- [#451](https://github.com/opengeospatial/teamengine/issues/451): Report visibility issue
- [#416](https://github.com/opengeospatial/teamengine/issues/416): Unable to build behind corporate proxy
- [#431](https://github.com/opengeospatial/teamengine/pull/431): Bump xercesImpl from 2.11.0 to 2.12.0 in /teamengine-core
- [#437](https://github.com/opengeospatial/teamengine/pull/437): Bump junit from 4.12 to 4.13.1
- [#456](https://github.com/opengeospatial/teamengine/pull/456): Fix mailing list links
- [#400](https://github.com/opengeospatial/teamengine/issues/400): Update tests to use TEAM Engine 5.4

## 5.4 (2019-05-23)
- [#395](https://github.com/opengeospatial/teamengine/issues/395): Move SoapUI tests to sub module
- [#385](https://github.com/opengeospatial/teamengine/issues/385): Failed to load ets-wms-client13 ctl form
- [#357](https://github.com/opengeospatial/teamengine/issues/357): WCS test is slow due to repeated schema loading
- [#261](https://github.com/opengeospatial/teamengine/issues/261): REST API: Create path for latest version of each test suite
- [#370](https://github.com/opengeospatial/teamengine/issues/370): Enhance mandatory informations when registering to TEAM Engine
- [#398](https://github.com/opengeospatial/teamengine/issues/398): Fix fallback of writting the end of the log by writting the conformanceClass element
- [#331](https://github.com/opengeospatial/teamengine/issues/331): New HTML report of CTL test suites: Inherited failures are not marked but counted as usual failures
- [#364](https://github.com/opengeospatial/teamengine/issues/364): Change login time interval
- [#323](https://github.com/opengeospatial/teamengine/issues/323): Provide documentation how to get information about if the test can get certified
- [#358](https://github.com/opengeospatial/teamengine/issues/358): TEAM Engine source code has mixed line endings
- [#359](https://github.com/opengeospatial/teamengine/issues/359): Root cause exception is not logged when XMLValidatingParser fails
- [#365](https://github.com/opengeospatial/teamengine/issues/365): Save images of interactive tests to session folder of user
- [#367](https://github.com/opengeospatial/teamengine/issues/367): Link to JavaDoc is broken in HTML report in a SNAPSHOT version
- [#348](https://github.com/opengeospatial/teamengine/issues/348): Teamengine failed to generate new HTML report if the testInputs are empty.
- [#362](https://github.com/opengeospatial/teamengine/issues/362): Update resource accessing method of XMLValidationParser as URL instead of File
- [#356](https://github.com/opengeospatial/teamengine/pull/356): Clarify that the "deps" zip may not exist
- [#352](https://github.com/opengeospatial/teamengine/pull/352): fix: teamengine-web/pom.xml to reduce potential vulnerabilities (#1)
- [#344](https://github.com/opengeospatial/teamengine/issues/344): The wms-client13 test shows the one test failure in table even all the tests are passed.
- [#350](https://github.com/opengeospatial/teamengine/issues/350): New HTML report: Homogenize layout

## 5.3.1 (2019-01-14)
- Fix [#381](https://github.com/opengeospatial/teamengine/issues/381): Ensure correct comparison of session dates

## 5.3 (2018-05-15)
- Fix [#336](https://github.com/opengeospatial/teamengine/issues/336): Improve header of new HTML report: Test INPUT contains confusing informations
- Fix [#315](https://github.com/opengeospatial/teamengine/issues/315): Circular dependencies
- Fix [#337](https://github.com/opengeospatial/teamengine/issues/337): Improve header of new HTML report: Mark passed, failed and skipped conformance classes
- Fix [#340](https://github.com/opengeospatial/teamengine/issues/340): New HTML report: empty count of the failed and total test.
- Fix [#338](https://github.com/opengeospatial/teamengine/issues/338): Improve header of new HTML report: Improve structure of header
- Fix [#333](https://github.com/opengeospatial/teamengine/issues/333): Teamengine: The result of GetMapRequest is marked as failed in wms-client13
- Fix [#326](https://github.com/opengeospatial/teamengine/issues/326): Change URLs used in EARL Report
- Fix [#319](https://github.com/opengeospatial/teamengine/issues/319): Implement display of hierarchies in new HTML report of TEAM Engine
- Fix [#320](https://github.com/opengeospatial/teamengine/issues/320): Improve error handling, logging and reporting regarding HTTPS
- Fix [#293](https://github.com/opengeospatial/teamengine/issues/293): Fortify Issue - XML External Entity Injection
- Merge [#325](https://github.com/opengeospatial/teamengine/pull/325): Log Download Bug: remove leading slash from .zip files

## 5.2 (2018-02-26)
- Fix [#298](https://github.com/opengeospatial/teamengine/issues/298): Fortify Issue: Unreleased Resource
- Fix [#286](https://github.com/opengeospatial/teamengine/issues/286): HTML 5.0 Report: Add method and class of the failing test
- Fix [#287](https://github.com/opengeospatial/teamengine/issues/287): Add cite:testSuiteType earl property to identify the test-suite is implemented using ctl or testng.
- Fix [#308](https://github.com/opengeospatial/teamengine/issues/308): Status color of tests in left frame
- Fix [#284](https://github.com/opengeospatial/teamengine/issues/284): Enhance TEAM Engine to evaluate if core conformance classes are configured
- Fix [#179](https://github.com/opengeospatial/teamengine/issues/179): Clean header of source code files
- Fix [#300](https://github.com/opengeospatial/teamengine/issues/300): Fortify Issue: Null Dereference
- Fix [#304](https://github.com/opengeospatial/teamengine/issues/304): Result view (tree) is missing of wms-client test
- Fix [#276](https://github.com/opengeospatial/teamengine/issues/276): Enhance impementation of SOAP request to be able to handle elements in CDATA
- Fix [#280](https://github.com/opengeospatial/teamengine/issues/280): Improve report text for core conformance classes
- Fix [#278](https://github.com/opengeospatial/teamengine/issues/278): Detailed test messages with XML special characters are incomplete
- Fix [#277](https://github.com/opengeospatial/teamengine/issues/277): Incomplete test result (new html report) with ETS NSG WMTS 1.0
- Fix [#265](https://github.com/opengeospatial/teamengine/issues/265): Warning in viewlog.xsl

## 5.1 (2017-11-30)
- Fix [#272](https://github.com/opengeospatial/teamengine/issues/272): Failing tests do not report performed requests
- Fix [#273](https://github.com/opengeospatial/teamengine/issues/273): HTML report: Report has additional empty column if test is skipped
- Fix [#263](https://github.com/opengeospatial/teamengine/issues/263): NPE during execution of WFS 1.1 test suite
- Fix [#215](https://github.com/opengeospatial/teamengine/issues/215): View session is not sorted correctly
- Fix [#268](https://github.com/opengeospatial/teamengine/issues/268): messages of skipped tests are empty in new HTML report
- Fix [#266](https://github.com/opengeospatial/teamengine/issues/266): Colors in detailed test report are confusing or not correct
- Fix [#209](https://github.com/opengeospatial/teamengine/issues/209): View Session delete icons are not align
- Fix [#237](https://github.com/opengeospatial/teamengine/issues/237): REST API: Improve response if a test run fails
- Fix [#235](https://github.com/opengeospatial/teamengine/issues/235): REST API: Set all required request and response HTTP header
- Fix [#257](https://github.com/opengeospatial/teamengine/issues/257): HTML report via Web Browser Interface: Buttons for old test report and session list navigate to wrong pages in some cases
- Fix [#251](https://github.com/opengeospatial/teamengine/issues/251): Detailed old test report of TestNG test suites is erroneous
- Fix [#242](https://github.com/opengeospatial/teamengine/issues/242): REST API: Setting of multiple content types in request header leads to unexpected behaviour

## 5.0 (2017-10-10)
- Fix [#252](https://github.com/opengeospatial/teamengine/issues/252): HTML report is erroneous on beta environment
- Merge [#250](https://github.com/opengeospatial/teamengine/pull/250): Rework documentation of profiles
- Fix [#248](https://github.com/opengeospatial/teamengine/issues/248): HTML report: Fix 'Passed core (Can be certified)' result
- [core] Fix [#212](https://github.com/opengeospatial/teamengine/issues/212): Cannot write more than one result document to the same URI.
- [core] Fix [#245](https://github.com/opengeospatial/teamengine/issues/245): Failed to create testdetail html page of particular test
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

