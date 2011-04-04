<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
String username = request.getParameter("username");
String email = request.getParameter("email");
%>
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
		<title>Register</title>
		<script>
			function showerror(msg) {
				var error = document.getElementById("error");
				var child = error.lastChild;
				if (child) {
					error.replaceChild(document.createTextNode(msg), child);
				} else {
					error.appendChild(document.createTextNode(msg));
				}
			}

			function submitform() {
				var form = document.forms["registration"];
				var username = form.elements["username"].value;
				if (username.length < 6) {
					showerror("Username must be at least 6 characters.");
					return;
				}
				var password = form.elements["password"].value;
				if (password.length == 0) {
					showerror("Password is required.");
					return;
				}
				var repeat_password = form.elements["repeat_password"].value;
				if (repeat_password != password) {
					showerror("Passwords don't match.");
					return;
				}
				var email = form.elements["email"].value;
				if (email.length > 0) {
					var amp = email.indexOf("@");
					var dot = email.lastIndexOf(".");
					if (!(amp > 0 && dot > amp+1)) {
						showerror("Invalid email address.");
						return;
					}
				}
<% if (!request.isSecure()) { %>
				if (!form.elements["disclaimer"].checked) {
					showerror("Please acknowledge that you have read the warning below by checking the box.");
					return;
				}
<% } %>
				form.submit();
			}
			
			function resetform() {
				var form = document.forms["registration"];
				form.elements["username"].value = "";
				form.elements["password"].value = "";
				form.elements["repeat_password"].value = "";
				form.elements["email"].value = "";
			}
		</script>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Register</h2>
		<div id="error" style="color: red">
<%
if ("duplicate".equals(request.getParameter("error"))) {
	out.println("Sorry, username \"" + username + "\" is not available.  Please try another.");
}
%>
		</div>
		<form name="registration" method="post" action="registrationHandler">
			<p>
				Create a username and password:<br/>
				<br/>
				<table>
					<tr>
						<td>Username:</td>
						<td><input name="username" type="text" value="<%= username == null ? "" : username %>"/></td>
					</tr>
					<tr>
						<td>Password:</td>
						<td><input name="password" type="password"/></td>
					</tr>
					<tr>
						<td>Repeat Password:</td>
						<td><input name="repeat_password" type="password"/></td>
					</tr>
					<tr>
						<td>Email (Optional):</td>
						<td><input name="email" type="text" value="<%= email == null ? "" : email %>"/></td>
					</tr>
				</table>
<% if (!request.isSecure()) { %>
				<br/>
				<div style="width:600px; border-width: 1px; border-style:solid; padding:2px">
					<b>WARNING:</b> This site does not use a secure protocol.
					The information presented to you and the information you enter is not encrypted.
					Do not enter a valuable password, and do not use this
					site if you are concerned with secrecy for your test sessions.<br/>
					<input name="disclaimer" type="checkbox"/>I have read and acknowledge this warning.<br/>
				</div>
				<br/>
<% } %>
				<input type="button" value="Submit" onclick="submitform()"/>
				<input type="button" value="Reset" onclick="resetform()"/>
				<br/>
			</p>
		</form>
		<%@ include file="footer.jsp" %>
	</body>
</html>
