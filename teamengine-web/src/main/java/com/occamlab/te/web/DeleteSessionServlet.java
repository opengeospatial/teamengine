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

 Contributor(s): No additional contributors to date

 ***************************************************************************
 */
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.occamlab.te.config.Config;
import com.occamlab.te.util.Misc;

/**
 * Processes a request to delete an existing test session.
 *
 */
public class DeleteSessionServlet extends HttpServlet {

	private static final long serialVersionUID = 7544788524756976408L;

	Config Conf;

	public void init() throws ServletException {
		Conf = new Config();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			String sessionId = request.getParameter("session");
			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
			File sessiondir = new File(userdir, sessionId);
			Misc.deleteDir(sessiondir);
			response.sendRedirect("sessionDeleted.jsp");
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
