/**
 * **************************************************************************
 *
 * Version Date: January 24, 2018
 *
 * Contributor(s): 
 *	C. Heazel (WiSC): 
 *          Added Fortify adjudication changes
 *          Changed session id format to UUID
 *
 ***************************************************************************
 */
package com.occamlab.te.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.TECore;
import com.occamlab.te.util.TEPath;    // Fortify Mod


public class LogUtils {

    private static final Logger LOGR = Logger.getLogger(LogUtils.class
            .getName());

    /**
     * Creates a Writer used to write test results to the log.xml file.
     * 
     * @param logDir
     *            The directory containing the test session results.
     * @param callpath
     *            A test session identifier.
     * @return A PrintWriter object, or {@code null} if one could not be
     *         created.
     * @throws Exception
     */
    public static PrintWriter createLog(File logDir, String callpath)
            throws Exception {
        if (logDir != null) {
            File dir = new File(logDir, callpath);
            // Fortify Mod: use TEPath to validate the path to the log file
            TEPath tpath = new TEPath(dir.getAbsolutePath());
            if( ! tpath.isValid() ) {
                return null;
                } 
            String path=logDir.toString() + "/" + callpath.split("/")[0];
            System.setProperty("PATH", path);
            dir.mkdir();
            File f = new File(dir, "log.xml");
            f.delete();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8"));
            return new PrintWriter(writer);
        }
        return null;
    }

    // Reads a log from disk
    public static Document readLog(File logDir, String callpath)
            throws Exception {
        File dir = new File(logDir, callpath);
        File f = new File(dir, "log.xml");
        if (f.exists()) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
		 // Fortify Mod: Disable entity expansion to foil External Entity Injections
		 dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            TransformerFactory tf = TransformerFactory.newInstance();
       	     // Fortify Mod: prevent external entity injection
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer t = tf.newTransformer();
            t.setErrorListener(new com.occamlab.te.NullErrorListener());
            try {
                t.transform(new StreamSource(f), new DOMResult(doc));
            } catch (Exception e) {
                // The log may not have been closed properly.
                // Try again with a closing </log> tag
                RandomAccessFile raf = new RandomAccessFile(f, "r");
                int l = new Long(raf.length()).intValue();
                byte[] buf = new byte[l + 8];
                raf.read(buf);
                raf.close();
                buf[l] = '\n';
                buf[l + 1] = '<';
                buf[l + 2] = '/';
                buf[l + 3] = 'l';
                buf[l + 4] = 'o';
                buf[l + 5] = 'g';
                buf[l + 6] = '>';
                buf[l + 7] = '\n';
                doc = db.newDocument();
                tf.newTransformer().transform(
                        new StreamSource(new ByteArrayInputStream(buf)),
                        new DOMResult(doc));
            }
            return doc;
        } else {
            return null;
        }
    }

    // Returns the id of a test from its log document
    public static String getTestIdFromLog(Document log) throws Exception {
        Element starttest = DomUtils.getElementByTagName(log, "starttest");
        String namespace = starttest.getAttribute("namespace-uri");
        String localName = starttest.getAttribute("local-name");
        return "{" + namespace + "}" + localName;
    }

    public static int getResultFromLog(Document log) throws Exception {
        if (log != null) {
            Element endtest = DomUtils.getElementByTagName(log, "endtest");
            if (endtest != null) {
                return Integer.parseInt(endtest.getAttribute("result"));
            }
        }
        return -1;
    }

    // Returns the parameters to a test from its log document
    public static List<String> getParamListFromLog(
            net.sf.saxon.s9api.DocumentBuilder builder, Document log)
            throws Exception {
        List<String> list = new ArrayList<String>();
        Element starttest = (Element) log.getElementsByTagName("starttest")
                .item(0);
        for (Element param : DomUtils.getElementsByTagName(starttest, "param")) {
            String value = DomUtils.getElementByTagName(param, "value")
                    .getTextContent();
            list.add(param.getAttribute("local-name") + "=" + value);
        }
        return list;
    }

    // Returns the parameters to a test from its log document
    public static XdmNode getParamsFromLog(
            net.sf.saxon.s9api.DocumentBuilder builder, Document log)
            throws Exception {
        Element starttest = (Element) log.getElementsByTagName("starttest")
                .item(0);
        NodeList nl = starttest.getElementsByTagName("params");
        if (nl == null || nl.getLength() == 0) {
            return null;
        } else {
            Document doc = DomUtils.createDocument(nl.item(0));
            return builder.build(new DOMSource(doc));
        }
    }

    // Returns the context node for a test from its log document
    public static XdmNode getContextFromLog(
            net.sf.saxon.s9api.DocumentBuilder builder, Document log)
            throws Exception {
        Element starttest = (Element) log.getElementsByTagName("starttest")
                .item(0);
        NodeList nl = starttest.getElementsByTagName("context");
        if (nl == null || nl.getLength() == 0) {
            return null;
        } else {
            Element context = (Element) nl.item(0);
            Element value = (Element) context.getElementsByTagName("value")
                    .item(0);
            nl = value.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                    String s = DomUtils.serializeNode(value);
                    XdmNode xn = builder.build(new StreamSource(
                            new CharArrayReader(s.toCharArray())));
                    return (XdmNode) xn.axisIterator(Axis.ATTRIBUTE).next();
                } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Document doc = DomUtils.createDocument(n);
                    return builder.build(new DOMSource(doc));
                }
            }
        }
        return null;
    }

    private static Element makeTestListElement(DocumentBuilder db,
            Document owner, File logdir, String path) throws Exception {
        File log = new File(new File(logdir, path), "log.xml");
        Document logdoc = LogUtils.readLog(log.getParentFile(), ".");
        if (logdoc == null) {
            return null;
        }
        Element log_e = DomUtils.getElementByTagName(logdoc, "log");
        if (log_e == null) {
            return null;
        }
        Element test = owner.createElement("test");
        int result = TECore.PASS;
        String type = "Mandatory";
        boolean complete = false;
        boolean childrenFailed = false;
        boolean hasCache = false;
        for (Element e : DomUtils.getChildElements(log_e)) {
            if (e.getNodeName().equals("starttest")) {
                NamedNodeMap atts = e.getAttributes();
                for (int j = 0; j < atts.getLength(); j++) {
                    String nodeName = atts.item(j).getNodeName();
                    String nodeValue = atts.item(j).getNodeValue();
                    if ("defaultResult".equals(nodeName)) {
                        result = Integer.parseInt(nodeValue);
                    } else if ("type".equals(nodeName)) { // 2011-03-07 PwD
                        type = nodeValue;
                    }
                    test.setAttribute(nodeName, nodeValue);
                }
            } else if (e.getNodeName().equals("endtest")) {
                complete = true;
                int code = Integer.parseInt(e.getAttribute("result"));
                if (childrenFailed) {
                    result = TECore.INHERITED_FAILURE;
                } else {
                    result = code;
                }
            } else if (e.getNodeName().equals("testcall")) {
                String newpath = e.getAttribute("path");
                Element child = makeTestListElement(db, owner, logdir, newpath);
                if (child != null) {
                    child.setAttribute("path", newpath);
                    int code = Integer.parseInt(child.getAttribute("result"));
                    if (code == TECore.FAIL || code == TECore.INHERITED_FAILURE) {
                        childrenFailed = true;
                    }
                    test.appendChild(child);
                }
            } else if (e.getNodeName().equals("cache")) {
                hasCache = true;
            }
        }
        test.setAttribute("result", Integer.toString(result));
        test.setAttribute("complete", complete ? "yes" : "no");
        test.setAttribute("hasCache", hasCache ? "yes" : "no");
        return test;
    }

    /**
     * Produces a document containing a collection of tests run in a base suite.
     * 
     * @param logdir
     *            A File denoting the location of the test log directory.
     * @param path
     *            A session identifier.
     * @param excludes
     *            A list of tests to ignore.
     * @return A Document node where &lt;test&gt; is the document element
     * @throws Exception
     *             If any errors occur.
     */
    public static Document makeTestList(File logdir, String path,
            List<List<QName>> excludes) throws Exception {
        // Fortify Mod: validate logdir and path
        // If they don't form a valid path, throw an error
        File tfile = new File(logdir, path);
        TEPath tpath = new TEPath(tfile.getAbsolutePath());
        if(! tpath.isValid()) {
            throw new IllegalArgumentException("Illegal path = " + tfile.getAbsolutePath());
            }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
	   // Fortify Mod: Disable entity expansion to foil External Entity Injections
	   dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc;
        File testListFile = new File(logdir, path + File.separator
                + "testlist.xml");
        long testListDate = testListFile.lastModified();
        File rootlog = new File(logdir, path + File.separator + "log.xml");
        boolean updated;
        if (testListFile.exists() && testListDate >= rootlog.lastModified()) {
        	try{
            doc = db.parse(testListFile);
            updated = (updateTestListElement(db, doc.getDocumentElement(),
                    logdir, testListDate) != null);
	        } catch(Exception e){
	    		
	    		if(e.toString().contains("Premature end of file")){
	    			return null;
	    		} else {
	    		throw new Exception("Error while writting the 'testlist.xml' file."+ e);
	    		}
	    	}
        } else {
            doc = db.newDocument();
            Element test = makeTestListElement(db, doc, logdir, path);
            if (test != null) {
                doc.appendChild(test);
                doc.getDocumentElement().setAttribute("path", path);
            }
            updated = true;
        }
        if (updated) {
            TransformerFactory tf = TransformerFactory.newInstance();
        	 // Fortify Mod: disable external entity injection            
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(testListFile));
        }
        if (excludes.size() > 0) {
            removeExcludes(doc.getDocumentElement(), new ArrayList<QName>(),
                    excludes);
            updateTestListElement(db, doc.getDocumentElement(), logdir, 0);
        }
        if (LOGR.isLoggable(Level.CONFIG)) {
            StringBuilder msg = new StringBuilder("Read source test list in ");
            msg.append(testListFile.getParent()).append("\n");
            msg.append(DomUtils.serializeNode(doc));
            LOGR.config(msg.toString());
        }
        return doc;
    }

    public static Document makeTestList(File logdir, String path)
            throws Exception {
        List<List<QName>> excludes = new ArrayList<List<QName>>();
        return makeTestList(logdir, path, excludes);
    }

    /*
     * Recalculate each result. If testListDate != 0, then reread new log files
     * as well
     */
    private static Element updateTestListElement(DocumentBuilder db,
            Element test, File logdir, long testListDate) throws Exception {
        String path = test.getAttribute("path");
        long logdate = 0;
        if (testListDate > 0) {
            logdate = new File(logdir, path + File.separator + "log.xml")
                    .lastModified();
        }
        if (logdate > testListDate) {
            Element newtest = makeTestListElement(db, test.getOwnerDocument(),
                    logdir, path);
            test.getParentNode().replaceChild(newtest, test);
            return newtest;
        } else {
            boolean updated = false;
            boolean childrenFailed = false;
            for (Element subtest : DomUtils.getChildElements(test)) {
                Element newsubtest = updateTestListElement(db, subtest, logdir,
                        testListDate);
                if (newsubtest != null) {
                    updated = true;
                    int code = Integer.parseInt(newsubtest
                            .getAttribute("result"));
                    if (code == TECore.FAIL || code == TECore.INHERITED_FAILURE) {
                        childrenFailed = true;
                    }
                }
            }
            if (updated || testListDate == 0) {
                int result = Integer.parseInt(test.getAttribute("result"));
                int newresult = TECore.PASS;
                if (result == TECore.FAIL) {
                    newresult = TECore.FAIL;
                } else if (childrenFailed) {
                    newresult = TECore.INHERITED_FAILURE;
                } else if (result == TECore.WARNING) {
                    newresult = TECore.WARNING;
                }
                if (newresult != result) {
                    test.setAttribute("result", Integer.toString(newresult));
                    return test;
                }
            }
            return null;
        }
    }

    private static void removeExcludes(Element test, List<QName> pathQName,
            List<List<QName>> excludes) throws Exception {
        List<QName> testQName = new ArrayList<QName>();
        testQName.addAll(pathQName);
        String namespaceURI = test.getAttribute("namespace-uri");
        String localPart = test.getAttribute("local-name");
        String prefix = test.getAttribute("prefix");
        QName qname = new QName(namespaceURI, localPart, prefix);
        testQName.add(qname);
        if (excludes.contains(testQName)) {
            test.getParentNode().removeChild(test);
        } else {
            for (Element subtest : DomUtils.getChildElements(test)) {
                removeExcludes(subtest, testQName, excludes);
            }
        }
    }

    /**
     * Generates a session identifier. The value corresponds to the name of a
     * sub-directory (session) in the root test log directory.
     * 
     * @return a session id string
     */
    public static String generateSessionId(File logDir) {
        String session = UUID.randomUUID().toString();
        return session;
    }

    /**
     * Generate a file in logDir refererring all logfiles. Create a file called
     * "report_logs.xml" in the log folder that includes all logs listed inside
     * the directory.
     * 
     * @param sessionLogDir
     *            considered log directory
     * @throws Exception
     * @author F.Vitale vitale@imaa.cnr.it
     */
    public static void createFullReportLog(String sessionLogDir)
            throws Exception {
        LOGR.log(Level.WARNING, "Creating report log for " + sessionLogDir);
        // Fortify Mod: validate sessionLogDir argument
        TEPath tpath = new TEPath(sessionLogDir);
        if( ! tpath.isValid() ) {
            throw new IllegalArgumentException("Illegal path = " + tpath.toString());
            }
        // Make sure the session log directory exits 
        File dir = new File(sessionLogDir);
        if( ! dir.exists() ) {
            if( ! dir.mkdir() ) {
                throw new RuntimeException("Unable to create report log directory " + sessionLogDir);
            }
        }
        File xml_logs_report_file = new File(sessionLogDir + File.separator
                + "report_logs.xml");
        if (xml_logs_report_file.exists()) {
            xml_logs_report_file.delete();
            xml_logs_report_file.createNewFile();
        }
        xml_logs_report_file = new File(sessionLogDir + File.separator
                + "report_logs.xml");
        // xml_logs_report_file = new File("C:\\TE_BASE\\users\\cheazel\\dummy\\report_logs.xml");
        OutputStream report_logs = new FileOutputStream(xml_logs_report_file);
        List<File> files = null;
        Document result = null;

        files = getFileListing(new File(sessionLogDir));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
          // Fortify Mod: Disable entity expansion to foil External Entity Injections
     	   factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Create the document
        Document doc = builder.newDocument();
        // Fill the document
        Element execution = doc.createElement("execution");
        execution.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xi",
                "http://www.w3.org/2001/XInclude");
        doc.appendChild(execution);
        for (File file : files) {
            // all files are Sorted with CompareTO
            Element include = doc.createElementNS(
                    "http://www.w3.org/2001/XInclude", "xi:include");
            include.setAttribute("href", file.getAbsolutePath());
            execution.appendChild(include);
        }
        // Serialize the document into System.out
        TransformerFactory xformFactory = TransformerFactory.newInstance();
          //Fortify Mod: disable external entity injection 
        xformFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer idTransform = xformFactory.newTransformer();
        Source input = new DOMSource(doc);
        Result output = new StreamResult(report_logs);
        idTransform.transform(input, output);
        result = doc; // actually we do not needs results
    }

    /**
     * Recursively walk a directory tree and return a List of all log files
     * found.
     * 
     * 
     * @param logDir
     *            die to walk
     * @return
     * @throws Exception
     */
    private static List<File> getFileListing(File logDir) throws Exception {
        List<File> result = getFileListingLogs(logDir);
        return result;
    }

    /**
     * Get all log files and directories and make recursive call.
     * 
     * @param aStartingDir
     * @return
     * @throws Exception
     */
    static private List<File> getFileListingLogs(File aStartingDir)
            throws Exception {
        List<File> result = new ArrayList<File>();
        File[] logfiles = aStartingDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        List<File> logFilesList = Arrays.asList(logfiles);
        File[] allDirs = aStartingDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File file : logFilesList) {
            if (file.getName().equals("log.xml")) {
                result.add(file);
            }
        }
        List<File> allDirsList = Arrays.asList(allDirs);
        Collections.sort(allDirsList, new Comparator<File>() {
            public int compare(File o1, File o2) {

                if (o1.lastModified() > o2.lastModified()) {
                    return +1;
                } else if (o1.lastModified() < o2.lastModified()) {
                    return -1;
                } else {
                    return 0;
                }
            }

        });
        for (File file : allDirsList) {
            if (!file.isFile()) {
                List<File> deeperList = getFileListingLogs(file);
                result.addAll(deeperList);
            }
        }
        return result;
    }

}
