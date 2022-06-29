<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="backSlash" value="\\u005C" />
<c:set var="configFilePath"
	value="${f:replace(teConfigFile, backSlash, '/')}" />

<div>
  <%@include file="site/welcome.html" %>
 
  
		<%
		    if (user != null && user.length() > 0) {
		    	out.println("<a href=\"viewSessions.jsp\">View sessions</a>");
		    	out.println("<br />");
		    }
		%>
 
  <section id="noColumn">
	       <h1>Available Test Suites</h1>
	       <c:import var="xslt" url="/styles/config2table.xsl" />
	       <c:import var="configFile" url="file:///${configFilePath}" />
	       <x:transform doc="${configFile}" xslt="${xslt}" />
  </section>
    
</div>