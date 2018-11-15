<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
String username = request.getParameter("username");
String email = request.getParameter("email");
String firstName = request.getParameter("firstName");
String lastName = request.getParameter("lastName");
String organization = request.getParameter("organization");
%>
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
				var firstName = form.elements["firstName"].value;
				if (firstName == null || firstName == '') {
					showerror("FirstName is required.");
					return;
				}
				var lastName = form.elements["lastName"].value;
				if (lastName == null || lastName == '') {
					showerror("LastName is required.");
					return;
				}
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
				if (email == null || email == '') {
						showerror("Email address is required.");
						return;
				} else if (email.length > 0) {
					var amp = email.indexOf("@");
					var dot = email.lastIndexOf(".");
					if (!(amp > 0 && dot > amp+1)) {
						showerror("Invalid email address.");
						return;
					}
				}
				var organization = form.elements["organization"].value;
				if (organization == null || organization == '') {
					showerror("Organization is required.");
					return;
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
				form.elements["firstName"].value = "";
				form.elements["lastName"].value = "";
				form.elements["username"].value = "";
				form.elements["password"].value = "";
				form.elements["repeat_password"].value = "";
				form.elements["email"].value = "";
				form.elements["organization"].value = "";
			}
		</script>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Register</h2>
		<div id="error" style="color: red">
<%
if ("duplicate".equals(request.getParameter("error"))) {
	out.println("Sorry, username \"" + username + "\" is not available.  Please try another user.");
}
%>
		</div>
		<form name="registration" method="post" action="registrationHandler">
			<p>
				Create a username (with at least 6 characters) and password:<br/>
				<br/>
				<table>
					<tr>
						<td>First Name:</td>
						<td><input name="firstName" type="text" value="<%= firstName == null ? "" : firstName %>"/></td>
					</tr>
					<tr>
						<td>Last Name:</td>
						<td><input name="lastName" type="text" value="<%= lastName == null ? "" : lastName %>"/></td>
					</tr>
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
						<td>Email:</td>
						<td><input name="email" type="text" value="<%= email == null ? "" : email %>"/></td>
					</tr>
					<tr>
						<td>Organization:</td>
						<td><input name="organization" type="text" value="<%= organization == null ? "" : organization %>"/></td>
					</tr>
				</table>
<% if (!request.isSecure()) { %>
				<br/>
				<div style="width:600px; border-width: 1px; border-style:solid; padding:2px">
					<b>WARNING:</b> This site cannot guarantee the confidentiality of information sent. It  uses a basic HTTP authentication protocol and the information sent by you and by the server to you is not encrypted. Do not enter a valuable password, and do not use this site if you are concerned with secrecy for your test sessions.<br/>
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
