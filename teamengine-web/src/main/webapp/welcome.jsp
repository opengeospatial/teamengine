<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="teConfigFile" value="${initParam.teConfigFile}" />
<c:set var="backSlash" value="\\u005C" />
<c:set var="configFilePath"
	value="${f:replace(teConfigFile, backSlash, '/')}" />

<div>
	<p>
		The Test, Evaluation, And Measurement (TEAM) Engine is a test harness
		that executes test suites written using the OGC CTL test grammar or
		the <a href="http://testng.org/" target="_blank">TestNG framework</a>.
		It is typically used to verify specification compliance.
	</p>

	<h3>Available test suites</h3>
	<c:import var="xslt" url="/styles/config2table.xsl" />
	<c:import var="configFile" url="file:///${configFilePath}" />
	<x:transform doc="${configFile}" xslt="${xslt}" />
</div>
