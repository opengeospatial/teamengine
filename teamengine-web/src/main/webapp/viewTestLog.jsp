<%@ page
 language="java"
 session="false"
 import="javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, javax.xml.transform.stream.*, java.io.File, java.util.*, com.occamlab.te.*, com.occamlab.te.util.Misc, com.occamlab.te.web.*, net.sf.saxon.dom.DocumentBuilderImpl, net.sf.saxon.FeatureKeys, net.sf.saxon.Configuration"
%><%!
Config Conf;
DocumentBuilderImpl DB;
Templates ViewLogTemplates;
String sessionId;

public void jspInit() {
	try {
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
		ViewLogTemplates = ViewLog.transformerFactory.newTemplates(new StreamSource(stylesheet));
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<title>Test Log</title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Log for test <%=request.getParameter("test")%></h2>
<%
      File userlog = new File(Conf.getUsersDir(), request.getRemoteUser());
      String test = request.getParameter("test");
      String testName=null;
      int i = test.indexOf("/");
      sessionId = (i > 0) ? test.substring(0, i) : test;
      ArrayList tests = new ArrayList();
      tests.add(test);
      boolean complete = ViewLog.view_log(testName,userlog, sessionId, tests, ViewLogTemplates, out);
      out.println("<br/>");
      if (!complete) {
        out.println("<input type=\"button\" value=\"Resume executing this session\" onclick=\"window.location = 'test.jsp?mode=resume&amp;session=" + sessionId + "'\"/>");
      }
%>
		<br/>
		<input type="button" value="Execute this test again" onclick="window.location = 'test.jsp?mode=retest&amp;test=<%=request.getParameter("test")%>'"/>
		<br/>
		<br/>
		<a href="viewSessionLog.jsp?session=<%=sessionId%>">Complete session results</a>
		<br/>
		<a href="viewSessions.jsp"/>Sessions list</a>
		<%@ include file="footer.jsp" %>
	</body>
</html>
