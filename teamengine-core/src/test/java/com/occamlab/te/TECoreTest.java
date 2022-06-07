/**
 *
 * Contributor(s):
 *     C. Heazel: WiSC Enterprises - Modifications to adjudicate Fortify issues
 *        February 26, 2018
 *
*/
package com.occamlab.te;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;

public class TECoreTest {

    static Index index;
    static RuntimeOptions runOpts;

    @BeforeClass
    public static void setupFixture() throws Exception {
        index = new Index();
        runOpts = new RuntimeOptions();
        runOpts.setLogDir(null);
        runOpts.setSessionId("s0001");
    }

    @Test
    public void parseXmlWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/article.xml");
        URLConnection urlConn = url.openConnection();
        TECore iut = new TECore(new Engine(), index, runOpts);
        Element result = iut.parse(urlConn, null);
        Element content = (Element) result.getElementsByTagName("content")
                .item(0);
        assertNotNull("Expected content element.", content);
        assertTrue("Expected XML content ", content.hasChildNodes());
    }

    @Test
    public void parseTextWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/jabberwocky.txt");
        URLConnection urlConn = url.openConnection();
        TECore iut = new TECore(new Engine(), index, runOpts);
        Element result = iut.parse(urlConn, null);
        Element content = (Element) result.getElementsByTagName("content")
                .item(0);
        assertNotNull("Expected content element.", content);
        assertTrue("Expected text content starting with 'Twas brillig", content
                .getTextContent().startsWith("'Twas brillig"));
    }

    private Index getTestIndex(File ctlFile) throws Throwable {
        assertTrue(ctlFile.exists());
        SetupOptions setupOptions = new SetupOptions();
        // Fortify Mod: addSource now returns a boolean indicating that
        // the ctlFile is at a valid location.
        assertTrue("Invalid path to ctl file: " + ctlFile.getAbsolutePath(), setupOptions.addSource(ctlFile));
        Index testIndex = Generator.generateXsl(setupOptions);
        assertNotNull(testIndex);
        return testIndex;
    }

    private void assertTestResult(Index index, String testName, int expectedResult) throws Throwable {
        TestEntry test = index.getTest(testName);
        assertNotNull(test);
        assertEquals(expectedResult, test.getResult());
    }

    @Test
    public void testNestedFailure() throws Throwable {
        ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
        PrintStream origPrintStream = System.out;
        System.setOut(new PrintStream(outCapture));

        Index testIndex = getTestIndex(new File("src/test/resources/ctl/nested-failure.xml"));
        TECore teCore = new TECore(new Engine(), testIndex, runOpts);
        assertNotNull(teCore);

        try {
            teCore.execute();
        } finally {
            System.setOut(origPrintStream);
        }

        String output = outCapture.toString();
        System.out.print(output); 

        // Check result of starting test directly since the value of the 
        // verdict instance variable probably corresponds to some subtest.
        TestEntry startingTest = teCore.testStack.getFirst();
        assertEquals(TECore.MSG_INHERITED_FAILURE,  
            TECore.getResultDescription(startingTest.getResult()));

        SuiteEntry testSuite = teCore.getIndex().getSuite("test:suite");
        assertNotNull(testSuite);

        TestEntry mainTest = teCore.getIndex().getTest(testSuite.getStartingTest());
        assertNotNull(mainTest);
        // Starting test verdict is set by failing subtest(s)
        assertEquals(TECore.INHERITED_FAILURE, mainTest.getResult());
        assertTestResult(teCore.getIndex(), "testA", TECore.INHERITED_FAILURE);
        assertTestResult(teCore.getIndex(), "testB", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testA1", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testA2", TECore.FAIL);
        assertTestResult(teCore.getIndex(), "testA3", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testB1", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testB2", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testB3", TECore.PASS);

        //also verify the output messages
        verifyOutputContainsResult(output, "suite", TECore.FAIL);
        verifyOutputContainsResult(output, "test:main", TECore.INHERITED_FAILURE);
        verifyOutputContainsResult(output, "test:testA", TECore.INHERITED_FAILURE);
        verifyOutputContainsResult(output, "test:testB", TECore.PASS);
        verifyOutputContainsResult(output, "test:testA1", TECore.PASS);
        verifyOutputContainsResult(output, "test:testA2", TECore.FAIL);
        verifyOutputContainsResult(output, "test:testA3", TECore.PASS);
        verifyOutputContainsResult(output, "test:testB1", TECore.PASS);
        verifyOutputContainsResult(output, "test:testB2", TECore.PASS);
        verifyOutputContainsResult(output, "test:testB3", TECore.PASS);
    }

    @Test
    public void testNestedWarning() throws Throwable {
        ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
        PrintStream origPrintStream = System.out;
        System.setOut(new PrintStream(outCapture));

        Index testIndex = getTestIndex(new File("src/test/resources/ctl/nested-warning.xml"));
        TECore teCore = new TECore(new Engine(), testIndex, runOpts);
        assertNotNull(teCore);

        try {
            teCore.execute();
        } finally {
            System.setOut(origPrintStream);
        }

        String output = outCapture.toString();
        System.out.print(output);

        // Check result of starting test directly since the value of the
        // verdict instance variable probably corresponds to some subtest.
        TestEntry startingTest = teCore.testStack.getFirst();
        assertEquals(TECore.MSG_WARNING,
            TECore.getResultDescription(startingTest.getResult()));

        SuiteEntry testSuite = teCore.getIndex().getSuite("test:suite");
        assertNotNull(testSuite);

        TestEntry mainTest = teCore.getIndex().getTest(testSuite.getStartingTest());
        assertNotNull(mainTest);
        // Starting test verdict is set by failing subtest(s)
        assertEquals(TECore.WARNING, mainTest.getResult());
        assertTestResult(teCore.getIndex(), "testA", TECore.WARNING);
        assertTestResult(teCore.getIndex(), "testB", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testA1", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testA2", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testA3", TECore.WARNING);
        assertTestResult(teCore.getIndex(), "testB1", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testB2", TECore.PASS);
        assertTestResult(teCore.getIndex(), "testB3", TECore.PASS);

        //also verify the output messages
        verifyOutputContainsResult(output, "suite", TECore.WARNING);
        verifyOutputContainsResult(output, "test:main", TECore.WARNING);
        verifyOutputContainsResult(output, "test:testA", TECore.WARNING);
        verifyOutputContainsResult(output, "test:testB", TECore.PASS);
        verifyOutputContainsResult(output, "test:testA1", TECore.PASS);
        verifyOutputContainsResult(output, "test:testA2", TECore.PASS);
        verifyOutputContainsResult(output, "test:testA3", TECore.WARNING);
        verifyOutputContainsResult(output, "test:testB1", TECore.PASS);
    }

    private void verifyOutputContainsResult(String output, String target, int result) {
        String expectedResult = TECore.getResultDescription(result);
        assertTrue(target + " didn't have result " + expectedResult,
                output.contains(target + " " + TECore.getResultDescription(result)));        
    }
}
