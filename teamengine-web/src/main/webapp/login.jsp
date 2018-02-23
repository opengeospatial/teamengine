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
		<title>Login</title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Login</h2>
<%
if (request.getParameter("error") != null) {
	out.println("<span style=\"color: red\">The username and/or password did not match.  Please try again.</span>");
}
%>
		<form method="post" action="j_security_check">
			<p>
				Enter your username and password:<br/>
				<br/>
				Username: <input type="text" name="j_username"/><br/>
				Password: <input type="password" name="j_password"/><br/>
				<input type="submit" value="Log In"/><br/>
			</p>
		</form>
		If you don't have a username and password, please <a href="register.jsp">register</a>.
		<%@ include file="footer.jsp" %>
	</body>
</html>
