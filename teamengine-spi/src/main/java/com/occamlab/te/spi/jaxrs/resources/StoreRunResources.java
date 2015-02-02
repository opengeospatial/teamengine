package com.occamlab.te.spi.jaxrs.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Stack;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A controller resource that provides the results of a test run. An XML
 * representation of the results is obtained using HTTP/1.1 methods in accord
 * with the JAX-RS 1.1 specification (JSR 311).
 *
 * @see <a href="http://jcp.org/en/jsr/detail?id=311">JSR 311</a>
 */
@Path("suiteJson")
@Produces("application/json")
public class StoreRunResources {

  private static final String TE_BASE = "TE_BASE";

  /**
   * Processes a request submitted using the POST method. The test run arguments
   * are specified in the query component of the Request-URI as a sequence of
   * key-value pairs.
   *
   * @param userId
   * @param sessionID
   * @param testData
   * @return
   * @throws java.io.IOException
   * @throws org.json.JSONException
   */
  @POST
  @Consumes({MediaType.TEXT_PLAIN})
  public String handleGet(
          @QueryParam("userID") String userId,
          @QueryParam("sessionID") String sessionID, String testData) throws IOException, JSONException {
    JSONObject jsonObjTestDetail = new JSONObject();
    JSONArray jsonArrTestDetail = new JSONArray();
    // Get TE_Base Directory path.
    String basePath = System.getProperty(TE_BASE);
    if (null == basePath) {
      basePath = System.getenv(TE_BASE);
    }
    String pathAddress = basePath + "/users/" + userId + "/" + sessionID + "/test_data";
    int testCount = 1;
    int runTestId = 1;
    Stack runTestStack = new Stack();
    Stack traverseTestStack = new Stack();
    Stack outputTestStack = new Stack();
    // Read the test result from file.
    try {
      InputStream inputStreamDirectory = new FileInputStream(new File(pathAddress + "/test_result.txt"));
      BufferedReader dataReader = new BufferedReader(new InputStreamReader(inputStreamDirectory));
      String lineReader;
      // Used Buffered Reader to read file line by line.
      while ((lineReader = dataReader.readLine()) != null) {
        JSONObject subTestLayerJsonObject = new JSONObject(lineReader);
        String jsonResult = (String) subTestLayerJsonObject.get("Result");
        //Check test start or end.
        if ("".equals(jsonResult)) {
          runTestStack.push(testCount + " " + subTestLayerJsonObject.get("Name"));
          testCount++;
        } else {
          while (runTestStack.peek().toString().contains("Passed") || runTestStack.peek().toString().contains("Failed")) {
            traverseTestStack.push(runTestStack.peek());
            runTestStack.pop();
          }
          testCount--;
          runTestStack.pop();
          runTestStack.push(testCount + "  Name :" + subTestLayerJsonObject.get("Name") + "  Result :" + subTestLayerJsonObject.get("Result") + "  Time :" + subTestLayerJsonObject.get("Time"));
          while (!traverseTestStack.isEmpty()) {
            runTestStack.push(traverseTestStack.peek());
            traverseTestStack.pop();
          }
        }
      }
      int testCounter = 1;
      int testParentID = 1;
      int testSpacing = 1;
      try {
        //Check Stack is empty or not.
        if ((null != runTestStack) && (!runTestStack.isEmpty())) {
          //Traverse stack and get the data from stack
          for (int index = 0; index < runTestStack.size(); index++) {
            //Convert stack data into Object.
            Object object = runTestStack.get(index);
            testSpacing = Integer.parseInt(object.toString().split(" ")[0]);

            if (testCounter < testSpacing) {
              outputTestStack.push(runTestId);
              testCounter = testSpacing;
            }
            if (testCounter > testSpacing) {
              while (testCounter - testSpacing > 0) {
                if (!outputTestStack.isEmpty()) {
                  outputTestStack.pop();
                }
                testCounter--;
              }
            }
            if (testSpacing == 1) {
              testParentID = 0;
            }
            if (!outputTestStack.isEmpty()) {
              testParentID = ((Integer) outputTestStack.peek()) - 1;
            }
            //Created json array for test which manage test fail and pass including their parents result.
            JSONObject objectEachTest = new JSONObject();
            objectEachTest.put("Indent", testSpacing);
            objectEachTest.put("Name", object.toString().split(" ")[3].substring(1));
            objectEachTest.put("Result", object.toString().split(" ")[6].substring(1));
            objectEachTest.put("Time", object.toString().split(" ")[9].substring(1));
            objectEachTest.put("ObjectID", runTestId);
            objectEachTest.put("ParentID", testParentID);
            jsonArrTestDetail.put(objectEachTest);
            runTestId++;
          }
        }
      } catch (JSONException e) {
        JSONObject obj = new JSONObject();
        obj.put("Result", e.toString());
      }
      if (null != jsonArrTestDetail && jsonArrTestDetail.length() > 0) {
        for (int index = 1; index < jsonArrTestDetail.length(); index++) {
          JSONObject data = jsonArrTestDetail.getJSONObject(index);
          if (data.getString("Result").equals("Failed")) {
            StoreRunResources test = new StoreRunResources();
            test.update(data.getInt("ParentID"), jsonArrTestDetail);
          }
        }
      }
      //Use DocumentBuilderFactory for creating a file according to test.
      if (null != jsonArrTestDetail && jsonArrTestDetail.length() > 0) {
        for (int index = 0; index < jsonArrTestDetail.length(); index++) {
          JSONObject objec = jsonArrTestDetail.getJSONObject(index);
          if ("1".equals(objec.getString("Indent"))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document resultDocument = builder.newDocument();
            Element execution = resultDocument.createElement("execution");
            resultDocument.appendChild(execution);
            Source inputSource = new DOMSource(resultDocument);

            JSONObject testName = new JSONObject(testData);
            JSONArray testDetail = testName.getJSONArray("TestName");

            if (null != testDetail && testDetail.length() > 0) {
              for (int i = 0; i < testDetail.length(); i++) {

                JSONObject test = testDetail.getJSONObject(i);
                String fileName;
                if (objec.getString("Name").equals(test.getString("Name"))) {
                  if ("Passed".equals(objec.getString("Result"))) {
                    fileName = test.getString("File").split(".xml")[0] + "Pass.xml";
                  } else {
                    fileName = test.getString("File").split(".xml")[0] + "Fail.xml";
                  }
                  fileCreate(new File(pathAddress, fileName), inputSource);
                }
              }
            }
          }
        }
      }
      jsonObjTestDetail.put("TEST", jsonArrTestDetail);
      return jsonObjTestDetail.toString();
    } catch (Exception e) {
      JSONObject obj = new JSONObject();
      obj.put("File", "File Not Present");
      return obj.toString();
    }
  }

  /**
   * This method is used to create a file.
   *
   * @param file
   * @param input
   * @throws javax.xml.transform.TransformerConfigurationException
   * @throws java.io.FileNotFoundException
   */
  public void fileCreate(File file, Source input) throws TransformerConfigurationException, TransformerException, FileNotFoundException {
    TransformerFactory xformFactory = TransformerFactory.newInstance();
    Transformer idTransform = xformFactory.newTransformer();
    OutputStream report_logs = new FileOutputStream(file);
    Result output = new StreamResult(report_logs);
    idTransform.transform(input, output);
  }

  /**
   * This method is used to maintain parent Child relationship for test.
   *
   * @param parentID
   * @param jsonArr
   * @throws JSONException
   */
  public void update(int parentID, JSONArray jsonArr) throws JSONException {
    int parentId = parentID;
    while (jsonArr.getJSONObject(parentId - 1).getInt("ParentID") != 0) {
      jsonArr.getJSONObject(parentId - 1).put("Result", "Failed");
      parentId = jsonArr.getJSONObject(parentId - 1).getInt("ParentID");

    }
    if (jsonArr.getJSONObject(parentId - 1).getInt("ParentID") == 0) {
      jsonArr.getJSONObject(parentId - 1).put("Result", "Failed");
    }
  }

}
