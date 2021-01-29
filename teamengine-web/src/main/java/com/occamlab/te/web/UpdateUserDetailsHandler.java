package com.occamlab.te.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.realm.PasswordStorage;

import java.io.File;

/**
 * Handles requests to update user details.
 * 
 */
public class UpdateUserDetailsHandler extends HttpServlet {

  Config conf;
  
  public void init() throws ServletException {
    conf = new Config();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    process(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    process(request, response);
  }
  
  public void process(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {

    try {
      String username = request.getParameter("username");
      if(username == null){
        username = request.getRemoteUser();
      }
      String verifyPassword = request.getParameter("password");
      String email = request.getParameter("email");
      String organization = request.getParameter("organization");

      File userDir = new File(conf.getUsersDir(), username);
      if (!userDir.exists()) {
        String url = "updateUserDetails.jsp?error=userNotExists&username="
            + username;
        response.sendRedirect(url);
      } else {
        File xmlfile = new File(userDir, "user.xml");
        Document doc = XMLUtils.parseDocument(xmlfile);
        Element userDetails = (Element) (doc.getElementsByTagName("user")
            .item(0));
        
        if(email == null && organization == null){        
        
        NodeList emailList = userDetails
            .getElementsByTagName("email");
        String registeredEmail = "";
        if (emailList.getLength() > 0) {
          Element registeredEmailElement = (Element) emailList.item(0);
          registeredEmail = registeredEmailElement.getTextContent();
        }
        HttpSession session = request.getSession();
        
        session.setAttribute("email", registeredEmail);
        NodeList organizationList = userDetails
            .getElementsByTagName("organization");
        String registeredOrganization = "";
        if (organizationList.getLength() > 0) {
          Element registeredOrgElement = (Element) organizationList.item(0);
          registeredOrganization = registeredOrgElement.getTextContent();
        }
        session.setAttribute("organization", registeredOrganization);
        response.sendRedirect("updateUserDetails.jsp");
        } else {
          NodeList storedPwdList = userDetails
              .getElementsByTagName("password");
          String storedPassword = null;
          if (storedPwdList.getLength() > 0) {
            Element storedPwdElement = (Element) storedPwdList.item(0);
            storedPassword = storedPwdElement.getTextContent();
          }
          Boolean isValid = PasswordStorage.verifyPassword(verifyPassword, storedPassword);
          if(isValid){
          //Update email
          doc = XMLUtils.removeElement(doc, userDetails, "email");
          Element emailElement = doc.createElement("email");
          emailElement.setTextContent(email);
          userDetails.appendChild(emailElement);
          //Update organization
          doc = XMLUtils.removeElement(doc, userDetails, "organization");
          Element orgElement = doc.createElement("organization");
          orgElement.setTextContent(organization);
          userDetails.appendChild(orgElement);
          
          XMLUtils.transformDocument(doc, new File(userDir, "user.xml"));
          
          String url = "viewSessions.jsp?success=updateDetails";
          response.sendRedirect(url);
          } else {
            String url = "updateUserDetails.jsp?error=invalidPwd";
            response.sendRedirect(url);
          }
        }        
      }
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
