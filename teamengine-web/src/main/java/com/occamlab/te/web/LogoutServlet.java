/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles (GET) requests to log out and terminate a test session.
 *
 */
public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 2713575227560756943L;

	public void init() throws ServletException {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			request.getSession().invalidate();
			response.sendRedirect(request.getContextPath());
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
