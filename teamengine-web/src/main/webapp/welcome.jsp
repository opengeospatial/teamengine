<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="teConfigFile" value="${initParam.teConfigFile}" />
<c:set var="backSlash" value="\\u005C" />
<c:set var="configFilePath"
	value="${f:replace(teConfigFile, backSlash, '/')}" />

<div>
  <%@include file="site/welcome.html" %>

	<h3>Available test suites</h3>
	<c:import var="xslt" url="/styles/config2table.xsl" />
	<c:import var="configFile" url="file:///${configFilePath}" />
	<x:transform doc="${configFile}" xslt="${xslt}" />
</div>
