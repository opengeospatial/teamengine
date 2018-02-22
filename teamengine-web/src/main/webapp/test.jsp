<%@ page
    language="java"
    session="false"
%><%!
    String mode;
    String test;
    String sessionId;
    java.util.Map<String, String[]> paramMap;
    %><!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
      The Original Code is TEAM Engine.
    
      The Initial Developer of the Original Code is Northrop Grumman Corporation
      jointly with The National Technology Alliance.  Portions created by
      Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
      Grumman Corporation. All Rights Reserved.
    
      Contributor(s): No additional contributors to date
    
     +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
    <%@page import="java.net.URLEncoder"%>
    <html>
        <head>
            <title>Executing Tests</title>
            <script>
                var LONG_TIMEOUT = 4000;
                var SHORT_TIMEOUT = 1000;
                var timeout = LONG_TIMEOUT;
                var timerId = 0;
                var form;
                var xhr;
                var sessionId;
                var threadId;
                var console;

                function start() {
                    if (window.XMLHttpRequest) {
                        xhr = new XMLHttpRequest();
                    } else {
                        xhr = new ActiveXObject("Microsoft.XMLHTTP");
                    }

                    var d = new Date();
                <%
mode = request.getParameter("mode");
test = request.getParameter("test");
sessionId = request.getParameter("session");
paramMap = request.getParameterMap();
String params = "mode=" + mode;
if (mode.equals("retest") || mode.equals("resume") || mode.equals("cache")) {
    if (test != null) {
        params += "&test=" + test;
    }
    if (sessionId != null) {
        params += "&session=" + sessionId;
    }
} else {
    params += "&sources=" + request.getParameter("sources");
    params += "&suite=" + request.getParameter("suite");
    params += "&description=" + URLEncoder.encode(request.getParameter("description"), "UTF-8");
}
if (mode.equals("test") || mode.equals("retest") || mode.equals("cache")) {
  for (String key: new java.util.TreeSet<String>(paramMap.keySet())) {
        if (key.startsWith("profile_")) {
            String values[] = paramMap.get(key);
            String profile = values[0];
            if (profile != null) {
                params += "&" + key + "=" + URLEncoder.encode(profile, "UTF-8");
            }
        }
    }
}
                %>
                    var url = "test?te-operation=Test&<%=params%>&t=" + d.getTime();
                    xhr.open("get", url, false);
                    xhr.send(null);
                    var xml = xhr.responseXML;
                    var threadNodes = xml.getElementsByTagName("thread");
                    if (threadNodes.length != 1) {
                        alert("Error " + url + " did not return a thread element");
                        return;
                    }
                    var thread = threadNodes[0];
                    threadId = thread.getAttribute("id");
                    sessionId = thread.getAttribute("sessionId");
                    document.cookie="Sesion_ID="+sessionId;
                    console = window.open("console.html?t=" + d.getTime(), "te_console", "height=500,width=700,resizable=yes,scrollbars=yes");
                    if (console) {
                        console.focus();
                    }
                    timerId = setTimeout("update()", timeout);
                }

                function stop() {
                    if (timerId != 0) {
                        clearTimeout(timerId);
                        timerId = 0;
                    }
                    var d = new Date();
                    var url = "test?te-operation=Stop&thread=" + threadId + "&t=" + d.getTime();
                    xhr.open("get", url, false);
                    xhr.send(null);
                    var stop="true";
                    loadLog(stop);
                }

                function formSubmitted() {
                    window.te_test_panel.location = "executing.html";
                    timeout = LONG_TIMEOUT;
                    update();
                }

                function update() {
                    if (timerId != 0) {
                        clearTimeout(timerId);
                        timerId = 0;
                    }
                    var d = new Date();
                    var url = "test?te-operation=GetStatus&thread=" + threadId + "&t=" + d.getTime();
                    xhr.open("get", url, false);
                    xhr.send(null);
                    var xml = xhr.responseXML;
                    var statusNodes = xml.getElementsByTagName("status");
                    if (statusNodes.length != 1) {
                        alert("Error " + url + " did not return a status element");
                        return;
                    }
                    var status = statusNodes[0];
                    var node = status.firstChild;
                    var s = "";
				while(node) {
                        if (node.nodeType == 4) {
                            s += node.nodeValue;
                        }
                        node = node.nextSibling;
                    }
                    if (console && console.write && s != "") {
                        console.write(decodeURIComponent(s).replace(/\n\r?/g, '\n\r'));
                    }
                    var form = status.getAttribute("form");
                    if (form) {
                        url = "test?te-operation=GetForm&thread=" + threadId + "&t=" + d.getTime();
                        window.te_test_panel.location = url;
                        window.te_test_panel.focus();
                        timeout = LONG_TIMEOUT;
                    }
                    var complete = status.getAttribute("complete");
                    if (complete) {
                        loadLog();
                    } else {
					timerId  = setTimeout("update()", timeout);
                    }
                }

                function loadLog(stop) {
                    var d = new Date();
                <%
if (mode.equals("retest") || mode.equals("resume") || mode.equals("cache")) {
    if (test == null) {
        out.println("\t\t\t\tvar url = \"viewSessionLog.jsp?session=" + sessionId + "\";");
    } else {
        out.println("\t\t\t\tvar url = \"viewTestLog.jsp?test=" + test + "\";");
    }
} else {
    out.println("\t\t\t\tvar url = \"viewSessionLog.jsp?session=\" + sessionId;");
}
                %>
                    window.location = url + "&stop=" + stop + "&t=" + d.getTime();
                }
            </script>
        </head>
	<frameset onload="start()" rows="102,*">
            <frame name="te_test_controls" src="testControls.html">
                <frame name="te_test_panel" src="executing.html">
                        </frameset>
                        </html>
