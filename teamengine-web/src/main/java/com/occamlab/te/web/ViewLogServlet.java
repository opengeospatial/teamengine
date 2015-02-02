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

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import com.occamlab.te.ViewLog;
import com.occamlab.te.util.Misc;

/**
 * Processes (GET method) requests to view a test log.
 * 
 */
public class ViewLogServlet extends HttpServlet {

    private static final long serialVersionUID = 2891486945236875019L;

    Config conf;

    Templates viewLogTemplates;

    public void init() throws ServletException {
        try {
            conf = new Config();
            File stylesheet = Misc
                    .getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            viewLogTemplates = transformerFactory
                    .newTemplates(new StreamSource(stylesheet));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            ArrayList<String> tests = new ArrayList<String>();
            String user = request.getRemoteUser();
            File userlog = new File(conf.getUsersDir(), user);
            String session = request.getParameter("session");
            String test = request.getParameter("test");
            if (test != null) {
                tests.add(test);
            }
            String suiteName=null;
            ViewLog.view_log(suiteName,userlog, session, tests, viewLogTemplates,
                    new OutputStreamWriter(response.getOutputStream()));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}