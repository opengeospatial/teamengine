<%@page import="java.util.Collection"%>
<%@ page
  language="java"
  session="false"
  import="java.io.File, javax.xml.parsers.*, java.util.Arrays, com.occamlab.te.web.*, java.util.List, java.util.ArrayList"
%><%!
  Config Conf;
  DocumentBuilder DB;
  List<TestSession> testData;

  public void jspInit() {
    Conf = new Config();
    try {
      DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <title>Test Sessions</title>
	  <style>
		   .session-table td {
		   border: 1px solid black;
		   height: 30px;
		   }
		   .session-table th {
		   border: 1px solid black;
		   height: 30px;
		   }
		   td.trash-noBorder {
		   border: none;
		   }
		   .session-table {
		   border-collapse: collapse;
		   }
	   </style>
      <script>
        function deleteSession(sessionID) {
          var sessionid = sessionID;
          if (sessionid != "") {
            if (confirm("Are you sure you want to delete session " + sessionid + " ?")) {
              window.location = "deleteSession?session=" + sessionid
            }
          } else {
            alert("Please select session ID.");
          }
        }
      </script>
    </head>
    <body>
      <%@ include file="header.jsp" %>
      <h2>Test Sessions</h2>
      
      <br />
      <a href="createSession.jsp">Create a new session</a>
      <br />
	  <br />
	  
      <p>
        <table border="0"><tr><td><table class="session-table">
                <%  File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
                  String[] dirs = userdir.list();
                  Arrays.sort(dirs);
                  testData = new ArrayList<TestSession>();
                  

                  out.println("<tr style='height:22px;'>");
                  out.println("<td>" + "<b>Session</b>" + "</td>");
                  out.println("<td>" + "<b>Test suite name</b>" + "</td>");
                  out.println("<td>" + "<b>Version</b>" + "</td>");
                  out.println("<td>" + "<b>Test Suite run time</b>" + "</td>");
                  out.println("<td>" + "<b>Description</b>" + "</td>");
                  out.println("</tr>");
                  
                  for (int i = 0; i < dirs.length; i++) {
                    if (new File(new File(userdir, dirs[i]), "session.xml").exists()) {
                      TestSession s = new TestSession();
                      s.load(userdir, dirs[i]);
                      testData.add(s);
                    }
                  }
                  TestSession ts = new TestSession();
                  testData = ts.getSortedMap(testData);
                  for (TestSession testSession : testData) {
                	  out.println("<tr style='height:23px;'>");
                      out.println("<td><a href=\"viewSessionLog.jsp?session=" + testSession.getSessionId() + "\">" + testSession.getSessionId() + "</a></td>");
                      out.println("<td>" + testSession.getSourcesName().split("_")[1] + "</td>");
                      out.println("<td>" + testSession.getSourcesName().split("_")[2] + "</td>");
                      out.println("<td>" + testSession.getCurrentDate() + "</td>");
                      out.println("<td>" + testSession.getDescription() + "</td>");
                      out.println("<td class='trash-noBorder' ><img src='images/trash.png' style='height:17px;' id='" + testSession.getSessionId() + "' onclick='deleteSession(this.id)'/></td>");
                      out.println("</tr>");
              		}
                %>
              </table>
            </td>
            <td>
              <table border="0">
                <% // out.println("<tr style='height:22px;'>");
                 /* out.println("<td></td>");
                  out.println("</tr>");

                  for (int i = 0; i < dirs.length; i++) {
                    if (new File(new File(userdir, dirs[i]), "session.xml").exists()) {
                      TestSession s = new TestSession();
                      s.load(userdir, dirs[i]);
                      out.println("<tr>");
                      out.println("<td><img src='images/trash.png' style='height:17px;' id='" + s.getSessionId() + "' onclick='deleteSession(this.id)'/></td>");
                      out.println("</tr>");
                    }
                  }
                  */
                %>
              </table>
            </td>
          </tr>
        </table>
        <br/>
      </p>
      <%@ include file="footer.jsp" %>
    </body>
  </html>
