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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItemFactory;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.Test;
import com.occamlab.te.TECore;
import com.occamlab.te.TestDriverConfig;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;

/**
 * Main request handler.
 * 
 */
public class TestServlet extends HttpServlet {
    public static final String CTL_NS = "http://www.occamlab.com/ctl";

    DocumentBuilder DB;
    Engine engine;
    Map<String, Index> indexes;
    Config conf;


//    File getDir(String dirname) throws ServletException {
//        File dir = new File(getInitParameter(dirname + "Dir"));
//        if (!dir.isDirectory()) {
//            dir = new File("WEB-INF", dirname);
//        }
//        if (!dir.isDirectory()) {
//            throw new ServletException("Can't find " + dirname);
//        }
//        return dir;
//    }

    /**
     * Generates executable test suites from available CTL sources.
     */
    public void init() throws ServletException {
        try {
            conf = new Config();

            DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();

//            File logsDir = new File(System.getProperty("catalina.base"), "logs");

            indexes = new HashMap<String, Index>();
            
            Processor processor = new Processor(false);
            processor.setConfigurationProperty(FeatureKeys.XINCLUDE, Boolean.TRUE);
            XsltCompiler sourceGeneratorCompiler = processor.newXsltCompiler();
            File sourceGeneratorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_source_html.xsl");
            XsltExecutable sourceGeneratorXsltExecutable = sourceGeneratorCompiler.compile(new StreamSource(sourceGeneratorStylesheet));
            XsltTransformer sourceGeneratorTransformer = sourceGeneratorXsltExecutable.load();

            File listings = new File(getServletConfig().getServletContext().getRealPath("/"), "listings");
            listings.mkdir();

            for (Entry<String, List<File>> sourceEntry : conf.getSources().entrySet()) {
                String sourcesName = sourceEntry.getKey();
//              System.out.println("TestServlet: " + sourcesName);
                SetupOptions setupOpts = new SetupOptions();
                setupOpts.setWorkDir(conf.getWorkDir());
                setupOpts.setSourcesName(sourcesName);
                for (File source : sourceEntry.getValue()) {
                    setupOpts.addSource(source);
                }
                Index index = Generator.generateXsl(setupOpts);
                indexes.put(sourcesName, index);
                
                for (File ctlFile: index.getDependencies()) {
                    String encodedName = URLEncoder.encode(ctlFile.getAbsolutePath(), "UTF-8");
                    encodedName = encodedName.replace('%', '~');  // In Java 5, the Document.parse function has trouble with the URL % encoding
                    String basename = encodedName;
                    int i = basename.lastIndexOf('.');
                    if (i > 0) {
                        basename = basename.substring(0, i);
                    }
                    File indexFile = new File(new File(conf.getWorkDir(), encodedName), "index.xml");
                    File htmlFile = new File(listings, basename + ".html");
                    boolean needsGenerating = true;
                    if (htmlFile.exists()) {
                        needsGenerating = (indexFile.lastModified() > htmlFile.lastModified());
                    }
                    if (needsGenerating) {
                        sourceGeneratorTransformer.setSource(new StreamSource(ctlFile));
                        Serializer sourceGeneratorSerializer = new Serializer();
                        sourceGeneratorSerializer.setOutputFile(htmlFile);
                        sourceGeneratorTransformer.setDestination(sourceGeneratorSerializer);
                        sourceGeneratorTransformer.transform();
                    }
                }
            }
/*
            File scriptsDir = conf.getScriptsDir();
            String sourcesNames[] = scriptsDir.list();
            for (int i = 0; i < sourcesNames.length; i++) {
                String sourcesName = sourcesNames[i];
//System.out.println("TestServlet: " + sourcesName);
                SetupOptions setupOpts = new SetupOptions();
                setupOpts.setWorkDir(conf.getWorkDir());
                setupOpts.setSourcesName(sourcesName);
                File sourcesDir = new File(scriptsDir, sourcesName);
                File configFile = new File(sourcesDir, "config.xml");
                if (configFile.canRead()) {
                    // TODO: process config file
                    // For now, presence of config.xml means load ctl/main.xml or ctl/main.ctl
                    File ctlDir = new File(sourcesDir, "ctl");
                    File source = new File(ctlDir, "main.xml");
                    if (source.canRead()) {
                        setupOpts.addSource(source);
                    } else {
                        setupOpts.addSource(new File(ctlDir, "main.ctl"));
                    }
                } else {
                    setupOpts.addSource(new File(sourcesDir, "ctl"));
                }
                Index index = Generator.generateXsl(setupOpts);
                indexes.put(sourcesName, index);
            }
*/
            engine = new Engine(indexes.values());
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        processFormData(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        processFormData(request, response);
    }

    // Parse data for POST/GET method and process it accordingly
    public void processFormData(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        try {
            FileItemFactory ffactory;
            ServletFileUpload upload;
            List /* FileItem */ items = null;
            HashMap<String, String> params = new HashMap<String, String>();
            boolean multipart = ServletFileUpload.isMultipartContent(request);
            if (multipart) {
                ffactory = new DiskFileItemFactory();
                upload = new ServletFileUpload(ffactory);
                items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {
                        params.put(item.getFieldName(), item.getString());
                    }
                }
            } else {
                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String name = (String)paramNames.nextElement();
                    params.put(name, request.getParameter(name));
                }
            }
            HttpSession session = request.getSession();
            ServletOutputStream out = response.getOutputStream();
            String operation = params.get("te-operation");
            if (operation.equals("Test")) {
                TestSession s = new TestSession();
                String user = request.getRemoteUser();
                File logdir = new File(conf.getUsersDir(), user);
                String mode = params.get("mode");
                RuntimeOptions opts = new RuntimeOptions();
                opts.setWorkDir(conf.getWorkDir());
                opts.setLogDir(logdir);
                if (mode.equals("retest")) {
                    opts.setMode(Test.RETEST_MODE);
                    String sessionid = params.get("session");
                    String test = params.get("test");
                    if (sessionid == null) {
                        int i = test.indexOf("/");
                        sessionid = i > 0 ? test.substring(0, i) : test;
                    }
                    opts.setSessionId(sessionid);
                    if (test == null) {
                        opts.addTestPath(sessionid);
                    } else {
                        opts.addTestPath(test);
                    }
                    for (Entry<String,String> entry : params.entrySet()) {
                        if(entry.getKey().startsWith("profile_")) {
                            String profileId = entry.getValue();
                            int i = profileId.indexOf("}");
                            opts.addTestPath(sessionid + "/" + profileId.substring(i + 1));
                        }
                    }
                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
                } else if (mode.equals("resume")) {
                    opts.setMode(Test.RESUME_MODE);
                    String sessionid = params.get("session");
                    opts.setSessionId(sessionid);
                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
                } else {
                    opts.setMode(Test.TEST_MODE);
                    String sessionid = LogUtils.generateSessionId(logdir);
                    s.setSessionId(sessionid);
                    String sources = params.get("sources");
                    s.setSourcesName(sources);
                    SuiteEntry suite = conf.getSuites().get(sources);
                    s.setSuiteName(suite.getId());
//                    String suite = params.get("suite");
//                    s.setSuiteName(suite);
                    String description = params.get("description");
                    s.setDescription(description);
                    opts.setSessionId(sessionid);
                    opts.setSourcesName(sources);
                    opts.setSuiteName(suite.getId());
                    ArrayList<String> profiles = new ArrayList<String>();
                    for (Entry<String,String> entry : params.entrySet()) {
                        if(entry.getKey().startsWith("profile_")) {
                            profiles.add(entry.getValue());
                            opts.addProfile(entry.getValue());
                        }
                    }
                    s.setProfiles(profiles);
                    s.save(logdir);
                }
//System.out.println(opts.getSourcesName());
                TECore core = new TECore(engine, indexes.get(opts.getSourcesName()), opts);
//System.out.println(indexes.get(opts.getSourcesName()).toString());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                core.setOut(ps);
                core.setWeb(true);
                Thread thread = new Thread(core);
                session.setAttribute("testsession", core);
                thread.start();
                response.setContentType("text/xml");
                out.println("<thread id=\"" + thread.getId()
                        + "\" sessionId=\"" + s.getSessionId() + "\"/>");
            } else if (operation.equals("Stop")) {
                response.setContentType("text/xml");
                TECore core = (TECore)session.getAttribute("testsession");
                if (core != null) {
                    core.stopThread();
                    session.removeAttribute("testsession");
                    out.println("<stopped/>");
                } else {
                    out.println("<message>Could not retrieve core object</message>");
                }
            } else if (operation.equals("GetStatus")) {
                TECore core = (TECore)session.getAttribute("testsession");
                response.setContentType("text/xml");
                out.print("<status");
                if (core.getFormHtml() != null) {
                    out.print(" form=\"true\"");
                }
                if (core.isThreadComplete()) {
                    out.print(" complete=\"true\"");
                    session.removeAttribute("testsession");
                }
                out.println(">");
                out.print("<![CDATA[");
//                out.print(core.getOutput());
                out.print(URLEncoder.encode(core.getOutput(), "UTF-8").replace('+', ' '));
                out.println("]]>");
                out.println("</status>");
            } else if (operation.equals("GetForm")) {
                TECore core = (TECore)session.getAttribute("testsession");
                String html = core.getFormHtml();
                core.setFormHtml(null);
                response.setContentType("text/html");
                out.print(html);
            } else if (operation.equals("SubmitForm")) {
                TECore core = (TECore)session.getAttribute("testsession");
                Document doc = DB.newDocument();
                Element root = doc.createElement("values");
                doc.appendChild(root);
                for (String key : params.keySet()) {
                    if (!key.startsWith("te-")) {
                        Element valueElement = doc.createElement("value");
                        valueElement.setAttribute("key", key);
                        valueElement.appendChild(doc.createTextNode(params.get(key)));
                        root.appendChild(valueElement);
                    }
                }
                if (multipart) {
                    Iterator iter = items.iterator();
                    while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();
                        if (!item.isFormField() && !item.getName().equals("")) {
                            File uploadedFile = new File(core.getLogDir(),
                                StringUtils.getFilenameFromString(item.getName()));
                            item.write(uploadedFile);
                            Element valueElement = doc.createElement("value");
                            String key = item.getFieldName();
                            valueElement.setAttribute("key", key);
                            if (core.getFormParsers().containsKey(key)) {
                                Element parser = core.getFormParsers().get(key); 
                                URL url = uploadedFile.toURI().toURL();
                                Element resp = core.parse(url.openConnection(), parser, doc);
                                Element content = DomUtils.getElementByTagName(resp, "content");
                                if (content != null) {
                                    Element child = DomUtils.getChildElement(content);
                                    if (child != null) {
                                        valueElement.appendChild(child);
                                    }
                                }
                            } else {
                                Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
                                fileEntry.setAttribute("full-path", uploadedFile.getAbsolutePath().replace('\\','/'));
                                fileEntry.setAttribute("media-type", item.getContentType());
                                fileEntry.setAttribute("size", String.valueOf(item.getSize()));
                                valueElement.appendChild(fileEntry);
                            }
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
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
}
