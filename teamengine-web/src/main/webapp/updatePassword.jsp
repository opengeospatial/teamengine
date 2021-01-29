<%@ page language="java" session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
String vCode = request.getParameter("vCode");
String username = request.getParameter("username");
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
		var form = document.forms["updatepassword"];
		var vCode = form.elements["vCode"].value;
		var username = form.elements["username"].value;
		if (vCode.length < 6) {
			showerror("Verification code must be at least 6 characters and should not be empty.");
			return;
		}
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
		form.submit();
	}

	function resetform() {
		var form = document.forms["updatepassword"];
		form.elements["vCode"].value = "";
		form.elements["username"].value = "";
		form.elements["password"].value = "";
		form.elements["repeat_password"].value = "";
	}
</script>
</head>
<body>
	<%@ include file="header.jsp"%>
	<h2>Reset Password</h2>
	<div id="error" style="color: red">
		<%
		  if ("invalidVcode".equals(request.getParameter("error"))) {
		    out.println("The Verification code is invalid.");
		  }
		%>
	</div>
	<div id="success" style="color: #0325f9">
		<%
		  if ("true".equals(request.getParameter("emailStatus"))) {
		    out.println("Thank you! The verification code has been sent successfully to registered email.");
		  }
		%>
	</div>
	<form name="updatepassword" method="post"
		action="updatePasswordHandler">
		<p>
			Enter all mandatory fields: <br /> <br />
			<table>
				<tr>
					<td>Verification code :</td>
					<td><input name="vCode" type="text"
						value="<%=vCode == null ? "" : vCode%>" /></td>
				</tr>
				<tr>
					<td>Username :</td>
					<td><input name="username" type="text"
						value="<%=username == null ? "" : username%>" /></td>
				</tr>
				<tr>
					<td>New Password:</td>
					<td><input name="password" type="password" /></td>
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
