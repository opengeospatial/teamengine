<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="teConfigFile" value="${initParam.teConfigFile}" />
<c:set var="backSlash" value="\\u005C" />
<c:set var="configFilePath"
	value="${f:replace(teConfigFile, backSlash, '/')}" />

<div>
	<p>The Test, Evaluation, And Measurement (TEAM) Engine is a test
		script interpreter. It executes test scripts written using Compliance
		Test Language (CTL) tags to verify that an implementation of a
		specification complies with the relevant specification(s).</p>

	<h3>Available test suites</h3>
	<c:import var="xslt" url="/styles/config2table.xsl" />
	<c:import var="configFile" url="file:///${configFilePath}" />
	<x:transform doc="${configFile}" xslt="${xslt}" />
</div>
