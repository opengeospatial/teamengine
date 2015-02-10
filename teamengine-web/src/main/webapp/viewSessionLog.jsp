<%@ page
 language="java"
 session="false"
 import="javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, javax.xml.transform.stream.*, java.io.Writer, java.io.File, java.util.*, com.occamlab.te.*, com.occamlab.te.index.*, com.occamlab.te.util.Misc, com.occamlab.te.web.*, net.sf.saxon.dom.DocumentBuilderImpl, net.sf.saxon.FeatureKeys, net.sf.saxon.Configuration"
%><%!
Config Conf;
DocumentBuilderImpl DB;
Templates ViewLogTemplates;

public void jspInit() {
	try {
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
		ViewLogTemplates = ViewLog.transformerFactory.newTemplates(new StreamSource(stylesheet));
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
<%@page import="java.net.URLEncoder"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>			
		<title>Test Session Results</title>
		<script>
			function toggleAll(testnum, state) {
			  for (key in document.images) {
			    var image = document.images[key];
                            if (image.name) {
			      if (image.name.substring(0, 5 + testnum.length) == "image" + testnum) {
			        image.src = "images/" + (state ? "minus.png" : "plus.png");
                                var div = document.getElementById('test' + image.name.substring(5));
			        div.style.display = state ? "block" : "none";
			      }
			    }
			  }
			}

			function toggle(testnum, e) {
			  var evt = e || window.event;
			  if (document.images['image' + testnum].src.indexOf('minus') > 0) {
			    if (evt.ctrlKey) {
			      toggleAll(testnum, false);
			    } else {
			      document.images['image' + testnum].src = 'images/plus.png';
			      document.getElementById('test' + testnum).style.display = "none";
			    }
			  } else {
			    if (evt.ctrlKey) {
			      toggleAll(testnum, true);
			    } else {
			      document.images['image' + testnum].src = 'images/minus.png';
			      document.getElementById('test' + testnum).style.display = "block";
			    }
			  }
			}


			function deleteSession() {
				if (confirm("Are you sure you want to delete session <%=request.getParameter("session")%>?")) {
					window.location = "deleteSession?session=<%=request.getParameter("session")%>"
				}
			}
		</script>		
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Results for session <%=request.getParameter("session")%></h2>
<%
      File userlog = new File(Conf.getUsersDir(), request.getRemoteUser());
      String sessionId = request.getParameter("session");
      TestSession ts = new TestSession();
      ts.load(userlog, sessionId);
      String suiteName = "null";
      String sourcesName = ts.getSourcesName();
      if (sourcesName == null) {
          suiteName = "error: sourcesName is null";
      } else {
          SuiteEntry se = Conf.getSuites().get(sourcesName);
          if (se == null) {
             suiteName = "error: suitEntry is null";
          } else {
              String title = se.getTitle();
              suiteName = (title == null) ? "error: suiteEntry title is null" : title;
          }
      }
      out.println("<h3>Test Suite: " + suiteName + "</h3>");
      ArrayList tests = new ArrayList();
      boolean complete = ViewLog.view_log(suiteName,userlog, sessionId, tests, ViewLogTemplates, out);     
      out.println("<br/>");
      if (!complete) {
          out.println("<input type=\"button\" value=\"Resume executing these tests\" onclick=\"window.location = 'test.jsp?mode=resume&amp;session=" + sessionId + "'\"/>");
      }

      String profileParams = "";
      if (complete) {
        int i = 0;
	      for (ProfileEntry profile : Conf.getProfiles().get(ts.getSourcesName())) {
	          out.println("<h3>Profile: " + profile.getTitle() + "</h3>");
	          if (ts.getProfiles().contains(profile.getId())) {
	        	  String path = sessionId + "/" + profile.getLocalName();
              complete = ViewLog.view_log(suiteName,userlog, path, tests, ViewLogTemplates, out, (i+2));
				      out.println("<br/>");
              profileParams += "&amp;" + "profile_" + Integer.toString(i) + "=" + URLEncoder.encode(profile.getId(), "UTF-8");
              i++;
		      }
	      }
      }
%>

<%-- Insert link to TestNG report if it exists.  --%>
<%
File userLog = new File(Conf.getUsersDir(), request.getRemoteUser());
File htmlReportDir = new File(userLog, sessionId + System.getProperty("file.separator") + "html");
if ( htmlReportDir.isDirectory()) {
%>
    <p>
    See the <a href="<%=request.getContextPath()%>/reports/<%=request.getRemoteUser()%>/<%=sessionId%>/html/">detailed test report</a>.
 	</p>
<% } %>

<%--
      File userlog = new File(Conf.getUsersDir(), request.getRemoteUser());
      String sessionId = request.getParameter("session");
      ArrayList tests = new ArrayList();    
      boolean complete = ViewLog.view_log(userlog, sessionId, tests, ViewLogTemplates, out);     
      out.println("<br/>");
      if (!complete) {
        out.println("<input type=\"button\" value=\"Resume executing this session\" onclick=\"window.location = 'test.jsp?mode=resume&amp;session=" + sessionId + "'\"/>");
      }
--%>
		<br/>
		<input type="button" value="Execute this session again" onclick="window.location = 'test.jsp?mode=retest&amp;session=<%=request.getParameter("session")%><%=profileParams%>'"/>
<%
      boolean hasCache = ViewLog.hasCache();
      if (hasCache) {
          out.print(  "<input type=\"button\" value=\"Redo using cached values\" onclick=\"window.location = 'test.jsp?mode=cache&amp;session=" + sessionId + profileParams + "'\"/>");
      }
%>
		<input type="button" value="Delete this session" onclick="deleteSession()"/>
		<input type="button" value="Download log Files" onclick="window.location = 'downloadLog?session=<%=request.getParameter("session")%>'"/>
		<input type="button" value="Create execution log report file" onclick="window.location = 'prettyPrintLogs?session=<%=request.getParameter("session")%>'"/>
<%--		<input type="button" value="Email log Files" onclick="window.location = 'emailLog?session=<%=request.getParameter("session")%>'"/> --%>
		<br/>
    <p><a href="viewSessions.jsp">Sessions list</a></p>
		<%@ include file="footer.jsp" %>				
	</body>
</html>
