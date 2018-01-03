<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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
		<title>mail Log Files</title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Email Log Files</h2>
		
		<form action="emailLog" method="post">
			<table cellpadding="3" cellspacing="0" border="0" style="padding-left:20px;" width="90%">
				<tr>
					<td width="10%">To:</td>
					<td><input type=text name=to size=40 value=<%=request.getAttribute("to") %>></td>
				</tr>
				
				<tr>
					<td width="10%">From:</td>
					<td><input type=text name=from size=40></td>
				</tr>
				
				<tr>
					<td width="10%">Subject:</td>
					<td><input type=text name=subject size=40 value="Submitting Log Files"></td>
				</tr>
				
				<tr>
					<td width="10%">Attachment:</td>
					<td><%=request.getAttribute("zipFileName")%>
				</td>
				</tr>
				
				<tr>
					<td width="10%">Message:</td>
					<td><textarea name="message" cols="40" rows="8" wrap="virtual"></textarea></td>
				</tr>
				
				<tr>
					<td><input type=submit name="sendEmail" value="Send Email" ></td>
				</tr>
			</table>
			<input type=hidden name="userId" value="<%=request.getRemoteUser()%>" >
			<input type=hidden name="sessionId" value="<%=request.getParameter("session")%>" >
			<input type=hidden name="zipFileName" value="<%=request.getAttribute("zipFileName")%>" >
		</form>
		<%@ include file="footer.jsp" %>
	</body>
</html>
