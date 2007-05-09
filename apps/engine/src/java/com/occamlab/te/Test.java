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

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.ClassLoader;
import java.net.URLDecoder;

import net.sf.saxon.FeatureKeys;
import org.w3c.dom.*;
import java.util.*;
import javax.xml.validation.*;
import javax.xml.XMLConstants;


public class Test {
	public static final int TEST_MODE = 0;
	public static final int RETEST_MODE = 1;
	public static final int RESUME_MODE = 2;
	public static final int DOC_MODE = 4;
	public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
	public static final String TE_NS = "java:com.occamlab.te.TECore";
	public static final String CTL_NS = "http://www.occamlab.com/ctl";

	ClassLoader CL;
	DocumentBuilderFactory DBF;
	DocumentBuilder DB;
	TransformerFactory TF;
	Templates ScriptTemplates;
	/*
	static Document parameterizeDoc(Document doc) {
	Node root = doc.getDocumentElement();
	doc.removeChild(root);
	Element param = doc.createElement("param");
	param.appendChild(root);
	doc.appendChild(param);
	return doc;
	}

	static Document unParameterizeDoc(Document doc) {
	Node root = doc.getDocumentElement();
	doc.removeChild(root);
	Node content = root.getFirstChild();
	doc.appendChild(content);
	return doc;
	}
	*/
	
	// Prepare/compile the test file
	public Test(List sources, boolean validate, int mode) throws Exception {
		
		// Setup the DocumentBuilderFactory and DocumentBuilder
		System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration","org.apache.xerces.parsers.XIncludeParserConfiguration");
		DBF = DocumentBuilderFactory.newInstance();
		DBF.setNamespaceAware(true);
		DBF.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);		
		DB = DBF.newDocumentBuilder();

		// Setup the transformer to convert from CTL to engine-runnable test script
		TF = TransformerFactory.newInstance();
		TF.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
		TF.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
		CL = Thread.currentThread().getContextClassLoader();

		// Setup a CTL validator to check against the test file
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema ctl_schema = sf.newSchema(getResourceAsFile("com/occamlab/te/schemas/ctl.xsd"));
		Validator ctl_validator = ctl_schema.newValidator();
		CtlErrorHandler validation_eh = new CtlErrorHandler();
		ctl_validator.setErrorHandler(validation_eh);

		Transformer copy_t = TF.newTransformer();
		copy_t.setOutputProperty(OutputKeys.INDENT, "yes");
		File compile_file;
		String extension;
		// Transform the test file into a document (dxsl) or for actually running (txsl)
		if (mode == DOC_MODE) {
			compile_file = getResourceAsFile("com/occamlab/te/generate_dxsl.xsl");
			extension = "dxsl";
		} else {
			compile_file = getResourceAsFile("com/occamlab/te/compile.xsl");
			extension = "txsl";
		}
		// Prepare the XSLT transformers
		Transformer compile_t = TF.newTransformer(new StreamSource(compile_file));
		InputStream main_is = CL.getResourceAsStream("com/occamlab/te/main.xsl");
		Transformer main_t = TF.newTransformer(new StreamSource(main_is));

		char[] script_chars = null;
		Document script_doc = DB.newDocument();
		Node script_element = script_doc.createElement("script");
		script_doc.appendChild(script_element);

		// Goes through each test source file and compiles it together to run later (txsl file)
		Iterator it = sources.iterator();
		while (it.hasNext()) {
			File sourcefile = (File)it.next();
			Document txsl = DB.newDocument();
			DocumentBuilder inputDB = DBF.newDocumentBuilder();
			Document inputDoc = null;
			compile_t.clearParameters();
			main_t.clearParameters();
			// Get all test source files in a directory
			if (sourcefile.isDirectory()) {
				Element transform = txsl.createElementNS(XSL_NS, "xsl:transform");
				transform.setAttribute("version", "1.0");
				txsl.appendChild(transform);
				String[] children = sourcefile.list();
				for (int i = 0; i < children.length; i++) {
					// Finds all .ctl and .xml files in the directory to use
					if (children[i].toLowerCase().endsWith(".ctl") || children[i].toLowerCase().endsWith(".xml")) {
						File ctl_file = new File(sourcefile, children[i]);
						if (ctl_file.isFile()) {
							File txsl_file = new File(sourcefile, children[i].substring(0, children[i].length() - 3) + extension);
							boolean needs_compiling;
							if (txsl_file.exists()) {
								needs_compiling = txsl_file.lastModified() < ctl_file.lastModified() ||
								txsl_file.lastModified() < compile_file.lastModified();
							} else {
								needs_compiling = true;
							}
							if (needs_compiling) {
								try {
									int old_count = validation_eh.getErrorCount();
									if (validate) ctl_validator.validate(new StreamSource(ctl_file));
									if (validation_eh.getErrorCount() == old_count) {
										compile_t.setParameter("filename", ctl_file.getAbsolutePath());
										compile_t.setParameter("txsl_filename", txsl_file.toURL().toString());
										// OLD: compile_t.transform(new StreamSource(ctl_file), new StreamResult(txsl_file));
										inputDoc = inputDB.parse(ctl_file);
										compile_t.transform(new DOMSource(inputDoc), new StreamResult(txsl_file));
									}
								} catch (org.xml.sax.SAXException e) {
									System.exit(1);
								} catch (TransformerException e) {
									System.err.println(e.getMessageAndLocation());
									System.exit(1);
								}
							}
							Element include = txsl.createElementNS(XSL_NS, "xsl:include");
							include.setAttribute("href", txsl_file.toURL().toString());
							transform.appendChild(include);
						}
					}
				}
			} 
			// Or get the one specific test file
			else {
				try {
					int old_count = validation_eh.getErrorCount();
					if (validate)	ctl_validator.validate(new StreamSource(sourcefile));
					if (validation_eh.getErrorCount() == old_count) {
						compile_t.setParameter("filename", sourcefile.getAbsolutePath());
						// OLD: compile_t.transform(new StreamSource(sourcefile), new DOMResult(txsl));
						inputDoc = inputDB.parse(sourcefile);
						compile_t.transform(new DOMSource(inputDoc), new DOMResult(txsl));
					}
				} catch (org.xml.sax.SAXException e) {
					System.exit(1);
				} catch (TransformerException e) {
					System.err.println(e.getMessageAndLocation());
					System.exit(1);
				}
			}
			if (script_chars != null) {
				CharArrayReader car = new CharArrayReader(script_chars);
				copy_t.transform(new StreamSource(car), new DOMResult(script_element));
				main_t.setParameter("prev", script_element);
			}
			CharArrayWriter caw = new CharArrayWriter();
			main_t.transform(new DOMSource(txsl), new StreamResult(caw));
			//main_t.transform(new DOMSource(txsl), new StreamResult(System.out));
			script_chars = caw.toCharArray();
		}

		int error_count = validation_eh.getErrorCount();
		if (error_count > 0) {
			String msg = error_count + " validation error" + (error_count == 1 ? "" : "s");
			int warning_count = validation_eh.getWarningCount();
			if (warning_count > 0) {
				msg += " and " + warning_count + " warning" + (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			System.err.println(msg);
			System.exit(1);
		}

		// Continues to compile the source test files together
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
			tf.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
			tf.setErrorListener(new TeErrorListener(script_chars));
			ScriptTemplates = tf.newTemplates(new StreamSource(new CharArrayReader(script_chars)));
		} catch (TransformerException e) {
			System.err.println(e.getMessageAndLocation());
			System.exit(1);
		}
	}

	// Deletes a directory and all it's chilldren files
	public static void deleteDir(File dir) {
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++) {
			File f = new File(dir, children[i]);
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	// Deletes just the sub directories for a certain directory
	public static void deleteSubDirs(File dir) {
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++) {
			File f = new File(dir, children[i]);
			if (f.isDirectory()) {
				deleteDir(f);
			}
		}
	}

	// Loads a file into memory from the classpath
	public static File getResourceAsFile(String resource) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return new File(URLDecoder.decode(cl.getResource(resource).getFile(), "UTF-8"));
	}

	// Loads a DOM Document from the classpath
	Document getResourceAsDoc(String resource) throws Exception {
		InputStream is = CL.getResourceAsStream(resource);
		if (is != null) {
			Transformer t = TF.newTransformer();
			Document doc = DB.newDocument();
			t.transform(new StreamSource(is), new DOMResult(doc));
			return doc;
		} else {
			return null;
		}
	}

	// Get information on a specific test and return the information in a DOM Document
	Document getTemplateFromLog(File logdir, String callpath) throws Exception {
		Document logdoc;
		logdoc = TECore.read_log(logdir.getAbsolutePath(), callpath);
		Element starttest = (Element)logdoc.getElementsByTagName("starttest").item(0);
		String prefix = starttest.getAttribute("prefix");
		String namespace = starttest.getAttribute("namespace-uri");
		String local_name = starttest.getAttribute("local-name");
		Document doc = DB.newDocument();
		Element e = doc.createElementNS(namespace, prefix + ":" + local_name);
		doc.appendChild(e);
		return doc;
	}

	// Main test method
	public void test(int mode, File logdir, String suitename, String session, List tests, TECore core) throws Exception {
		// Setup session (log) directory
		File session_dir = null;

		if (session != null) session_dir = new File(logdir, session);

		if (logdir == null) {
			if (mode != TEST_MODE) {
				throw new Exception("logdir not specified.");
			}
		} else {
			if (!logdir.isDirectory()) {
				logdir.mkdir();
			}
			if (logdir.isDirectory()) {
				if (mode == TEST_MODE) {
					if (session_dir.isDirectory()) {
						File f = new File(session_dir, "log.xml");
						if (f.exists()) {
							f.delete();
						}
						deleteSubDirs(session_dir);
					}
				}
			} else {
				new Exception("Couldn't create log directory " + logdir.toString());
			}
		}

		// Prepare suite
		Map templates = new HashMap();
		if (tests.isEmpty()) {
			if (mode == RETEST_MODE) {
				// ToDo: Find failed tests
			} else if (mode == TEST_MODE) {
				Document doc = DB.newDocument();
				String namespace = null;
				String simple_name = "suite";
				if (suitename != null) {
					int i = suitename.lastIndexOf(",");
					if (i > 0) {
						namespace = suitename.substring(0, i);
						simple_name = suitename.substring(i + 1);
					} else {
						simple_name = suitename.replaceFirst(":", "-");
					}
				}
				Element e;
				if (namespace == null) {
					e = doc.createElement(simple_name);
				} else {
					e = doc.createElementNS(namespace, simple_name);
				}
				doc.appendChild(e);
				templates.put(session, doc);
				//        TF.newTransformer().transform(new DOMSource(doc), new StreamResult(System.out));
			} else if (mode == RESUME_MODE) {
				File log = new File(session_dir, "log.xml");
				if (log.exists()) {
					templates.put(session, getTemplateFromLog(logdir, session));
				} else {
					System.out.println("Error: Can't find " + log.getAbsolutePath());
					return;
				}
			}
		} else {
			Iterator it = tests.iterator();
			while (it.hasNext()) {
				String path = it.next().toString();
				templates.put(path, getTemplateFromLog(logdir, path));
			}
		}

		// Run each test and log the results
		Transformer t = ScriptTemplates.newTransformer();
		Iterator it = templates.keySet().iterator();
		while (it.hasNext()) {
			String path = (String)it.next();
			if (mode == RETEST_MODE) {
				File f = new File(logdir, path);
				deleteSubDirs(f);
			}
			Document doc = (Document)templates.get(path);
			t.clearParameters();
			if (logdir != null) {
				t.setParameter("{" + TE_NS + "}logdir", logdir.getCanonicalPath());
			}
			t.setParameter("{" + TE_NS + "}mode", Integer.toString(mode));
			t.setParameter("{" + TE_NS + "}starting-test-path", path);
			t.setParameter("{" + TE_NS + "}core", core);
			try {
				t.transform(new DOMSource(doc), new StreamResult(new ByteArrayOutputStream(16)));
			} catch (TransformerException e) {
				PrintWriter logger = core.getLogger();
				if (logger != null) {
					logger.println("<exception><![CDATA[" + e.getMessageAndLocation() + "]]></exception>");
					logger.println("<endtest result=\"3\"/>");
					core.close_log();
					while (core.getLogger() != null) {
						core.close_log();
					}
				}
				System.err.println(e.getMessageAndLocation());
			}
		}
	}

	// Determine next session number
	public static String newSessionId(File logdir) {
		int i = 1;
		String session = "s0001";
		while (new File(logdir, session).exists() && i < 10000) {
			i++;
			session = "s" + Integer.toString(10000 + i).substring(1);
		}
		return session;
	}

	public static void main(String[] args) throws Exception {
		int mode = TEST_MODE;
		boolean validate = true;
		File logdir = null;
		String session = null;
		String suite_name = null;
		ArrayList sources = new ArrayList();
		ArrayList tests = new ArrayList();
		String cmd = "java com.occamlab.te.Test";

		File f = getResourceAsFile("com/occamlab/te/compile.xsl");
		sources.add(new File(f.getParentFile(), "scripts"));

		// Parse arguments from command-line
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-cmd=")) {
				cmd = args[i].substring(5);
			} else if (args[i].startsWith("-source=")) {
				//OLD: sources.add(new File(args[i].substring(8)));
				boolean exists = new File(args[i].substring(8)).exists();
				File sourceFile = exists ? new File(args[i].substring(8)) : getResourceAsFile(args[i].substring(8));
				sources.add(sourceFile);
			} else if (args[i].startsWith("-package=")) {
				//OLD: File packagefile = new File(args[i].substring(9));
				boolean exists = new File(args[i].substring(9)).exists();
				File packagefile = exists ? new File(args[i].substring(9)) : getResourceAsFile(args[i].substring(9));
				sources.add(packagefile);
			} else if (args[i].startsWith("-sourcedir=")) {
				//OLD: File sourcedir = new File(args[i].substring(11));
				boolean exists = new File(args[i].substring(11)).exists();
				File sourcedir = exists ? new File(args[i].substring(11)) : getResourceAsFile(args[i].substring(11));
				sources.add(sourcedir);
			} else if (args[i].startsWith("-logdir=")) {
				logdir = new File(args[i].substring(8));
			} else if (args[i].startsWith("-session=")) {
				session = args[i].substring(9);
			} else if (args[i].startsWith("-suite=")) {
				suite_name = args[i].substring(7);
			} else if (args[i].equals("-mode=test")) {
				mode = TEST_MODE;
			} else if (args[i].equals("-mode=retest")) {
				mode = RETEST_MODE;
			} else if (args[i].equals("-mode=resume")) {
				mode = RESUME_MODE;
			} else if (args[i].equals("-mode=doc")) {
				mode = DOC_MODE;
			} else if (args[i].startsWith("-mode=")) {
				System.out.println("Error: Invalid mode.");
				return;
			} else if (args[i].equals("-validate=no")) {
				validate = false;
			} else if (!args[i].startsWith("-")) {
				if (mode == TEST_MODE) {
					suite_name = args[i];
				} else if (mode == RETEST_MODE) {
					tests.add(args[i]);
				}
			}
		}

		if (sources.size() == 1) {
			System.out.println();
			System.out.println("Test mode:");
			System.out.println("  Use to start a test session.\n");
			System.out.println("  " + cmd + " [-mode=test] -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
			System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name] [-logdir=dir] [-session=session]\n");
			System.out.println("Resume mode:");
			System.out.println("  Use to resume a test session that was interrupted before completion.\n");
			System.out.println("  " + cmd + " -mode=resume -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
			System.out.println("    -logdir=dir -session=session\n");
			System.out.println("Retest mode:");
			System.out.println("  Use to reexecute individual tests.\n");
			System.out.println("  " + cmd + " -mode=retest -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
			System.out.println("    -logdir=dir test1 [test2] ...\n");
			System.out.println("Doc mode:");
			System.out.println("  Use to generate a list of assertions.\n");
			System.out.println("  " + cmd + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
			System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
			return;
		}

		if (session == null) {
			if (mode == TEST_MODE || mode == DOC_MODE) {
				session = newSessionId(logdir);
			} else if (mode == RESUME_MODE) {
				System.out.println("Please provide a session parameter.");
				return;
			}
		}

		Thread.currentThread().setName("CTL Test Engine");
		
		// Setup/compile test object
		Test t = new Test(sources, validate, mode);
		if (mode == DOC_MODE) mode = TEST_MODE;
		TECore core = new TECore(System.out, false);
		// Run the compiled test object
		t.test(mode, logdir, suite_name, session, tests, core);
	}
}
