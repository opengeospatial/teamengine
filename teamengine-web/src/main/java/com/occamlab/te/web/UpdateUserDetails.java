package com.occamlab.te.web;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UpdateUserDetails extends HttpServlet {

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
      String username = request.getRemoteUser();

      String firstName = request.getParameter("firstName");
      String lastName = request.getParameter("lastName");
      String email = request.getParameter("email");
      String organization = request.getParameter("organization");

      File userDir = new File(conf.getUsersDir(), username);
      if (!userDir.exists()) {
        String msg = "error=userNotExists";
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(msg);
      } else {
        File xmlfile = new File(userDir, "user.xml");
        Document doc = XMLUtils.parseDocument(xmlfile);
        Element userDetails = (Element) (doc.getElementsByTagName("user")
            .item(0));
          //FirstName
          doc = XMLUtils.removeElement(doc, userDetails, "firstName");
          Element firstNameElement = doc.createElement("firstName");
          firstNameElement.setTextContent(firstName);
          userDetails.appendChild(firstNameElement);
          //LastName
          doc = XMLUtils.removeElement(doc, userDetails, "lastName");
          Element lastNameElement = doc.createElement("lastName");
          lastNameElement.setTextContent(lastName);
          userDetails.appendChild(lastNameElement);
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
          
          String msg = "success=detailsUpdated";
          response.setContentType("text/plain");
          response.setCharacterEncoding("UTF-8");
          response.getWriter().write(msg);
        }        
      
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
