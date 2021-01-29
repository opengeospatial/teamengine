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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.web.Config;
import com.occamlab.te.web.XMLUtils;

public class ClearVerificationCode implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String username = req.getRemoteUser();

    if (username != null) {

      Config conf = new Config();
      File userDir = new File(conf.getUsersDir(), username);
      if (userDir.exists()) {
        String fileName = "user.xml";
        File xmlfile = new File(userDir, fileName);
        Document doc = XMLUtils.parseDocument(xmlfile);
        Element userDetails = (Element) (doc.getElementsByTagName("user")
            .item(0));
        doc = XMLUtils.removeElement(doc, userDetails, "verificationCode");
        XMLUtils.transformDocument(doc, new File(userDir, fileName));
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

}
