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
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;
import com.oracle.webservices.api.message.PropertySet;
import java.util.Properties;

/**
 * Main request handler.
 * 
 */
public class TestServlet extends HttpServlet {
    public static final String CTL_NS = "http://www.occamlab.com/ctl";
    /** Alias is declared in the web app Context element. */
    private static final String ABOUT_ALIAS = "about/";

    private static Logger LOGR = Logger
            .getLogger("com.occamlab.te.web.TestServlet");

    DocumentBuilder DB;
    Transformer identityTransformer;
    Engine engine;
    Map<String, Index> indexes;
    Config conf;
    SetupOptions setupOpts;

    /**
     * Generates executable test suites from available CTL sources.
     */
    public void init() throws ServletException {
        try {
            conf = new Config();
            this.setupOpts = new SetupOptions();

            DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            identityTransformer = TransformerFactory.newInstance()
                    .newTransformer();

            indexes = new HashMap<String, Index>();

            HashMap<String, TEClassLoader> classLoaders = new HashMap<String, TEClassLoader>();

            Processor processor = new Processor(false);
            processor.setConfigurationProperty(FeatureKeys.XINCLUDE,
                    Boolean.TRUE);
            XsltCompiler sourceGeneratorCompiler = processor.newXsltCompiler();
            File sourceGeneratorStylesheet = Misc
                    .getResourceAsFile("com/occamlab/te/generate_source_html.xsl");
            XsltExecutable sourceGeneratorXsltExecutable = sourceGeneratorCompiler
                    .compile(new StreamSource(sourceGeneratorStylesheet));
            XsltTransformer sourceGeneratorTransformer = sourceGeneratorXsltExecutable
                    .load();
            // Generate simple HTML representation of CTL scripts referenced in
            // test report; use real location of web app context (e.g.
            // CATALINA_BASE/webapps/teamengine/)
            File listingsBaseDir = new File(getServletConfig()
                    .getServletContext().getRealPath("/"));
            File listings = new File(listingsBaseDir, "listings");
            if (!listings.mkdir() && !listings.exists()) {
                LOGR.warning("Failed to create directory at "
                        + listings.getAbsolutePath());
            }
            for (Entry<String, List<File>> sourceEntry : conf.getSources()
                    .entrySet()) {
                String sourcesName = sourceEntry.getKey();
                LOGR.fine("TestServlet - Processing Test Suite: " + sourcesName);
                setupOpts.setSourcesName(sourcesName);
                for (File source : sourceEntry.getValue()) {
                    setupOpts.addSource(source);
                }
                Index index = Generator.generateXsl(setupOpts);
                indexes.put(sourcesName, index);

                for (File ctlFile : index.getDependencies()) {
                    String encodedName = Generator.createEncodedName(ctlFile);
                    String basename = encodedName;
                    int i = basename.lastIndexOf('.');
                    if (i > 0) {
                        basename = basename.substring(0, i);
                    }
                    File indexFile = new File(new File(setupOpts.getWorkDir(),
                            encodedName), "index.xml");
                    File htmlFile = new File(listings, basename + ".html");
                    boolean needsGenerating = true;
                    if (htmlFile.exists()) {
                        needsGenerating = (indexFile.lastModified() > htmlFile
                                .lastModified());
                    }
                    if (needsGenerating) {
                        sourceGeneratorTransformer.setSource(new StreamSource(
                                ctlFile));
                        Serializer sourceGeneratorSerializer = new Serializer();
                        sourceGeneratorSerializer.setOutputFile(htmlFile);
                        sourceGeneratorTransformer
                                .setDestination(sourceGeneratorSerializer);
                        sourceGeneratorTransformer.transform();
                    }
                }
                File resourcesDir = conf.getResources().get(sourcesName);
                LOGR.config(String.format(
                        "Adding resources directory for %s: %s", sourcesName,
                        resourcesDir));
                classLoaders.put(sourcesName, new TEClassLoader(resourcesDir));
            }
            int cacheSize = 0;
            String s = getServletConfig().getInitParameter("cacheSize");
            if (s != null) {
                cacheSize = Integer.parseInt(s);
                LOGR.fine("Set cacheSize to " + s);
            }
            engine = new Engine(indexes.values(), classLoaders, cacheSize);
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        process(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        process(request, response);
    }

    public void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            FileItemFactory ffactory;
            ServletFileUpload upload;
            List<FileItem> items = null;
            HashMap<String, String> params = new HashMap<String, String>();
            boolean multipart = ServletFileUpload.isMultipartContent(request);
            if (multipart) {
                ffactory = new DiskFileItemFactory();
                upload = new ServletFileUpload(ffactory);
                items = upload.parseRequest(request);
                Iterator<FileItem> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = iter.next();
                    if (item.isFormField()) {
                        params.put(item.getFieldName(), item.getString());
                    }
                }
            } else {
                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String name = (String) paramNames.nextElement();
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
                LOGR.info("Creating test session in "
                        + logdir.getAbsolutePath());
                String mode = params.get("mode");
                RuntimeOptions opts = new RuntimeOptions();
                opts.setWorkDir(setupOpts.getWorkDir());
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
                    for (String key : new java.util.TreeSet<String>(
                            params.keySet())) {
                        if (key.startsWith("profile_")) {
                            String profileId = params.get(key);
                            int i = profileId.indexOf("}");
                            opts.addTestPath(sessionid + "/"
                                    + profileId.substring(i + 1));
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
                } else if (mode.equals("cache")) {
                    opts.setMode(Test.REDO_FROM_CACHE_MODE);
                    String sessionid = params.get("session");
                    opts.setSessionId(sessionid);
                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
                    ArrayList<String> profiles = new ArrayList<String>();
                    for (String key : new java.util.TreeSet<String>(
                            params.keySet())) {
                        if (key.startsWith("profile_")) {
                            String profileId = params.get(key);
                            profiles.add(profileId);
                            opts.addProfile(profileId);
                        }
                    }
                    s.setProfiles(profiles);
                } else {
                    opts.setMode(Test.TEST_MODE);
                    String sessionid = LogUtils.generateSessionId(logdir);
                    s.setSessionId(sessionid);
                    String sources = params.get("sources");
                    s.setSourcesName(sources);
                    SuiteEntry suite = conf.getSuites().get(sources);
                    s.setSuiteName(suite.getId());
                    String description = params.get("description");
                    s.setDescription(description);
                    opts.setSessionId(sessionid);
                    opts.setSourcesName(sources);
                    opts.setSuiteName(suite.getId());
                    ArrayList<String> profiles = new ArrayList<String>();
                    for (String key : new java.util.TreeSet<String>(
                            params.keySet())) {
                        if (key.startsWith("profile_")) {
                            String profileId = params.get(key);
                            profiles.add(profileId);
                            opts.addProfile(profileId);
                        }
                    }
                    s.setProfiles(profiles);
                    s.save(logdir);
                }
                String webdir = conf.getWebDirs().get(s.getSourcesName());
                URI contextURI = new URI(request.getScheme(), null,
                        request.getServerName(), request.getServerPort(),
                        request.getRequestURI(), null, null);
                if (webdir == null) {
                    webdir = ".";
                }
                URL baseURL = new URL(contextURI.toURL(), ABOUT_ALIAS + webdir
                        + "/");
                LOGR.fine("Base URL is " + baseURL);
                opts.setBaseURI(baseURL.toString());
                TECore core = new TECore(engine, indexes.get(opts
                        .getSourcesName()), opts);
                String servletURL = request.getRequestURL().toString();
                LOGR.fine("Request URL is " + servletURL);
                core.setTestServletURL(servletURL);
                MonitorServlet.setBaseServletURL(servletURL.substring(0,
                        servletURL.lastIndexOf('/')));
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
                TECore core = (TECore) session.getAttribute("testsession");
                if (core != null) {
                    core.stopThread();
                    session.removeAttribute("testsession");
                    out.println("<stopped/>");
                } else {
                    out.println("<message>Could not retrieve core object</message>");
                }
            } else if (operation.equals("GetStatus")) {
                TECore core = (TECore) session.getAttribute("testsession");
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
                // out.print(core.getOutput());
                out.print(URLEncoder.encode(core.getOutput(), "UTF-8").replace(
                        '+', ' '));
                out.println("]]>");
                out.println("</status>");
            } else if (operation.equals("GetForm")) {
                TECore core = (TECore) session.getAttribute("testsession");
                String html = core.getFormHtml();
                core.setFormHtml(null);
                response.setContentType("text/html");
                out.print(html);
            } else if (operation.equals("SubmitForm")) {
                TECore core = (TECore) session.getAttribute("testsession");
                Document doc = DB.newDocument();
                Element root = doc.createElement("values");
                doc.appendChild(root);
                for (String key : params.keySet()) {
                    if (!key.startsWith("te-")) {
                        Element valueElement = doc.createElement("value");
                        valueElement.setAttribute("key", key);
                        valueElement.appendChild(doc.createTextNode(params
                                .get(key)));
                        root.appendChild(valueElement);
                    }
                }
                if (multipart) {
                    Iterator iter = items.iterator();
                    while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();
                        if (!item.isFormField() && !item.getName().equals("")) {
                            File tempDir = new File(URI.create(core
                                    .getTestRunDirectory()));
                            File uploadedFile = new File(tempDir,
                                    StringUtils.getFilenameFromString(item
                                            .getName()));
                            item.write(uploadedFile);
                            Element valueElement = doc.createElement("value");
                            String key = item.getFieldName();
                            valueElement.setAttribute("key", key);
                            if (core.getFormParsers().containsKey(key)) {
                                Element parser = core.getFormParsers().get(key);
                                URL url = uploadedFile.toURI().toURL();
                                Element resp = core.parse(url.openConnection(),
                                        parser, doc);
                                Element content = DomUtils.getElementByTagName(
                                        resp, "content");
                                if (content != null) {
                                    Element child = DomUtils
                                            .getChildElement(content);
                                    if (child != null) {
                                        valueElement.appendChild(child);
                                    }
                                }
                            } else {
                                Element fileEntry = doc.createElementNS(CTL_NS,
                                        "file-entry");
                                fileEntry.setAttribute(
                                        "full-path",
                                        uploadedFile.getAbsolutePath().replace(
                                                '\\', '/'));
                                fileEntry.setAttribute("media-type",
                                        item.getContentType());
                                fileEntry.setAttribute("size",
                                        String.valueOf(item.getSize()));
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
                out.print("<body onload=\"window.parent.formSubmitted()\"></body>");
                out.println("</html>");
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
}
