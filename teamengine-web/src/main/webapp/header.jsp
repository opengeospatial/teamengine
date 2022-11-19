<style>
.dropbtn {
  padding: 16px;
  font-size: 16px;
  border: none;
  background-color: white;
}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: fixed;
  background-color: #f1f1f1;
  min-width: 165px;
  box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
  z-index: 1;
}

.dropdown-content a {
  color: black;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
}

.dropdown:hover .dropdown-content {display: block;}

</style>
<div style="position: static">
	<div
		style="position: static; background-color: black; width: 100%; height: 100px; overflow: hidden"
		>
		<!-- Image derived from "Dinky the Steam Engine - main drive wheel", Steve Karg, http://www.burningwell.org -->
		<% String contextPath = request.getContextPath(); %>
		<img style="position: absolute" src='<%= contextPath + "/images/banner.jpg" %>' alt="TEAM Engine Banner" />
		<div style="position: absolute;">
      <div style="margin-bottom: 0.75em;"><img src='<%= contextPath + "/site/logo.png" %>' /></div>
      <%@include file="site/title.html" %>
		</div>
		<%
		    String user = request.getRemoteUser();
                    Cookie userName=new Cookie("User", user);
                    response.addCookie(userName);
		    if (user != null && user.length() > 0) {
		        out.println("\t\t<div style=\"position: absolute; right:20px; top:25px; background-color: white; border-style: inset\">");
		        out.println("<div class=\"dropdown\">");
				out.println("   <div class=\"dropbtn\"> User: " + user + " &#9660;</div>");
				out.println("	<div class=\"dropdown-content\">");
				out.println("		<a href=\"changePassword.jsp\">Change Password</a>");
				out.println("		<a href=\"updateUserDetailsHandler\">Update User Details</a>");
				out.println("		<a href=\"logout\">Logout</a>");
				out.println("  </div>");
				out.println("</div>");
				
		        out.println("\t\t</div>");
		    } else {
		        out.println("<div style=\"position: absolute; right:20px; top:44px; font-family:Verdana, sans-serif; font-size:1em; margin:0.2em;\">");
		        if (!request.getRequestURI().equalsIgnoreCase("/teamengine/login.jsp")) {
		            out.println("<a href=\"viewSessions.jsp\" style=\"text-decoration: none; padding:0.4em; color:White; background-color:SteelBlue;\">Sign in</a>");
		        }
		        if (!request.getRequestURI().equalsIgnoreCase("/teamengine/register.jsp")) {
		            out.println("<a href=\"register.jsp\" style=\"text-decoration: none; padding:0.4em; color:White; background-color:SteelBlue;\">Create an account</a>");
		        }
		        out.println("</div>");
		    }
		%>
	</div>
<hr>
</div>
