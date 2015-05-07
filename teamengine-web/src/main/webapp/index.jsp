<%@ page
  language="java"
  session="false"
  %>
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <title>TEAM Engine</title>
      <link rel="stylesheet" type="text/css" href="styles/main.css">
    </head>
    <body>
      <%@ include file="header.jsp" %>
      <%@ include file="welcome.jsp" %>
      <section id="noColumn">
      <a href="viewSessions.jsp" style="text-decoration: none">
        <span class="box">Sign in</span></a> 
      or 
      <a href="register.jsp" style="text-decoration: none">
        <span class="box">Create an account</span></a>
      </section>  
        <%@ include file="footer.jsp" %>
    </body>
  </html>
