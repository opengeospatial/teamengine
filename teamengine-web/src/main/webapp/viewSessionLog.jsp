<%@ page
 language="java"
 session="false"
 import="javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, java.io.BufferedReader, javax.xml.transform.stream.*, java.io.Writer, java.io.File, java.util.*, com.occamlab.te.*, com.occamlab.te.index.*, com.occamlab.te.util.Misc, com.occamlab.te.web.*, net.sf.saxon.dom.DocumentBuilderImpl, net.sf.saxon.FeatureKeys, net.sf.saxon.Configuration"
%><%!
Config Conf;
TECore core;
DocumentBuilderImpl DB;
Templates ViewLogTemplates;

public void jspInit() {
	try {
		core = new TECore();
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
		ViewLogTemplates = ViewLog.transformerFactory.newTemplates(new StreamSource(stylesheet));
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
}
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

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
		<script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>			
		<title>Test Session Results</title>
		<script>
		var checkStopSession="<%=request.getParameter("stop")%>";
		if(checkStopSession == "true"){
			alert("Session was stopped!!!");
		}
		
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
			
			$( document ).ready(function() {
				$("a.testDetailsLink").each(function() {
					   var $this = $(this);       
					   var _href = $this.attr("href"); 
					   $this.attr("href", "<%=request.getContextPath()%>"+ "/reports/" + "<%=request.getRemoteUser()%>" + "/" + "<%=request.getParameter("session")%>" +"/result/"+ _href);
					});
			});
			
		    $( document ).ready(function() {
		    	console.log( "document loaded" );
		    	if(document.getElementById("sessionIdHtml")) {
		    		$('#sessionIdHtml').append('<%=request.getParameter("session")%>');
		    		$('#sessionId').hide();
		    		} 
		    	if(document.getElementById("oldResultUrl")) {
		    		$('#oldResultUrl').attr('href', '<%=request.getContextPath()%>/viewOldSessionLog.jsp?session=<%=request.getParameter("session")%>');
		    		$('#oldResultUrlPara').hide();
		    		} 
		    });
			
		</script>		
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<span id ="sessionId"><h2>Results for session <%=request.getParameter("session")%></h2></span>
<%
      File userlog = new File(Conf.getUsersDir(), request.getRemoteUser());
      String sessionId = request.getParameter("session");
      TestSession ts = new TestSession();
      ts.load(userlog, sessionId);
      String suiteName = "null";
      String sourcesName = ts.getSourcesName();
      SuiteEntry suiteEntry = null;
      String sourceIdKey = "";
      Map<String, List<ProfileEntry>> profileInfos;
      for(String key : Conf.getSuites().keySet()) {  	  
	      String onlySourceName = sourcesName.substring(0,sourcesName.lastIndexOf("_"));
    	  if(key.contains(onlySourceName)){
    		  sourceIdKey = key;
    		 suiteEntry = Conf.getSuites().get(key);
    	  }
      }
      if (sourcesName == null) {
          suiteName = "error: sourcesName is null";
      } else {
          SuiteEntry se = Conf.getSuites().get(sourcesName);
          if (se == null) {
        	  if(suiteEntry != null){
        		  String title = suiteEntry.getTitle();
                  suiteName = (title == null) ? "error: suiteEntry title is null" : title;
        	  } else {
             		suiteName = "error: suitEntry is null";
        	  }
          } else {
              String title = se.getTitle();
              suiteName = (title == null) ? "error: suiteEntry title is null" : title;
          }
      }
     /*  out.println("<h3>Test Suite: " + suiteName + "</h3>"); */
      ArrayList tests = new ArrayList();
      boolean complete = ViewLog.view_log(suiteName,userlog, sessionId, tests, ViewLogTemplates, out);     
      /* out.println("<br/>"); */
      if (!complete) {
          out.println("<input type=\"button\" value=\"Continue executing this session\" onclick=\"window.location = 'test.jsp?mode=resume&amp;session=" + sessionId + "'\"/>");
      }

      String profileParams = "";
      String sourceId = "";
      if (complete) {
        int i = 0;
        
        if(Conf.getProfiles().get(ts.getSourcesName()) == null){
        	sourceId = sourceIdKey;
        } else {
        	sourceId = ts.getSourcesName();
        }
	      for (ProfileEntry profile : Conf.getProfiles().get(sourceId)) {
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
String resultdir = userLog.toString() + System.getProperty("file.separator") + sessionId;
File resDir = new File(resultdir + System.getProperty("file.separator") + "testng");
File earlHtml = null;
if(!resDir.exists()){
	resDir = new File(userLog.toString() + System.getProperty("file.separator") + sessionId );
}
if(resDir.exists()){
core.earlHtmlReport(resultdir.toString());
earlHtml = new  File(resultdir + System.getProperty("file.separator") + "result" + System.getProperty("file.separator") + "index.html");

}
/* if ( htmlReportDir.isDirectory()) { */
%>
<%
String testurl;
if(earlHtml.exists()){
testurl = "reports/" + request.getRemoteUser()+ "/" + request.getParameter("session") + "/result/index.html";
} else {
	//This page will be displayed if test-suite is not supporting the new html report.
	testurl = "/infoMessage.html";
}
%>

<jsp:include page="<%= testurl %>"></jsp:include>

    <%-- <p>
    See the <a href="<%=request.getContextPath()%>/reports/<%=request.getRemoteUser()%>/<%=sessionId%>/html/">detailed test report</a>.
 	</p> --%>
<% /* } */ %>

<% /* if ( earlHtml.isFile()) {  */%>
	<%-- <p>
    See the <a href="<%=request.getContextPath()%>/reports/<%=request.getRemoteUser()%>/<%=sessionId%>/testng/output.html">EARL Report</a>.
 	</p> --%>
<% /* } */ %>

 <p id="oldResultUrlPara">
    See the <a id="oldResultUrlPara" href="<%=request.getContextPath()%>/viewOldSessionLog.jsp?session=<%=sessionId%>">detailed old test report</a>.
 </p> 

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
<!-- 		<input type="button" value="Create execution log report file" onclick="window.location = 'prettyPrintLogs?session=<%=request.getParameter("session")%>'"/>  -->
<%--		<input type="button" value="Email log Files" onclick="window.location = 'emailLog?session=<%=request.getParameter("session")%>'"/> --%>
		<br/>
    <p><a href="viewSessions.jsp">Sessions list</a></p>
		<%@ include file="footer.jsp" %>				
	</body>
</html>
