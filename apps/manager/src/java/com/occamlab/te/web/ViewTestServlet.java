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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import com.occamlab.te.Test;

import java.io.File;
import java.net.URL;

/**
 * Handles (GET) requests to view a test case specification (from the test
 * summary report).
 * 
 */
public class ViewTestServlet extends HttpServlet {

	private static final long serialVersionUID = -1396673675342836097L;

	Templates viewTestTemplates;

	public void init() throws ServletException {
		try {
			File stylesheet = Test
					.getResourceAsFile("com/occamlab/te/web/viewtest.xsl");
			viewTestTemplates = TransformerFactory.newInstance().newTemplates(
					new StreamSource(stylesheet));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		try {
			File file = new File(request.getParameter("file"));
			Transformer t = viewTestTemplates.newTransformer();
			t.setParameter("namespace-uri", request.getParameter("namespace"));
			t.setParameter("local-name", request.getParameter("name"));
			URL url = new URL(request.getScheme(), request.getServerName(),
					request.getServerPort(), request.getContextPath());
			t.setParameter("baseURL", url.toString());
			t.setParameter("user", request.getRemoteUser());
			t.transform(new StreamSource(file), new StreamResult(response
					.getOutputStream()));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}