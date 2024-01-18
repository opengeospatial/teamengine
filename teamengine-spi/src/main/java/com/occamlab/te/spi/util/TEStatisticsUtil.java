package com.occamlab.te.spi.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.management.AttributeNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.occamlab.te.spi.stats.SessionDetails;
import com.occamlab.te.spi.stats.TEStatisticsErrorHandler;

public class TEStatisticsUtil {
    static Logger logger = Logger.getLogger(TEStatisticsUtil.class.getName());

    public static Map<String, Long> testSuiteFailedTestDrillDownMap = new HashMap<>();

    public static Map<String, List<SessionDetails>> processUserDir(File usersDir) {
        Map<String, List<SessionDetails>> userDetails = new HashMap<>();
        String[] userDirList = usersDir.list();

        if (null != userDirList && 0 < userDirList.length) {
            Arrays.sort(userDirList);

            for (Integer i = 0; i < userDirList.length; i++) {
                String[] sessionDirs = new File(usersDir, userDirList[i]).list();

                if (null != sessionDirs && 0 < sessionDirs.length) {
                    Arrays.sort(sessionDirs);
                    List<SessionDetails> sessions = new ArrayList<>();

                    for (Integer j = 0; j < sessionDirs.length; j++) {
                        File sessionDir = new File(new File(usersDir, userDirList[i]), sessionDirs[j]);
                        File sessionFile = new File(sessionDir, "session.xml");
                        File logFile = null;

                        if (sessionFile.exists()) {
                            try {
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                dbf.setValidating(false);
                                dbf.setNamespaceAware(true);

                                DocumentBuilder db = dbf.newDocumentBuilder();
                                db.setErrorHandler(new TEStatisticsErrorHandler());

                                Document doc = db.parse(sessionFile);
                                Element session = (Element) (doc.getElementsByTagName("session").item(0));
                                SessionDetails user = new SessionDetails();

                                if (session.hasAttribute("id")) {
                                    user.setId(session.getAttribute("id"));
                                } else {
                                    throw new AttributeNotFoundException("'id' attribute not found in : '" + sessionFile + "'");
                                }

                                if (session.hasAttribute("sourcesId")) {
                                    user.setEtsName(session.getAttribute("sourcesId"));
                                } else {
                                    throw new AttributeNotFoundException("'sourceId' attribute not found in : '" + sessionFile + "'");
                                }

                                if (session.hasAttribute("date")) {
                                    user.setDate(session.getAttribute("date"));
                                } else {
                                    throw new AttributeNotFoundException("'date' attribute not found in : '" + sessionFile + "'");
                                }

                                sessions.add(user);

                                // Get test result from log.xml
                                logFile = new File(sessionDir, "log.xml");
                                List<String> failedTestList = null;
                                Integer status = getSessionResult(logFile);

                                if (status == 6) {
                                    failedTestList = getListOfFailedTest(sessionDir);
                                }

                                user.setStatus(status);
                                user.setFailedTestList(failedTestList);

                            } catch (SAXParseException pe) {
                                logger.log(Level.SEVERE, "Error: Unable to parse xml >>" + " Public ID: " + pe.getPublicId()
                                                + ", System ID: " + pe.getSystemId() + ", Line number: " + pe.getLineNumber()
                                                + ", Column number: " + pe.getColumnNumber() + ", Message: " + pe.getMessage());
                            } catch (FileNotFoundException fnfe) {
                                logger.log(Level.SEVERE, "Error: Log file not found at -> " + logFile);
                            } catch (NullPointerException npe) {
                                logger.log(Level.SEVERE, "Error:" + npe.getMessage() + " at -> " + logFile);
                            } catch (AttributeNotFoundException anfe) {
                                logger.log(Level.SEVERE, "Error: Attribute not found in session."
                                                + anfe.getMessage() + " at -> " + logFile);
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Error: Mandatory values are not valid: "
                                                + "' " + e.getMessage() + " ' at -> " + logFile);
                            }
                        }
                    }
                    userDetails.put(userDirList[i], sessions);
                }
            }
        }
        return userDetails;
    }

    public static Integer getSessionResult(File logFile) {

        if (logFile.exists()) {
            Document doc = parse(logFile);
            NodeList logElementList = doc.getElementsByTagName("log");

            Element logElement = (Element) logElementList.item(0);
            NodeList testResult = logElement.getElementsByTagName("endtest");

            if (testResult.getLength() == 0) {
                throw new NoSuchElementException("The 'endtest' element not found in log file.");
            } else {
                Element resultStatus = (Element) testResult.item(0);

                if (resultStatus.hasAttribute("result") && !resultStatus.getAttribute("result").isEmpty()) {
                    return Integer.parseInt(resultStatus.getAttribute("result"));
                } else {
                    throw new RuntimeException("The 'result' attribute not found or having the NULL value in log file.");
                }
            }
        }
        return 0;
    }

    public static List<String> getListOfFailedTest(File sessionDir) {
        File testngDir = new File(sessionDir, "testng");
        List<String> failedTestList = new ArrayList<>();
        File testngResult = null;

        if (testngDir.exists()) {
            String[] dir = testngDir.list();
            File testngUuidDirectory = new File(testngDir, dir[0]);

            if (testngUuidDirectory.isDirectory()) {
                testngResult = new File(testngUuidDirectory,"testng-results.xml");
            }

            if (testngResult.exists()) {
                Document doc = parse(testngResult);
                NodeList failedNodeList = null;
                try {
                    failedNodeList = evaluateXPath(doc, "//test/class/test-method[@status='FAIL']");

                    for (Integer i = 0; i < failedNodeList.getLength(); i++) {
                        Element testElement = (Element) failedNodeList.item(i);

                        if (testElement.hasAttribute("name")) {
                            failedTestList.add(testElement.getAttribute("name"));
                        }
                    }
                } catch (XPathExpressionException xpe) {
                    throw new RuntimeException(xpe);
                }
            }
        }
        return failedTestList;
    }

    public static NodeList evaluateXPath(Node context, String expr) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        Object result = xpath.evaluate(expr, context, XPathConstants.NODESET);

        if (!NodeList.class.isInstance(result)) {
            throw new XPathExpressionException("Expression does not evaluate to a NodeList: " + expr);
        }
        return (NodeList) result;
    }

    /**
     * The method is used to parse given XML file.
     * 
     * @param configFile
     * @return Document object
     */
    public static Document parse(File configFile) {
        Document doc = null;
        try {
            InputStream inputStream = new FileInputStream(configFile);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setErrorHandler(new TEStatisticsErrorHandler());
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
        } catch (SAXParseException pe) {
            logger.log(Level.SEVERE, "Error: Unable to parse xml >>"
                    + " Public ID: " + pe.getPublicId() + ", System ID: "
                    + pe.getSystemId() + ", Line number: " + pe.getLineNumber()
                    + ", Column number: " + pe.getColumnNumber() + ", Message: " + pe.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error: In main method Mandatory values are not valid: " + "' " + e.getMessage() + " '");
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * Generates number of users executed test suite per month in current year.
     * 
     * @param testSuiteName
     *            Name of the test suite with version.
     * @param sessionDetailsList
     *            Map of the users session list.
     * @return ArrayList of user count per month
     */
    public static ArrayList<Long> numberOfUsersExecutedTestSuitePerMonth(String testSuiteName, 
            Map<String, List<SessionDetails>> sessionDetailsList) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
        DateTime currentTime = DateTime.now();
        ArrayList<Integer> monthList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        ArrayList<Long> testSuiteUsersPerMonth = new ArrayList<>();

        for (Integer month : monthList) {
            long count = 0;
            long cnt = 0;

            for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
                List<SessionDetails> sessionList = userSessions.getValue();
                count = sessionList.stream()
                        .filter(session -> session.getEtsName().contains(testSuiteName)
                                && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                && formatter.parseDateTime(session.getDate()).getMonthOfYear() == month)
                        .collect(Collectors.counting());

                if (count > 0) {
                    cnt++;
                }
            }
            testSuiteUsersPerMonth.add(cnt);
        }
        return testSuiteUsersPerMonth;
    }

    /**
     * Generates the result for test suite standard runs per month in current year.
     * 
     * @param testSuiteName
     *            Name of the test suite with version.
     * @param sessionDetailsList
     *            Map of the users session list.
     * @return ArrayList of test counts per month.
     */
    public static ArrayList<Long> testSuiteRunPerMonth(String testSuiteName, 
            Map<String, List<SessionDetails>> sessionDetailsList) {

        long jan = 0, feb = 0, mar = 0, apr = 0, may = 0, jun = 0, jul = 0, aug = 0, sep = 0, oct = 0, nov = 0, dec = 0;
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
        DateTime currentTime = DateTime.now();
        List<SessionDetails> foundSessions = null;

        for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList
                .entrySet()) {
            List<SessionDetails> sessionList = userSessions.getValue();

            foundSessions = sessionList.stream()
                    .filter(session -> session.getEtsName().contains(testSuiteName)
                            && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear())
                    .collect(Collectors.toList());

            if (foundSessions != null && !foundSessions.isEmpty()) {
                for (SessionDetails session : foundSessions) {
                    DateTime sessionDate = formatter.parseDateTime(session.getDate());
                    Integer sessionMonth = sessionDate.getMonthOfYear();
                    switch (sessionMonth) {
                    case 1:
                        jan++;
                        break;
                    case 2:
                        feb++;
                        break;
                    case 3:
                        mar++;
                        break;
                    case 4:
                        apr++;
                        break;
                    case 5:
                        may++;
                        break;
                    case 6:
                        jun++;
                        break;
                    case 7:
                        jul++;
                        break;
                    case 8:
                        aug++;
                        break;
                    case 9:
                        sep++;
                        break;
                    case 10:
                        oct++;
                        break;
                    case 11:
                        nov++;
                        break;
                    case 12:
                        dec++;
                        break;
                    }
                }
            }
        }
        ArrayList<Long> testSuiteRunsPerMonth = new ArrayList<>(
                Arrays.asList(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec));
        return testSuiteRunsPerMonth;
    }

    /**
     * Generates the result for test suite standards success, failure and
     * incomplete per month in last year
     * 
     * @param testSuiteName
     *            Name of the test suite with version.
     * @param sessionDetailsList
     *            Map of the users session list.
     * @param testStatus
     *            Status of test success, failure or incomplete
     * @return Map Object Returns the map object with success, failure,
     *         incomplete count.
     */
    public static Map<String, ArrayList<Long>> testSuiteStatusPerMonth(String testSuiteName, 
            Map<String, List<SessionDetails>> sessionDetailsList, Map<String, Integer> testStatus) {

        Map<String, ArrayList<Long>> testSuiteStatusPerMonth = new HashMap<>();

        for (Entry<String, Integer> status : testStatus.entrySet()) {
            long jan = 0, feb = 0, mar = 0, apr = 0, may = 0, jun = 0, jul = 0, aug = 0, sep = 0, oct = 0, nov = 0, dec = 0;
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
            DateTime currentTime = DateTime.now();
            List<SessionDetails> foundSessions = null;

            for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
                List<SessionDetails> sessionList = userSessions.getValue();

                if (status.getValue() == 0) {
                    foundSessions = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                    && session.getStatus() != testStatus.get("Success")
                                    && session.getStatus() != testStatus.get("Failure"))
                            .collect(Collectors.toList());
                } else if (status.getValue() == 6) {
                    foundSessions = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear())
                            .collect(Collectors.toList());

                    foundSessions = foundSessions.stream()
                            .filter(session -> session.getStatus() == 5 || session.getStatus() == testStatus.get("Failure"))
                            .collect(Collectors.toList());
                } else {
                    foundSessions = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                    && session.getStatus() == status.getValue())
                            .collect(Collectors.toList());
                }

                if (foundSessions != null && !foundSessions.isEmpty()) {
                    for (SessionDetails session : foundSessions) {
                        DateTime sessionDt = formatter.parseDateTime(session.getDate());
                        Integer sessionMonth = sessionDt.getMonthOfYear();
                        switch (sessionMonth) {
                        case 1:
                            jan++;
                            break;
                        case 2:
                            feb++;
                            break;
                        case 3:
                            mar++;
                            break;
                        case 4:
                            apr++;
                            break;
                        case 5:
                            may++;
                            break;
                        case 6:
                            jun++;
                            break;
                        case 7:
                            jul++;
                            break;
                        case 8:
                            aug++;
                            break;
                        case 9:
                            sep++;
                            break;
                        case 10:
                            oct++;
                            break;
                        case 11:
                            nov++;
                            break;
                        case 12:
                            dec++;
                            break;
                        }
                    }
                }
            }
            ArrayList<Long> statusPerMonth = new ArrayList<>(
                    Arrays.asList(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec));
            testSuiteStatusPerMonth.put(status.getKey(), statusPerMonth);
        }
        return testSuiteStatusPerMonth;
    }

    public static Map<String, Object> testSuiteStatusWithDrilldown(String testSuiteName, 
            Map<String, List<SessionDetails>> sessionDetailsList, Map<String, Integer> testStatus) {

        Map<String, Object> testSuiteStatusDrillDownMap = new HashMap<>();
        Map<String, Long> testSuiteFailedTestMap = new HashMap<>();

        for (Entry<String, Integer> status : testStatus.entrySet()) {
            long testCount = 0;

            DateTimeFormatter formatter = DateTimeFormat
                    .forPattern("yyyy/MM/dd  HH:mm:ss");
            DateTime currentTime = DateTime.now();
            List<SessionDetails> foundSessions = null;

            for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
                List<SessionDetails> sessionList = userSessions.getValue();
                long count = 0;

                if (status.getValue() == 0) {
                    count = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                    && session.getStatus() != testStatus.get("Success")
                                    && session.getStatus() != testStatus.get("Failure"))
                            .collect(Collectors.counting());

                    if (count > 0) {
                        testCount += count;
                    }
                } else if (status.getValue() == 6) {
                    foundSessions = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear())
                            .collect(Collectors.toList());

                    count = foundSessions.stream()
                            .filter(session -> session.getStatus() == 5 || session.getStatus() == testStatus.get("Failure"))
                            .collect(Collectors.counting());

                    if (count > 0) {
                        testCount += count;
                    }

                    foundSessions = foundSessions.stream()
                            .filter(session -> session.getStatus() == 5 || session.getStatus() == testStatus.get("Failure"))
                            .collect(Collectors.toList());

                    // Iterate each session to get list of failed tests
                    if (foundSessions != null && !foundSessions.isEmpty()) {
                        for (SessionDetails session : foundSessions) {
                            List<String> testList = session.getFailedTestList();
                            if (testList != null && !testList.isEmpty()) {
                                for (String test : testList) {
                                    testSuiteFailedTestMap.compute(test, (k, v) -> v == null ? 1 : v + 1);
                                }
                            }
                        }
                    }
                } else {
                    count = sessionList.stream()
                            .filter(session -> session.getEtsName().contains(testSuiteName)
                                    && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                    && session.getStatus() == status.getValue())
                            .collect(Collectors.counting());

                    if (count > 0) {
                        testCount += count;
                    }
                }
            }
            testSuiteStatusDrillDownMap.put(status.getKey(), testCount);
        }
        testSuiteFailedTestDrillDownMap.clear();
        testSuiteFailedTestDrillDownMap.putAll(testSuiteFailedTestMap);
        return testSuiteStatusDrillDownMap;
    }

    /**
     * This method is used to convert ArrayList to string which is used in
     * charts.
     * 
     * @param usersPerMonth
     * @return String
     */
    public static String getArrayListAsString(ArrayList<Long> usersPerMonth) {
        Iterator<Long> it = usersPerMonth.iterator();
        String arrayList = "[";

        while (it.hasNext()) {
            arrayList += it.next();

            if (it.hasNext()) {
                arrayList += ",";
            } else {
                arrayList += "]";
            }
        }
        return arrayList;
    }

    /**
     * Generate test suite statistics HTML report by using XSL file and results.
     *
     * @param testSuiteName
     * @param year
     * @param statisticsResultDir
     * @param numberOfUsersExecutedTestSuitePerMonth
     * @param testSuiteRunPerMonth
     * @param successArray
     * @param failureArray
     * @param incompleteArray
     * @param testSuiteStatusWithDrilldown
     * @param testSuiteFailedTestDrillDownMap
     */
    public static void generateTestSuiteStatisticsHtmlReport(String testSuiteName, Integer year, 
            File statisticsResultDir, String numberOfUsersExecutedTestSuitePerMonth, String testSuiteRunPerMonth,
            String successArray, String failureArray, String incompleteArray, 
            String testSuiteStatusWithDrilldown, String testSuiteFailedTestDrillDownMap) {

        FileOutputStream fo;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String xmlTemplate1 = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
            InputStream input = new ByteArrayInputStream(xmlTemplate1.getBytes(StandardCharsets.UTF_8));
            String statXsl = cl.getResource("com/occamlab/te/stats/test_suite_stats.xsl").toString();
            Transformer transformer = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                    .newTransformer(new StreamSource(statXsl));
            transformer.setParameter("testSuiteName", testSuiteName);
            transformer.setParameter("year", year);
            transformer.setParameter("numberOfUsersExecutedTestSuitePerMonth", numberOfUsersExecutedTestSuitePerMonth);
            transformer.setParameter("testSuiteRunPerMonth", testSuiteRunPerMonth);
            transformer.setParameter("successArray", successArray);
            transformer.setParameter("failureArray", failureArray);
            transformer.setParameter("incompleteArray", incompleteArray);
            transformer.setParameter("testSuiteStatusWithDrilldown", testSuiteStatusWithDrilldown);
            transformer.setParameter("testSuiteFailedTestDrillDownMap", testSuiteFailedTestDrillDownMap);

            if (!statisticsResultDir.exists()) {
                statisticsResultDir.mkdir();
            }
            testSuiteName = testSuiteName.replace(" ", "_");
            File testSuiteHtml = new File(statisticsResultDir, testSuiteName + "_stats.html");
            testSuiteHtml.createNewFile();
            fo = new FileOutputStream(testSuiteHtml);
            transformer.transform(new StreamSource(input), new StreamResult(fo));
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to generate result of test runs per month in current
     * year.
     * 
     * @param testSuiteName
     *            Name of the test suite with version.
     * @param sessionDetailsList
     *            Map of the users session list.
     */
    public static Map<String, Object> testSuiteRunDetailsOfCurrentYear(String testSuiteName,
            Map<String, List<SessionDetails>> sessionDetailsList) {

        Map<String, Object> testSuiteRunDetails = new HashMap<>();
        long count = 0;
        List<SessionDetails> foundSessions = null;

        for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
            List<SessionDetails> sessionList = userSessions.getValue();

            foundSessions = sessionList.stream()
                    .filter(session -> session.getEtsName().contains(testSuiteName)).collect(Collectors.toList());

            DateTime currentTime = null;

            if (foundSessions != null && !foundSessions.isEmpty()) {
                for (SessionDetails session : foundSessions) {
                    try {
                        currentTime = DateTime.now();
                        String testExecutionTime = session.getDate();
                        DateTime testExecutionDt = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss").parseDateTime(testExecutionTime);

                        if (testExecutionDt.getYear() == currentTime.getYear()) {
                            count++;
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        }
        testSuiteRunDetails.put(testSuiteName, count);
        return testSuiteRunDetails;
    }

    /**
     * This method will create statistics or test runs per month in current year.
     * 
     * @param sessionDetailsList
     *            Map of users sessions.
     * @return ArrayList Of Integer test count per month.
     */
    public static ArrayList<Long> testsRunPerMonthofCurrentYear(Map<String, List<SessionDetails>> sessionDetailsList) {

        long jan = 0, feb = 0, mar = 0, apr = 0, may = 0, jun = 0, jul = 0, aug = 0, sep = 0, oct = 0, nov = 0, dec = 0;
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
        DateTime currentTime = DateTime.now();
        List<SessionDetails> foundSessions = null;

        for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
            List<SessionDetails> sessionList = userSessions.getValue();
            foundSessions = sessionList.stream()
                    .filter(session -> formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear())
                    .collect(Collectors.toList());

            if (foundSessions != null && !foundSessions.isEmpty()) {
                for (SessionDetails session : foundSessions) {
                    DateTime sessionDt = formatter.parseDateTime(session.getDate());
                    Integer sessionMonth = sessionDt.getMonthOfYear();
                    switch (sessionMonth) {
                    case 1:
                        jan++;
                        break;
                    case 2:
                        feb++;
                        break;
                    case 3:
                        mar++;
                        break;
                    case 4:
                        apr++;
                        break;
                    case 5:
                        may++;
                        break;
                    case 6:
                        jun++;
                        break;
                    case 7:
                        jul++;
                        break;
                    case 8:
                        aug++;
                        break;
                    case 9:
                        sep++;
                        break;
                    case 10:
                        oct++;
                        break;
                    case 11:
                        nov++;
                        break;
                    case 12:
                        dec++;
                        break;
                    }
                }
            }
        }
        ArrayList<Long> testRunsPerMonth = new ArrayList<>(
                Arrays.asList(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec));
        return testRunsPerMonth;
    }

    /**
     * This method is used to get result of users executed test per month in
     * last year.
     * 
     * @param sessionDetailsList
     * @return ArrayList of user count per month
     */
    public static ArrayList<Long> usersPerMonthofCurrentYear(Map<String, List<SessionDetails>> sessionDetailsList) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
        DateTime currentTime = DateTime.now();
        ArrayList<Integer> monthList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        ArrayList<Long> userPerMonth = new ArrayList<>();

        for (Integer month : monthList) {
            long count = 0;
            long cnt = 0;
            for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
                List<SessionDetails> sessionList = userSessions.getValue();
                count = sessionList.stream()
                        .filter(session -> formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear()
                                && formatter.parseDateTime(session.getDate()).getMonthOfYear() == month)
                        .collect(Collectors.counting());
                if (count > 0) {
                    cnt++;
                }
            }
            userPerMonth.add(cnt);
        }
        return userPerMonth;
    }

    /**
     * This method is used to generate statistics of number of users per test
     * suite in last year.
     * 
     * @param testSuiteName
     *            Test suite name with version
     * @param sessionDetailsList
     *            List of user object with sessions.
     * @return Map of test suite with user count.
     */
    public static Map<String, Object> numberOfUsersPerTestSuite(String testSuiteName,
            Map<String, List<SessionDetails>> sessionDetailsList) {

        Map<String, Object> numberOfUsersPerTestSuite = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd  HH:mm:ss");
        DateTime currentTime = DateTime.now();
        long count = 0;
        long userCount = 0;

        for (Map.Entry<String, List<SessionDetails>> userSessions : sessionDetailsList.entrySet()) {
            List<SessionDetails> sessionList = userSessions.getValue();

            count = sessionList.stream()
                    .filter(session -> session.getEtsName().contains(testSuiteName)
                            && formatter.parseDateTime(session.getDate()).getYear() == currentTime.getYear())
                    .collect(Collectors.counting());

            if (count > 0) {
                userCount++;
            }
        }
        numberOfUsersPerTestSuite.put(testSuiteName, userCount);
        return numberOfUsersPerTestSuite;
    }

    /**
     * Generate overall statistics HTML report by using XSL file and results.
     * 
     * @param testSuiteNames
     * @param year
     * @param statResultDir
     * @param allTestSuiteRunDetails
     * @param testsRunPerMonth
     * @param usersPerMonth
     * @param numberOfUsersAndTestSuite
     */
    public static void generateOverallStatisticsHtmlReport(List<String> testSuiteNames, Integer year, File statResultDir,
            String allTestSuiteRunDetails, String testsRunPerMonth, String usersPerMonth, String numberOfUsersAndTestSuite) {

        FileOutputStream fo;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String xmlTemplate1 = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
            InputStream input = new ByteArrayInputStream(xmlTemplate1.getBytes(StandardCharsets.UTF_8));
            String statXsl = cl.getResource("com/occamlab/te/stats/overall_stats.xsl").toString();
            Transformer transformer = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                    .newTransformer(new StreamSource(statXsl));
            transformer.setParameter("testSuiteNames", testSuiteNames);
            transformer.setParameter("year", year);
            transformer.setParameter("allTestSuiteRunDetails", allTestSuiteRunDetails);
            transformer.setParameter("testsRunPerMonth", testsRunPerMonth);
            transformer.setParameter("usersPerMonth", usersPerMonth);
            transformer.setParameter("numberOfUsersAndTestSuite", numberOfUsersAndTestSuite);
            
            if (!statResultDir.exists()) {
                statResultDir.mkdir();
            }
            File indexHtml = new File(statResultDir, "index.html");

            if (indexHtml.exists()) {
                indexHtml.delete();
            }
            indexHtml.createNewFile();
            fo = new FileOutputStream(indexHtml);
            transformer.transform(new StreamSource(input), new StreamResult(fo));
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Zips the directory and all of it's sub directories
     * 
     * @param zipFile
     *            Path of zip file.
     * @param dirPath
     *            Location of test result
     * @throws Exception
     *             It will throw this exception if file not found.
     */
    public static void zipDir(File zipFile, File dirPath) throws Exception {

        if (!dirPath.isDirectory()) {
            System.err.println(dirPath.getName() + " is not a directory");
            System.exit(1);
        }

        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

            addDir(dirPath, out);
            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Add directory to zip file
     * 
     * @param dirPath
     * @param out
     * @throws IOException
     */
    private static void addDir(File dirPath, ZipOutputStream out) throws IOException {
        File[] files = dirPath.listFiles();
        byte[] tempBuffer = new byte[1024];

        for (File file : files) {
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            out.putNextEntry(new ZipEntry(file.getName()));

            // Transfer from the file to the ZIP file
            int length;
            while ((length = in.read(tempBuffer)) > 0) {
                out.write(tempBuffer, 0, length);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
    }
}