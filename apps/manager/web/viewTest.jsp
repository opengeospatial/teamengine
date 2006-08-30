<%@ page
 language="java"
 session="false"
 import="javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.stream.*, java.io.File, com.occamlab.te.Test"
%><%!
Templates ViewTestTemplates;

public void jspInit() {
	try {
		File stylesheet = Test.getResourceAsFile("com/occamlab/te/web/viewtest.xsl");
		ViewTestTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(stylesheet));
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
		<title>Test <%=request.getParameter("name")%></title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
<%
      File file = new File(request.getParameter("file"));
      Transformer t = ViewTestTemplates.newTransformer();
      t.setParameter("namespace-uri", request.getParameter("namespace"));
      t.setParameter("local-name", request.getParameter("name"));
      t.transform(new StreamSource(file), new StreamResult(out));
%>
		<%@ include file="footer.jsp" %>
	</body>
</html>
