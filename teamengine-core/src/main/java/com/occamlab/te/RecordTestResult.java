package com.occamlab.te;

import static com.occamlab.te.TECore.getResultDescription;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.util.Constants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RecordTestResult {
  private static final String UT_F8 = "UTF-8";
  private static final String A_TRUE = "True";
  private static final String RECORD = "Record";
  private static final String TIME = "Time";
  private static final String RESULT = "Result";
  private static final String NAME = "Name";
  private static final String NO = "no";
  private static final String XML = "xml";
  private static final String YES = "yes";
  private static final String ERROR_ON__RECORDING_AT_STARTING_ = "Error on Recording at starting :";
  private static final String ERROR_ON_SAVE_THE__RECORDING__ = "Error on save the Recording : ";
  private static final String ERROR_LOGLOGTXT = "/error_log/log.txt";
  private static final String SEE_THE_DETAIL_ERROR__REPORT_ = "\t\t\tSee the detail error Report ";
  private static final String SEE_THE_DETAIL_TEST__REPORT_ = "\t\t\tSee the detail test Report ";
  private static final String LOGXML = "/log.xml";
  private static final String TESTNG = "/testng";
  private static final String PATH = "PATH";
  
  /**
   * This method helps to display output and error stack details.
   */
  public void detailTestPath() {
    // Get test run file
    File report_path = new File(System.getProperty(PATH) + TESTNG);
    // Check if the path exists
    if (!report_path.exists()) {
      System.out.println(SEE_THE_DETAIL_TEST__REPORT_ + System.getProperty(PATH) + LOGXML);
      // Get error log file
      File path = new File(System.getProperty(PATH) + ERROR_LOGLOGTXT);
      // If file exists then display error log file path
      if (path.exists()) {
        System.out.println(SEE_THE_DETAIL_ERROR__REPORT_ + System.getProperty(PATH) + ERROR_LOGLOGTXT);
      }
    }
  }
  
  /**
   * Check Test recording ON or OFF if ON then create a builder for storing the
   * log in single file.
   * @param suite
   * @throws Exception
   */
  public void recordingStartCheck(SuiteEntry suite) throws Exception {
    // Check suite variable is not empty and get suite local name.
    if (null != suite && SetupOptions.recordingInfo(suite.getLocalName()) == true) {
      //Create a document builder for storing the data.
      TECore.icFactory = DocumentBuilderFactory.newInstance();
      try {
        TECore.icBuilder = TECore.icFactory.newDocumentBuilder();
        // Create a document for storing the xml data
        TECore.doc = TECore.icBuilder.newDocument();
        TECore.mainRootElement = TECore.doc.createElement(Constants.Requests);
        //Append the data in previous saved data.
        TECore.doc.appendChild(TECore.mainRootElement);
      } catch (Exception e) {
        System.out.println(ERROR_ON__RECORDING_AT_STARTING_ + e.toString());
      }
    }
  }

  public void recordingStartClause(SuiteEntry suite) throws Exception{
    if (null != suite && SetupOptions.recordingInfo(suite.getLocalName()) == true) {
      //Create a document builder for storing the data.
      TECore.icFactoryClause = DocumentBuilderFactory.newInstance();
      try {
        TECore.icBuilderClause = TECore.icFactoryClause.newDocumentBuilder();
        // Create a document for storing the xml data
        TECore.docClause = TECore.icBuilder.newDocument();
        TECore.mainRootElementClause = TECore.docClause.createElement(Constants.Requests);
        //Append the data in previous saved data.
        TECore.docClause.appendChild(TECore.mainRootElementClause);
      } catch (Exception e) {
        System.out.println(ERROR_ON__RECORDING_AT_STARTING_ + e.toString());
      }
    }
  }
  /**
   * Save all recording into file.
   *
   * @param suite
   * @param dirPath
   * @throws Exception
   */

  public void saveRecordingData(SuiteEntry suite, File dirPath) throws Exception {
    if (null != suite && SetupOptions.recordingInfo(suite.getLocalName()) == true) {
      try {
        //Create a Source for saving the data.
        DOMSource source = new DOMSource(TECore.doc);
        TransformerFactory xformFactory = TransformerFactory.newInstance();
        Transformer idTransform = xformFactory.newTransformer();
        // Declare document is XML
        idTransform.setOutputProperty(OutputKeys.METHOD, XML);
        // Declare document standard UTF-8
        idTransform.setOutputProperty(OutputKeys.ENCODING, UT_F8);
        // Declare document is well indented
        idTransform.setOutputProperty(OutputKeys.INDENT, YES);
        OutputStream report_logs = new FileOutputStream(new File(dirPath + Constants.tmp_File));
        Result output = new StreamResult(report_logs);
        //transform the output in xml.
        idTransform.transform(source, output);
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        // Read the xml data from file
        bufferedReader = new BufferedReader(new FileReader(dirPath + Constants.tmp_File));
        // Create a xml file for saving the data.
        bufferedWriter = new BufferedWriter(new FileWriter(dirPath + Constants.result_logxml));
        String dataString = "";
        //Read the data from file.
        while ((dataString = bufferedReader.readLine()) != null) {
          // Replace special symbol code to its symbol
          dataString = dataString.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
          bufferedWriter.write(dataString);
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
        TECore.methodCount=0;
        TECore.rootTestName.clear();
        // Check file exists
        File file = new File(dirPath + Constants.tmp_File);
        if (file.exists()) {
          // Delete file if exists
          file.delete();
        }
      } catch (Exception e) {
        System.out.println(ERROR_ON_SAVE_THE__RECORDING__ + e.toString());
      }
    }
  }
  
  public void saveRecordingClause(SuiteEntry suite, File dirPath) throws Exception {
    if (null != suite && SetupOptions.recordingInfo(suite.getLocalName()) == true) {
      try {
        //Create a Source for saving the data.
        DOMSource source = new DOMSource(TECore.docClause);
        TransformerFactory xformFactory = TransformerFactory.newInstance();
        Transformer idTransform = xformFactory.newTransformer();
        // Declare document is XML
        idTransform.setOutputProperty(OutputKeys.METHOD, XML);
        // Declare document standard UTF-8
        idTransform.setOutputProperty(OutputKeys.ENCODING, UT_F8);
        // Declare document is well indented
        idTransform.setOutputProperty(OutputKeys.INDENT, YES);
        OutputStream report_logs = new FileOutputStream(new File(dirPath + Constants.tmp_File));
        Result output = new StreamResult(report_logs);
        //transform the output in xml.
        idTransform.transform(source, output);
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        // Read the xml data from file
        bufferedReader = new BufferedReader(new FileReader(dirPath + Constants.tmp_File));
        // Create a xml file for saving the data.
        bufferedWriter = new BufferedWriter(new FileWriter(dirPath + Constants.result_clausexml));
        String dataString = "";
        //Read the data from file.
        while ((dataString = bufferedReader.readLine()) != null) {
          // Replace special symbol code to its symbol
          dataString = dataString.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
          bufferedWriter.write(dataString);
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
        // Check file exists
        File file = new File(dirPath + Constants.tmp_File);
        if (file.exists()) {
          // Delete file if exists
          file.delete();
        }
      } catch (Exception e) {
        System.out.println(ERROR_ON_SAVE_THE__RECORDING__ + e.toString());
      }
    }
  }
  
  /**
   * Convert the data into Node format
   */
  public static Node getMethod() {
    Element testRequest = TECore.doc.createElement(Constants.Request);
    testRequest.setAttribute(NO, String.valueOf(TECore.methodCount));
    // append child into testRequest
    testRequest.appendChild(getMethodElements(TECore.doc, testRequest, Constants.Assertion, TECore.assertionMsz));
    testRequest.appendChild(getMethodElements(TECore.doc, testRequest, Constants.URL, TECore.pathURL));
    testRequest.appendChild(getMethodElements(TECore.doc, testRequest, Constants.Message, TECore.messageTest));
    return testRequest;
  }
  
  public static Node getClause() {
    Element testRequest = TECore.docClause.createElement(Constants.Request);
    testRequest.setAttribute(NO, String.valueOf(TECore.rootNo));
    // append child into testRequest
    testRequest.appendChild(getMethodElements(TECore.docClause, testRequest, Constants.TESTNAME, TECore.TESTNAME));
    testRequest.appendChild(getMethodElements(TECore.docClause, testRequest, Constants.Clause, TECore.Clause));
    testRequest.appendChild(getMethodElements(TECore.docClause, testRequest, Constants.Purpose, TECore.Purpose));
    return testRequest;
  }

  /**
   * Convert the data in form of key-value pair and return in form of Node.
   * @param doc
   * @param element
   * @param name
   * @param value
   * @return 
   */
  
  public static Node getMethodElements(Document doc, Element element, String name, String value) {
    Element node = doc.createElement(name);
    node.appendChild(doc.createTextNode(value));
    return node;
  }

  /**
   * Store start test detail into file.
   * @param test
   * @param dirPath
   * @throws java.lang.Exception
   */
  public void storeStartTestDetail(TestEntry test, File dirPath) throws Exception {
    // Check recording enable
    if (A_TRUE.equals(System.getProperty(RECORD))) {
      //Check test No
      if (TECore.testCount != 0) {
        JSONObject objBeforeTest = new JSONObject();
        objBeforeTest.put(NAME, test.getName());
        objBeforeTest.put(RESULT, "");
        // write the data into file in form of json
        OutputStreamWriter writerBefore = new OutputStreamWriter(
                new FileOutputStream(dirPath + Constants.TEST_RESULTTXT, true), UT_F8);
        try (BufferedWriter fbwBefore = new BufferedWriter(writerBefore)) {
          fbwBefore.write(objBeforeTest.toString());
          fbwBefore.newLine();
          fbwBefore.close();
        }
      } else {
        //update test no
        TECore.testCount = TECore.testCount + 1;
        // update test name
        TECore.nameOfTest = test.getName();
      }
    }
  }
  
  /**
   * Save test detail into file.
   * @param test
   * @param verdict
   * @param dirPath
   * @param cal
   * @param dateFormat
   * @throws java.lang.Exception
   */
  public void storeFinalTestDetail(TestEntry test, int verdict, DateFormat dateFormat, Calendar cal, File dirPath) throws Exception {
    // Check recording is enable
    if (A_TRUE.equals(System.getProperty(RECORD))) {
      // match test name
      if (!TECore.nameOfTest.equals(test.getName())) {
        JSONObject obj = new JSONObject();
        obj.put(NAME, test.getName());
        obj.put(RESULT, getResultDescription(verdict));
        obj.put(TIME, dateFormat.format(cal.getTime()));
        // write the data into file in form of json
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(dirPath + Constants.TEST_RESULTTXT, true), UT_F8);
        try (BufferedWriter fbw = new BufferedWriter(writer)) {
          fbw.write(obj.toString());
          fbw.newLine();
        }
      } else {
        // update test no
        TECore.testCount = 0;
      }
    }
  }
}
