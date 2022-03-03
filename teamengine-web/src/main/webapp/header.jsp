<div style="position: static">
	<div
		style="position: static; background-color: black; width: 100%; height: 100px; overflow: hidden"
		onclick="window.location = ''">
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
		        out.println("\t\t<div style=\"position: absolute; right:20px; top:25px; background-color: white; padding: 3px; border-style: inset\">");
		        out.println("\t\t\tUser: " + user + "<br/>");
		        out.println("\t\t\t<a href=\"logout\">Logout</a>");
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
