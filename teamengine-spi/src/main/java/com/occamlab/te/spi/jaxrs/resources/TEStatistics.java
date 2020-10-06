package com.occamlab.te.spi.jaxrs.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.occamlab.te.spi.stats.SessionDetails;
import com.occamlab.te.spi.util.TEStatisticsUtil;

@Path("stats")
public class TEStatistics {

    static Logger logger = Logger.getLogger(TEStatistics.class.getName());

    @GET
    @Produces("application/zip;qs=0.25;charset='utf-8'")
    public Response generateStatisticsReport() throws IOException {
        Source results = generateStatisticsReports();
        String htmlOutput = results.getSystemId().toString();
        Integer count = htmlOutput.split(":", -1).length - 1;
        String zipFile = (count > 1) ? htmlOutput.split("file:/")[1] : htmlOutput.split("file:")[1];
        File fileOut = new File(zipFile);

        if (!fileOut.exists()) {
            throw new WebApplicationException(404);
        }

        return Response.ok(FileUtils.readFileToByteArray(fileOut)).type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"TEStatistics.zip\";").header("Cache-Control", "no-cache").build();
    }

    private Source generateStatisticsReports() {
        String teBasePath = System.getProperty("TE_BASE");

        if (null == teBasePath || teBasePath.isEmpty() || !(new File(teBasePath).exists())) {
            throw new WebApplicationException(Response.serverError()
                    .entity("TE_BASE directory does not exists.").type(MediaType.TEXT_PLAIN).build());
        }

        File configFile = new File(teBasePath + "/config.xml");

        if (!configFile.exists()) {
            throw new WebApplicationException(Response.serverError()
                    .entity("Config file does not exists in TE_BASE directory.").type(MediaType.TEXT_PLAIN).build());
        }

        File usersDir = new File(teBasePath + "/users");

        if (!usersDir.exists()) {
            throw new WebApplicationException(Response.serverError()
                    .entity("User directory does not exists in TE_BASE directory.").type(MediaType.TEXT_PLAIN).build());
        }

        DateTime logDate = new DateTime();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
        String loggerDate = formatter.print(logDate);
        Integer year = logDate.getYear();
        File statisticsLogDir = new File(System.getProperty("java.io.tmpdir"));
        File statisticsLogFile = new File(System.getProperty("java.io.tmpdir"), "TE_StatisticsLog.log");
        File statisticsResultDir = new File(System.getProperty("java.io.tmpdir"), "TE_Statistics_" + loggerDate);

        if (!statisticsLogDir.exists()) {
            statisticsLogDir.mkdir();
        }

        if (statisticsLogFile.exists()) {
            statisticsLogFile.delete();
        }

        FileHandler logFile = null;
        try {
            logFile = new FileHandler(statisticsLogFile.toString(), true);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.setUseParentHandlers(false);
        logFile.setFormatter(new SimpleFormatter());
        logger.addHandler(logFile);

        // Process all the session from all users
        Map<String, List<SessionDetails>> userDetails = TEStatisticsUtil.processUserDir(usersDir);

        // Parse config.xml file
        Document doc = TEStatisticsUtil.parse(configFile);

        NodeList standardList = doc.getElementsByTagName("standard");
        List<String> testSuitesName =  new ArrayList<String>();

        // Tests run per test suite in current year
        Map<String, Object> allTestSuiteRunDetails = new HashMap<String, Object>();

        for (Integer i = 0; i < standardList.getLength(); i++) {
            String testName = "";
            Element standard = (Element) standardList.item(i);

            NodeList nameList = standard.getElementsByTagName("name");
            NodeList versionList = standard.getElementsByTagName("version");
            testName = nameList.item(0).getTextContent();

            for (Integer j = 0; j < versionList.getLength(); j++) {
                Element version = (Element) versionList.item(j);
                NodeList versionValue = version.getElementsByTagName("name");
                String testSuiteName = "";

                if (!"".equals(testName)) {
                    testSuiteName = testName + "_";
                }

                testSuiteName = testSuiteName + versionValue.item(0).getTextContent();

                testSuitesName.add(testSuiteName);

                // Number of users executed the test suite standard per month in current year
                ArrayList<Long> numberOfUsersExecutedTestSuitePerMonth = 
                        TEStatisticsUtil.numberOfUsersExecutedTestSuitePerMonth(testSuiteName, userDetails);

                // Number of test suite run per month in current year.
                ArrayList<Long> testSuiteRunPerMonth = 
                        TEStatisticsUtil.testSuiteRunPerMonth(testSuiteName, userDetails);

                // Number of test suite status(success, failure, Incomplete) run per month.
                Map<String, Integer> testStatus = new HashMap<String, Integer>();
                testStatus.put("Success", 1);
                testStatus.put("Failure", 6);
                testStatus.put("Incomplete", 0);
                Map<String, ArrayList<Long>> testSuiteStatusPerMonth = 
                        TEStatisticsUtil.testSuiteStatusPerMonth(testSuiteName, userDetails, testStatus);

                ArrayList<Long> successArray = testSuiteStatusPerMonth.get("Success");
                ArrayList<Long> failureArray = testSuiteStatusPerMonth.get("Failure");
                ArrayList<Long> incompleteArray = testSuiteStatusPerMonth.get("Incomplete");

                // Test suite - Passing, failing & incomplete test runs in current year.
                Map<String, Object> testSuiteStatusWithDrilldown = 
                        TEStatisticsUtil.testSuiteStatusWithDrilldown(testSuiteName, userDetails, testStatus);

                Map<String, Long> testSuiteFailedTestDrillDownMap = TEStatisticsUtil.testSuiteFailedTestDrillDownMap;

                // Generate test suite statistics HTML report using above result data
                TEStatisticsUtil.generateTestSuiteStatisticsHtmlReport(testSuiteName, year, statisticsResultDir,
                        TEStatisticsUtil.getArrayListAsString(numberOfUsersExecutedTestSuitePerMonth),
                        TEStatisticsUtil.getArrayListAsString(testSuiteRunPerMonth),
                        TEStatisticsUtil.getArrayListAsString(successArray),
                        TEStatisticsUtil.getArrayListAsString(failureArray),
                        TEStatisticsUtil.getArrayListAsString(incompleteArray),
                        new JSONObject(testSuiteStatusWithDrilldown).toString(),
                        new JSONObject(testSuiteFailedTestDrillDownMap).toString());

                // Tests run per test suite in current year
                Map<String, Object> testSuiteRunDetails = 
                        TEStatisticsUtil.testSuiteRunDetailsOfCurrentYear(testSuiteName, userDetails);

                allTestSuiteRunDetails.putAll(testSuiteRunDetails);
            }
        }

        // Tests run per month in current year
        ArrayList<Long> testsRunPerMonth =  TEStatisticsUtil.testsRunPerMonthofCurrentYear(userDetails);

        // Number of users per month in current year
        ArrayList<Long> usersPerMonth = TEStatisticsUtil.usersPerMonthofCurrentYear(userDetails);

        // Number of users per test suite in current year.
        Map<String, Object> numberOfUsersPerTestSuite = new HashMap<String, Object>();
        Map<String, Object> numberOfUsersAndTestSuite = new HashMap<String, Object>();

        for (String testSuiteName : testSuitesName) {
            numberOfUsersPerTestSuite = TEStatisticsUtil.numberOfUsersPerTestSuite(testSuiteName, userDetails);
            numberOfUsersAndTestSuite.putAll(numberOfUsersPerTestSuite);
        }

        // Generate overall statistics HTML report using above result data
        TEStatisticsUtil.generateOverallStatisticsHtmlReport(testSuitesName, year, statisticsResultDir,
                new JSONObject(allTestSuiteRunDetails).toString(),
                TEStatisticsUtil.getArrayListAsString(testsRunPerMonth),
                TEStatisticsUtil.getArrayListAsString(usersPerMonth),
                new JSONObject(numberOfUsersAndTestSuite).toString());

        File zipFile = new File(statisticsLogDir, "TEStatistics.zip");

        if (zipFile.exists()) {
            zipFile.delete();
        }

        Source results = null;
        try {
            TEStatisticsUtil.zipDir(zipFile, statisticsResultDir);
            InputStream inStream = new FileInputStream(zipFile);
            InputSource inSource = new InputSource(new InputStreamReader(inStream, StandardCharsets.UTF_8));
            results = new SAXSource(inSource);
            results.setSystemId(zipFile.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}