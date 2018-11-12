/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

/**
 * Handles requests to register new users.
 * 
 */
public class ResetPasswordHandler extends HttpServlet {

    private static final long serialVersionUID = 7428127065308163495L;

    Config conf;
    private String host;
    private String port;
    private String user;
    private String pass;
    private String subject = "Reset your Atlassian password";

    private String message;

    public void init() throws ServletException {
        conf = new Config();
        ServletContext context = getServletContext();
        host = context.getInitParameter("host");
        port = context.getInitParameter("port");
        user = context.getInitParameter("user");
        pass = context.getInitParameter("pass");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String username = request.getParameter("username");
            File userDir = new File(conf.getUsersDir(), username);
            if (!userDir.exists()) {
                String url = "resetPassword.jsp?error=userNotExists&username=" + username;
                response.sendRedirect(url);
            } else {
                File xmlfile = new File(userDir, "user.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setExpandEntityReferences(false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlfile);
                Element userDetails = (Element) (doc.getElementsByTagName("user")
                        .item(0));
                NodeList emailList = userDetails.getElementsByTagName("email");
                message = "&nbsp; &nbsp; &nbsp; &nbsp; <h2>Verification Code:</h2>";
                message += "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Username" + username;
                message += "<br/> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Verification code: <h3>" + EmailUtility.getRandomNumberString() + "</h3>";
                message += "<br/><br/>";
                message += "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Regards,";
                message += "<br/>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; CITE team<br/>";
                
                if (emailList.getLength() > 0) {
                  EmailUtility.sendEmail(host, port, user, pass, emailList.item(0).getTextContent(), subject, message);
                  response.sendRedirect("resetPassword.jsp");
                } else { 
                  String url = "resetPassword.jsp?error=emailNotExists&username="
                    + username;
                  response.sendRedirect(url);
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
