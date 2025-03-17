/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.occamlab.te.config.Config;
import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.Misc;

/**
 * A servlet that generates an HTML report from the contents of a test session log. The
 * report is placed at the root of the user's work directory (in TE_BASE/users/) and is
 * named after the session identifier (e.g. s0001.html).
 */
public class PrettyPrintLogsServlet extends HttpServlet implements Servlet {

	private static Logger LOGR = Logger.getLogger(PrettyPrintLogsServlet.class.getName());

	private static final long serialVersionUID = 4555573278222613701L;

	Config Conf;

	DocumentationHelper docHelper;

	public void init() throws ServletException {
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("/com/occamlab/te/test_report_html.xsl");
		docHelper = new DocumentationHelper(stylesheet);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			String sessionId = request.getParameter("session");
			String reportFileName = sessionId + ".html";
			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
			File sessiondir = new File(userdir, sessionId);
			File prettyPrintReportFile = new File(userdir, reportFileName);
			docHelper.prettyPrintsReport(sessiondir, prettyPrintReportFile);
			response.setContentType("text/html");
			response.setHeader("Content-Disposition", "attachment; filename=" + reportFileName + ";");
			response.setHeader("Cache-Control", "no-cache");
			byte[] buf = new byte[response.getBufferSize()];
			response.setContentLength((int) prettyPrintReportFile.length());
			System.out.println("file length : " + (int) prettyPrintReportFile.length());
			int length;
			BufferedInputStream fileInBuf = null;
			try {
				fileInBuf = new BufferedInputStream(new FileInputStream(prettyPrintReportFile));
				OutputStream out = response.getOutputStream();
				while ((length = fileInBuf.read(buf)) > 0) {
					out.write(buf, 0, length);
				}
			}
			finally {
				if (null != fileInBuf)
					fileInBuf.close();
			}
		}
		catch (Exception e) {
			LOGR.log(Level.WARNING, "Failed to generate HTML test report.", e);
		}
	}

}
