package com.occamlab.te;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;

public class TECoreTest {

    static Engine engine;
    static Index index;
    static RuntimeOptions runOpts;

    @BeforeClass
    public static void setupFixture() throws Exception {
        engine = new Engine();
        index = new Index();
        runOpts = new RuntimeOptions();
        runOpts.setLogDir(null);
    }

    @Test
    public void parseXmlWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/article.xml");
        URLConnection urlConn = url.openConnection();
        TECore iut = new TECore(engine, index, runOpts);
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
        TECore iut = new TECore(engine, index, runOpts);
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
        setupOptions.addSource(ctlFile);
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
        Index testIndex = getTestIndex(new File("src/test/resources/ctl/nested-failure.xml"));
        TECore teCore = new TECore(engine, testIndex, runOpts);
        assertNotNull(teCore);        
        teCore.execute();

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
    }
}
