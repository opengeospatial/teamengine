package com.occamlab.te.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles (GET) requests to log out and terminate a test session.
 * 
 */
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 2713575227560756943L;

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            request.getSession().invalidate();
            response.sendRedirect(request.getContextPath());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}