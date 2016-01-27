package com.occamlab.te;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;

public class VerifyTECoreResults {

	static Engine engine;
	static RuntimeOptions runOpts;

	@BeforeClass
	public static void setupFixture() throws Exception {
		engine = new Engine();
		runOpts = new RuntimeOptions();
		runOpts.setLogDir(null);
	}

	@After
	public void clearParams() {
		runOpts.getParams().clear();
	}

	@Test
	public void allSubtestsSkipped() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=103");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be SKIP
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)),
				TECore.INHERITED_FAILURE, mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.SKIPPED);
		assertTestResult(teCore.getIndex(), "test-2", TECore.SKIPPED);
	}
	
	@Test
	public void allMandatoryIfImplementedSubtestsSkipped() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip_mif.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=103");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be SKIP
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)),
				TECore.SKIPPED, mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.SKIPPED);
		assertTestResult(teCore.getIndex(), "test-2", TECore.SKIPPED);
	}
	
	@Test
	public void someSubtestsSkipped2() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=41");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be PASS
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)), TECore.INHERITED_FAILURE,
				mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.SKIPPED);
		assertTestResult(teCore.getIndex(), "test-2", TECore.PASS);
	}

	@Test
	public void someSubtestsSkipped() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=140");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be PASS
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)), TECore.INHERITED_FAILURE,
				mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.PASS);
		assertTestResult(teCore.getIndex(), "test-2", TECore.SKIPPED);
	}
	
	@Test
	public void someMandatoryIfImplementedSubtestsSkipped() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip_mif.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=-13");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be PASS
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)), TECore.INHERITED_FAILURE,
				mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.SKIPPED);
		assertTestResult(teCore.getIndex(), "test-2", TECore.FAIL);
	}

	@Test
	public void subtestFailed() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=-2");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be Failed-Inherited
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)),
				TECore.INHERITED_FAILURE, mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.PASS);
		assertTestResult(teCore.getIndex(), "test-2", TECore.FAIL);
	}
	
	@Test
	public void subtestMandatoryIfImplementedFailed() throws Throwable {
		Index testIndex = getTestIndex(new File(
				"src/test/resources/ctl/skip_mif.ctl"));
		engine.addFunctionLibrary(Collections.singletonList(testIndex));
		runOpts.addParam("input=-2");
		TECore teCore = new TECore(engine, testIndex, runOpts);
		assertNotNull(teCore);
		teCore.execute();
		SuiteEntry testSuite = teCore.getIndex().getSuite("ex:skip");
		assertNotNull(testSuite);
		TestEntry mainTest = teCore.getIndex().getTest(
				testSuite.getStartingTest());
		assertNotNull(mainTest);
		// Main test result should be Failed-Inherited
		int mainResult = mainTest.getResult();
		assertEquals(
				String.format("Unexpected main result: '%s'.",
						TECore.getResultDescription(mainResult)),
				TECore.INHERITED_FAILURE, mainResult);
		assertTestResult(teCore.getIndex(), "test-1", TECore.PASS);
		assertTestResult(teCore.getIndex(), "test-2", TECore.FAIL);
	}

	Index getTestIndex(File ctlFile) throws Throwable {
		assertTrue(ctlFile.exists());
		SetupOptions setupOptions = new SetupOptions();
		setupOptions.addSource(ctlFile);
		Index testIndex = Generator.generateXsl(setupOptions);
		assertNotNull(testIndex);
		return testIndex;
	}

	void assertTestResult(Index index, String testName, int expectedResult)
			throws Throwable {
		TestEntry test = index.getTest(testName);
		assertNotNull(test);
		assertEquals(expectedResult, test.getResult());
	}
}
