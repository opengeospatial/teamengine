<%@ page isErrorPage="true" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<html>
  <head>
      <title>Error</title>
  </head>
  <body>
     <div id="error">
    <%
      String error = exception.getMeesage();
     
         
    %>
    <p>There are errors in the configuration of TEAM Engine</p>
    <p><%=error %></p>
    
    
     </div>
  </body>
<html>