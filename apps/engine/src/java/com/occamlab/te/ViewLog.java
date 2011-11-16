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

 Contributor(s): Paul Daisey (Image Matters LLC) add hasCache for cache mode

 ****************************************************************************/
package com.occamlab.te;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

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

import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;

/**
 * Presents a test log for display.
 * 
 */
public class ViewLog {

    // Count how many errors and passes for output later
//    static public int passCount = 0;
//    static public int failCount = 0;
//    static public int warnCount = 0;

	static public boolean hasCache = false; // 2011-06-27 PwD
	
    public static TransformerFactory transformerFactory = TransformerFactory.newInstance();

//    static void increment_counts(Element test) {
//        int result = Integer.parseInt(test.getAttribute("result"));
//        String complete = test.getAttribute("complete");
//        if (!("no".equals(complete))) {
//            if (result == TECore.PASS) {
//                passCount++;
//            } else if (result == TECore.WARNING) {
//                warnCount++;
//            } else if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
//                failCount++;
//            }
//        }
//        for (Element subtest : DomUtils.getChildElements(test)) {
//            increment_counts(subtest);
//        }
//    }

//    static Element parse_log(DocumentBuilder db, Document owner, File logdir,
//            String path) throws Exception {
//        File log = new File(new File(logdir, path), "log.xml");
//        Document logdoc = LogUtils.readLog(log.getParentFile(), ".");
//        Element test = owner.createElement("test");
//        Element log_e = (Element) logdoc.getElementsByTagName("log").item(0);
//        NodeList children = log_e.getChildNodes();
//        boolean complete = false;
//        for (int i = 0; i < children.getLength(); i++) {
//            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
//                Element e = (Element) children.item(i);
//                if (e.getNodeName().equals("starttest")) {
//                    NamedNodeMap atts = e.getAttributes();
//                    for (int j = 0; j < atts.getLength(); j++) {
//                        test.setAttribute(atts.item(j).getNodeName(), atts
//                                .item(j).getNodeValue());
//                    }
//                } else if (e.getNodeName().equals("endtest")) {
//                    complete = true;
//                    if (Integer.parseInt(e.getAttribute("result")) == 3) {
//                        failCount++;
//                        test.setAttribute("failed", "yes");
//                    }
//                    if (Integer.parseInt(e.getAttribute("result")) == 1) {
//                        warnCount++;
//                        test.setAttribute("warning", "yes");
//                    }
//                    if (Integer.parseInt(e.getAttribute("result")) == 2) {
//                        failCount++;
//                    }
//                    if (Integer.parseInt(e.getAttribute("result")) == 0) {
//                        passCount++;
//                    }
//                } else if (e.getNodeName().equals("testcall")) {
//                    test.appendChild(parse_log(db, owner, logdir, e
//                            .getAttribute("path")));
//                }
//            }
//        }
//        test.setAttribute("complete", complete ? "yes" : "no");
//        return test;
//    }

    public static boolean view_log(File logdir, String session, ArrayList tests, Templates templates, Writer out) throws Exception {
//        passCount = 0;
//        failCount = 0;
//        warnCount = 0;
    	hasCache = false; // 2011-06-27 PwD
        Transformer t = templates.newTransformer();
        t.setParameter("logdir", logdir.getAbsolutePath());
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

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
            Document doc = LogUtils.makeTestList(logdir, session);
//            increment_counts(doc.getDocumentElement());
            t.transform(new DOMSource(doc), new StreamResult(out));
            Element testElement = DomUtils.getElementByTagName(doc, "test");
            if (testElement == null) {
                return false;
            } else {
            	setHasCache(testElement); // 2011-06-27 PwD
                return testElement.getAttribute("complete").equals("yes");
            }
        } else {
            boolean ret = true;
            Iterator it = tests.iterator();
            while (it.hasNext()) {
                String test = (String) it.next();
                File f = new File(new File(logdir, test), "log.xml");
                if (f.exists()) {
                    Document doc = LogUtils.makeTestList(logdir, test);
                    // begin 2011-06-27 PwD
                    Element testElement = DomUtils.getElementByTagName(doc, "test");
                    if (testElement != null) {
                    	setHasCache(testElement);
                    }
                    // end 2011-06-27 PwD
                    t.setParameter("index", doc);
                    // t.transform(new StreamSource(f), new StreamResult(System.out));
                    Document log = LogUtils.readLog(logdir, test);
                    t.transform(new DOMSource(log), new StreamResult(out));
                    Element logElement = (Element) (log.getElementsByTagName("log").item(0));
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
    
 // begin 2011-06-27 PwD
    static void setHasCache(Element testElement) {
    	String hasCacheAttributeValue = testElement.getAttribute("hasCache");
    	hasCache = (hasCacheAttributeValue == null) ? false 
    			 : (hasCacheAttributeValue.equals("yes") ? true : false);
    	
    }
    
    public static boolean hasCache() {
    	return hasCache;
    }
 // end 2011-06-27 PwD
    
    public static void main(String[] args) throws Exception {
        File logdir = null;
        String session = null;
        ArrayList<String> tests = new ArrayList<String>();
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

        File stylesheet = Misc.getResourceAsFile("com/occamlab/te/logstyles/default.xsl");
        if (style != null) {
            stylesheet = new File(stylesheet.getParent(), style + ".xsl");
            if (!stylesheet.exists()) {
                System.out.println("Invalid style '" + style + "': "
                        + stylesheet.getAbsolutePath() + " does not exist.");
                return;
            }
        }

        Templates templates = transformerFactory.newTemplates(new StreamSource(
                stylesheet));

        Writer out = new OutputStreamWriter(System.out);
        view_log(logdir, session, tests, templates, out);
    }
}
