package com.occamlab.te.web;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;
import com.occamlab.te.util.DocumentationHelper;

/**
 * Verifies the results of executing a CTL test suite. The test suite parameters
 * are read from a properties file located at ${user.home}/sut.properties; if
 * this file does not exist then load the default sut.properties file from the
 * classpath (src/test/resources).
 */
public class VerifyTestSuite {

    static final int VERDICT_PASS = 1;
    static final int VERDICT_SKIP = 3;
    static final int VERDICT_FAIL = 6;
    static final int VERDICT_INHERIT_FAILURE = 5;
    private static Properties testProps;
    private static String previousTEBase;
    private static DocumentBuilder docBuilder;
    private static final String EX_NS = "http://example.org/";
    private RuntimeOptions runOpts;
    private SetupOptions setupOpts;
    private File sessionDir;
    private String kvpTestParam;

    @BeforeClass
    public static void initFixture() throws IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        testProps = new Properties();
        try (InputStream propsInput = VerifyTestSuite.class.getResourceAsStream("/integration-test.properties")) {
            testProps.load(propsInput);
        }
        File targetDir = new File("target");
        File teBaseDir = new File(targetDir, "te-base");
        teBaseDir.mkdir();
        for (String dir : new String[] { "users", "work" }) {
            new File(teBaseDir, dir).mkdir();
        }
        previousTEBase = System.getProperty("TE_BASE");
        System.setProperty("TE_BASE", teBaseDir.getAbsolutePath());
    }

    @AfterClass
    public static void restoreEnvironment() {
        if (null != previousTEBase) {
            System.setProperty("TE_BASE", previousTEBase);
        }
    }

    @Before
    public void initTestSession() {
        this.setupOpts = new SetupOptions();
        this.setupOpts.setValidate(false);
        this.runOpts = new RuntimeOptions();
        this.runOpts.setMode(com.occamlab.te.Test.TEST_MODE);
        this.runOpts.setWorkDir(setupOpts.getWorkDir());
        UUID sessionId = UUID.randomUUID();
        this.runOpts.setSessionId(sessionId.toString());
        File logDir = this.runOpts.getLogDir();
        this.sessionDir = new File(logDir, sessionId.toString());
    }

    @After
    public void generateReport() throws Exception {
        URL reportStylesheet = getClass().getResource("/com/occamlab/te/test_report_html.xsl");
        DocumentationHelper reporter = new DocumentationHelper(reportStylesheet);
        reporter.prettyPrintsReport(this.sessionDir);
    }

    @Test
    public void runNumParityTestSuite() throws Exception {
        this.kvpTestParam = "input=4";
        URL ctlScript = getClass().getResource("/tebase/scripts/num-parity.ctl");
        File ctlFile = new File(ctlScript.toURI());
        this.setupOpts.addSource(ctlFile);
        QName startingTest = new QName(EX_NS, "num-parity-main", "ex");
        this.runOpts.setTestName(startingTest.toString());
        this.runOpts.addParam(this.kvpTestParam);
        File indexFile = new File(this.sessionDir, "index.xml");
        Index mainIndex = Generator.generateXsl(this.setupOpts);
        mainIndex.persist(indexFile);
        File resourcesDir = new File(getClass().getResource("/").toURI());
        TEClassLoader teLoader = new TEClassLoader(resourcesDir);
        Engine engine = new Engine(mainIndex, this.setupOpts.getSourcesName(), teLoader);
        TECore core = new TECore(engine, mainIndex, this.runOpts);
        core.execute();
        Document testLog = docBuilder.parse(new File(this.sessionDir, "log.xml"));
        XPath xpath = XPathFactory.newInstance().newXPath();
        String result = xpath.evaluate("/log/endtest/@result", testLog);
        assertEquals("Unexpected verdict.", VERDICT_PASS, Integer.parseInt(result));
    }
}
