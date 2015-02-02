/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.occamlab.te.spi.jaxrs.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * A controller resource that provides the results of a test run. An XML
 * representation of the results is obtained using HTTP/1.1 methods in accord
 * with the JAX-RS 1.1 specification (JSR 311).
 *
 * @see <a href="http://jcp.org/en/jsr/detail?id=311">JSR 311</a>
 */
@Path("suiteResult")
@Produces("application/json")
public class TestFinalResult {

  private static final String TE_BASE = "TE_BASE";

  /**
   * Processes a request submitted using the GET method. The test run arguments
   * are specified in the query component of the Request-URI as a sequence of
   * key-value pairs.
   *
   * @param userId
   * @param sessionID
   * @return
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */
  @GET
  public String handleGet(
          @QueryParam("userID") String userId,
          @QueryParam("sessionID") String sessionID) throws FileNotFoundException, IOException {

    String basePath = System.getProperty(TE_BASE);
    if (null == basePath) {
      basePath = System.getenv(TE_BASE);
    }
    String pathAddress = basePath + "/users/" + userId + "/" + sessionID + "/test_data";

    //Get the Final result from file which save after stop button click.
    //Used bufferedReader to get the data from file.
    InputStream inputStreamFilePath = new FileInputStream(new File(pathAddress + "/finalResult.txt"));
    BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStreamFilePath));
    String lineReader;
    String finalResult = "";
    while ((lineReader = fileReader.readLine()) != null) {
      finalResult = finalResult + lineReader;
    }
    return finalResult;
  }
}
