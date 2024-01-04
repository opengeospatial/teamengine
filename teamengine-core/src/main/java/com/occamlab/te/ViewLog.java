/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): 
    Charles Heazel (WiSC): Modifications to address Fortify issues

 ****************************************************************************/
package com.occamlab.te;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.html.EarlToHtmlTransformation;
import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.NullWriter;
import com.occamlab.te.util.TEPath;    // Fortify addition

/**
 * Presents a test log for display.
 *
 */
public class ViewLog {

  static public boolean hasCache = false;
  static String testName = " ";
  public static TransformerFactory transformerFactory = TransformerFactory
          .newInstance();

  public static boolean view_log(String suiteName, File logdir, String session,
          ArrayList<String> tests, Templates templates, Writer out) throws Exception {
    return view_log(suiteName, logdir, session, tests, templates, out, 1);
  }

  public static boolean view_log(String suiteName, File logdir, String session,
          ArrayList<String> tests, Templates templates, Writer out, int testnum)
          throws Exception {
	//Fortify mod: Validate logDir path
	TEPath tpath = new TEPath(logdir.getAbsolutePath());
	if(! tpath.isValid() ) {
	    System.out.println("ViewLog Error: Invalid log file name " + logdir);
	    return false;
	}
	hasCache = false;
    Transformer t;
    if (templates == null ) {
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
    	InputStream stream = cl.getResourceAsStream("com/occamlab/te/logstyles/default.xsl");
    	t = transformerFactory.newTemplates(new StreamSource(stream)).newTransformer();
    } else {
    	t = templates.newTransformer();
    }
    t.setParameter("sessionDir", session);
    t.setParameter("TESTNAME", suiteName);
    t.setParameter("logdir", logdir.getAbsolutePath());
    t.setParameter("testnum", Integer.toString(testnum));
    DocumentBuilder db = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder();

    if (tests.isEmpty() && session == null) {
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
    } else if (tests.isEmpty()) {
      File session_dir = new File(logdir, session);
      if (!session_dir.isDirectory()) {
        System.out.println("Error: Directory "
                + session_dir.getAbsolutePath() + " does not exist.");
        return false;
      }
      Document doc = LogUtils.makeTestList(logdir, session);
      if(doc == null){
    	  return false;
      }
      // increment_counts(doc.getDocumentElement());
      t.transform(new DOMSource(doc), new StreamResult(out));
      Element testElement = DomUtils.getElementByTagName(doc, "test");
      if (testElement == null) {
        return false;
      } else {
        setHasCache(testElement);
        return testElement.getAttribute("complete").equals("yes");
      }
    } else {
      boolean ret = true;
      for (String test: tests) {
        File f = new File(new File(logdir, test), "log.xml");
        if (f.exists()) {
          Document doc = LogUtils.makeTestList(logdir, test);
          if(doc == null){
        	  return false;
          }
          Element testElement = DomUtils.getElementByTagName(doc,
                  "test");
          if (testElement != null) {
            setHasCache(testElement);
          }
          t.setParameter("index", doc);
          Document log = LogUtils.readLog(logdir, test);
          t.transform(new DOMSource(log), new StreamResult(out));
          Element logElement = (Element) (log
                  .getElementsByTagName("log").item(0));
          NodeList endtestlist = logElement
                  .getElementsByTagName("endtest");
          ret = ret && (endtestlist.getLength() > 0);
        } else {
          System.out.println("Error: " + f.getAbsolutePath()
                  + " does not exist.");
          ret = ret && false;
        }
      }
      return ret;
    }
  }

  static void setHasCache(Element testElement) {
    String hasCacheAttributeValue = testElement.getAttribute("hasCache");
    hasCache = (hasCacheAttributeValue == null) ? false
            : (hasCacheAttributeValue.equals("yes") ? true : false);
  }

  public static boolean hasCache() {
    return hasCache;
  }

  public static boolean checkCache(File logdir, String session) throws Exception {
    view_log(null, logdir, session, new ArrayList<>(), null, new NullWriter(), 1);
    return hasCache;
  }

  public static void main(String[] args) throws Exception {
    String testName = null;
    File logDir = (new RuntimeOptions()).getLogDir();
    String session = null;
    ArrayList<String> tests = new ArrayList<>();
    String cmd = "java com.occamlab.te.ViewLog";
    String style = null;
    boolean listSessions = false;
    boolean ppLogs = false;
    boolean generateHtml = false;

    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-style=")) {
        style = args[i].substring(7);
      } else if (args[i].startsWith("-cmd=")) {
        cmd = args[i].substring(5);
      } else if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("-?")) {
          syntax(cmd);
          return;
      } else if (args[i].startsWith("-logdir=")) {
      	String path = args[i].substring(8);
        File file = new File(path);
        if (file.isAbsolute()) {
        	logDir = file;
        } else {
        	logDir = new File(SetupOptions.getBaseConfigDirectory(), path);
        }
      } else if (args[i].equals("-sessions")) {
    	  listSessions = true;
      } else if (args[i].startsWith("-session=")) {
        session = args[i].substring(9);
      } else if (args[i].equals("-pp")) {
    	  ppLogs = true;
      } else if (args[i].equals("-html")) {
    	  generateHtml = true;
      } else if (!args[i].startsWith("-")) {
        tests.add(args[i]);
      }
    }

    if (ppLogs) {
      if (session == null) {
        syntax(cmd);
        return;
      }
      File sessionDir = new File(logDir, session);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      DocumentationHelper docLogs = new DocumentationHelper(
                cl.getResource("com/occamlab/te/test_report_html.xsl"));
      File report = docLogs.prettyPrintsReport(sessionDir);
      if (report != null) {
    	System.out.println("Generated " + report);
      }
      return;
    }

    if (generateHtml) {
        if (session == null) {
          syntax(cmd);
          return;
        }
        File sessionDir = new File(logDir, session);
		File testLog = new File(sessionDir, "report_logs.xml");
		RuntimeOptions opts = new RuntimeOptions();
		opts.setLogDir(logDir);
		opts.setSessionId(session);
		Map<String, String> testInputMap = new HashMap<>();
		CtlEarlReporter reporter = new CtlEarlReporter();
		reporter.generateEarlReport(sessionDir, testLog, opts.getSourcesName(),	testInputMap);
		EarlToHtmlTransformation earlToHtml = new EarlToHtmlTransformation();
		System.out.println("Generated EARL report " + earlToHtml.findEarlResultFile(sessionDir.getAbsolutePath()));
		File htmlResult = earlToHtml.earlHtmlReport(sessionDir.getAbsolutePath());
		System.out.println("Generated HTML report " + new File(htmlResult, "index.html"));
    	return;
    }
    
    if (tests.isEmpty() && session == null && !listSessions) {
   		syntax(cmd);
    	return;
    }
    
    Templates templates = null;
    if (style != null) {
      File stylesheet = Misc.getResourceAsFile("com/occamlab/te/logstyles/default.xsl");
      stylesheet = new File(stylesheet.getParent(), style + ".xsl");
      if (!stylesheet.exists()) {
        System.out.println("Invalid style '" + style + "': " + stylesheet.getAbsolutePath() + " does not exist.");
        return;
      }
      templates = transformerFactory.newTemplates(new StreamSource(stylesheet));
    }


    Writer out = new OutputStreamWriter(System.out);
    view_log(testName, logDir, session, tests, templates, out);
  }
  
  static void syntax(String cmd) {
      System.out.println();
      System.out.println("To list user sessions:");
      System.out.println("  " + cmd + " [-logdir=dir] -sessions\n");
      System.out.println("To list tests in a session:");
      System.out.println("  " + cmd + " [-logdir=dir] -session=session\n");
      System.out.println("To view text results for individual tests:");
      System.out.println("  " + cmd + " [-logdir=dir] testpath1 [testpath2] ...\n");
      System.out.println("To \"Pretty Print\" the session results:");
      System.out.println("  " + cmd + " [-logdir=dir] -session=session -pp");
      System.out.println("To generate EARL and HTML reports of session results:");
      System.out.println("  " + cmd + " [-logdir=dir] -session=session -html");
  }
}
