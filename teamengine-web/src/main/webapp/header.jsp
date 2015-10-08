<div style="position: static">
	<div
		style="position: static; background-color: black; width: 100%; height: 100px; overflow: hidden"
		onclick="window.location = ''">
		<!-- Image derived from "Dinky the Steam Engine - main drive wheel", Steve Karg, http://www.burningwell.org -->
		<img style="position: absolute" src="images/banner.jpg" alt="TEAM Engine Banner" />
		<div style="position: absolute;">
      <div style="margin-bottom: 0.75em;"><img src="site/logo.png"/></div>
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
		    }
		%>
	</div>
<hr>
</div>
