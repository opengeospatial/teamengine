package com.occamlab.te.web;
/****************************************************************************

The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
the specific language governing rights and limitations under the License.

The Original Code is TEAM Engine.

The Initial Developer of the Original Code is Fabrizio Vitale
jointly with the Institute of Methodologies for Environmental Analysis 
(IMAA) part of the Italian National Research Council (CNR). 
Portions created by Fabrizio Vitale are Copyright (C) 2009. All Rights Reserved.

Contributor(s): No additional contributors to date

****************************************************************************/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.Misc;


public class PrettyPrintLogsServlet extends HttpServlet implements Servlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4555573278222613701L;
	Config Conf;
	//DocumentHelper docHelper;
	DocumentationHelper docHelper;

	public void init() throws ServletException {
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/test_report_html.xsl");
		//docHelper= new DocumentHelper(stylesheet);
		docHelper= new DocumentationHelper(stylesheet);
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			String sessionId = request.getParameter("session");
			String reportFileName = sessionId + ".html";
			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
	
			File sessiondir = new File(userdir, sessionId);
			File prettyPrintReportFile = new File(userdir, reportFileName);
			docHelper.prettyPrintsReport(sessiondir,prettyPrintReportFile);
			response.setContentType("text/html");
			response.setHeader("Content-Disposition", "attachment; filename="+reportFileName+";");
			response.setHeader("Cache-Control", "no-cache");
			byte[] buf = new byte[response.getBufferSize()];
			response.setContentLength((int)prettyPrintReportFile.length());
			System.out.println("file length : " + (int)prettyPrintReportFile.length());
			int length;

			BufferedInputStream fileInBuf = new BufferedInputStream(new FileInputStream (prettyPrintReportFile));

			OutputStream out = response.getOutputStream();

			while((length = fileInBuf.read(buf)) > 0) {
				out.write(buf, 0, length);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}


}
