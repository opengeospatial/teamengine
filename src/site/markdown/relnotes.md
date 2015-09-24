Release Notes
=============

## 4.3 (2015-MM-DD)
- [core] Merge [PR #86](https://github.com/opengeospatial/teamengine/pull/86): Add 
  XmlValidatingParser.validateSingleResult
- [web] Merge [PR #126](https://github.com/opengeospatial/teamengine/pull/126) to fix 
  [#124](https://github.com/opengeospatial/teamengine/issues/124): Unit test 
  `VerifyTestSuite.executeStartingTest` fails in Windows environment


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

