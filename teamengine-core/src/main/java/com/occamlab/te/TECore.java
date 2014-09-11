/**
 * **************************************************************************
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is TEAM Engine.
 *
 * The Initial Developer of the Original Code is Northrop Grumman Corporation
 * jointly with The National Technology Alliance. Portions created by Northrop
 * Grumman Corporation are Copyright (C) 2005-2006, Northrop Grumman
 * Corporation. All Rights Reserved.
 *
 * Contributor(s): S. Gianfranceschi (Intecs): Added the SOAP suport
 *
 ***************************************************************************
 */
package com.occamlab.te;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.S9APIUtils;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.ParserEntry;
import com.occamlab.te.index.ProfileEntry;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TemplateEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.saxon.ObjValue;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.IOUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;
import com.occamlab.te.util.SoapUtils;
import com.occamlab.te.util.URLConnectionUtils;

import java.util.Date;
import org.w3c.dom.Comment;

/**
 * Provides various utility methods to support test execution and logging.
 * Primary ones include implementation and execution of ctl:suite, ctl:profile,
 * ctl:test, ctl:function, ctl:request and ctl:soap-request instructions, and
 * invocation of any parsers specified therein.
 * 
 */
public class TECore implements Runnable {

    private static final Logger LOGR = Logger.getLogger(TECore.class.getName());
    public static final String SOAP_V_1_1 = "1.1";
    public static final String SOAP_V_1_2 = "1.2";
    Engine engine; // Engine object
    Index index;
    RuntimeOptions opts;
    String testServletURL = null;
    volatile PrintStream out; // Console destination
    boolean web = false; // True when running as a servlet
    String testPath; // Uniquely identifies a test instance
    String fnPath = ""; // Uniquely identifies an XSL function instance within a
                        // test instance
    String indent = ""; // Contains the appropriate number of spaces for the
                        // current indent level
    String contextLabel = ""; // Current context label set by ctl:for-each
    String testType = "Mandatory"; // Type of current test
    String defaultResultName = "Pass"; // Default result name for current test
    int defaultResult = PASS; // Default result for current test
    /** Test verdict for current test */
    private int verdict;
    Document prevLog = null; // Log document for current test from previous test
                             // execution (resume and retest modes only)
    // Log document for suite to enable use of getLogCache by profile test
    Document suiteLog = null;
    PrintWriter logger = null; // Logger for current test
    volatile String formHtml; // HTML representation for an active form
    volatile Document formResults; // Holds form results until they are
                                   // retrieved
    Map<String, Element> formParsers = new HashMap<String, Element>();
    Map<Integer, Object> functionInstances = new HashMap<Integer, Object>();
    Map<String, Object> parserInstances = new HashMap<String, Object>();
    Map<String, Method> parserMethods = new HashMap<String, Method>();
    LinkedList<TestEntry> testStack = new LinkedList<TestEntry>();
    volatile boolean threadComplete = false;
    volatile boolean stop = false;
    volatile ByteArrayOutputStream threadOutput;
    public static final int CONTINUE = -1;
    public static final int BEST_PRACTICE = 0;
    public static final int PASS = 1;
    public static final int NOT_TESTED = 2;
    public static final int SKIPPED = 3;
    public static final int WARNING = 4;
    public static final int INHERITED_FAILURE = 5;
    public static final int FAIL = 6;

    public static final int MANDATORY = 0;
    public static final int MANDATORY_IF_IMPLEMENTED = 1;
    public static final int OPTIONAL = 2;

    static final String XSL_NS = Test.XSL_NS;
    static final String CTL_NS = Test.CTL_NS;
    static final String TE_NS = Test.TE_NS;
    static final String INDENT = "   ";
    static final QName TECORE_QNAME = new QName("te", TE_NS, "core");
    static final QName TEPARAMS_QNAME = new QName("te", TE_NS, "params");
    static final QName LOCALNAME_QNAME = new QName("local-name");
    static final QName LABEL_QNAME = new QName("label");
    static final String HEADER_BLOCKS = "header-blocks";
    private static Logger jlogger = Logger.getLogger("com.occamlab.te.TECore");

    public TECore() {

    }

    public TECore(Engine engine, Index index, RuntimeOptions opts) {
        this.engine = engine;
        this.index = index;
        this.opts = opts;
        testPath = opts.getSessionId();
        out = System.out;
    }

    public TestEntry getParentTest() {
        if (testStack.size() < 2) {
            return testStack.peek();
        } else {
            return testStack.get(1);
        }
    }

    public String getParamsXML(List<String> params) throws Exception {
        String paramsXML = "<params>";
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            String name = param.substring(0, param.indexOf('='));
            String value = param.substring(param.indexOf('=') + 1);
            if (params.get(i).indexOf('=') != 0) {
                paramsXML += "<param local-name=\""
                        + name
                        + "\" namespace-uri=\"\" prefix=\"\" type=\"xs:string\">";
                paramsXML += "<value><![CDATA[" + value + "]]></value>";
                paramsXML += "</param>";
            }
        }
        paramsXML += "</params>";
        // System.out.println("paramsXML: "+paramsXML);
        return paramsXML;
    }

    XPathContext getXPathContext(TestEntry test, String sourcesName,
            XdmNode contextNode) throws Exception {
        XPathContext context = null;
        if (test.usesContext()) {
            XsltExecutable xe = engine.loadExecutable(test, sourcesName);
            Executable ex = xe.getUnderlyingCompiledStylesheet()
                    .getExecutable();
            context = new XPathContextMajor(contextNode.getUnderlyingNode(), ex);
        }
        return context;
    }

    // Execute tests
    public void execute() throws Exception {
        try {
            TestEntry grandParent = new TestEntry();
            grandParent.setType("Mandatory");
            testStack.push(grandParent);
            String sessionId = opts.getSessionId();
            // File logDir = opts.getLogDir();
            int mode = opts.getMode();
            // String sourcesName = opts.getSourcesName();
            ArrayList<String> params = opts.getParams();

            if (mode == Test.RESUME_MODE) {
                reexecute_test(sessionId);
            } else if (mode == Test.REDO_FROM_CACHE_MODE) {
                reexecute_test(sessionId);
            } else if (mode == Test.RETEST_MODE) {
                for (String testPath : opts.getTestPaths()) {
                    reexecute_test(testPath);
                }
            } else if (mode == Test.TEST_MODE) {
                String testName = opts.getTestName();
                if (testName != null) {
                    XdmNode contextNode = opts.getContextNode();
                    execute_test(testName, params, contextNode);
                } else {
                    String suiteName = opts.getSuiteName();
                    List<String> profiles = opts.getProfiles();
                    if (suiteName != null || profiles.size() == 0) {
                        execute_suite(suiteName, params);
                    }
                    if (profiles.contains("*")) {
                        for (String profile : index.getProfileKeys()) {
                            try {
                                execute_profile(profile, params, false);
                            } catch (Exception e) {
                                jlogger.log(Level.WARNING, e.getMessage(),
                                        e.getCause());
                            }
                        }
                    } else {
                        for (String profile : profiles) {
                            try {
                                execute_profile(profile, params, true);
                            } catch (Exception e) {
                                jlogger.log(Level.WARNING, e.getMessage(),
                                        e.getCause());
                            }
                        }
                    }
                }
            } else {
                throw new Exception("Unsupported mode");
            }
        } finally {
            if (!web) {
                SwingForm.destroy();
            }
            if (opts.getLogDir() != null) {
                // Create xml execution report file
                LogUtils.createFullReportLog(opts.getLogDir().getAbsolutePath()
                        + File.separator + opts.getSessionId());
            }
        }
    }

    public void reexecute_test(String testPath) throws Exception {
        Document log = LogUtils.readLog(opts.getLogDir(), testPath);
        String testId = LogUtils.getTestIdFromLog(log);
        TestEntry test = index.getTest(testId);
        net.sf.saxon.s9api.DocumentBuilder builder = engine.getBuilder();
        XdmNode paramsNode = LogUtils.getParamsFromLog(builder, log);
        XdmNode contextNode = LogUtils.getContextFromLog(builder, log);
        XPathContext context = getXPathContext(test, opts.getSourcesName(),
                contextNode);
        setTestPath(testPath);
        executeTest(test, paramsNode, context);
        if (testPath.equals(opts.getSessionId())) {
            // Profile not executed in retest mode
            suiteLog = LogUtils.readLog(opts.getLogDir(), testPath);
            ArrayList<String> params = opts.getParams();
            List<String> profiles = opts.getProfiles();
            if (profiles.contains("*")) {
                for (String profile : index.getProfileKeys()) {
                    try {
                        execute_profile(profile, params, false);
                    } catch (Exception e) {
                        jlogger.log(Level.WARNING, e.getMessage(), e.getCause());
                    }
                }
            } else {
                for (String profile : profiles) {
                    try { // 2011-12-21 PwD
                        execute_profile(profile, params, true);
                    } catch (Exception e) {
                        jlogger.log(Level.WARNING, e.getMessage(), e.getCause());
                    }
                }
            }
        }
    }

    public int execute_test(String testName, List<String> params,
            XdmNode contextNode) throws Exception {
        if (LOGR.isLoggable(Level.FINE)) {
            String logMsg = String.format(
                    "Preparing test %s for execution, using params:%n %s",
                    testName, params);
            LOGR.fine(logMsg);
        }
        TestEntry test = index.getTest(testName);
        if (test == null) {
            throw new Exception("Error: Test " + testName + " not found.");
        }
        XdmNode paramsNode = engine.getBuilder().build(
                new StreamSource(new StringReader(getParamsXML(params))));
        if (contextNode == null && test.usesContext()) {
            String contextNodeXML = "<context><value>" + test.getContext()
                    + "</value></context>";
            contextNode = engine.getBuilder().build(
                    new StreamSource(new StringReader(contextNodeXML)));
        }
        XPathContext context = getXPathContext(test, opts.getSourcesName(),
                contextNode);
        return executeTest(test, paramsNode, context);
    }

    public void execute_suite(String suiteName, List<String> params)
            throws Exception {
        SuiteEntry suite = null;
        if (suiteName == null) {
            Iterator<String> it = index.getSuiteKeys().iterator();
            if (!it.hasNext()) {
                throw new Exception("Error: No suites in sources.");
            }
            suite = index.getSuite(it.next());
            if (it.hasNext()) {
                throw new Exception(
                        "Error: Suite name must be specified since there is more than one suite in sources.");
            }
        } else {
            suite = index.getSuite(suiteName);
            if (suite == null) {
                throw new Exception("Error: Suite " + suiteName + " not found.");
            }
        }
        defaultResultName = suite.getDefaultResult();
        defaultResult = defaultResultName.equals("BestPractice") ? BEST_PRACTICE
                : PASS;
        testStack.peek().setDefaultResult(defaultResult);
        testStack.peek().setResult(defaultResult);

        ArrayList<String> kvps = new ArrayList<String>();
        kvps.addAll(params);
        Document form = suite.getForm();
        if (form != null) {
            Document results = (Document) form(form, suite.getId());
            for (Element value : DomUtils
                    .getElementsByTagName(results, "value")) {
                kvps.add(value.getAttribute("key") + "="
                        + value.getTextContent());
            }
        }
        String name = suite.getPrefix() + ":" + suite.getLocalName();
        out.println("Testing suite " + name + " in " + getMode()
                + " with defaultResult of " + defaultResultName + " ...");
        setIndentLevel(1);
        int result = execute_test(suite.getStartingTest().toString(), kvps,
                null);
        out.print("Suite " + suite.getPrefix() + ":" + suite.getLocalName()
                + " ");
        if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
            out.println("Failed");
        } else if (result == TECore.BEST_PRACTICE) {
            out.println("Passed as Best Practice");
        } else {
            out.println("Passed");
        }
    }

    public void execute_profile(String profileName, List<String> params,
            boolean required) throws Exception {
        ProfileEntry profile = index.getProfile(profileName);
        if (profile == null) {
            throw new Exception("Error: Profile " + profileName + " not found.");
        }
        SuiteEntry suite = index.getSuite(profile.getBaseSuite());
        if (suite == null) {
            throw new Exception("Error: The base suite ("
                    + profile.getBaseSuite().toString() + ") for the profile ("
                    + profileName + ") not found.");
        }
        String sessionId = opts.getSessionId();
        Document log = LogUtils.readLog(opts.getLogDir(), sessionId);
        if (log == null) {
            execute_suite(suite.getId(), params);
            log = LogUtils.readLog(opts.getLogDir(), sessionId);
        }
        suiteLog = log;
        String testId = LogUtils.getTestIdFromLog(log);
        List<String> baseParams = LogUtils.getParamListFromLog(
                engine.getBuilder(), log);
        TestEntry test = index.getTest(testId);
        if (suite.getStartingTest().equals(test.getQName())) {
            ArrayList<String> kvps = new ArrayList<String>();
            kvps.addAll(baseParams);
            kvps.addAll(params);
            Document form = profile.getForm();
            if (form != null) {
                Document results = (Document) form(form, profile.getId());
                for (Element value : DomUtils.getElementsByTagName(results,
                        "value")) {
                    kvps.add(value.getAttribute("key") + "="
                            + value.getTextContent());
                }
            }
            setTestPath(sessionId + "/" + profile.getLocalName());
            String name = profile.getPrefix() + ":" + profile.getLocalName();
            out.println("\nTesting profile " + name + "...");
            Document baseLog = LogUtils.makeTestList(opts.getLogDir(),
                    sessionId, profile.getExcludes());
            Element baseTest = DomUtils.getElement(baseLog);
            // out.println(DomUtils.serializeNode(baseLog));
            out.print(TECore.INDENT + "Base tests from suite "
                    + suite.getPrefix() + ":" + suite.getLocalName() + " ");
            String summary = "Not complete";
            if ("yes".equals(baseTest.getAttribute("complete"))) {
                int baseResult = Integer.parseInt(baseTest
                        .getAttribute("result"));
                if (baseResult == TECore.FAIL
                        || baseResult == TECore.INHERITED_FAILURE) {
                    summary = "Failed";
                } else if (verdict == TECore.BEST_PRACTICE) {
                    summary = "Passed as Best Practice";
                } else if (verdict == TECore.SKIPPED) {
                    summary = "Skipped";
                } else {
                    summary = "Passed";
                }
            }
            out.println(summary);
            setIndentLevel(1);
            String defaultResultName = profile.getDefaultResult();
            defaultResult = defaultResultName.equals("BestPractice") ? BEST_PRACTICE
                    : PASS;
            out.println("\nExecuting profile " + name
                    + " with defaultResult of " + defaultResultName + "...");
            int result = execute_test(profile.getStartingTest().toString(),
                    kvps, null);
            out.print("Profile " + profile.getPrefix() + ":"
                    + profile.getLocalName() + " ");
            if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
                summary = "Failed";
            } else if (result == TECore.BEST_PRACTICE) {
                summary = "Passed as Best Practice";
            } else if (verdict == TECore.SKIPPED) {
                summary = "Skipped";
            } else {
                summary = "Passed";
            }
            out.println(summary);
        } else {
            if (required) {
                throw new Exception("Error: Profile " + profileName
                        + " is not a valid profile for session " + sessionId
                        + ".");
            }
        }
    }

    public XdmNode executeTemplate(TemplateEntry template, XdmNode params,
            XPathContext context) throws Exception {
        if (stop) {
            throw new Exception("Execution was stopped by the user.");
        }
        XsltExecutable executable = engine.loadExecutable(template,
                opts.getSourcesName());
        XsltTransformer xt = executable.load();
        XdmDestination dest = new XdmDestination();
        xt.setDestination(dest);
        if (template.usesContext() && context != null) {
            xt.setSource((NodeInfo) context.getContextItem());
        } else {
            xt.setSource(new StreamSource(new StringReader("<nil/>")));
        }
        xt.setParameter(TECORE_QNAME, new ObjValue(this));
        if (params != null) {
            xt.setParameter(TEPARAMS_QNAME, params);
        }
        // test may set global verdict, e.g. by calling ctl:fail
        if (LOGR.isLoggable(Level.FINE)) {
            LOGR.log(Level.FINE,
                    "Executing TemplateEntry {0}" + template.getQName());
        }
        xt.transform();
        XdmNode ret = dest.getXdmNode();
        return ret;
    }

    static String getLabel(XdmNode n) {
        String label = n.getAttributeValue(LABEL_QNAME);
        if (label == null) {
            XdmNode value = (XdmNode) n.axisIterator(Axis.CHILD).next();
            XdmItem childItem = null;
            try {
                childItem = value.axisIterator(Axis.CHILD).next();
            } catch (Exception e) {
                // Not an error
            }
            if (childItem == null) {
                XdmSequenceIterator it = value.axisIterator(Axis.ATTRIBUTE);
                if (it.hasNext()) {
                    label = it.next().getStringValue();
                } else {
                    label = "";
                }
            } else if (childItem.isAtomicValue()) {
                label = childItem.getStringValue();
            } else if (childItem instanceof XdmNode) {
                XdmNode n2 = (XdmNode) childItem;
                if (n2.getNodeKind() == XdmNodeKind.ELEMENT) {
                    label = "<" + n2.getNodeName().toString() + ">";
                } else {
                    label = n2.toString();
                }
            }
        }
        return label;
    }

    String getAssertionValue(String text, XdmNode paramsVar) {
        if (text.indexOf("$") < 0) {
            return text;
        }

        String newText = text;
        XdmNode params = (XdmNode) paramsVar.axisIterator(Axis.CHILD).next();
        XdmSequenceIterator it = params.axisIterator(Axis.CHILD);
        while (it.hasNext()) {
            XdmNode n = (XdmNode) it.next();
            QName qname = n.getNodeName();
            if (qname != null) {
                String tagname = qname.getLocalName();
                if (tagname.equals("param")) {
                    String name = n.getAttributeValue(LOCALNAME_QNAME);
                    String label = getLabel(n);
                    newText = StringUtils.replaceAll(newText,
                            "{$" + name + "}", label);
                }
            }
        }
        newText = StringUtils.replaceAll(newText, "{$context}", contextLabel);
        return newText;
    }

    static String getResultDescription(int result) {
        if (result == CONTINUE) {
            return "Inconclusive! Continue Test";
        } else if (result == BEST_PRACTICE) {
            return "Passed as Best Practice";
        } else if (result == PASS) {
            return "Passed";
        } else if (result == NOT_TESTED) {
            return "Not Tested";
        } else if (result == SKIPPED) {
            return "Skipped - Prerequisites not satisfied";
        } else if (result == WARNING) {
            return ("Warning");
        } else if (result == INHERITED_FAILURE) {
            return "Failed - Inherited)";
        } else {
            return "Failed";
        }
    }

    public int executeTest(TestEntry test, XdmNode params, XPathContext context)
            throws Exception {
        testStack.push(test);
        testType = test.getType();
        defaultResult = test.getDefaultResult();
        defaultResultName = (defaultResult == BEST_PRACTICE) ? "BestPractice"
                : "Pass";
        Document oldPrevLog = prevLog;
        if (opts.getMode() == Test.RESUME_MODE) {
            prevLog = readLog();
        } else if (opts.getMode() == Test.REDO_FROM_CACHE_MODE) {
            prevLog = readLog();
        } else {
            prevLog = null;
        }
        String assertion = getAssertionValue(test.getAssertion(), params);
        out.print("Testing ");
        out.print(test.getName() + " type " + test.getType());
        out.print(" in " + getMode() + " with defaultResult "
                + defaultResultName + " ");
        out.println("(" + testPath + ")...");
        String oldIndent = indent;
        indent += INDENT;
        if (test.usesContext()) {
            out.println(indent + "Context: " + test.getContext());
        }
        out.println(indent + "Assertion: " + assertion);

        PrintWriter oldLogger = logger;
        if (opts.getLogDir() != null) {
            logger = createLog();
            logger.println("<log>");
            logger.print("<starttest ");
            logger.print("local-name=\"" + test.getLocalName() + "\" ");
            logger.print("prefix=\"" + test.getPrefix() + "\" ");
            logger.print("namespace-uri=\"" + test.getNamespaceURI() + "\" ");
            logger.print("type=\"" + test.getType() + "\" ");
            logger.print("defaultResult=\""
                    + Integer.toString(test.getDefaultResult()) + "\" ");
            logger.print("path=\"" + testPath + "\" ");
            logger.println("file=\"" + test.getTemplateFile().getAbsolutePath()
                    + "\">");
            logger.println("<assertion>" + StringUtils.escapeXML(assertion)
                    + "</assertion>");
            if (params != null) {
                logger.println(params.toString());
            }
            if (test.usesContext()) {
                logger.print("<context label=\""
                        + StringUtils.escapeXML(contextLabel) + "\">");
                logger.print("<value>");
                logger.print(test.getContext());
                logger.print("</value>");
                logger.println("</context>");
            }
            logger.println("</starttest>");
            logger.flush();
        }

        this.verdict = defaultResult;
        try {
            // Note: Test may alter default result
            executeTemplate(test, params, context);
        } catch (SaxonApiException e) {
            jlogger.log(Level.SEVERE, e.getMessage(), e.getCause());
            out.println(e.getMessage());
            if (logger != null) {
                logger.println("<exception><![CDATA[" + e.getMessage()
                        + "]]></exception>");
            }
            TestEntry parentTest = getParentTest();
            if (verdict == CONTINUE) {
                if ("Optional".equals(testType)) {
                    verdict = parentTest.getResult();
                } else {
                    verdict = INHERITED_FAILURE;
                    parentTest.setResult(verdict);
                }
            } else {
                verdict = FAIL; // all other exceptions
                if (null != parentTest) {
                    parentTest.setResult(verdict);
                }
            }
            if(!testStack.isEmpty()){
            testStack.pop();
            }
        }

        if (logger != null) {
            logger.println("<endtest result=\"" + Integer.toString(verdict)
                    + "\"/>");
            logger.println("</log>");
            logger.close();
        }
        logger = oldLogger;
        prevLog = oldPrevLog;
        indent = oldIndent;
        out.println(indent + "Test " + test.getName() + " "
                + getResultDescription(verdict));
        test.setResult(verdict);
        if (LOGR.isLoggable(Level.FINE)) {
            String msg = String.format("Executed test %s - Verdict: %s",
                    test.getLocalName(), getResultDescription(verdict));
            LOGR.log(Level.FINE, msg);
        }
        return verdict;
    }

    /**
     * Runs a subtest as directed by a &lt;ctl:call-test&gt; instruction.
     * 
     * @param context
     *            The context in which the subtest is executed.
     * @param localName
     *            The [local name] of the subtest.
     * @param namespaceURI
     *            The [namespace name] of the subtest.
     * @param params
     *            A NodeInfo object containing test parameters.
     * @param callId
     *            A node identifier used to build a file path reference for the
     *            test results.
     * @throws Exception
     *             If an error occcurs while executing the test.
     */
    public synchronized void callTest(XPathContext context, String localName,
            String namespaceURI, NodeInfo params, String callId)
            throws Exception {
        String key = "{" + namespaceURI + "}" + localName;
        TestEntry test = index.getTest(key);

        if (logger != null) {
            logger.println("<testcall path=\"" + testPath + "/" + callId
                    + "\"/>");
            logger.flush();
        }
        if (opts.getMode() == Test.RESUME_MODE) {
            Document doc = LogUtils.readLog(opts.getLogDir(), testPath + "/"
                    + callId);
            int result = LogUtils.getResultFromLog(doc);
            // TODO revise the following
            if (result >= 0) {
                out.println(indent + "Test " + test.getName() + " "
                        + getResultDescription(result));
                if (result == WARNING) {
                    warning();
                } else if (result == CONTINUE) {
                    throw new IllegalStateException(
                            "Error: 'continue' is not allowed when a test is called using 'call-test' instruction");
                } else if (result != PASS) {
                    inheritedFailure();
                }
                return;
            }
        }

        String oldTestPath = testPath;
        testPath += "/" + callId;
        executeTest(test, S9APIUtils.makeNode(params), context);
        testPath = oldTestPath;
        // called test result has been set; now setting parent result
        if (verdict == CONTINUE) {
            throw new IllegalStateException(
                    "Error: 'continue' is not allowed when a test is called using 'call-test' instruction");
        }
        updateParentTestResult(test);
        testStack.pop();
    }

    /**
     * Modifies the result of the parent test according to the result of the
     * current test. The parent test will be 'tainted' with an inherited failure
     * if (a) a subtest failed, or (b) a non-optional subtest was skipped.
     * 
     * @param currTest
     *            The TestEntry for the current test.
     */
    private void updateParentTestResult(TestEntry currTest) {
        TestEntry parentTest = getParentTest();
        if (null == parentTest)
            return;
        if (LOGR.isLoggable(Level.FINE)) {
            LOGR.log(
                    Level.FINE,
                    "Entered setParentTestResult with TestEntry {0} (result={1})",
                    new Object[] { currTest.getQName(), this.verdict });
            LOGR.log(
                    Level.FINE,
                    "Parent TestEntry is {0} (result={1})",
                    new Object[] { parentTest.getQName(),
                            parentTest.getResult() });
        }
        switch (this.verdict) {
        case FAIL:
        case INHERITED_FAILURE:
            parentTest.setResult(INHERITED_FAILURE);
            break;
        case SKIPPED:
            if (!parentTest.getType().equalsIgnoreCase("Optional")) {
                parentTest.setResult(INHERITED_FAILURE);
            }
            break;
        default:
            break;
        }
    }

    public void repeatTest(XPathContext context, String localName,
            String NamespaceURI, NodeInfo params, String callId, int count,
            int pause) throws Exception {
        String key = "{" + NamespaceURI + "}" + localName;
        TestEntry test = index.getTest(key);

        if (logger != null) {
            logger.println("<testcall path=\"" + testPath + "/" + callId
                    + "\"/>");
            logger.flush();
        }
        if (opts.getMode() == Test.RESUME_MODE) {
            Document doc = LogUtils.readLog(opts.getLogDir(), testPath + "/"
                    + callId);
            int result = LogUtils.getResultFromLog(doc);
            if (result >= 0) {
                out.println(indent + "Test " + test.getName() + " "
                        + getResultDescription(result));
                if (result == WARNING) {
                    warning();
                } else if (result != PASS) {
                    inheritedFailure();
                }
                return;
            }
        }
        int oldResult = verdict;
        String oldTestPath = testPath;
        testPath += "/" + callId;

        for (int i = 0; i < count; i++) {
            executeTest(test, S9APIUtils.makeNode(params), context);
            testPath = oldTestPath;
            if (verdict == FAIL && oldResult != FAIL) {
                // If the child result was FAIL and parent hasn't directly
                // failed,
                // set parent result to INHERITED_FAILURE
                verdict = INHERITED_FAILURE;

                return;
            } else if (verdict == CONTINUE) {
                // System.out.println("Pausing for..."+pause);
                if (pause > 0 && i < count - 1) {

                    try {

                        Thread.sleep(pause);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if (verdict <= oldResult) {
                // Restore parent result if the child results aren't worse
                verdict = oldResult;
                return;

            }
        }
        verdict = FAIL;
        if (oldResult != FAIL) {
            // If the child result was FAIL and parent hasn't directly failed,
            // set parent result to INHERITED_FAILURE
            verdict = INHERITED_FAILURE;
        }

    }

    public NodeInfo executeXSLFunction(XPathContext context, FunctionEntry fe,
            NodeInfo params) throws Exception {
        String oldFnPath = fnPath;
        CRC32 crc = new CRC32();
        crc.update((fe.getPrefix() + fe.getId()).getBytes());
        fnPath += Long.toHexString(crc.getValue()) + "/";
        XdmNode n = executeTemplate(fe, S9APIUtils.makeNode(params), context);
        fnPath = oldFnPath;
        if (n == null) {
            return null;
        }
        return n.getUnderlyingNode();
    }

    public Object callFunction(XPathContext context, String localName,
            String namespaceURI, NodeInfo params) throws Exception {
        // System.out.println("callFunction {" + NamespaceURI + "}" +
        // localName);
        String key = "{" + namespaceURI + "}" + localName;
        List<FunctionEntry> functions = index.getFunctions(key);
        Node paramsNode = NodeOverNodeInfo.wrap(params);
        List<Element> paramElements = DomUtils.getElementsByTagName(paramsNode,
                "param");
        for (FunctionEntry fe : functions) {
            if (!fe.isJava()) {
                boolean valid = true;
                for (Element el : paramElements) {
                    String uri = el.getAttribute("namespace-uri");
                    String name = el.getAttribute("local-name");
                    String prefix = el.getAttribute("prefix");
                    javax.xml.namespace.QName qname = new javax.xml.namespace.QName(
                            uri, name, prefix);
                    if (!fe.getParams().contains(qname)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    return executeXSLFunction(context, fe, params);
                }
            }
        }

        for (FunctionEntry fe : functions) {
            if (fe.isJava()) {
                int argCount = paramElements.size();
                if (fe.getMinArgs() >= argCount && fe.getMaxArgs() <= argCount) {
                    TEClassLoader cl = engine.getClassLoader(opts
                            .getSourcesName());
                    Method method = Misc.getMethod(fe.getClassName(),
                            fe.getMethod(), cl, argCount);
                    Class<?>[] types = method.getParameterTypes();
                    Object[] args = new Object[argCount];
                    for (int i = 0; i < argCount; i++) {
                        Element el = DomUtils.getElementByTagName(
                                paramElements.get(i), "value");
                        if (types[i].equals(String.class)) {
                            Map<javax.xml.namespace.QName, String> attrs = DomUtils
                                    .getAttributes(el);
                            if (attrs.size() > 0) {
                                args[i] = attrs.values().iterator().next();
                            } else {
                                args[i] = el.getTextContent();
                            }
                        } else if (types[i].toString().equals("char")) {
                            args[i] = el.getTextContent().charAt(0);
                        } else if (types[i].toString().equals("boolean")) {
                            args[i] = Boolean.parseBoolean(el.getTextContent());
                        } else if (types[i].toString().equals("byte")) {
                            args[i] = Byte.parseByte(el.getTextContent());
                        } else if (types[i].toString().equals("short")) {
                            args[i] = Short.parseShort(el.getTextContent());
                        } else if (types[i].toString().equals("int")) {
                            args[i] = Integer.parseInt(el.getTextContent());
                        } else if (types[i].toString().equals("long")) {
                            args[i] = Long.parseLong(el.getTextContent());
                        } else if (types[i].toString().equals("float")) {
                            args[i] = Float.parseFloat(el.getTextContent());
                        } else if (types[i].toString().equals("double")) {
                            args[i] = Double.parseDouble(el.getTextContent());
                        } else if (Document.class.isAssignableFrom(types[i])) {
                            args[i] = DomUtils.createDocument(DomUtils
                                    .getChildElement(el));
                        } else if (NodeList.class.isAssignableFrom(types[i])) {
                            args[i] = el.getChildNodes();
                        } else if (Node.class.isAssignableFrom(types[i])) {
                            args[i] = el.getFirstChild();
                        } else {
                            throw new Exception("Error: Function " + key
                                    + " uses unsupported Java type "
                                    + types[i].toString());
                        }
                    }
                    try {
                        Object instance = null;
                        if (fe.isInitialized()) {
                            // String instkey = fe.getId() + "," +
                            // Integer.toString(fe.getMinArgs()) + "," +
                            // Integer.toString(fe.getMaxArgs());
                            instance = getFunctionInstance(fe.hashCode());
                            if (instance == null) {
                                try {
                                    instance = Misc.makeInstance(
                                            fe.getClassName(),
                                            fe.getClassParams(), cl);
                                    putFunctionInstance(fe.hashCode(), instance);
                                } catch (Exception e) {
                                    throw new XPathException(e);
                                }
                            }
                        }
                        return method.invoke(instance, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        String msg = "Error invoking function " + fe.getId()
                                + "\n" + cause.getClass().getName();
                        if (cause.getMessage() != null) {
                            msg += ": " + cause.getMessage();
                        }
                        jlogger.log(Level.SEVERE, "InvocationTargetException",
                                e);

                        throw new Exception(msg, cause);
                    }
                }
            }
        }
        throw new Exception("No function {" + namespaceURI + "}" + localName
                + " with a compatible signature.");
    }

    public void _continue() {
        verdict = CONTINUE;
    }

    public void bestPractice() {
        if (verdict < BEST_PRACTICE) {
            verdict = BEST_PRACTICE;
        }
    }

    public void notTested() {
        if (verdict < NOT_TESTED) {
            verdict = NOT_TESTED;
        }
    }

    public void skipped() {
        if (verdict < SKIPPED) {
            verdict = SKIPPED;
        }
    }

    /**
     * A test with defaultResult of BEST_PRACTICE.
     */
    public void pass() {
        if (verdict < PASS) {
            verdict = PASS;
        }
    }

    public void warning() {
        if (verdict < WARNING) {
            verdict = WARNING;
        }
    }

    public void inheritedFailure() {
        if (verdict < INHERITED_FAILURE) {
            verdict = INHERITED_FAILURE;
        }
    }

    public void fail() {
        if (verdict < FAIL) {
            verdict = FAIL;
        }
    }

    public String getResult() {
        return getResultDescription(verdict);
    }

    public String getMode() {
        return Test.getModeName(opts.getMode());
    }

    public void setContextLabel(String label) {
        contextLabel = label;
    }

    public String getFormHtml() {
        return formHtml;
    }

    public void setFormHtml(String html) {
        this.formHtml = html;
    }

    public Document getFormResults() {
        return formResults;
    }

    public void setFormResults(Document doc) {
        this.formResults = doc;
    }

    public Map<String, Element> getFormParsers() {
        return formParsers;
    }

    public Document readLog() throws Exception {
        return LogUtils.readLog(opts.getLogDir(), testPath);
    }

    public PrintWriter createLog() throws Exception {
        return LogUtils.createLog(opts.getLogDir(), testPath);
    }

    // Get a File pointer to a file reference (in XML)
    public static File getFile(NodeList fileNodes) {
        File file = null;
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element e = (Element) fileNodes.item(i);
            String type = e.getAttribute("type");

            try {
                // URL, File, or Resource
                if (type.equals("url")) {
                    URL url = new URL(e.getTextContent());
                    file = new File(url.toURI());
                } else if (type.equals("file")) {
                    file = new File(e.getTextContent());
                } else if (type.equals("resource")) {
                    ClassLoader cl = Thread.currentThread()
                            .getContextClassLoader();
                    file = new File(cl.getResource(e.getTextContent())
                            .getFile());
                } else {
                    System.out
                            .println("Incorrect file reference:  Unknown type!");
                }
            } catch (Exception exception) {
                System.err.println("Error getting file. "
                        + exception.getMessage());
                jlogger.log(Level.SEVERE,
                        "Error getting file. " + exception.getMessage(), e);

                return null;
            }
        }
        return file;
    }

    // BEGIN SOAP SUPPORT
    public NodeList soap_request(Document ctlRequest, String id)
            throws Throwable {
        Element request = (Element) ctlRequest.getElementsByTagNameNS(
                Test.CTL_NS, "soap-request").item(0);
        if (opts.getMode() == Test.RESUME_MODE && prevLog != null) {
            for (Element request_e : DomUtils.getElementsByTagName(prevLog,
                    "soap-request")) {
                if (request_e.getAttribute("id").equals(fnPath + id)) {
                    logger.println(DomUtils.serializeNode(request_e));
                    logger.flush();
                    Element response_e = DomUtils.getElementByTagName(
                            request_e, "response");
                    Element content_e = DomUtils.getElementByTagName(
                            response_e, "content");
                    return content_e.getChildNodes();
                    // return DomUtils.getChildElement(content_e);
                }
            }
        }

        String logTag = "<soap-request id=\"" + fnPath + id + "\">\n";
        logTag += DomUtils.serializeNode(request) + "\n";
        // if (logger != null) {
        // logger.println("<request id=\"" + fnPath + id + "\">");
        // logger.println(DomUtils.serializeNode(request));
        // }
        Exception ex = null;
        Element response = null;
        Element parserInstruction = null;
        NodeList nl = request.getChildNodes();
        long elapsedTime = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && !n.getNamespaceURI().equals(CTL_NS)) {
                parserInstruction = (Element) n;
            }
        }
        try {
            Date before = new Date();
            URLConnection uc = build_soap_request(request);
            response = parse(uc, parserInstruction);
            Date after = new Date();
            elapsedTime = after.getTime() - before.getTime();

            // Adding the exchange time in the response as comment the format is
            // the following
            // <!--Response received in [XXX] milliseconds-->
            // the comment is included in the first tag of the response
            // SOAP:Envelope in case a SOAP message is returned the specific
            // interface tag if a SOAP parser is applied
            Element content = DomUtils.getElementByTagName(response, "content");
            if (content != null) {
                nl = content.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Document doc;
                        doc = response.getOwnerDocument();
                        Comment comm = doc
                                .createComment("Response received in ["
                                        + elapsedTime + "] milliseconds");
                        n.appendChild(comm);
                    }
                }
            }

            logTag += DomUtils.serializeNode(response) + "\n";
            jlogger.log(Level.FINE, DomUtils.serializeNode(response));
        } catch (Exception e) {
            ex = e;
        }
        logTag += "<!-- elapsed time :" + elapsedTime + " (milliseconds) -->";
        logTag += "</soap-request>";
        if (logger != null) {
            logger.println(logTag);
            logger.flush();
        }
        if (ex == null) {
            Element parser = DomUtils.getElementByTagName(response, "parser");
            if (parser != null) {
                String text = parser.getTextContent();
                if (text.length() > 0) {
                    out.println(parser.getTextContent());
                }
            }
            Element content = DomUtils.getElementByTagName(response, "content");
            return content.getChildNodes();
        } else {
            throw ex;
        }
    }

    // Create and send a soap request over HTTP then return an HttpResponse
    // (HttpResponse)
    /**
     * Create SOAP request, sends it and return an URL Connection ready to be
     * parsed.
     * 
     * @param xml
     *            the soap-request node (from CTL)
     * 
     * @return The URL Connection
     * 
     * @throws Exception
     *             the exception
     * 
     *             <soap-request version="1.1|1.2" charset="UTF-8">
     *             <url>http://blah</url> <action>Some-URI</action> <headers>
     *             <header MutUnderstand="true" rely="true" role="http://etc">
     *             <t:Transaction xmlns:t="some-URI" >5</t:Transaction>
     *             </header> </headers> <body> <m:GetLastTradePrice
     *             xmlns:m="Some-URI"> <symbol>DEF</symbol>
     *             </m:GetLastTradePrice> </body> <parsers:SOAPParser
     *             return="content"> <parsers:XMLValidatingParser>
     *             <parsers:schemas> <parsers:schema
     *             type="url">http://blah/schema.xsd</parsers:schema>
     *             </parsers:schemas> </parsers:XMLValidatingParser>
     *             </parsers:SOAPParser> </soap-request>
     */
    static public URLConnection build_soap_request(Node xml) throws Exception {
        String sUrl = null;
        String method = "POST";
        String charset = ((Element) xml).getAttribute("charset").equals("") ? ((Element) xml)
                .getAttribute("charset") : "UTF-8";
        String version = ((Element) xml).getAttribute("version");
        String action = "";
        String contentType = "";
        Element body = null;

        // Read in the test information (from CTL)
        NodeList nl = xml.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = (Node) nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getLocalName().equals("url")) {
                    sUrl = n.getTextContent();
                } else if (n.getLocalName().equals("action")) {
                    action = n.getTextContent();
                } // else if (n.getLocalName().equals("header")) {
                  // header = (org.w3c.dom.Element) n;
                /*
                 * }
                 */else if (n.getLocalName().equals("body")) {
                    body = (org.w3c.dom.Element) n;
                }
            }
        }

        // Get the list of the header blocks needed to build the SOAP Header
        // section
        List<Element> headerBloks = DomUtils.getElementsByTagNameNS(xml,
                CTL_NS, HEADER_BLOCKS);
        // Open the URLConnection
        URLConnection uc = new URL(sUrl).openConnection();
        if (uc instanceof HttpURLConnection) {
            ((HttpURLConnection) uc).setRequestMethod(method);
        }

        uc.setDoOutput(true);
        byte[] bytes = null;

        // SOAP POST
        bytes = SoapUtils.getSoapMessageAsByte(version, headerBloks, body,
                charset);
        // System.out.println("SOAP MESSAGE  " + new String(bytes));

        uc.setRequestProperty("User-Agent", "Team Engine 1.2");
        uc.setRequestProperty("Cache-Control", "no-cache");
        uc.setRequestProperty("Pragma", "no-cache");
        uc.setRequestProperty("charset", charset);
        uc.setRequestProperty("Content-Length", Integer.toString(bytes.length));

        if (version.equals(SOAP_V_1_1)) {
            // Handle HTTP binding for SOAP 1.1
            // uc.setRequestProperty("Accept", "application/soap+xml");
            uc.setRequestProperty("Accept", "text/xml");
            uc.setRequestProperty("SOAPAction", action);
            contentType = "text/xml";
            if (!charset.equals("")) {
                contentType = contentType + "; charset=" + charset;
            }
            uc.setRequestProperty("Content-Type", contentType);
        } else {
            // Handle HTTP binding for SOAP 1.2
            uc.setRequestProperty("Accept", "application/soap+xml");
            contentType = "application/soap+xml";
            if (!charset.equals("")) {
                contentType = contentType + "; charset=" + charset;
            }
            if (!action.equals("")) {
                contentType = contentType + "; action=" + action;
            }
            uc.setRequestProperty("Content-Type", contentType);
        }
        OutputStream os = uc.getOutputStream();
        os.write(bytes);
        return uc;

    }

    /**
     * Implements ctl:request. Create and send an HTTP request then return an
     * HttpResponse. Invoke any specified parsers on the response to validate
     * it, change its format or derive specific information from it.
     */
    public NodeList request(Document ctlRequest, String id) throws Throwable {
        Element request = (Element) ctlRequest.getElementsByTagNameNS(
                Test.CTL_NS, "request").item(0);
        if (opts.getMode() == Test.RESUME_MODE && prevLog != null) {
            for (Element request_e : DomUtils.getElementsByTagName(prevLog,
                    "request")) {
                if (request_e.getAttribute("id").equals(fnPath + id)) {
                    logger.println(DomUtils.serializeNode(request_e));
                    logger.flush();
                    Element response_e = DomUtils.getElementByTagName(
                            request_e, "response");
                    Element content_e = DomUtils.getElementByTagName(
                            response_e, "content");
                    return content_e.getChildNodes();
                    // return DomUtils.getChildElement(content_e);
                }
            }
        }

        String logTag = "<request id=\"" + fnPath + id + "\">\n";
        logTag += DomUtils.serializeNode(request) + "\n";
        // if (logger != null) {
        // logger.println("<request id=\"" + fnPath + id + "\">");
        // logger.println(DomUtils.serializeNode(request));
        // }
        long elapsedTime = 0;
        Exception ex = null;
        Element response = null;
        Element parserInstruction = null;
        NodeList nl = request.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && !n.getNamespaceURI().equals(CTL_NS)) {
                parserInstruction = (Element) n;
            }
        }

        try {
            Date before = new Date();
            URLConnection uc = build_request(request);
            response = parse(uc, parserInstruction);
            Date after = new Date();
            elapsedTime = after.getTime() - before.getTime();

            // Adding the exchange time in the response as comment the format is
            // the following
            // <!--Response received in [XXX] milliseconds-->
            // the comment is included in the first tag of the response
            Element content = DomUtils.getElementByTagName(response, "content");
            if (content != null) {
                nl = content.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Document doc;
                        doc = response.getOwnerDocument();
                        Comment comm = doc
                                .createComment("Response received in ["
                                        + elapsedTime + "] milliseconds");
                        n.appendChild(comm);
                    }
                }
            }

            logTag += DomUtils.serializeNode(response) + "\n";
            // if (logger != null) {
            // logger.println(DomUtils.serializeNode(response));
            // }
        } catch (Exception e) {
            ex = e;
        }
        // logTag += "<!-- elapsed time :"+elapsedTime+" (milliseconds) -->";
        logTag += "</request>";
        if (logger != null) {
            // logger.println("</request>");
            logger.println(logTag);
            logger.flush();
        }
        if (ex == null) {
            Element parser = DomUtils.getElementByTagName(response, "parser");
            if (parser != null) {
                String text = parser.getTextContent();
                if (text.length() > 0) {
                    out.println(parser.getTextContent());
                }
            }
            Element content = DomUtils.getElementByTagName(response, "content");
            return content.getChildNodes();
        } else {
            throw ex;
        }
    }

    /**
     * Submits a request to some HTTP endpoint using the given request details.
     * 
     * @param xml
     *            An ctl:request element.
     * @return A URLConnection object representing an open communications link.
     * @throws Exception
     *             If any error occurs while submitting the request or
     *             establishing a conection.
     */
    public URLConnection build_request(Node xml) throws Exception {
        Node body = null;
        ArrayList<String[]> headers = new ArrayList<String[]>();
        ArrayList<Node> parts = new ArrayList<Node>();
        String sUrl = null;
        String sParams = "";
        String method = "GET";
        String charset = "UTF-8";
        boolean multipart = false;

        // Read in the test information (from CTL)
        NodeList nl = xml.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = (Node) nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getLocalName().equals("url")) {
                    sUrl = n.getTextContent();
                } else if (n.getLocalName().equals("method")) {
                    method = n.getTextContent().toUpperCase();
                } else if (n.getLocalName().equals("header")) {
                    headers.add(new String[] {
                            ((Element) n).getAttribute("name"),
                            n.getTextContent() });
                } else if (n.getLocalName().equals("param")) {
                    if (sParams.length() > 0) {
                        sParams += "&";
                    }
                    sParams += ((Element) n).getAttribute("name") + "="
                            + n.getTextContent();
                    // WARNING! May break some existing test suites
                    // + URLEncoder.encode(n.getTextContent(), "UTF-8");
                } else if (n.getLocalName().equals("dynamicParam")) {
                    String name = null;
                    String val = null;
                    NodeList dpnl = n.getChildNodes();
                    for (int j = 0; j < dpnl.getLength(); j++) {
                        Node dpn = (Node) dpnl.item(j);
                        if (dpn.getNodeType() == Node.ELEMENT_NODE) {
                            if (dpn.getLocalName().equals("name")) {
                                name = dpn.getTextContent();
                            } else if (dpn.getLocalName().equals("value")) {
                                val = dpn.getTextContent();
                                // val =
                                // URLEncoder.encode(dpn.getTextContent(),"UTF-8");
                            }
                        }
                    }
                    if (name != null && val != null) {
                        if (sParams.length() > 0)
                            sParams += "&";
                        sParams += name + "=" + val;
                    }
                } else if (n.getLocalName().equals("body")) {
                    body = n;
                } else if (n.getLocalName().equals("part")) {
                    parts.add(n);
                }
            }
        }

        // Complete GET KVP syntax
        if (method.equals("GET") && sParams.length() > 0) {
            if (sUrl.indexOf("?") == -1) {
                sUrl += "?";
            } else if (!sUrl.endsWith("?") && !sUrl.endsWith("&")) {
                sUrl += "&";
            }
            sUrl += sParams;
        }

        // System.out.println(sUrl);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();

        // Open the URLConnection
        URLConnection uc = new URL(sUrl).openConnection();
        if (uc instanceof HttpURLConnection) {
            ((HttpURLConnection) uc).setRequestMethod(method);
        }

        // POST setup (XML payload and header information)
        if (method.equals("POST") || method.equals("PUT")) {
            uc.setDoOutput(true);
            byte[] bytes = null;
            String mime = null;

            // KVP over POST
            if (body == null) {
                bytes = sParams.getBytes();
                mime = "application/x-www-form-urlencoded";
            } // XML POST
            else {
                String bodyContent = "";

                NodeList children = body.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        t.transform(new DOMSource(children.item(i)),
                                new StreamResult(baos));
                        bodyContent = baos.toString();
                        bytes = baos.toByteArray();

                        if (mime == null) {
                            mime = "application/xml; charset=" + charset;
                        }
                        break;
                    }
                }
                if (bytes == null) {
                    bytes = body.getTextContent().getBytes();
                    mime = "text/plain";
                }

                // Add parts if present
                if (parts.size() > 0) {
                    String prefix = "--";
                    String boundary = "7bdc3bba-e2c9-11db-8314-0800200c9a66";
                    String newline = "\r\n";
                    multipart = true;

                    // Set main body and related headers
                    ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
                    String bodyPart = prefix + boundary + newline;
                    bodyPart += "Content-Type: " + mime + newline + newline;
                    bodyPart += bodyContent;
                    writeBytes(contentBytes, bodyPart.getBytes(charset));

                    // Append all parts to the original body, seperated by the
                    // boundary sequence
                    for (int i = 0; i < parts.size(); i++) {
                        Element currentPart = (Element) parts.get(i);
                        String cid = currentPart.getAttribute("cid");
                        if (cid.indexOf("cid:") != -1) {
                            cid = cid.substring(cid.indexOf("cid:")
                                    + "cid:".length());
                        }
                        String contentType = currentPart
                                .getAttribute("content-type");

                        // Default encodings and content-type
                        if (contentType.equals("application/xml")) {
                            contentType = "application/xml; charset=" + charset;
                        }
                        if (contentType == null || contentType.equals("")) {
                            contentType = "application/octet-stream";
                        }

                        // Set headers for each part
                        String partHeaders = newline + prefix + boundary
                                + newline;
                        partHeaders += "Content-Type: " + contentType + newline;
                        partHeaders += "Content-ID: <" + cid + ">" + newline
                                + newline;
                        writeBytes(contentBytes, partHeaders.getBytes(charset));

                        // Get the fileName, if it exists
                        NodeList files = currentPart.getElementsByTagNameNS(
                                CTL_NS, "file");

                        // Get part for a specified file
                        if (files.getLength() > 0) {
                            File contentFile = getFile(files);

                            InputStream is = new FileInputStream(contentFile);
                            long length = contentFile.length();
                            byte[] fileBytes = new byte[(int) length];
                            int offset = 0;
                            int numRead = 0;
                            while (offset < fileBytes.length
                                    && (numRead = is.read(fileBytes, offset,
                                            fileBytes.length - offset)) >= 0) {
                                offset += numRead;
                            }
                            is.close();

                            writeBytes(contentBytes, fileBytes);
                        } // Get part from inline data (or xi:include)
                        else {
                            // Text
                            if (currentPart.getFirstChild() instanceof Text) {
                                writeBytes(contentBytes, currentPart
                                        .getTextContent().getBytes(charset));
                            } // XML
                            else {
                                writeBytes(
                                        contentBytes,
                                        DomUtils.serializeNode(
                                                currentPart.getFirstChild())
                                                .getBytes(charset));
                            }
                        }
                    }

                    String endingBoundary = newline + prefix + boundary
                            + prefix + newline;
                    writeBytes(contentBytes, endingBoundary.getBytes(charset));

                    bytes = contentBytes.toByteArray();

                    // Global Content-Type and Length to be added after the
                    // parts have been parsed
                    mime = "multipart/related; type=\"" + mime
                            + "\"; boundary=\"" + boundary + "\"";

                    // String contentsString = new String(bytes, charset);
                    // System.out.println("Content-Type: "+mime+"\n"+contentsString);
                }
            }

            // Set headers
            if (body != null) {
                String mid = ((Element) body).getAttribute("mid");
                if (mid != null && !mid.equals("")) {
                    if (mid.indexOf("mid:") != -1) {
                        mid = mid.substring(mid.indexOf("mid:")
                                + "mid:".length());
                    }
                    uc.setRequestProperty("Message-ID", "<" + mid + ">");
                }
            }
            uc.setRequestProperty("Content-Type", mime);
            uc.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));

            // Enter the custom headers (overwrites the defaults if present)
            for (int i = 0; i < headers.size(); i++) {
                String[] header = (String[]) headers.get(i);
                if (multipart && header[0].toLowerCase().equals("content-type")) {
                } else {
                    uc.setRequestProperty(header[0], header[1]);
                }
            }

            OutputStream os = uc.getOutputStream();
            os.write(bytes);
        }
        return uc;
    }

    public static void writeBytes(ByteArrayOutputStream baos, byte[] bytes) {
        baos.write(bytes, 0, bytes.length);
    }

    public Element parse(Document parse_instruction, String xsl_version)
            throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        Node content = null;
        Document parser_instruction = null;

        Element parse_element = (Element) parse_instruction
                .getElementsByTagNameNS(CTL_NS, "parse").item(0);

        NodeList children = parse_element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) children.item(i);
                if (e.getNamespaceURI().equals(XSL_NS)
                        && e.getLocalName().equals("output")) {
                    Document doc = db.newDocument();
                    Element transform = doc
                            .createElementNS(XSL_NS, "transform");
                    transform.setAttribute("version", xsl_version);
                    doc.appendChild(transform);
                    Element output = doc.createElementNS(XSL_NS, "output");
                    NamedNodeMap atts = e.getAttributes();
                    for (int j = 0; j < atts.getLength(); j++) {
                        Attr a = (Attr) atts.item(i);
                        output.setAttribute(a.getName(), a.getValue());
                    }
                    transform.appendChild(output);
                    Element template = doc.createElementNS(XSL_NS, "template");
                    template.setAttribute("match", "node()|@*");
                    transform.appendChild(template);
                    Element copy = doc.createElementNS(XSL_NS, "copy");
                    template.appendChild(copy);
                    Element apply = doc.createElementNS(XSL_NS,
                            "apply-templates");
                    apply.setAttribute("select", "node()|@*");
                    copy.appendChild(apply);
                    t = tf.newTransformer(new DOMSource(doc));
                } else if (e.getLocalName().equals("content")) {
                    NodeList children2 = e.getChildNodes();
                    for (int j = 0; j < children2.getLength(); j++) {
                        if (children2.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            content = (Element) children2.item(j);
                        }
                    }
                    if (content == null) {
                        content = (Node) children2.item(0);
                    }
                } else {
                    parser_instruction = db.newDocument();
                    tf.newTransformer().transform(new DOMSource(e),
                            new DOMResult(parser_instruction));
                }
            }
        }
        if (t == null) {
            t = tf.newTransformer();
        }
        File temp = File.createTempFile("$te_", ".xml");
        if (content.getNodeType() == Node.TEXT_NODE) {
            RandomAccessFile raf = new RandomAccessFile(temp, "rw");
            raf.writeBytes(((Text) content).getTextContent());
            raf.close();
        } else {
            t.transform(new DOMSource((Node) content), new StreamResult(temp));
        }
        URLConnection uc = temp.toURI().toURL().openConnection();
        Element result = parse(uc, parser_instruction);
        temp.delete();
        return result;
    }

    /**
     * Parses the content retrieved from some URI and builds a DOM Document
     * containing information extracted from the response message. Subsidiary
     * parsers are invoked in accord with the supplied parser instructions.
     * 
     * @param uc
     *            A URLConnection object.
     * @param instruction
     *            A Document or Element node containing parser instructions.
     * @return An Element containing selected info from a URLConnection as
     *         specified by instruction Element and children.
     */
    public Element parse(URLConnection uc, Node instruction) throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document response_doc = db.newDocument();
        return parse(uc, instruction, response_doc);
    }

    /**
     * Invoke a parser or chain of parsers as specified by instruction element
     * and children. Parsers in chain share uc, strip off their own
     * instructions, and pass child instructions to next parser in chain. Final
     * parser in chain modifies content. All parsers in chain can return info in
     * attributes and child elements of instructions. If parser specified in
     * instruction, call it to return specified info from uc.
     */
    public Element parse(URLConnection uc, Node instruction,
            Document response_doc) throws Exception {
        Transformer idt = TransformerFactory.newInstance().newTransformer();
        Element parser_e = response_doc.createElement("parser");
        Element response_e = response_doc.createElement("response");
        Element content_e = response_doc.createElement("content");
        if (instruction == null) {
            InputStream is = null;
            uc.connect();
            String contentType = uc.getContentType();
            try {
                is = URLConnectionUtils.getInputStream(uc);
                if (contentType.contains("xml")) { // a crude check
                    idt.transform(new StreamSource(is),
                            new DOMResult(content_e));
                } else {
                    content_e.setTextContent(IOUtils.inputStreamToString(is));
                }
            } finally {
                if (null != is)
                    is.close();
            }
        } else {
            Element instruction_e;
            if (instruction instanceof Element) {
                instruction_e = (Element) instruction;
            } else {
                instruction_e = ((Document) instruction).getDocumentElement();
            }
            String key = "{" + instruction_e.getNamespaceURI() + "}"
                    + instruction_e.getLocalName();
            ParserEntry pe = index.getParser(key);
            Object instance = null;
            if (pe.isInitialized()) {
                instance = parserInstances.get(key);
                if (instance == null) {
                    try {
                        TEClassLoader cl = engine.getClassLoader(opts
                                .getSourcesName());
                        instance = Misc.makeInstance(pe.getClassName(),
                                pe.getClassParams(), cl);
                    } catch (Exception e) {
                        throw new Exception("Can't instantiate parser "
                                + pe.getName(), e);
                    }
                    parserInstances.put(key, instance);
                }
            }
            Method method = parserMethods.get(key);
            if (method == null) {
                TEClassLoader cl = engine.getClassLoader(opts.getSourcesName());
                method = Misc.getMethod(pe.getClassName(), pe.getMethod(), cl,
                        3, 4);
                parserMethods.put(key, method);
            }
            StringWriter swLogger = new StringWriter();
            PrintWriter pwLogger = new PrintWriter(swLogger);
            int arg_count = method.getParameterTypes().length;
            Object[] args = new Object[arg_count];
            args[0] = uc;
            args[1] = instruction_e;
            args[2] = pwLogger;
            if (arg_count > 3) {
                args[3] = this;
            }
            Object return_object;
            try {
                if (LOGR.isLoggable(Level.FINER)) {
                    LOGR.finer("Invoking method " + method.toGenericString()
                            + "size args[] = " + args.length + "\n args[0]: "
                            + args[0].toString() + "\n args[1]:\n"
                            + DomUtils.serializeNode((Node) args[1]));
                }
                return_object = method.invoke(instance, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String msg = "Error invoking parser " + pe.getId() + "\n"
                        + cause.getClass().getName();
                if (cause.getMessage() != null) {
                    msg += ": " + cause.getMessage();
                }
                jlogger.log(Level.SEVERE, msg, e);
                throw e;
            }
            pwLogger.close();
            if (return_object instanceof Node) {
                idt.transform(new DOMSource((Node) return_object),
                        new DOMResult(content_e));
            } else if (return_object != null) {
                content_e.appendChild(response_doc.createTextNode(return_object
                        .toString()));
            }
            parser_e.setAttribute("prefix", instruction_e.getPrefix());
            parser_e.setAttribute("local-name", instruction_e.getLocalName());
            parser_e.setAttribute("namespace-uri",
                    instruction_e.getNamespaceURI());
            parser_e.setTextContent(swLogger.toString());
        }
        response_e.appendChild(parser_e);
        response_e.appendChild(content_e);
        return response_e;
    }

    public Node message(String message, String id) {
        String formatted_message = indent
                + message.trim().replaceAll("\n", "\n" + indent);
        out.println(formatted_message);
        if (logger != null) {
            logger.println("<message id=\"" + id + "\"><![CDATA[" + message
                    + "]]></message>");
        }
        return null;
    }

    public void putLogCache(String id, Document xmlToCache) {
        if (logger != null) {
            String xmlString = DomUtils.serializeNode(xmlToCache);
            logger.println("<cache id=\"" + id + "\">" + xmlString + "</cache>");
        }
    }

    public Element getLogCache(String id) {
        Element child_e = null;
        if (prevLog != null) {
            for (Element cache_e : DomUtils.getElementsByTagName(prevLog,
                    "cache")) {
                if (cache_e.getAttribute("id").equals(id)) {
                    child_e = DomUtils.getChildElement(cache_e);
                }
            }
        }
        if (suiteLog != null && child_e == null) {
            for (Element cache_e : DomUtils.getElementsByTagName(suiteLog,
                    "cache")) {
                if (cache_e.getAttribute("id").equals(id)) {
                    child_e = DomUtils.getChildElement(cache_e);
                }
            }
        }
        return (child_e == null) ? null : child_e;
    }

    /**
     * Converts CTL input form elements to generate a Swing-based or XHTML form
     * and reports the results of processing the submitted form. The results
     * document is produced in (web context) or
     * {@link SwingForm.CustomFormView#submitData}.
     * 
     * @param ctlForm
     *            a DOM Document representing a &lt;ctl:form&gt; element.
     * @throws java.lang.Exception
     * @return a DOM Document containing the resulting &lt;values&gt; element as
     *         the document element.
     */
    public Node form(Document ctlForm, String id) throws Exception {
        if (opts.getMode() == Test.RESUME_MODE && prevLog != null) {
            for (Element e : DomUtils.getElementsByTagName(prevLog,
                    "formresults")) {
                if (e.getAttribute("id").equals(fnPath + id)) {
                    logger.println(DomUtils.serializeNode(e));
                    logger.flush();
                    return DomUtils.getChildElement(e);
                }
            }
        }

        String name = Thread.currentThread().getName();
        Element form = (Element) ctlForm.getElementsByTagNameNS(CTL_NS, "form")
                .item(0);

        NamedNodeMap attrs = form.getAttributes();
        Attr attr = (Attr) attrs.getNamedItem("name");
        if (attr != null) {
            name = attr.getValue();
        }

        for (Element parseInstruction : DomUtils.getElementsByTagNameNS(form,
                CTL_NS, "parse")) {
            String key = parseInstruction.getAttribute("file");
            formParsers.put(key, DomUtils.getChildElement(parseInstruction));
        }

        // Determine if there are file widgets or not
        boolean hasFiles = false;
        List<Element> inputs = DomUtils.getElementsByTagName(form, "input");
        inputs.addAll(DomUtils.getElementsByTagNameNS(form,
                "http://www.w3.org/1999/xhtml", "input"));
        for (Element input : inputs) {
            if (input.getAttribute("type").toLowerCase().equals("file")) {
                hasFiles = true;
                break;
            }
        }

        // Get "method" attribute - "post" or "get"
        attr = (Attr) attrs.getNamedItem("method");
        String method = "get";
        if (attr != null) {
            method = attr.getValue().toLowerCase();
        } else if (hasFiles) {
            method = "post";
        }

        XsltTransformer formTransformer = engine.getFormExecutable().load();
        formTransformer.setSource(new DOMSource(ctlForm));
        formTransformer.setParameter(new QName("title"), new XdmAtomicValue(name));
        formTransformer.setParameter(new QName("web"),
                new XdmAtomicValue(web ? "yes" : "no"));
        formTransformer.setParameter(new QName("files"), new XdmAtomicValue(hasFiles ? "yes"
                : "no"));
        formTransformer.setParameter(
                new QName("thread"),
                new XdmAtomicValue(Long
                        .toString(Thread.currentThread().getId())));
        formTransformer.setParameter(new QName("method"), new XdmAtomicValue(method));
        formTransformer.setParameter(new QName("base"),
                new XdmAtomicValue(opts.getBaseURI()));
        formTransformer.setParameter(new QName("action"), new XdmAtomicValue(
                getTestServletURL()));
        StringWriter sw = new StringWriter();
        Serializer serializer = new Serializer();
        serializer.setOutputWriter(sw);
        serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,
                "yes");
        formTransformer.setDestination(serializer);
        formTransformer.transform();
        this.formHtml = sw.toString();
        if (LOGR.isLoggable(Level.FINE))
            LOGR.fine(this.formHtml);

        if (!web) {
            int width = 700;
            int height = 500;
            attr = (Attr) attrs.getNamedItem("width");
            if (attr != null) {
                width = Integer.parseInt(attr.getValue());
            }
            attr = (Attr) attrs.getNamedItem("height");
            if (attr != null) {
                height = Integer.parseInt(attr.getValue());
            }
            SwingForm.create(name, width, height, this);
        }

        while (formResults == null) {
            if (stop) {
                formParsers.clear();
                throw new Exception("Execution was stopped by the user.");
            }
            Thread.sleep(250);
        }

        Document doc = formResults;
        if (LOGR.isLoggable(Level.FINE))
            LOGR.fine(DomUtils.serializeNode(doc));
        formResults = null;
        formParsers.clear();

        if (logger != null) {
            logger.println("<formresults id=\"" + fnPath + id + "\">");
            logger.println(DomUtils.serializeNode(doc));
            logger.println("</formresults>");
        }
        return doc;
    }

    public void setIndentLevel(int level) {
        indent = "";
        for (int i = 0; i < level; i++) {
            indent += INDENT;
        }
    }

    public String getOutput() {
        String output = threadOutput.toString();
        threadOutput.reset();
        return output;
    }

    public void stopThread() throws Exception {
        stop = true;
        while (!threadComplete) {
            Thread.sleep(100);
        }
    }

    public boolean isThreadComplete() {
        return threadComplete;
    }

    public void run() {
        threadComplete = false;
        // activeThread = Thread.currentThread();
        try {
            opts.testLogDir.mkdir();
            threadOutput = new ByteArrayOutputStream();
            out = new PrintStream(threadOutput);
            execute();
            out.close();
        } catch (Exception e) {
            jlogger.log(Level.SEVERE, "", e);
        }
        // activeThread = null;
        threadComplete = true;
    }

    public File getLogDir() {
        return opts.getLogDir();
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public String getTestPath() {
        return testPath;
    }

    /**
     * Returns the location of the directory containing the test run output.
     * 
     * @return A String representing a file URI denoting the path name of a
     *         directory.
     */
    public String getTestRunDirectory() {
        String logDirURI = opts.getLogDir().toURI().toString();
        return logDirURI + opts.getSessionId();
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public boolean isWeb() {
        return web;
    }

    public void setWeb(boolean web) {
        this.web = web;
    }

    public Object getFunctionInstance(Integer key) {
        return functionInstances.get(key);
    }

    public Object putFunctionInstance(Integer key, Object instance) {
        return functionInstances.put(key, instance);
    }

    public Engine getEngine() {
        return engine;
    }

    public Index getIndex() {
        return index;
    }

    public RuntimeOptions getOpts() {
        return opts;
    }

    public String getTestServletURL() {
        return testServletURL;
    }

    public void setTestServletURL(String testServletURL) {
        this.testServletURL = testServletURL;
    }

    /**
     * Builds a DOM Document representing a classpath resource.
     * 
     * @param name
     *            The name of an XML resource.
     * @return A Document node, or {@code null} if the resource cannot be parsed
     *         for any reason.
     */
    public Document findXMLResource(String name) {
        URL url = this.getClass().getResource(name);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        docFactory.setNamespaceAware(true);
        Document doc = null;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(url.toURI().toString());
        } catch (Exception e) {
            LOGR.log(Level.WARNING, "Failed to parse classpath resource "
                    + name, e);
        }
        return doc;
    }
}
