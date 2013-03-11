<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="teConfigFile" value="${initParam.teConfigFile}" />
<c:set var="backSlash" value="\\u005C" />
<c:set var="configFilePath"
	value="${f:replace(teConfigFile, backSlash, '/')}" />

<div>
  <p>This web testing facility provides a testing service for OGC standards as 
  part of the <a href="http://www.opengeospatial.org/compliance" target="_blank">OGC 
  Compliance Program</a>. This is the beta installation that provides the latest 
  test suites, including those that have not yet been finalized. The official testing 
  site is <a href="http://cite.opengeospatial.org/teamengine/" target="_blank">here</a>.
  </p>

  <p>If you have any questions, issues, or great ideas please raise them at the 
  <a href="http://cite.opengeospatial.org/forum" target="_blank">CITE forum</a>, 
  where experts and enthusiasts will join the discussion and provide help. You can 
  find more information about the tests and this testing facility at the 
  <a href="http://cite.opengeospatial.org/" target="_blank">main CITE website</a>.
  If you are thinking about providing a <a href="http://cite.opengeospatial.org/reference" 
  target="_blank">reference implementation</a> please email us at &lt;<code>compliance 
  at opengeospatial.org</code>&gt;.
  </p>

	<h3>Available test suites</h3>
	<c:import var="xslt" url="/styles/config2table.xsl" />
	<c:import var="configFile" url="file:///${configFilePath}" />
	<x:transform doc="${configFile}" xslt="${xslt}" />
</div>
