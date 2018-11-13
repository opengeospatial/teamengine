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
    private String subject = "Reset your Teamengine password";

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
                message = String.format("<html>"
                    + "<head>"
                    +   "<meta charset=\"utf-8\">"
                    +   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                    +   "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />"
                    + "</head>"
                    + "<body style=\"margin: 0 !important; padding: 0 !important;\">"
                    +   "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
                    +       "<tr>"
                    + "        <td bgcolor=\"#D4DEDB\" align=\"center\" style=\"padding: 15px;\" >"
                    + "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 500px;\">"
                    + "                <tr>"
                    + "                    <td>"
                    + "                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
                    + "                            <tr>"
                    + "                                <td>"
                    + "                                    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">Hey %s,</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">You recently requested to reset your password for your Teamengine account. Use below verfication code to reset your password.</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px;  font-family: Helvetica, Arial, sans-serif;\"><b>Verification Code :&nbsp;%s</b></td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">If you did not request a password reset, please ignore this email or contact to CITE team.</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">Regards,<br>CITE TEAM</td>"
                    + "                                        </tr>"
                    + "                                    </table>"
                    + "                                </td>"
                    + "                            </tr>"
                    + "                        </table>"
                    + "                    </td>"
                    + "                </tr>"
                    + "            </table>"
                    + "        </td>"
                    + "    </tr>"
                    + "</table>"
                    + "</body>"
                    + "</html>" 
                    + username, EmailUtility.getRandomNumberString());
                
                if (emailList.getLength() > 0) {
                  EmailUtility.sendEmail(host, port, user, pass, emailList.item(0).getTextContent(), subject, message);
                  response.sendRedirect("resetPassword.jsp?success=true");
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
