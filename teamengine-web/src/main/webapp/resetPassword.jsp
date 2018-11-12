<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
String email = request.getParameter("email");
String username = request.getParameter("username");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<title>Reset Password</title>
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
				var form = document.forms["resetPassowrd"];
				var username = form.elements["username"].value;
				if (username.length < 6) {
					showerror("Username must be at least 6 characters.");
					return;
				}
				form.submit();
			}
			
			function resetform() {
				var form = document.forms["resetPassowrd"];
				form.elements["username"].value = "";
			}
		</script>
	</head>
	<body>
		<%@ include file="header.jsp" %>
		<h2>Reset Password</h2>
		<div id="error" style="color: red">
<%
if ("userNotExists".equals(request.getParameter("error"))) {
	out.println("The \"" + username + "\" is not registered username.  Please try with registered User.");
} else if ("emailNotExists".equals(request.getParameter("error"))) {
	out.println("Sorry, email \"" + email + "\" is not registered.  Please try with registered email.");
  }
%>
		</div>
		<form name="resetPassowrd" method="post" action="resetPasswordHandler">
			<p>
				Enter registered username only <br/>
				<br/>
				<table>
					<tr>
						<td>Username :</td>
						<td><input name="username" type="text" value="<%= username == null ? "" : username %>"/></td>
					</tr>
				</table>
				<input type="button" value="Submit" onclick="submitform()"/>
				<input type="button" value="Reset" onclick="resetform()"/>
				<br/>
			</p>
		</form>
		<%@ include file="footer.jsp" %>
	</body>
</html>
