package com.occamlab.te.web.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.web.Config;
import com.occamlab.te.web.XMLUtils;

public class CheckUserProfileFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String username = req.getRemoteUser();
    Boolean updateUserPopup = false;

    if (username != null) {
      Config conf = new Config();
      File userDir = new File(conf.getUsersDir(), username);
      if (userDir.exists()) {
        String fileName = "user.xml";
        File xmlfile = new File(userDir, fileName);
        Document doc = XMLUtils.parseDocument(xmlfile);
        Element userDetails = (Element) (doc.getElementsByTagName("user")
            .item(0));
        HttpSession session = ((HttpServletRequest) request).getSession();
        
        // FirstName
        NodeList firstNameList = userDetails.getElementsByTagName("firstName");
        String firstName = "";
        if (firstNameList.getLength() > 0) {
          Element firstNameElement = (Element) firstNameList.item(0);
          firstName = firstNameElement.getTextContent();
        }
        // LastName
        NodeList lastNameList = userDetails.getElementsByTagName("firstName");
        String lastName = "";
        if (firstNameList.getLength() > 0) {
          Element lastNameElement = (Element) lastNameList.item(0);
          lastName = lastNameElement.getTextContent();
        }
        //Email
        NodeList emailList = userDetails.getElementsByTagName("email");
        String registeredEmail = "";
        if (emailList.getLength() > 0) {
          Element registeredEmailElement = (Element) emailList.item(0);
          registeredEmail = registeredEmailElement.getTextContent();
        }        
        //Organization
        NodeList organizationList = userDetails.getElementsByTagName("organization");
        String registeredOrganization = "";
        if (organizationList.getLength() > 0) {
          Element registeredOrgElement = (Element) organizationList.item(0);
          registeredOrganization = registeredOrgElement.getTextContent();
        }
        
        if(firstName == "" || lastName == "" || registeredEmail == "" || registeredOrganization == ""){
          updateUserPopup = true;
        } 
        
        request.setAttribute("updateUserPopup",updateUserPopup);
        request.setAttribute("u_firstName", firstName);
        request.setAttribute("u_lastName", lastName);
        request.setAttribute("u_email", registeredEmail);
        request.setAttribute("u_organization", registeredOrganization);
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

}
