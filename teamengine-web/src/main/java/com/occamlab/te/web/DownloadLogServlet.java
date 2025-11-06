/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * **************************************************************************
 *
 * Version Date: January 5, 2018
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Changes to address Fortity issues
 *
 * **************************************************************************
 */

package com.occamlab.te.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.occamlab.te.config.Config;
import com.occamlab.te.util.ZipUtils;

/**
 * Servlet implementation class for Servlet: DownloadLogServlet
 *
 */
public class DownloadLogServlet extends jakarta.servlet.http.HttpServlet implements jakarta.servlet.Servlet {

	Config Conf;

	public void init() throws ServletException {
		Conf = new Config();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			String sessionId = request.getParameter("session");
			String zipFileName = sessionId + ".zip";
			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
			File sessiondir = new File(userdir, sessionId);
			File zipFile = new File(userdir, zipFileName);
			ZipUtils.zipDir(zipFile, sessiondir);
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName + ";");
			response.setHeader("Cache-Control", "no-cache");
			byte[] buf = new byte[response.getBufferSize()];
			response.setContentLength((int) zipFile.length());
			System.out.println("file length : " + (int) zipFile.length());
			int length;
			BufferedInputStream fileInBuf = new BufferedInputStream(new FileInputStream(zipFile));
			OutputStream out = response.getOutputStream();
			while ((length = fileInBuf.read(buf)) > 0) {
				out.write(buf, 0, length);
			}
			// Fortify Mod: close the input stream
			fileInBuf.close();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
