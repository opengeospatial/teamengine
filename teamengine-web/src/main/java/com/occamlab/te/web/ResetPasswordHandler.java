package com.occamlab.te.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.realm.PasswordStorage;

import java.io.File;

/**
 * Handles requests to register new users.
 * 
 */
public class ResetPasswordHandler extends HttpServlet {

    Config conf;
    private String host;
    private String port;
    private String user;
    private String pass;
    private String subject = "Reset your TEAM Engine password";
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
      String servletPath = request.getServletPath();
      if("/resetPasswordHandler".equalsIgnoreCase(servletPath)) {
        resetPassowrdHandler(request, response);
      } else if("/updatePasswordHandler".equalsIgnoreCase(servletPath)) {
        updatePassword(request, response);
      }
    }
    
    /**
     * This method will send email to registered user along with the 
     * verification code and verification code will stored into
     * user.xml file.
     */
    public void resetPassowrdHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String username = request.getParameter("username");
            userDir = new File(conf.getUsersDir(), username);
            if (!userDir.exists()) {
                String url = "resetPassword.jsp?error=userNotExists&username=" + username;
                response.sendRedirect(url);
            } else {
                File xmlfile = new File(userDir, "user.xml");
                Document doc = XMLUtils.parseDocument(xmlfile);
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
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">You recently requested to reset your password for your TEAM Engine account. Use below verfication code to reset your password.</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px;  font-family: Helvetica, Arial, sans-serif;\"><b>Verification Code :&nbsp;" + vCode + "</b></td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">If you did not request a password reset, please ignore this email or contact the CITE team.</td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\"> <a href=\"" + getBaseUrl(request) +"\"> Follow this link to reset your password.</a></td>"
                    + "                                        </tr>"
                    + "                                        <tr>"
                    + "                                            <td align=\"left\" style=\"padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Helvetica, Arial, sans-serif;\">Regards,<br>CITE team</td>"
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
                  saveVerificationCode(doc, userDetails, vCode);
                  EmailUtility.sendEmail(host, port, user, pass, emailList.item(0).getTextContent(), subject, message);
                  response.sendRedirect("updatePassword.jsp?emailStatus=true");
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
     * This method will validate the verification code and update the new password if
     * the code is valid. 
     * Otherwise it will throw error. 
     * @throws ServletException 
     */
    public void updatePassword(HttpServletRequest request,
        HttpServletResponse response) throws ServletException {
      try {
        String vCode = request.getParameter("vCode");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String hashedPassword = PasswordStorage.createHash(password);
  
        userDir = new File(conf.getUsersDir(), username);
        if (!userDir.exists()) {
          String url = "updatePassword.jsp?error=userNotExists&username="
              + username;
          response.sendRedirect(url);
        } else {
          File xmlfile = new File(userDir, "user.xml");
          Document doc = XMLUtils.parseDocument(xmlfile);
          Element userDetails = (Element) (doc.getElementsByTagName("user")
              .item(0));
  
          NodeList vCodeList = userDetails
              .getElementsByTagName("verificationCode");
          String storedVerificationCode = null;
          if (vCodeList.getLength() > 0) {
            Element vCodeElement = (Element) doc.getElementsByTagName(
                "verificationCode").item(0);
            storedVerificationCode = vCodeElement.getTextContent();
          }
  
          if (storedVerificationCode.equalsIgnoreCase(vCode)) {
            NodeList pwdList = userDetails.getElementsByTagName("password");
            if (pwdList.getLength() != 0) {
              Element pwdElement = (Element) doc.getElementsByTagName("password")
                  .item(0);
              Node parent = pwdElement.getParentNode();
              parent.removeChild(pwdElement);
            }
            Element pwdElement = doc.createElement("password");
            pwdElement.setTextContent(hashedPassword);
            userDetails.appendChild(pwdElement);
            XMLUtils.transformDocument(doc, new File(userDir, "user.xml"));
            String url = "login.jsp?success=pwd";
            response.sendRedirect(url);
          } else {
            String url = "updatePassword.jsp?error=invalidVcode&username=" + username + "&vCode=" + vCode;
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
  public void saveVerificationCode(Document doc, Element userDetails,
      String verifyCode) {

    //Remove element if exist.
    doc = XMLUtils.removeElement(doc, userDetails, "verificationCode");
    
    //Update new details to existing document
    Element verificationCode = doc.createElement("verificationCode");
    verificationCode.setTextContent(verifyCode);
    userDetails.appendChild(verificationCode);
    XMLUtils.transformDocument(doc, new File(userDir, "user.xml"));
  }
  /**
   * Returns the base URL from the current request context.
   * @param request
   * @return baseUrl
   */
  public static String getBaseUrl(HttpServletRequest request) {
    String scheme = request.getScheme();
    String host = request.getServerName();
    int port = request.getServerPort();
    String contextPath = request.getContextPath();

    String baseUrl = scheme + "://" + host + ((("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) ? "" : ":" + port) + contextPath;
    return baseUrl;
}
}
