package com.occamlab.te.spi.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.Reporter;

/**
 *  A service to send test statistics and test run results to TestNG HTML report.
 *  The test statistics are printed on 'Result overview' page and 
 *  'Reporter output page' of the HTML reports.
 *  Modifications are made in testng-report.xsl file  to change the 
 *  styling of the result table in the reports as well as to display 
 *  test name, test description and the reason why a test failed.
 * 
 */
public class ReportLog {
    
    /**
     * Creates report logs that consists of test statics by extracting information from 
     * the Test suite results. These Report logs are then printed in the TestNG HTML reports.
     * 
     * @param suite is the test suite from which you want to extract test results information.
     */
    public void generateLogs(ISuite suite) {
        Reporter.clear(); // clear output from previous test runs
        // Reporter.log("Test suite parameters:");
        // Reporter.log(suite.getXmlSuite().getAllParameters().toString());
        Reporter.log("The result of the test is-\n\n");

        //Following code gets the suite name
        String suiteName = suite.getName();
        //Getting the results for the said suite
        Map suiteResults = suite.getResults();
        String input = null;
        String result;
        String failReport = null;
        String failReportConformance2=",";
        int passedTest = 0;
        int failedTest = 0;
        int skippedTest = 0;
        int finalPassedTest=0;
        int finalSkippedTest=0;
        int finalFailedTest=0;
        int count = 0;
        String date = null;
        for (Object obj : suiteResults.values()) {
            count++;
            ISuiteResult sr = (ISuiteResult) obj;
            ITestContext tc = sr.getTestContext();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            if (count == 1) {
                date = dateFormat.format(cal.getTime());
                input = tc.getAttribute("Input").toString();
                failReport = tc.getAttribute("TestResultReport").toString();
                passedTest = tc.getPassedTests().getAllResults().size();
                skippedTest = tc.getSkippedTests().getAllResults().size();
                failedTest = tc.getFailedTests().getAllResults().size();

            } else {
                int no_of_failedTest = tc.getFailedTests().getAllResults().size();
                int no_of_skippedTest = tc.getSkippedTests().getAllResults().size();
                int no_of_passedTest = tc.getPassedTests().getAllResults().size();
                if(no_of_failedTest!=0 || no_of_passedTest !=0)
                {
                    if (no_of_failedTest == 0 && no_of_passedTest !=0 ) {
                    failReportConformance2 = failReportConformance2+", "+input + " conform to the clause A." + count + " of ISO 19139";
                } else {
                        failReportConformance2 = failReportConformance2+", "+input + " does not conform to the clause A." + count + " of ISO 19139";
                    
                }
                finalPassedTest = finalPassedTest + no_of_passedTest;
                finalSkippedTest = finalSkippedTest + no_of_skippedTest;
                finalFailedTest = finalFailedTest + no_of_failedTest;
            }
            }

        }
        failedTest+=finalFailedTest;
        skippedTest+=finalSkippedTest;
        passedTest+=finalPassedTest;
        if(failedTest>0){
          result="Fail";
        }else{
          result="Pass";
        }
        Reporter.log("**RESULT: " + result);
        Reporter.log("**INPUT: " + input);
        Reporter.log("**TEST NAME AND VERSION    :" + suiteName);
        Reporter.log("**DATE AND TIME PERFORMED  :" + date);
        Reporter.log("Passed tests for suite '" + suiteName
                + "' is:" + passedTest);

        Reporter.log("Failed tests for suite '" + suiteName
                + "' is:"
                + failedTest);

        Reporter.log("Skipped tests for suite '" + suiteName
                + "' is:"
                + skippedTest);
        Reporter.log("\nREASON:\n\n");
        Reporter.log(failReport);
        Reporter.log(failReportConformance2);

    }
}
