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
package com.occamlab.te;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

public class ViewLog {
	
	static Element parse_log(DocumentBuilder db, Document owner, File logdir, String path) throws Exception {
		File log = new File(new File(logdir, path), "log.xml");
		Document logdoc = TECore.read_log(log.getParent(), ".");
		Element test = owner.createElement("test");
		Element log_e = (Element)logdoc.getElementsByTagName("log").item(0);
		NodeList children = log_e.getChildNodes();
		boolean complete = false;
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element)children.item(i);
				if (e.getNodeName().equals("starttest")) {
					NamedNodeMap atts = e.getAttributes();
					for (int j = 0; j < atts.getLength(); j++) {
						test.setAttribute(atts.item(j).getNodeName(), atts.item(j).getNodeValue());
					}
				} else if (e.getNodeName().equals("endtest")) {
					complete = true;
					if (Integer.parseInt(e.getAttribute("result")) > 1) {
						test.setAttribute("failed", "yes");
					}
				} else if (e.getNodeName().equals("testcall")) {
					test.appendChild(parse_log(db, owner, logdir, e.getAttribute("path")));
				}
			}
		}
		test.setAttribute("complete", complete ? "yes" : "no");
		return test;
	}

	public static boolean view_log(DocumentBuilder db, File logdir, String session, ArrayList tests, Templates templates, Writer out) throws Exception {
		Transformer t = templates.newTransformer();
		t.setParameter("logdir", logdir.getAbsolutePath());

		if (tests.size() == 0 && session == null) {
			Document doc = db.newDocument();
			Element sessions_e = doc.createElement("sessions");
			doc.appendChild(sessions_e);
			String[] children = logdir.list();
			for (int i = 0; i < children.length; i++) {
				if (new File(logdir, children[i]).isDirectory()) {
					Element session_e = doc.createElement("session");
					session_e.setAttribute("id", children[i]);
					sessions_e.appendChild(session_e);
				}
			}
			t.transform(new DOMSource(doc), new StreamResult(out));
			return true;
		} else if (tests.size() == 0) {
			File session_dir = new File(logdir, session);
			if (!session_dir.isDirectory()) {
				System.out.println("Error: Directory " + session_dir.getAbsolutePath() + " does not exist.");
				return false;
			}
			Document doc = db.newDocument();
			doc.appendChild(parse_log(db, doc, logdir, session));
			t.transform(new DOMSource(doc), new StreamResult(out));
			Element testElement = (Element)(doc.getElementsByTagName("test").item(0));
			return testElement.getAttribute("complete").equals("yes");
		} else {
			boolean ret = true;
			Iterator it = tests.iterator();
			while (it.hasNext()) {
				String test = (String)it.next();
				File f = new File(new File(logdir, test), "log.xml");
				if (f.exists()) {
					Document index = db.newDocument();
					index.appendChild(parse_log(db, index, logdir, test));
					t.setParameter("index", index);
					//          t.transform(new StreamSource(f), new StreamResult(System.out));
					Document log = TECore.read_log(logdir.getAbsolutePath(), test);
					t.transform(new DOMSource(log), new StreamResult(out));
					Element logElement = (Element)(log.getElementsByTagName("log").item(0));
					NodeList endtestlist = logElement.getElementsByTagName("endtest");
					ret = ret && (endtestlist.getLength() > 0);
				} else {
					System.out.println("Error: " + f.getAbsolutePath() + " does not exist.");
					ret = ret && false;
				}
			}
			return ret;
		}
	}

	public static void main(String[] args) throws Exception {
		File logdir = null;
		String session = null;
		ArrayList tests = new ArrayList();
		String cmd = "java com.occamlab.te.ViewLog";
		String style = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-style=")) {
				style = args[i].substring(7);
			} else if (args[i].startsWith("-cmd=")) {
				cmd = args[i].substring(5);
			} else if (args[i].startsWith("-logdir=")) {
				logdir = new File(args[i].substring(8));
			} else if (args[i].startsWith("-session=")) {
				session = args[i].substring(9);
			} else if (!args[i].startsWith("-")) {
				tests.add(args[i]);
			}
		}

		if (logdir == null) {
			System.out.println();
			System.out.println("To list sessions in a log directory:");
			System.out.println("  " + cmd + " -logdir=dir\n");
			System.out.println("To list tests in a session:");
			System.out.println("  " + cmd + " -logdir=dir -session=session\n");
			System.out.println("To view detailed results for tests:");
			System.out.println("  " + cmd + " -logdir=dir test1 [test2] ...");
			return;
		}

		File stylesheet = Test.getResourceAsFile("com/occamlab/te/logstyles/default.xsl");
		if (style != null) {
			stylesheet = new File(stylesheet.getParent(), style + ".xsl");
			if (!stylesheet.exists()) {
				System.out.println("Invalid style '" + style + "': " + stylesheet.getAbsolutePath() + " does not exist.");
				return;
			}
		}
		Templates templates = TransformerFactory.newInstance().newTemplates(new StreamSource(stylesheet));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Writer out = new OutputStreamWriter(System.out);
		view_log(db, logdir, session, tests, templates, out);
	}
}
