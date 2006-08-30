/****************************************************************************

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class RegistrationHandlerServlet extends HttpServlet {
  Config Conf;
  
  public void init() throws ServletException {
    Conf = new Config();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    try {
      String username = request.getParameter("username");
      String password = request.getParameter("password");
      String email = request.getParameter("email");
      File userDir = new File(Conf.getUsersDir(), username);
      if (userDir.exists()) {
        String url = "register.jsp?error=duplicate&username=" + username;
        if (email != null) {
          url += "&email=" + email;
        }
        response.sendRedirect(url);
      } else {
        userDir.mkdir();
        File xmlfile = new File(userDir, "user.xml");
        PrintStream out = new PrintStream(new FileOutputStream(xmlfile));
        out.println("<user>");
        out.println(" <name>" + username + "</name>");
        out.println(" <roles>");
        out.println("  <name>user</name>");
        out.println(" </roles>");
        out.println(" <password>" + password + "</password>");
        out.println(" <email>" + email + "</email>");
        out.println("</user>");
        out.close();
        response.sendRedirect("registered.jsp");
      }
    } catch(Exception e) {
      throw new ServletException(e);
    }
  }
}