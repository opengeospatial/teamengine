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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    File userDir;

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
            userDir = new File(conf.getUsersDir(), username);
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
                String vCode = EmailUtility.getRandomNumberString();
                message = "<html>"
                    + "<head>"
                    +   "<meta charset=\"utf-8\">"
                    +   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                    +   "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />"
                    + "<style type=\"text/css\">"
                        + ".headerContainer {"
                        + "    background:"
                        + "        url(http://cite.opengeospatial.org/teamengine/images/banner.jpg)"
                        + "        no-repeat 0 0;"
                        + "    background-repeat: no-repeat;"
                        + "    background-color: black;"
                        + "    width: 100%;"
                        + "    height: 100px;"
                        + "}"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "    <div class=\"headerContainer\">"
                        + "        <img src=\"http://cite.opengeospatial.org/teamengine/site/logo.png\" />"
                        + "        <div style=\"font-size: 1.25em; margin-top: 10px;\">"
                        + "            <strong>OGC Validator</strong>"
                        + "        </div>"
                        + "    </div>"
                    +   "<table bgcolor=\"#D4DEDB\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
                    +       "<tr>"
                    + "        <td align=\"center\" style=\"padding: 15px;\" >"
                    + "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 500px;\">"
                    + "                <tr>"
                    + "                    <td>"
                    + "                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
                    + "                            <tr>"
                    + "                                <td bgcolor=\"#ffffff\" style=\"padding: 0 0 10px 15px;\">"
                    + "                                    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">Dear &nbsp;" + username + ",</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">You recently requested to reset your password for your Teamengine account. Use below verfication code to reset your password.</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px;  font-family: Helvetica, Arial, sans-serif;\"><b>Verification Code :&nbsp;" + vCode + "</b></td>"
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
                    + "</html>";
                
                if (emailList.getLength() > 0) {
                  updateUserDetails(doc, userDetails, vCode);
                  EmailUtility.sendEmail(host, port, user, pass, emailList.item(0).getTextContent(), subject, message);
                  response.sendRedirect("resetPassword.jsp?emailStatus=true");
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
  /**
   * Store verification code into the user.xml file to validate the code.
   * @param doc
   * @param userDetails
   * @param verifyCode
   */
  public void updateUserDetails(Document doc, Element userDetails,
      String verifyCode) {

    NodeList vCodeList = userDetails.getElementsByTagName("verificationCode");
    if (vCodeList.getLength() != 0) {
      Element element = (Element) doc.getElementsByTagName("verificationCode")
          .item(0);
      Node parent = element.getParentNode();
      parent.removeChild(element);
    }
    Element verificationCode = doc.createElement("verificationCode");
    verificationCode.setTextContent(verifyCode);
    userDetails.appendChild(verificationCode);
    try {
      DOMSource source = new DOMSource(doc);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StreamResult result = new StreamResult(new File(userDir, "user.xml"));
      transformer.transform(source, result);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to update userdetails with the verification code "
              + e.getMessage());
    }
  }
}
