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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Test;
import com.occamlab.te.TECore;
import com.occamlab.te.TestDriverConfig;

/**
 * Encapsulates all information pertaining to a test session.
 */
public class TestSession implements Runnable, HttpSessionBindingListener {
	Test testDriver;

	File userLogDir;

	String suiteId;

	String sessionId;

	String description;

	String suiteName;

	int Mode;

	ArrayList<String> testList;

	TECore core;

	ByteArrayOutputStream Out;

	Thread ActiveThread = null;

	boolean Complete = false;

	/**
	 * Creates a new test session.
	 */
	static TestSession create(File logdir, String suiteId, String suiteName,
			String description) throws Exception {
		TestSession s = new TestSession();
		s.userLogDir = logdir;
		s.suiteId = suiteId;
		s.sessionId = TestDriverConfig.generateSessionId(logdir);
		s.suiteName = suiteName;
		s.description = description;
		File sessionDir = new File(logdir, s.sessionId);
		sessionDir.mkdir();
		PrintStream out = new PrintStream(new File(sessionDir, "session.xml"));
		out.println("<session id=\"" + s.sessionId + "\" sourcesId=\""
				+ s.suiteId + "\">");
		out.println("<suite>" + s.suiteName + "</suite>");
		out.println("<description>" + s.description + "</description>");
		out.println("</session>");
		return s;
	}

	/**
	 * Creates a test session from a previous run.
	 */
	public static TestSession load(DocumentBuilder db, File logdir,
			String sessionId) throws Exception {
		TestSession s = new TestSession();
		s.sessionId = sessionId;
		File sessionDir = new File(logdir, s.sessionId);
		Document doc = db.parse(new File(sessionDir, "session.xml"));
		Element session = (Element) (doc.getElementsByTagName("session")
				.item(0));
		s.suiteId = session.getAttribute("sourcesId");
		Element suite = (Element) (session.getElementsByTagName("suite")
				.item(0));
		s.suiteName = suite.getTextContent();
		Element description = (Element) (session
				.getElementsByTagName("description").item(0));
		s.description = description.getTextContent();
		s.userLogDir = logdir;
		return s;
	}

	void prepare(Map testClasses, int mode) {
		testDriver = (Test) testClasses.get(suiteId);
		TestDriverConfig driverConfig = testDriver.getDriverConfig();
		driverConfig.setLogDir(userLogDir);
		driverConfig.setMode(mode);
		driverConfig.setSessionId(sessionId);
		driverConfig.setSuiteName(suiteName);
		Mode = mode;
		testList = new ArrayList<String>();
	}

	void prepare(Map testClasses, int mode, String test) {
		prepare(testClasses, mode);
		if (mode == Test.RETEST_MODE) {
			testList.add(test);
		}
	}

	synchronized public String getOutput() {
		String output = Out.toString();
		Out.reset();
		return output;
	}

	public boolean isComplete() {
		return Complete;
	}

	public TECore getCore() {
		return core;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getDescription() {
		return description;
	}

	public void run() {
		assert (null != testDriver) : "Test driver has not been initialized in TestSession.prepare().";
		try {
			ActiveThread = Thread.currentThread();
			userLogDir.mkdir();
			Out = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(Out);
			core = new TECore(ps, true);
			testDriver.test(testList, core);
			ps.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		ActiveThread = null;
		Complete = true;
	}

	public void valueBound(HttpSessionBindingEvent e) {
	}

	public void valueUnbound(HttpSessionBindingEvent e) {
		if (e.getName().equals("testsession")) {
			if (ActiveThread != null) {
				ActiveThread.interrupt();
			}
		}
	}
}
