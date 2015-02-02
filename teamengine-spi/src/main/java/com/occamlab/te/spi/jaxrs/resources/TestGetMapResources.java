package com.occamlab.te.spi.jaxrs.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Stack;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A controller resource that provides the results of a test run. An XML
 * representation of the results is obtained using HTTP/1.1 methods in accord
 * with the JAX-RS 1.1 specification (JSR 311).
 *
 * @see <a href="http://jcp.org/en/jsr/detail?id=311">JSR 311</a>
 */
@Path("suiteMap")
@Produces("application/json")
public class TestGetMapResources {

  private static final String TE_BASE = "TE_BASE";

  /**
   * Processes a request submitted using the GET method. The test run arguments
   * are specified in the query component of the Request-URI as a sequence of
   * key-value pairs.
   *
   * @param userId
   * @param sessionID
   * @return
   * @throws java.io.IOException
   * @throws org.json.JSONException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  @GET
  public String handleGet(
          @QueryParam("userID") String userId,
          @QueryParam("sessionID") String sessionID) throws IOException, JSONException, ParserConfigurationException, SAXException {

    String basePath = System.getProperty(TE_BASE);
    if (null == basePath) {
      basePath = System.getenv(TE_BASE);
    }
    String pathAddress = basePath + "/users/" + userId + "/" + sessionID + "/test_data";

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//Get-Map_layer.xml file contain all layer names which are run by Get-Map method.
    Document mapLayerDocument = docBuilder.parse(new File(pathAddress + "/Get-Map-Layer.xml"));

//Get Node list from xml file.
    NodeList listOfPersons = mapLayerDocument.getElementsByTagName("value");

//Get the name of each layer from nodelist and inserting into stack. 
    Stack mapLayerTestDetail = new Stack();
    if (null != listOfPersons && listOfPersons.getLength() > 0) {
      for (int index = 0; index < listOfPersons.getLength(); index++) {
        mapLayerTestDetail.push((listOfPersons.item(index).getFirstChild().toString().split(" ")[1]).split("]")[0]);
      }
    }

//Put all layer name into jsonArray.
    JSONObject jsonObj = new JSONObject();
    JSONArray jsonArr = new JSONArray();
    while (!mapLayerTestDetail.isEmpty()) {
      JSONObject mapLayerTestObject = new JSONObject();
      mapLayerTestObject.put("Name", mapLayerTestDetail.peek());
      mapLayerTestDetail.pop();
      jsonArr.put(mapLayerTestObject);
    }
    jsonObj.put("TEST", jsonArr);
    return jsonObj.toString();
  }

  /**
   * Processes a request submitted using the POST method. The test run arguments
   * are specified in the query component of the Request-URI as a sequence of
   * key-value pairs.
   *
   * @param userId
   * @param sessionID
   * @param data
   * @throws java.io.IOException
   * @throws javax.xml.transform.TransformerException
   * @throws javax.xml.transform.TransformerConfigurationException
   * @throws java.io.FileNotFoundException
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  @POST
  @Consumes({MediaType.TEXT_PLAIN})
  public void handlepost(@QueryParam("userID") String userId,
          @QueryParam("sessionID") String sessionID, String data) throws ParserConfigurationException, TransformerException, TransformerConfigurationException, FileNotFoundException, IOException {
    String basePath = System.getProperty(TE_BASE);
    if (null == basePath) {
      basePath = System.getenv(TE_BASE);
    }
    
// Save data into file which comes through Rest end point.
    String pathAddress = basePath + "/users/" + userId + "/" + sessionID + "/test_data";
    File fulePath = new File(pathAddress, "/finalResult.txt");
    OutputStreamWriter writerBefore = new OutputStreamWriter(
            new FileOutputStream(fulePath, true), "UTF-8");
    try (BufferedWriter fbwBefore = new BufferedWriter(writerBefore)) {
      fbwBefore.write(data);
      fbwBefore.newLine();
    }

  }

}
