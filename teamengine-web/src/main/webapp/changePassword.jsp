<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
String username = request.getRemoteUser();
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
		var form = document.forms["changePassword"];
		var oldPass = form.elements["oldPass"].value;
		var username = form.elements["username"].value;
		if (oldPass.length == 0) {
			showerror("Old password is required.");
			return;
		}
		if (username.length < 6) {
			showerror("Username must be at least 6 characters.");
			return;
		}
		var newPassword = form.elements["newPassword"].value;
		if (newPassword.length == 0) {
			showerror("New Password is required.");
			return;
		}
		var repeat_password = form.elements["repeat_password"].value;
		if (repeat_password != newPassword) {
			showerror("Passwords don't match.");
			return;
		}
		form.submit();
	}

	function resetform() {
		var form = document.forms["changePassword"];
		form.elements["oldPass"].value = "";
		form.elements["username"].value = "";
		form.elements["newPassword"].value = "";
		form.elements["repeat_password"].value = "";
	}
</script>
</head>
<body>
	<%@ include file="header.jsp"%>
	<h2>Change Password</h2>
	<div id="error" style="color: red">
		<%
		if ("userNotExists".equals(request.getParameter("error"))) {
		  out.println("The \"" + username + "\" is not registered username.  Please try with registered User.");
		  } else if ("invalidOldPwd".equals(request.getParameter("error"))) {
		    out.println("The Old password is not valid.");
		  }
		%>
	</div>
	<form name="changePassword" method="post"
		action="changePasswordHandler">
		<p>
			Enter all mandatory fields: <br /> <br />
			<table>
				<tr>
					<td>Username :</td>
					<td><input name="username" type="text"
						value="<%=username == null ? "" : username%>" readonly /></td>
				</tr>
				<tr>
					<td>Old Password :</td>
					<td><input name="oldPass" type="password" /></td>
				</tr>
				<tr>
					<td>New Password:</td>
					<td><input name="newPassword" type="password" /></td>
				</tr>
				<tr>
					<td>Repeat Password:</td>
					<td><input name="repeat_password" type="password" /></td>
				</tr>
				<tr>
					<td><input type="button" value="Submit" onclick="submitform()" /></td>
					<td><input type="button" value="Reset" onclick="resetform()" /></td>
				</tr>
			</table>
		</p>
	</form>
	<%@ include file="footer.jsp"%>
</body>
</html>
