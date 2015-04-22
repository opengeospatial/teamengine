<%@page import="org.w3c.dom.NodeList"%>
<%@page import="javax.xml.transform.dom.DOMSource"%>
<%@page import="javax.xml.transform.stream.StreamResult"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="javax.xml.transform.OutputKeys"%>
<%@page import="javax.xml.transform.Transformer"%>
<%@page import="javax.xml.transform.TransformerFactory"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="javax.xml.parsers.DocumentBuilder"%>
<%@page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@page import="org.w3c.dom.Element"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.io.File"%>
<%@ page
  language="java"
  session="false"
  %>
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
  <!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  
    The contents of this file are subject to the Mozilla Public License
    Version 1.1 (the "License"); you may not use this file except in
    compliance with the License. You may obtain a copy of the License at
    http://www.mozilla.org/MPL/ 
  
    Software distributed under the License is distributed on an "AS IS" basis,
    WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
    the specific language governing rights and limitations under the License. 
  
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
      <title>Welcome</title>
      <link rel="stylesheet" type="text/css" href="styles/main.css">
    </head>
    <body>
      <%! private static void addfiles(File input, ArrayList<File> files) {
          if (input.isDirectory()) {
            ArrayList<File> path = new ArrayList<File>(Arrays.asList(input.listFiles()));
            for (int i = 0; i < path.size(); ++i) {
              if (path.get(i).isDirectory()) {
                addfiles(path.get(i), files);
              }
              if (path.get(i).isFile()) {
                files.add(path.get(i));
              }
            }
          }
          if (input.isFile()) {
            files.add(input);
          }
        }%>

      <%        String path = getServletContext().getInitParameter("teConfigFile");
        String directory = path.split("config")[0] + "scripts";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document tournaments = builder.newDocument();
        Element rootconfig = tournaments.createElement("config");
        tournaments.appendChild(rootconfig);
        Element rootscripts = tournaments.createElement("scripts");
        rootconfig.appendChild(rootscripts);
        Element rootorganization = tournaments.createElement("organization");
        rootscripts.appendChild(rootorganization);
        Element rootname = tournaments.createElement("name");
        rootname.appendChild(tournaments.createTextNode("OGC"));
        rootorganization.appendChild(rootname);
        ArrayList<File> files = new ArrayList<File>();
        addfiles(new File(directory), files);
        for (File file : files) {
          if (file.getName().contains("config.xml")) {
            Document tournament = builder.parse(file);
            NodeList ndlst = tournament.getElementsByTagName("standard");
            Node tournamentElement = ndlst.item(0);
            Node firstDocImportedNode = tournaments.adoptNode(tournamentElement);
            rootorganization.appendChild(firstDocImportedNode);
          }
        }

        PrintWriter writer = new PrintWriter(new File(path));
        writer.print("");
        writer.close();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(tournaments), new StreamResult(new FileOutputStream(path)));
      %>

      <%@ include file="header.jsp" %>
      <h2>Welcome</h2>
      <%@ include file="welcome.jsp" %>
      <a href="viewSessions.jsp" style="text-decoration: none">
        <span class="box">Sign in</span></a> 
      or 
      <a href="register.jsp" style="text-decoration: none">
        <span class="box">Create an account</span></a>
        <%@ include file="footer.jsp" %>
    </body>
  </html>
