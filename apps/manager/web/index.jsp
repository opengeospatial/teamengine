<%@ page
 language="java"
 session="false"
 import="com.occamlab.te.index.*, com.occamlab.te.web.*"
%><%!
Config conf;

public void jspInit() {
	try {
		conf = new Config();
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
}
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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
		<title>Welcome</title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Welcome</h2>
		<p>
			The Test, Evaluation, And Measurement (TEAM) Engine is a test script interpreter.
			It executes test scripts written using the 
			<a href="docs/ctl/ctl.html">Compliance Test Language (CTL)</a>
			to verify that an implementation of a specification complies with the specification.
		</p>
		<p>
			<span>The following test suites are available:</span>
			<ul>
<%
  for (SuiteEntry suite : conf.getSuites().values()) {
	out.print("<li>");
	String link = suite.getLink();
	if (link == null) {
		out.print(suite.getTitle());
	} else {
		out.print("<a href=\"" + link + "\">" + suite.getTitle() + "</a>");
	}
	out.println("<br/>");
	String desc = suite.getDescription();
	if (desc != null) {
		out.println(desc);
	}
    out.println("<br/>");
    if (null != suite.getDataLink()) {
		out.print("<a href=\"" + suite.getDataLink() + "\">" + "Test data</a>");
	}
  }
%>
			</ul>
		</p>
        <p>
        <img alt="WARNING!" src="images/warn.png" align="bottom" hspace="4" />
        It may be necessary to load test data before running a test suite!
        </p>
		<a href="viewSessions.jsp">Start Testing</a>
		<%@ include file="footer.jsp" %>
	</body>
</html>
