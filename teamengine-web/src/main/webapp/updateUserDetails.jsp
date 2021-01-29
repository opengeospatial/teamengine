<%@page import="java.util.Collection"%>
<%@ page language="java"
	import="java.io.File, javax.xml.parsers.*, java.util.Arrays, com.occamlab.te.web.*, java.util.List, java.util.ArrayList"%>
<%
String username = request.getRemoteUser();
String email = session.getAttribute("email").toString();
String organization = session.getAttribute("organization").toString();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Update User Details</title>
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
		var form = document.forms["updateUserDetails"];
		var password = form.elements["password"].value;
		var email = form.elements["email"].value;
		var organization = form.elements["organization"].value;
		var emailFormat = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
		if (password.length == 0) {
			showerror("Password is required.");
			return;
		}
		
		if (!email.match(emailFormat)) {
			showerror("You have entered an invalid email address!");
			return;
		}
		if (organization.length < 3) {
			showerror("Organization cannot be empty!");
			return;
		}
		form.submit();
	}

	function resetform() {
		var form = document.forms["updateUserDetails"];
		form.elements["password"].value = "";
		form.elements["email"].value = "";
		form.elements["organization"].value = "";
	}
</script>
</head>
<body>
	<%@ include file="header.jsp"%>
	<h2>Update User Details</h2>
	<div id="error" style="color: red">
		<%
		  if ("invalidPwd".equals(request.getParameter("error"))) {
		    out.println("Password did not match.");
		  } else if("userNotExists".equals(request.getParameter("error"))){
		    out.println("Not valid user!");
		  }
		%>
	</div>

	<form name="updateUserDetails" method="post"
		action="updateUserDetailsHandler">
		<p>
			Enter all mandatory fields: <br /> <br />
			<table>
				<tr>
					<td></td>
					<td><input name="username" type="hidden"
						value="<%= username == null ? "" : username %>" /></td>
				</tr>
				<tr>
					<td>Password :</td>
					<td><input name="password" type="password" /></td>
				</tr>
				<tr>
					<td>Email :</td>
					<td><input name="email" type="text"
						value="<%=email == null ? "" : email %>" /></td>
				</tr>
				<tr>
					<td>Organization :</td>
					<td><input name="organization" type="text"
						value="<%=organization == null ? "" : organization%>" /></td>
				</tr>
				<td><input type="button" value="Submit" onclick="submitform()" /></td>
				<td><input type="button" value="Reset" onclick="resetform()" /></td>
				</tr>
			</table>
		</p>
	</form>
	<%@ include file="footer.jsp"%>
</body>
</html>
