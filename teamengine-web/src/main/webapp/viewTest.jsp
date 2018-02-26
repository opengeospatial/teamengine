<%@ page
 language="java"
 session="false"
 import="org.w3c.dom.*, javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, javax.xml.transform.stream.*, java.io.File, com.occamlab.te.Test, com.occamlab.te.util.Misc, net.sf.saxon.FeatureKeys"
%><%!
Templates ViewTestTemplates;

public void jspInit() {
	try {
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewtest.xsl");
		TransformerFactory tf = TransformerFactory.newInstance();
		// Fortify mod: Prevent external entity injections
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); 
//		tf.setAttribute(FeatureKeys.XINCLUDE, Boolean.TRUE);
		ViewTestTemplates = tf.newTemplates(new StreamSource(stylesheet));
	} catch (Exception e) {
		e.printStackTrace(System.out);
	}
}
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): 
    	C. Heazel (WiSC): Added Fortify adjudication changes

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<title>Test <%=request.getParameter("name")%></title>
	</head>
	<body>
		<%@ include file="header.jsp" %>
<%
      String sessionId = request.getParameter("sessionid");
      File file = new File(request.getParameter("file"));
      Transformer t = ViewTestTemplates.newTransformer();
      t.setParameter("namespace-uri", request.getParameter("namespace"));
      t.setParameter("local-name", request.getParameter("name"));
      t.setParameter("sesion-id", request.getParameter("sessionid"));
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      // Fortify Mod: prevent external entity injection
      dbf.setExpandEntityReferences(false);
      dbf.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      t.transform(new DOMSource(doc), new StreamResult(out));
//      t.transform(new StreamSource(file), new StreamResult(out));
%>
		<br/>
		<br/>
		<a href="viewSessionLog.jsp?session=<%=sessionId%>">Complete session results</a>
		<br/>
		<a href="viewSessions.jsp"/>Sessions list</a>
		<%@ include file="footer.jsp" %>
	</body>
</html>
