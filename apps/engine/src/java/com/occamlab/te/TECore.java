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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
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
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Item;
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
import net.sf.saxon.type.Type;

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
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;

/**
 * Provides various utility methods to support test execution and logging.
 *
 */
public class TECore implements Runnable {
    Engine engine;                      // Engine object
    Index index;
    RuntimeOptions opts;
//    String sessionId;                   // Session identifier
//    String sourcesName;                 // Name of active collection of sources
//    File logDir = null;                 // Log directory
    volatile PrintStream out;                    // Console destination
    boolean web = false;                // True when running as a servlet
//    int mode = Test.TEST_MODE;          // Test mode
    String testPath;                    // Uniquely identifies a test instance
    String fnPath = "";                 // Uniquely identifies an XSL function instance within a test instance
    String indent = "";                 // Contains the appropriate number of spaces for the current indet level
    String contextLabel = "";           // Current context label set by ctl:for-each
    int result;                         // Result for current test
    Document prevLog = null;            // Log document for current test from previous  test execution (resume and retest modes only)
    PrintWriter logger = null;          // Logger for current test
    volatile String formHtml;                    // HTML representation for an active form
    volatile Document formResults;               // Holds form results until they are retrieved
    Map<Integer, Object>functionInstances = new HashMap<Integer, Object>();
    Map<String, Object>parserInstances = new HashMap<String, Object>();
    Map<String, Method>parserMethods = new HashMap<String, Method>();

    volatile boolean threadComplete = false;
    volatile boolean stop = false;
    volatile ByteArrayOutputStream threadOutput;

    public static final int PASS = 0;
    public static final int WARNING = 1;
    public static final int INHERITED_FAILURE = 2;
    public static final int FAIL = 3;

    static final String XSL_NS = Test.XSL_NS;
    static final String CTL_NS = Test.CTL_NS;
    static final String TE_NS = Test.TE_NS;

    static final String INDENT = "   ";

    static final QName TECORE_QNAME = new QName("te", TE_NS, "core");
    static final QName TEPARAMS_QNAME = new QName("te", TE_NS, "params");
    static final QName LOCALNAME_QNAME = new QName("local-name");
    static final QName LABEL_QNAME = new QName("label");

//    public TECore(Engine engine, Index index, String sessionId, String sourcesName) {
//        this.engine = engine;
//        this.index = index;
//        this.sessionId = sessionId;
//        this.sourcesName = sourcesName;
//
//        testPath = sessionId;
//        out = System.out;
//    }

    public TECore(Engine engine, Index index, RuntimeOptions opts) {
        this.engine = engine;
        this.index = index;
        this.opts = opts;
//        this.sessionId = opts.getSessionId();
//        this.sourcesName = opts.getSourcesName();

        testPath = opts.getSessionId();
        out = System.out;
    }

    public String getParamsXML(List<String> params) throws Exception {
        String paramsXML = "<params>";
        for (int i = 0; i < params.size(); i++){
            String param = params.get(i);
            String name = param.substring(0, param.indexOf('='));
            String value = param.substring(param.indexOf('=') + 1);
            if (params.get(i).indexOf('=') != 0) {
                paramsXML += "<param local-name=\"" + name + "\" namespace-uri=\"\" prefix=\"\" type=\"xs:string\">";
                paramsXML += "<value><![CDATA[" + value + "]]></value>";
                paramsXML += "</param>";
            }
        }
        paramsXML += "</params>";
//System.out.println("paramsXML: "+paramsXML);
        return paramsXML;

//        return Globals.builder.build(new StreamSource(new StringReader(paramsXML)));
    }


    XPathContext getXPathContext(TestEntry test, String sourcesName, XdmNode contextNode) throws SaxonApiException {
        XPathContext context = null;
        if (test.usesContext()) {
            XsltExecutable xe = engine.loadExecutable(test, sourcesName);
            Executable ex = xe.getUnderlyingCompiledStylesheet().getExecutable();
            context = new XPathContextMajor(contextNode.getUnderlyingNode(), ex);
        }
        return context;
    }


    // Execute tests
    public void execute() throws Exception {
        String sessionId = opts.getSessionId();
//        File logDir = opts.getLogDir();
        int mode = opts.getMode();
//        String sourcesName = opts.getSourcesName();
        ArrayList<String> params = opts.getParams();

        if (mode == Test.RESUME_MODE) {
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
                        execute_profile(profile, params, false);
                    }
                } else {
                    for (String profile : profiles) {
                        execute_profile(profile, params, true);
                    }
                }
            }
        } else {
            throw new Exception("Unsupported mode");
        }
    }

    public void reexecute_test(String testPath) throws Exception {
        Document log = LogUtils.readLog(opts.getLogDir(), testPath);
        String testId = LogUtils.getTestIdFromLog(log);
        TestEntry test = index.getTest(testId);
        net.sf.saxon.s9api.DocumentBuilder builder = engine.getBuilder();
        XdmNode paramsNode = LogUtils.getParamsFromLog(builder, log);
        XdmNode contextNode = LogUtils.getContextFromLog(builder, log);
        XPathContext context = getXPathContext(test, opts.getSourcesName(), contextNode);
        setTestPath(testPath);
        executeTest(test, paramsNode, context);
    }

    public int execute_test(String testName, List<String> params, XdmNode contextNode) throws Exception {
        TestEntry test = index.getTest(testName);
        if (test == null) {
            throw new Exception("Error: Test " + testName + " not found.");
        }
        XdmNode paramsNode = engine.getBuilder().build(new StreamSource(new StringReader(getParamsXML(params))));
        XPathContext context = getXPathContext(test, opts.getSourcesName(), contextNode);
        return executeTest(test, paramsNode, context);
    }

    public void execute_suite(String suiteName, List<String> params) throws Exception {
        SuiteEntry suite = null;
        if (suiteName == null) {
            Iterator<String> it = index.getSuiteKeys().iterator();
            if (!it.hasNext()) {
                throw new Exception("Error: No suites in sources.");
            }
            suite = index.getSuite(it.next());
            if (it.hasNext()) {
                throw new Exception("Error: Suite name must be specified since there is more than one suite in sources.");
            }
        } else {
            suite = index.getSuite(suiteName);
            if (suite == null) {
                throw new Exception("Error: Suite " + suiteName + " not found.");
            }
        }
        ArrayList<String> kvps = new ArrayList<String>();
        kvps.addAll(params);
        Document form = suite.getForm();
        if (form != null) {
            Document results = (Document)form(form, suite.getId());
            for (Element value : DomUtils.getElementsByTagName(results, "value")) {
                kvps.add(value.getAttribute("key") + "=" + value.getTextContent());
            }
        }
        String name = suite.getPrefix() + ":" + suite.getLocalName();
        out.println("Testing suite " + name + "...");
        setIndentLevel(1);
        int result = execute_test(suite.getStartingTest().toString(), kvps, null);
        out.print("Suite " + suite.getPrefix() + ":" + suite.getLocalName() + " ");
        if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
            out.println("Failed");
        } else {
            out.println("Passed");
        }
    }

    public void execute_profile(String profileName, List<String> params, boolean required) throws Exception {
        ProfileEntry profile = index.getProfile(profileName);
        if (profile == null) {
            throw new Exception("Error: Profile " + profileName + " not found.");
        }

        SuiteEntry suite = index.getSuite(profile.getBaseSuite());
        String sessionId = opts.getSessionId();
        Document log = LogUtils.readLog(opts.getLogDir(), sessionId);
        if (log == null) {
//            if (opts.getLogDir() == null) {
//                File tempLogDir = File.createTempFile("telog", null);
//                tempLogDir.delete();
//                tempLogDir.mkdir();
//                opts.setLogDir(tempLogDir);
//            }
            execute_suite(suite.getId(), params);
            log = LogUtils.readLog(opts.getLogDir(), sessionId);
        }
        String testId = LogUtils.getTestIdFromLog(log);
        List<String> baseParams = LogUtils.getParamListFromLog(engine.getBuilder(), log);
        TestEntry test = index.getTest(testId);
        if (suite.getStartingTest().equals(test.getQName())) {
            ArrayList<String> kvps = new ArrayList<String>();
            kvps.addAll(baseParams);
            kvps.addAll(params);
            Document form = profile.getForm();
            if (form != null) {
                Document results = (Document)form(form, profile.getId());
                for (Element value : DomUtils.getElementsByTagName(results, "value")) {
                    kvps.add(value.getAttribute("key") + "=" + value.getTextContent());
                }
            }
            setTestPath(sessionId + "/" + profile.getLocalName());
            String name = profile.getPrefix() + ":" + profile.getLocalName();
            out.println("\nTesting profile " + name + "...");
            Document baseLog = LogUtils.makeTestList(opts.getLogDir(), sessionId, profile.getExcludes());
            Element baseTest = DomUtils.getElement(baseLog);
//out.println(DomUtils.serializeNode(baseLog));
            out.print(TECore.INDENT + "Base tests from suite " + suite.getPrefix() + ":" + suite.getLocalName() + " ");
            String summary = "Not complete";
            if ("yes".equals(baseTest.getAttribute("complete"))) {
                int baseResult = Integer.parseInt(baseTest.getAttribute("result"));
                if (baseResult == TECore.FAIL || baseResult == TECore.INHERITED_FAILURE) {
                    summary = "Failed";
                } else {
                    summary = "Passed";
                }
            }
            out.println(summary);
            setIndentLevel(1);
            int result = execute_test(profile.getStartingTest().toString(), kvps, null);
            out.print("Profile " + profile.getPrefix() + ":" + profile.getLocalName() + " ");
            if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
                summary = "Failed";
            }
            out.println(summary);
        } else {
            if (required) {
                throw new Exception("Error: Profile " + profileName + " is not a valid profile for session " + sessionId + ".");
            }
        }
    }

    public XdmNode executeTemplate(TemplateEntry template, XdmNode params, XPathContext context) throws Exception {
        if (stop) {
            throw new Exception("Execution was stopped by the user.");
        }
        XsltExecutable executable = engine.loadExecutable(template, opts.getSourcesName());
        XsltTransformer xt = executable.load();
        XdmDestination dest = new XdmDestination();
        xt.setDestination(dest);
        if (template.usesContext() && context != null) {
            xt.setSource((NodeInfo)context.getContextItem());
        } else {
            xt.setSource(new StreamSource(new StringReader("<nil/>")));
        }
        xt.setParameter(TECORE_QNAME, new ObjValue(this));
        if (params != null) {
            xt.setParameter(TEPARAMS_QNAME, params);
        }
        xt.transform();
        XdmNode ret = dest.getXdmNode();
        return ret;
    }

    static String getLabel(XdmNode n) {
        String label = n.getAttributeValue(LABEL_QNAME);
        if (label == null) {
            XdmNode value = (XdmNode)n.axisIterator(Axis.CHILD).next();
            XdmItem childItem = null;
            try {
                childItem = value.axisIterator(Axis.CHILD).next();
            } catch (Exception e) {
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
                XdmNode n2 = (XdmNode)childItem;
                if (n2.getNodeKind() == XdmNodeKind.ELEMENT ) {
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
        XdmNode params = (XdmNode)paramsVar.axisIterator(Axis.CHILD).next();
        XdmSequenceIterator it = params.axisIterator(Axis.CHILD);
        while (it.hasNext()) {
            XdmNode n = (XdmNode)it.next();
            QName qname = n.getNodeName();
            if (qname != null) {
            String tagname = qname.getLocalName();
                if (tagname.equals("param")) {
                    String name = n.getAttributeValue(LOCALNAME_QNAME);
                    String label = getLabel(n);
                    newText = StringUtils.replaceAll(newText, "{$" + name + "}", label);
                }
            }
        }
        newText = StringUtils.replaceAll(newText, "{$context}", contextLabel);
        return newText;
    }

    static String getResultDescription(int result) {
        if (result == PASS) {
            return "Passed";
        } else if (result == WARNING) {
            return ("generated a Warning.");
        } else if (result == INHERITED_FAILURE){
            return "Failed (Inherited failure)";
        } else {
            return "Failed";
        }
    }

    public int executeTest(TestEntry test, XdmNode params, XPathContext context) throws Exception {
        Document oldPrevLog = prevLog;
        if (opts.getMode() == Test.RESUME_MODE) {
            prevLog = readLog();
        } else {
            prevLog = null;
        }

        String assertion = getAssertionValue(test.getAssertion(), params);
        out.print(indent + (prevLog == null ? "Testing " : "Resuming Test "));
        out.println(test.getName() + " (" + testPath + ")...");

        String oldIndent = indent;
        indent += INDENT;

        out.println(indent + "Assertion: " + assertion);

        PrintWriter oldLogger = logger;
        if (opts.getLogDir() != null) {
            logger = createLog();
            logger.println("<log>");
            logger.println("<starttest local-name=\"" + test.getLocalName() + "\" " +
                                      "prefix=\"" + test.getPrefix() + "\" " +
                                      "namespace-uri=\"" + test.getNamespaceURI() + "\" " +
                                      "path=\"" + testPath + "\" " +
                                      "file=\"" + test.getTemplateFile().getAbsolutePath() + "\">");
            logger.println("<assertion>" + assertion + "</assertion>");
            if (params != null) {
                logger.println(params.toString());
            }
            if (test.usesContext()) {
                logger.println("<context label=\"" + contextLabel + "\">");
                NodeInfo contextNode = (NodeInfo)context.getContextItem();
                int kind = contextNode.getNodeKind();
                if (kind == Type.ATTRIBUTE) {
                    logger.print("<value " + contextNode.getDisplayName() + "=\""
                                           + contextNode.getStringValue() + "\"");
                    // TODO: set namespace
                    logger.println("/>");
                } else if (kind == Type.ELEMENT || kind == Type.DOCUMENT) {
                    logger.println("<value>");
                    logger.println(engine.getBuilder().build(contextNode).toString());
                    logger.println("</value>");
                }
                logger.println("</context>");

            }
            logger.println("</starttest>");
            logger.flush();
        }

        result = PASS;
        try {
            executeTemplate(test, params, context);
        } catch (SaxonApiException e) {
            out.println(e.getMessage());
            if (logger != null) {
                logger.println("<exception><![CDATA[" + e.getMessage() + "]]></exception>");
            }
            result = FAIL;
        }

        if (logger != null) {
            logger.println("<endtest result=\"" + Integer.toString(result) + "\"/>");
            logger.println("</log>");
            logger.close();
        }
        logger = oldLogger;

        prevLog = oldPrevLog;

        indent = oldIndent;

        out.println(indent + "Test " + test.getName() + " " + getResultDescription(result));

        return result;
    }

    public void callTest(XPathContext context, String localName, String NamespaceURI, NodeInfo params, String callId) throws Exception {
//        System.out.println("call_test");
//        System.out.println(params.getClass().getName());
        String key = "{" + NamespaceURI + "}" + localName;
        TestEntry test = index.getTest(key);

        if (logger != null) {
            logger.println("<testcall path=\"" + testPath + "/" + callId + "\"/>");
            logger.flush();
        }
        if (opts.getMode() == Test.RESUME_MODE) {
            Document doc = LogUtils.readLog(opts.getLogDir(), testPath + "/" + callId);
            int result = LogUtils.getResultFromLog(doc);
            if (result >= 0) {
                out.println(indent + "Test " + test.getName() + " " + getResultDescription(result));
                if (result == WARNING) {
                    warning();
                } else if (result != PASS){
                    inheritedFailure();
                }
                return;
            }
        }

        String oldTestPath = testPath;
        int oldResult = result;
        testPath += "/" + callId;
        executeTest(test, S9APIUtils.makeNode(params), context);
        testPath = oldTestPath;
        if (result < oldResult) {
            // Restore parent result if the child results aren't worse
            result = oldResult;
        } else if (result == FAIL && oldResult != FAIL) {
            // If the child result was FAIL and parent hasn't directly failed, set parent result to INHERITED_FAILURE
            result = INHERITED_FAILURE;
        } else {
            // Keep child result as parent result
        }
    }

    public NodeInfo executeXSLFunction(XPathContext context, FunctionEntry fe, NodeInfo params) throws Exception {
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

    public Object callFunction(XPathContext context, String localName, String NamespaceURI, NodeInfo params) throws Exception {
//        System.out.println("callFunction {" + NamespaceURI + "}" + localName);
        String key = "{" + NamespaceURI + "}" + localName;
        List<FunctionEntry> functions = index.getFunctions(key);
        Node paramsNode = NodeOverNodeInfo.wrap(params);
        List<Element> paramElements = DomUtils.getElementsByTagName(paramsNode, "param");
        for (FunctionEntry fe : functions) {
            if (!fe.isJava()) {
                boolean valid = true;
                for (Element el : paramElements) {
                    String uri = el.getAttribute("namespace-uri");
                    String name = el.getAttribute("local-name");
                    String prefix = el.getAttribute("prefix");
                    javax.xml.namespace.QName qname = new javax.xml.namespace.QName(uri, name, prefix);
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
                    Method method = Misc.getMethod(fe.getClassName(), fe.getMethod(), argCount);
                    Class<?>[] types = method.getParameterTypes();
                    Object[] args = new Object[argCount];
                    for (int i = 0; i < argCount; i++) {
                        Element el = DomUtils.getElementByTagName(paramElements.get(i), "value");
                        if (types[i].equals(String.class)) {
                            Map<javax.xml.namespace.QName, String> attrs = DomUtils.getAttributes(el);
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
                        } else if (types[i].toString().equals("float")) {
                            args[i] = Float.parseFloat(el.getTextContent());
                        } else if (types[i].toString().equals("double")) {
                            args[i] = Double.parseDouble(el.getTextContent());
                        } else if (Document.class.isAssignableFrom(types[i])) {
                            args[i] = DomUtils.createDocument(DomUtils.getChildElement(el));
                        } else if (NodeList.class.isAssignableFrom(types[i])) {
                            args[i] = el.getChildNodes();
                        } else if (Node.class.isAssignableFrom(types[i])) {
                            args[i] = el.getFirstChild();
                        } else {
                            throw new Exception("Error: Function " + key + " uses unsupported Java type " + types[i].toString());
                        }
                    }
                    try {
                        Object instance = null;
                        if (fe.isInitialized()) {
//                            String instkey = fe.getId() + "," + Integer.toString(fe.getMinArgs()) + "," + Integer.toString(fe.getMaxArgs());
                            instance = getFunctionInstance(fe.hashCode());
                            if (instance == null) {
                                try {
                                    instance = Misc.makeInstance(fe.getClassName(), fe.getClassParams());
                                    putFunctionInstance(fe.hashCode(), instance);
                                } catch (Exception e) {
                                    throw new XPathException(e);
                                }
                            }
                        }
                        return method.invoke(instance, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        String msg = "Error invoking function " + fe.getId() + "\n" + cause.getClass().getName();
                        if (cause.getMessage() != null) {
                            msg += ": " + cause.getMessage();
                        }
                        throw new Exception(msg, cause);
                    }
                }
            }
        }
        throw new Exception("No function {" + NamespaceURI + "}" + localName + " with a compatible signature.");
    }

    public void warning() {
        if (result < WARNING) {
            result = WARNING;
        }
    }

    public void inheritedFailure() {
        if (result < INHERITED_FAILURE) {
            result = INHERITED_FAILURE;
        }
    }

    public void fail() {
        if (result < FAIL) {
            result = FAIL;
        }
    }

    public void setContextLabel(String label) {
        // TODO: test
        contextLabel = label;
        System.out.println("setcontextLabel(" + label + ")");
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

    public Document readLog() throws Exception {
        return LogUtils.readLog(opts.getLogDir(), testPath);
    }

    public PrintWriter createLog() throws Exception {
        return LogUtils.createLog(opts.getLogDir(), testPath);
//        if (logDir != null) {
//            File dir = new File(logDir, testPath);
//            dir.mkdir();
//            File f = new File(dir, "log.xml");
//            f.delete();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//                    new FileOutputStream(f), "UTF-8"));
//            logger = new PrintWriter(writer);
//        }
//        return logger;
    }

//    public static Node reset_log(String logdir, String callpath)
//            throws Exception {
//        if (logdir.length() > 0) {
//            File dir = new File(logdir, callpath);
//            dir.mkdir();
//            File f = new File(dir, "log.xml");
//            RandomAccessFile raf = new RandomAccessFile(f, "rw");
//            raf.setLength(0);
//            raf.writeBytes("<log>\n</log>\n");
//            raf.close();
//        }
//        return null;
//    }

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
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    file = new File(cl.getResource(e.getTextContent()).getFile());
                } else {
                    System.out.println("Incorrect file reference:  Unknown type!");
                }
            } catch (Exception exception) {
            	System.err.println("Error getting file. " + exception.getMessage());
            	return null;
            }
        }
        return file;
    }

    public Node request(Document ctlRequest, String id) throws Throwable {
        Element request = (Element)ctlRequest.getElementsByTagNameNS(Test.CTL_NS, "request").item(0);
        if (opts.getMode() == Test.RESUME_MODE && prevLog != null) {
            for (Element request_e : DomUtils.getElementsByTagName(prevLog, "request")) {
                if (request_e.getAttribute("id").equals(fnPath + id)) {
                    logger.println(DomUtils.serializeNode(request_e));
                    logger.flush();
                    Element response_e = DomUtils.getElementByTagName(request_e, "response");
                    Element content_e = DomUtils.getElementByTagName(response_e, "content");
                    return DomUtils.getChildElement(content_e);
                }
            }
        }

        String logTag = "<request id=\"" + fnPath + id + "\">\n";
        logTag += DomUtils.serializeNode(request) + "\n";
//        if (logger != null) {
//            logger.println("<request id=\"" + fnPath + id + "\">");
//            logger.println(DomUtils.serializeNode(request));
//        }
        Exception ex = null;
        Element response = null;
        Element parserInstruction = null;
        NodeList nl = request.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && !n.getNamespaceURI().equals(CTL_NS)) {
                parserInstruction = (Element)n;
            }
        }
        try {
            URLConnection uc = build_request(request);
            response = parse(uc, parserInstruction);
            logTag += DomUtils.serializeNode(response) + "\n";
//            if (logger != null) {
//                logger.println(DomUtils.serializeNode(response));
//            }
        } catch (Exception e) {
            ex = e;
        }
        logTag += "</request>";
        if (logger != null) {
//            logger.println("</request>");
            logger.println(logTag);
            logger.flush();
        }
        if (ex == null) {
            Node n = response.getElementsByTagName("content").item(0);
//            System.out.println(DomUtils.serializeNode(n.getFirstChild()));
            return n.getFirstChild();
        } else {
            throw ex;
        }
    }

    // Create and send an HttpRequest then return an HttpResponse (HttpResponse)
    static public URLConnection build_request(Node xml) throws Exception {
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
                    if (sParams.length() > 0) sParams += "&";
                    sParams += ((Element) n).getAttribute("name") + "="
                            + n.getTextContent();
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
            }
            else if (!sUrl.endsWith("?") && !sUrl.endsWith("&")) {
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
            }
            // XML POST
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

/*
                        // Determine if we need to set a different Content-Type value (SOAP)
			for (int j = 0; j < headers.size(); j++) {
				String[] header = (String[]) headers.get(j);
				if (header[0].toLowerCase().equals("content-type")) {
					mime = header[1];
				}
			}
*/

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
                        if (cid.indexOf("cid:") != -1) cid = cid.substring(cid.indexOf("cid:")+"cid:".length());
                        String contentType = currentPart.getAttribute("content-type");

                        // Default encodings and content-type
                        if (contentType.equals("application/xml")) {
                            contentType = "application/xml; charset=" + charset;
                        }
                        if (contentType == null || contentType.equals("")) {
                            contentType = "application/octet-stream";
                        }

                        // Set headers for each part
                    	ByteArrayOutputStream partBytes = new ByteArrayOutputStream();
                        String partHeaders = newline + prefix + boundary + newline;
                        partHeaders += "Content-Type: " + contentType + newline;
                        partHeaders += "Content-ID: <" + cid + ">" + newline + newline;
			writeBytes(contentBytes, partHeaders.getBytes(charset));

                        // Get the fileName, if it exists
                        NodeList files = currentPart.getElementsByTagNameNS(
                                CTL_NS, "file");

                        // Get part for a specified file
                        if (files.getLength() > 0) {
                            File contentFile = getFile(files);

			    InputStream is = new FileInputStream(contentFile);
			    long length = contentFile.length();
			    byte[] fileBytes = new byte[(int)length];
			    int offset = 0;
			    int numRead = 0;
			    while (offset < fileBytes.length && (numRead=is.read(fileBytes, offset, fileBytes.length-offset)) >= 0) {
			    	offset += numRead;
			    }
			    is.close();

			    writeBytes(contentBytes, fileBytes);
                        }
                        // Get part from inline data (or xi:include)
                        else {
                            // Text
                            if (currentPart.getFirstChild() instanceof Text) {
				writeBytes(contentBytes, currentPart.getTextContent().getBytes(charset));
                            }
                            // XML
                            else {
                                writeBytes(contentBytes, DomUtils.serializeNode(currentPart.getFirstChild()).getBytes(charset));
                            }
                        }
                    }

                    String endingBoundary = newline + prefix + boundary + prefix + newline;
		    writeBytes(contentBytes, endingBoundary.getBytes(charset));

                    bytes = contentBytes.toByteArray();

                    // Global Content-Type and Length to be added after the
                    // parts have been parsed
                    mime = "multipart/related; type=\""+ mime
                    	    + "\"; boundary=\"" + boundary + "\"";

		    //String contentsString = new String(bytes, charset);
                    //System.out.println("Content-Type: "+mime+"\n"+contentsString);
                }
            }

            // Set headers
            if (body != null) {
	        String mid = ((Element) body).getAttribute("mid");
                if (mid != null && !mid.equals("")) {
	    	    if (mid.indexOf("mid:") != -1) mid = mid.substring(mid.indexOf("mid:")+"mid:".length());
                    uc.setRequestProperty("Message-ID", "<" + mid + ">");
                }
            }
            uc.setRequestProperty("Content-Type", mime);
            uc.setRequestProperty("Content-Length", Integer
                    .toString(bytes.length));

            // Enter the custom headers (overwrites the defaults if present)
            for (int i = 0; i < headers.size(); i++) {
                String[] header = (String[]) headers.get(i);
		if (multipart && header[0].toLowerCase().equals("content-type")) {
		}
		else {
                	uc.setRequestProperty(header[0], header[1]);
        	}
            }

            OutputStream os = uc.getOutputStream();
            os.write(bytes);
        }
/*
    	// Get URLConnection values
	InputStream is = uc.getInputStream();
	byte[] respBytes = IOUtils.inputStreamToBytes(is);
	int respCode = ((HttpURLConnection) uc).getResponseCode();
	String respMess = ((HttpURLConnection) uc).getResponseMessage();
    	Map respHeaders = ((HttpURLConnection) uc).getHeaderFields();

	// Construct the HttpResponse (BasicHttpResponse) to send to parsers
	HttpVersion version = new HttpVersion(1,1);
	BasicStatusLine statusLine = new BasicStatusLine(version, respCode, respMess);
	BasicHttpResponse resp = new BasicHttpResponse(statusLine);
	Set respHeadersSet = respHeaders.keySet();
	for( Iterator it = respHeadersSet.iterator(); it.hasNext(); ) {
		String name = (String) it.next();
		List valueList = (List) respHeaders.get(name);
		String value = (String) valueList.get(0);
		if (name == null) continue;
		resp.addHeader(name, value);
	}
	HttpEntity entity = new ByteArrayEntity(respBytes);
	resp.setEntity(entity);

        return resp;
*/
        return uc;
    }

    public static void writeBytes(ByteArrayOutputStream baos, byte[] bytes) {
        baos.write(bytes, 0, bytes.length);
    }

    public Element parse(Document parse_instruction, String xsl_version) throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        Node content = null;
        Document parser_instruction = null;

        Element parse_element = (Element)parse_instruction.getElementsByTagNameNS(CTL_NS, "parse").item(0);

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
        URLConnection uc = temp.toURL().openConnection();
        Element result = parse(uc, parser_instruction);
        temp.delete();
        return result;
    }

    public Element parse(URLConnection uc, Node instruction) throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        Document response_doc = db.newDocument();
        Element parser_e = response_doc.createElement("parser");
        Element response_e = response_doc.createElement("response");
        Element content_e = response_doc.createElement("content");
        if (instruction == null) {
            try {
                InputStream is = uc.getInputStream();
                t.transform(new StreamSource(is), new DOMResult(content_e));
            } catch (Exception e) {
                parser_e.setTextContent(e.getMessage());
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
                        instance = Misc.makeInstance(pe.getClassName(), pe.getClassParams());
                    } catch (Exception e) {
                        throw new Exception("Can't instantiate parser " + pe.getName(), e);
                    }
                    parserInstances.put(key, instance);
                }
            }
            Method method = parserMethods.get(key);
            if (method == null) {
                method = Misc.getMethod(pe.getClassName(), pe.getMethod(), 3, 4);
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
                return_object = method.invoke(instance, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String msg = "Error invoking parser " + pe.getId() + "\n" + cause.getClass().getName();
                if (cause.getMessage() != null) {
                    msg += ": " + cause.getMessage();
                }
                throw new Exception(msg, cause);
            }
            pwLogger.close();
            if (return_object instanceof Node) {
                t.transform(new DOMSource((Node) return_object), new DOMResult(
                        content_e));
            } else if (return_object != null) {
                content_e.appendChild(response_doc.createTextNode(return_object
                        .toString()));
            }

            parser_e.setAttribute("prefix", instruction_e.getPrefix());
            parser_e.setAttribute("local-name", instruction_e.getLocalName());
            parser_e.setAttribute("namespace-uri", instruction_e
                    .getNamespaceURI());
            parser_e.setTextContent(swLogger.toString());
        }
        response_e.appendChild(parser_e);
        response_e.appendChild(content_e);
        response_doc.appendChild(response_e);
        return response_e;
    }

    public Node message(String message, String id) {
        String formatted_message = indent + message.trim().replaceAll("\n", "\n" + indent);
        out.println(formatted_message);
        if (logger != null) {
            logger.println("<message id=\"" + id + "\"><![CDATA[" + message + "]]></message>");
        }
        return null;
    }

    /**
     * Converts CTL input form data to generate a Swing-based or XHTML form and
     * reports the results of processing the submitted form. The results document
     * is produced in {@link TestServlet#processFormData} (web context) or
     * {@link SwingForm.CustomFormView#submitData}.
     *
     * @param ctlForm
     *            a DOM Document representing a &lt;ctl:form&gt; element.
     * @throws java.lang.Exception
     * @return a DOM Document containing the resulting &lt;values&gt; element
     *        as the document element.
     */
    public Node form(Document ctlForm, String id) throws Exception {
        if (opts.getMode() == Test.RESUME_MODE && prevLog != null) {
            for (Element e : DomUtils.getElementsByTagName(prevLog, "formresults")) {
                if (e.getAttribute("id").equals(fnPath + id)) {
                    logger.println(DomUtils.serializeNode(e));
                    logger.flush();
                    return DomUtils.getChildElement(e);
                }
            }
        }

        String name = Thread.currentThread().getName();
        Element form = (Element)ctlForm.getElementsByTagNameNS(CTL_NS, "form").item(0);
        NamedNodeMap attrs = form.getAttributes();
        Attr attr = (Attr) attrs.getNamedItem("name");
        if (attr != null) {
            name = attr.getValue();
        }

        // Get "method" attribute - "post" or "get"
        attr = (Attr) attrs.getNamedItem("method");
        String method = "";
        if (attr != null) {
            method = attr.getValue();
        }

	// Determine if there are file widgets or not
	boolean hasFiles = false;
	NodeList inputs = ctlForm.getElementsByTagName("input");
	for (int i = 0; i < inputs.getLength(); i++) {
		NamedNodeMap inputAttrs = inputs.item(i).getAttributes();
		Attr typeAttr = (Attr) inputAttrs.getNamedItem("type");
		if (typeAttr != null) {
			if (typeAttr.getValue().toLowerCase().equals("file")) {
				hasFiles = true;
			}
		}
	}

        XsltTransformer xt = engine.getFormExecutable().load();
        xt.setSource(new DOMSource(ctlForm));
        xt.setParameter(new QName("title"), new XdmAtomicValue(name));
        xt.setParameter(new QName("web"), new XdmAtomicValue(web ? "yes" : "no"));
        xt.setParameter(new QName("files"), new XdmAtomicValue(hasFiles ? "yes" : "no"));
        xt.setParameter(new QName("thread"), new XdmAtomicValue(Long.toString(Thread.currentThread().getId())));
        xt.setParameter(new QName("method"), new XdmAtomicValue(method.toLowerCase().equals("post") ? "post" : "get"));
        StringWriter sw = new StringWriter();
        Serializer serializer = new Serializer();
        serializer.setOutputWriter(sw);
        serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
        xt.setDestination(serializer);
        xt.transform();
        formHtml = sw.toString();
//System.out.println(this.formHtml);

        if (!web) {
            int width = 700;
            int height = 500;
            attr = (Attr) attrs.getNamedItem("width");
            if (attr != null)
                width = Integer.parseInt(attr.getValue());
            attr = (Attr) attrs.getNamedItem("height");
            if (attr != null)
                height = Integer.parseInt(attr.getValue());
            new SwingForm(name, width, height, this);
        }

        while (formResults == null) {
            if (stop) {
                throw new Exception("Execution was stopped by the user.");
            }
            Thread.sleep(250);
        }

        Document doc = formResults;
//        System.out.println(DomUtils.serializeNode(doc));
        formResults = null;

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
//        activeThread = Thread.currentThread();
        try {
            opts.logDir.mkdir();
            threadOutput = new ByteArrayOutputStream();
            out = new PrintStream(threadOutput);
            execute();
            out.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
//        activeThread = null;
        threadComplete = true;
    }

    public File getLogDir() {
        return opts.getLogDir();
    }
//
//    public void setLogDir(File logDir) {
//        this.logDir = logDir;
//    }
//
//    public int getMode() {
//        return mode;
//    }
//
//    public void setMode(int mode) {
//        this.mode = mode;
//    }
//
    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public String getTestPath() {
        return testPath;
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
//
//    public String getSourcesName() {
//        return sourcesName;
//    }
//
//    public String getSessionId() {
//        return sessionId;
//    }
}
