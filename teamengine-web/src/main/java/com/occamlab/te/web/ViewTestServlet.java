/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
 	C. Heazel (WiSC): Added Fortify adjudication changes

 ****************************************************************************/
package com.occamlab.te.web;

/*-
 * #%L
 * TEAM Engine - Web Application
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import com.occamlab.te.util.Misc;

/**
 * Handles (GET) requests to view a test case specification (from the test summary
 * report).
 *
 */
public class ViewTestServlet extends HttpServlet {

	private static final long serialVersionUID = -1396673675342836097L;

	Templates viewTestTemplates;

	public void init() throws ServletException {
		try {
			File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewtest.xsl");
			// Fortify Mod: prevent external entity injection
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			viewTestTemplates = tf.newTemplates(new StreamSource(stylesheet));
			// viewTestTemplates = TransformerFactory.newInstance().newTemplates(
			// new StreamSource(stylesheet));
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			File file = new File(request.getParameter("file"));
			Transformer t = viewTestTemplates.newTransformer();
			t.setParameter("namespace-uri", request.getParameter("namespace"));
			t.setParameter("local-name", request.getParameter("name"));
			URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
					request.getContextPath());
			t.setParameter("baseURL", url.toString());
			t.setParameter("user", request.getRemoteUser());
			t.transform(new StreamSource(file), new StreamResult(response.getOutputStream()));
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
