package com.occamlab.te.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.realm.PasswordStorage;
import com.occamlab.te.realm.UserGenericPrincipal;

import java.io.File;
import java.security.Principal;

/**
 * Handles requests to change password.
 * 
 */
public class ChangePasswordHandler extends HttpServlet {

  Config conf;
  
  public void init() throws ServletException {
    conf = new Config();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {

    try {
      String oldPass = request.getParameter("oldPass");
      String username = request.getParameter("username");
      String newPassword = request.getParameter("newPassword");

      File userDir = new File(conf.getUsersDir(), username);
      if (!userDir.exists()) {
        String url = "changePassword.jsp?error=userNotExists&username="
            + username;
        response.sendRedirect(url);
      } else {
        File xmlfile = new File(userDir, "user.xml");
        Document doc = XMLUtils.parseDocument(xmlfile);
        Element userDetails = (Element) (doc.getElementsByTagName("user")
            .item(0));

        NodeList oldPwdList = userDetails
            .getElementsByTagName("password");
        String storedOldPassword = null;
        if (oldPwdList.getLength() > 0) {
          Element oldePwdElement = (Element) oldPwdList.item(0);
          storedOldPassword = oldePwdElement.getTextContent();
        }
        
        Boolean isValid = PasswordStorage.verifyPassword(oldPass, storedOldPassword);
        if (isValid) {
          doc = XMLUtils.removeElement(doc, userDetails, "password");
          Element pwdElement = doc.createElement("password");
          pwdElement.setTextContent(PasswordStorage.createHash(newPassword));
          userDetails.appendChild(pwdElement);
          XMLUtils.transformDocument(doc, new File(userDir, "user.xml"));
           Principal userPrincipal = UserGenericPrincipal.getInstance().removePrincipal(username);
           if(userPrincipal == null){
             throw new RuntimeException("Failed update old credentials");
           }
          request.getSession().invalidate();
          response.sendRedirect(request.getContextPath());
        } else {
          String url = "changePassword.jsp?error=invalidOldPwd";
          response.sendRedirect(url);
        }
      }
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
