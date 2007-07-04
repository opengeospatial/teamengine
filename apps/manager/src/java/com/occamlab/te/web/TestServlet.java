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
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItemFactory;

import com.occamlab.te.Test;
import com.occamlab.te.TECore;
import com.occamlab.te.TestDriverConfig;
import com.occamlab.te.util.StringUtils;

/**
 * Main request handler.
 *
 */
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 4553970234639898744L;

	public static final String CTL_NS = "http://www.occamlab.com/ctl";

	DocumentBuilder DB;
	Config conf;
	Map<String, Test> testDrivers;

	/**
	 * Generates executable test suites from available CTL sources.
	 */
	public void init() throws ServletException {
		try {
			DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			conf = new Config();
			File logsDir = new File(System.getProperty("catalina.base"), "logs");
			testDrivers = new HashMap<String, Test>();
			Map<String, List<File>> suites = conf.getAvailableSuites();
			Iterator it = suites.keySet().iterator();
			while (it.hasNext()) {
				String suiteId = (String) it.next();
				List<File> sources = suites.get(suiteId);
				TestDriverConfig driverConfig = new TestDriverConfig(suiteId,
						suiteId, sources, logsDir, true, Test.TEST_MODE);
				driverConfig.setWebAppContext(true);
				// generate executable test suites
				testDrivers.put(suiteId, new Test(driverConfig));
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {

		// Multipart form data
		if (ServletFileUpload.isMultipartContent(request)) {
			processMultipartFormData(request, response);
		}
		// Non-multipart
		else {
			processFormData(request, response);
		}
	}

	// Parse data for POST/GET method and process it accordingly
	public void processFormData(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		try {
			HttpSession session = request.getSession();
			ServletOutputStream out = response.getOutputStream();
			String operation = request.getParameter("te-operation");
			if (operation.equals("Test")) {
				TestSession s;
				String user = request.getRemoteUser();
				File userlogdir = new File(conf.getUsersDir(), user);
				String mode = request.getParameter("mode");
				if (mode.equals("retest")) {
					String sessionid = request.getParameter("session");
					String test = request.getParameter("test");
					if (sessionid == null) {
						int i = test.indexOf("/");
						sessionid = i > 0 ? test.substring(0, i) : test;
					}
					if (test == null) {
						test = sessionid;
					}
					s = TestSession.load(DB, userlogdir, sessionid);
					s.prepare(testDrivers, Test.RETEST_MODE, test);
				} else if (mode.equals("resume")) {
					String sessionid = request.getParameter("session");
					s = TestSession.load(DB, userlogdir, sessionid);
					s.prepare(testDrivers, Test.RESUME_MODE);
				} else {
					String sources = request.getParameter("sources");
					String suite = request.getParameter("suite");
					String description = request.getParameter("description");
					s = TestSession.create(userlogdir, sources, suite,
							description);
					s.prepare(testDrivers, Test.TEST_MODE);
				}
				Thread thread = new Thread(s);
				session.setAttribute("testsession", s);
				thread.start();
				response.setContentType("text/xml");
				out.println("<thread id=\"" + thread.getId()
						+ "\" sessionId=\"" + s.getSessionId() + "\"/>");
			} else if (operation.equals("Stop")) {
				session.removeAttribute("testsession");
				response.setContentType("text/xml");
				out.println("<stopped/>");
			} else if (operation.equals("GetStatus")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				response.setContentType("text/xml");
				out.print("<status");
				if (s.getCore().getFormHtml() != null) {
					out.print(" form=\"true\"");
				}
				if (s.isComplete()) {
					out.print(" complete=\"true\"");
					session.removeAttribute("testsession");
				}
				out.println(">");
				out.print("<![CDATA[");
				out.print(s.getOutput());
				out.println("]]>");
				out.println("</status>");
			} else if (operation.equals("GetForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				String html;
				synchronized (core) {
					html = core.getFormHtml();
					core.setFormHtml(null);
				}
				response.setContentType("text/html");
				out.print(html);
			} else if (operation.equals("SubmitForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				Document doc = DB.newDocument();
				Element root = doc.createElement("values");
				doc.appendChild(root);
				Iterator it = request.getParameterMap().keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					if (!key.startsWith("te-")) {
						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", key);
						valueElement.appendChild(doc.createTextNode(request
								.getParameter(key)));
						root.appendChild(valueElement);
					}
				}
				core.setFormResults(doc);
				response.setContentType("text/html");
				out.println("<html>");
				out.println("<head><title>Form Submitted</title></head>");
				out.print("<body onload=\"window.parent.update()\"></body>");
				out.println("</html>");
			} else if (operation.equals("SubmitPostForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				Document doc = DB.newDocument();
				Element root = doc.createElement("values");
				doc.appendChild(root);
				Iterator it = request.getParameterMap().keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					if (!key.startsWith("te-")) {
						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", key);
						valueElement.appendChild(doc.createTextNode(request
								.getParameter(key)));
						root.appendChild(valueElement);
					}
				}
				core.setFormResults(doc);
				response.setContentType("text/html");
				out.println("<html>");
				out.println("<head><title>Form Submitted</title></head>");
				out.print("<body onload=\"window.parent.update()\"></body>");
				out.println("</html>");
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	// Parse data for POST Multipart/form-data method and process it accordingly
	public void processMultipartFormData(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		try {
			// Get the parts as items
			FileItemFactory ffactory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(ffactory);
			List /* FileItem */ items = upload.parseRequest(request);

			String operation = "";
			String mode = "";
			String sessionid = "";
			String test = "";
			String sources = "";
			String suite = "";
			String description = "";

			// Process the uploaded items
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();

			    if (item.isFormField()) {
			        String name = item.getFieldName();
    			    	String value = item.getString();
    			    	if (name.equals("te-operation")) {
    			    		operation = value;
    			    	}
    			    	else if (name.equals("mode")) {
    			    		mode = value;
    			    	}
    			    	else if (name.equals("session")) {
    			    		sessionid = value;
    			    	}
    			    	else if (name.equals("test")) {
    			    		test = value;
    			    	}
    			    	else if (name.equals("sources")) {
    			    		sources = value;
    			    	}
    			    	else if (name.equals("suite")) {
    			    		suite = value;
    			    	}
    			    	else if (name.equals("description")) {
    			    		description = value;
    			    	}
			    }
			}

			HttpSession session = request.getSession();
			ServletOutputStream out = response.getOutputStream();
			if (operation.equals("Test")) {
				TestSession s;
				String user = request.getRemoteUser();
				File userlogdir = new File(conf.getUsersDir(), user);
				if (mode.equals("retest")) {
					if (sessionid == null) {
						int i = test.indexOf("/");
						sessionid = i > 0 ? test.substring(0, i) : test;
					}
					if (test == null) {
						test = sessionid;
					}
					s = TestSession.load(DB, userlogdir, sessionid);
					s.prepare(testDrivers, Test.RETEST_MODE, test);
				} else if (mode.equals("resume")) {
					s = TestSession.load(DB, userlogdir, sessionid);
					s.prepare(testDrivers, Test.RESUME_MODE);
				} else {
					s = TestSession.create(userlogdir, sources, suite,
							description);
					s.prepare(testDrivers, Test.TEST_MODE);
				}
				Thread thread = new Thread(s);
				session.setAttribute("testsession", s);
				thread.start();
				response.setContentType("text/xml");
				out.println("<thread id=\"" + thread.getId()
						+ "\" sessionId=\"" + s.getSessionId() + "\"/>");
			} else if (operation.equals("Stop")) {
				session.removeAttribute("testsession");
				response.setContentType("text/xml");
				out.println("<stopped/>");
			} else if (operation.equals("GetStatus")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				response.setContentType("text/xml");
				out.print("<status");
				if (s.getCore().getFormHtml() != null) {
					out.print(" form=\"true\"");
				}
				if (s.isComplete()) {
					out.print(" complete=\"true\"");
					session.removeAttribute("testsession");
				}
				out.println(">");
				out.print("<![CDATA[");
				out.print(s.getOutput());
				out.println("]]>");
				out.println("</status>");
			} else if (operation.equals("GetForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				String html;
				synchronized (core) {
					html = core.getFormHtml();
					core.setFormHtml(null);
				}
				response.setContentType("text/html");
				out.print(html);
			} else if (operation.equals("SubmitForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				Document doc = DB.newDocument();
				Element root = doc.createElement("values");
				doc.appendChild(root);
				Iterator params = items.iterator();
				while (params.hasNext()) {
				    FileItem param = (FileItem) params.next();
				    String name = param.getFieldName();

				    if (param.isFormField()) {
	    			    	String value = param.getString();
	    			    	if (!name.startsWith("te-")) {
						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", name);
						valueElement.appendChild(doc.createTextNode(value));
						root.appendChild(valueElement);
	    			    	}
				    }
	    			    else if (param.getName() != null) {
	    			    	if (!param.getName().equals("")) {
				        	File uploadedFile = new File(core.makeWorkingDir(), StringUtils.getFilenameFromString(param.getName()));
	    					param.write(uploadedFile);

						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", name);
				                Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
				                fileEntry.setAttribute("full-path", uploadedFile.getAbsolutePath().replace('\\','/'));
				                fileEntry.setAttribute("media-type", param.getContentType());
				                fileEntry.setAttribute("size", String.valueOf(param.getSize()));
				                valueElement.appendChild(fileEntry);
						root.appendChild(valueElement);
					}
	    			    }
				}
				core.setFormResults(doc);
				response.setContentType("text/html");
				out.println("<html>");
				out.println("<head><title>Form Submitted</title></head>");
				out.print("<body onload=\"window.parent.update()\"></body>");
				out.println("</html>");
			} else if (operation.equals("SubmitPostForm")) {
				TestSession s = (TestSession) session
						.getAttribute("testsession");
				TECore core = s.getCore();
				Document doc = DB.newDocument();
				Element root = doc.createElement("values");
				doc.appendChild(root);
				Iterator params = items.iterator();
				while (params.hasNext()) {
				    FileItem param = (FileItem) params.next();
				    String name = param.getFieldName();

				    if (param.isFormField()) {
	    			    	String value = param.getString();
	    			    	if (!name.startsWith("te-")) {
						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", name);
						valueElement.appendChild(doc.createTextNode(value));
						root.appendChild(valueElement);
	    			    	}
				    }
	    			    else if (param.getName() != null) {
	    			    	if (!param.getName().equals("")) {
				        	File uploadedFile = new File(core.makeWorkingDir(), StringUtils.getFilenameFromString(param.getName()));
	    					param.write(uploadedFile);

						Element valueElement = doc.createElement("value");
						valueElement.setAttribute("key", name);
				                Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
				                fileEntry.setAttribute("full-path", uploadedFile.getAbsolutePath().replace('\\','/'));
				                fileEntry.setAttribute("media-type", param.getContentType());
				                fileEntry.setAttribute("size", String.valueOf(param.getSize()));
				                valueElement.appendChild(fileEntry);
						root.appendChild(valueElement);
					}
	    			    }
				}
				core.setFormResults(doc);
				response.setContentType("text/html");
				out.println("<html>");
				out.println("<head><title>Form Submitted</title></head>");
				out.print("<body onload=\"window.parent.update()\"></body>");
				out.println("</html>");
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
