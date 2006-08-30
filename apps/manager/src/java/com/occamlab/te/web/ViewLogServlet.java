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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.occamlab.te.Test;
import com.occamlab.te.ViewLog;

public class ViewLogServlet extends HttpServlet {
  Config Conf;
  DocumentBuilder DB;
  Templates ViewLogTemplates;
  
  public void init() throws ServletException {
    try {
      Conf = new Config();
      DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      File stylesheet = Test.getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
      ViewLogTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(stylesheet));
    } catch(Exception e) {
      e.printStackTrace(System.out);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    try {
      ArrayList tests = new ArrayList();
      String user = request.getRemoteUser();
      File userlog = new File(Conf.getUsersDir(), user);
      String session = request.getParameter("session");
      String test = request.getParameter("test");
      if (test != null) {
        tests.add(test);
      }
//      URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
      ViewLog.view_log(DB, userlog, session, tests, ViewLogTemplates, new OutputStreamWriter(response.getOutputStream()));
    } catch(Exception e) {
      throw new ServletException(e);
    }
  }
}