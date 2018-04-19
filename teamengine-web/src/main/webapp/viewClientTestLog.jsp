<%@page import="org.json.simple.JSONArray"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="java.io.FileReader"%>
<%@page import="org.json.simple.parser.JSONParser"%>
<%@ page
  language="java"
  session="false"
  import="javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, javax.xml.transform.stream.*, java.io.File, java.util.*, com.occamlab.te.*, com.occamlab.te.util.Misc, com.occamlab.te.web.*, net.sf.saxon.dom.DocumentBuilderImpl, net.sf.saxon.FeatureKeys,javax.xml.parsers.DocumentBuilderFactory,javax.xml.parsers.DocumentBuilder,org.w3c.dom.*, net.sf.saxon.Configuration"
  %>
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
  <!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  
    The Original Code is TEAM Engine.
  
    The Initial Developer of the Original Code is Northrop Grumman Corporation
    jointly with The National Technology Alliance.  Portions created by
    Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
    Grumman Corporation. All Rights Reserved.
  
    Contributor(s): 
        C. Heazel (WiSC): Added Fortify adjudication changes
  
   +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <title>Test Log</title>
    </head>
    <body>
      <%@ include file="header.jsp" %>
      <%!
        Config Conf;
        String sessionId;

        public void jspInit() {
          try {
            Conf = new Config();
          } catch (Exception e) {
            e.printStackTrace(System.out);
          }
        }
      %>
      <%!
        int mainLoop = 0;
        int subLoop = 0;
        int failedCount = 0;
        int failGetCapability = 0;
        int failGetFeatureInfo = 0;
        String strin = "";
        ArrayList<String> strArray = new ArrayList<String>();
        ArrayList<String> strTest = new ArrayList<String>();
        JSONParser parser = new JSONParser();

        /**
         * Manage WMS-Client root node view detail result.
         */
        public String rootManager(JSONArray responseJson) {
          int countTestCheck = 0;
          int counter = 1;
          //mainLoop traverse the json array till all main test name.
          for (mainLoop = 1; mainLoop <= 17; mainLoop++) {
            JSONObject object = (JSONObject) responseJson.get(mainLoop);
            //subLoop traverse the json array for all the result which comes during test run.
            if (!responseJson.isEmpty()) {
              for (subLoop = 18; subLoop < responseJson.size(); subLoop++) {
                JSONObject child = (JSONObject) responseJson.get(subLoop);
                //check the mainLoop for it is gapMap layer or not.
                if (mainLoop != 2) {
                  //check test name id with their corresponding result id
                  if (object.get("id") == child.get("ParentID")) {
                    countTestCheck = 1;
                    //check if certain test failed.
                    if (child.get("Name").toString().contains("Failed")) {
                      failedCount = 1;
                      if (object.get("Name").toString().contains("GetCapabilities")) {
                        if (failGetCapability == 0) {
                          strTest.add((counter++) + ". At least one request of " + object.get("node_name").toString() + " request failed");
                          failGetCapability++;
                        }
                      }
                      if (object.get("Name").toString().contains("GetFeatureInfo")) {
                        if (failGetFeatureInfo == 0) {
                          strTest.add((counter++) + ". At least one request of " + object.get("node_name").toString() + " request failed");
                          failGetFeatureInfo++;
                        }
                      }
                    }
                  }
                } else {
                  countTestCheck = 1;
                }
              }
            }
            if (countTestCheck == 0) {
              if (object.get("Name").toString().contains("GetFeatureInfo") || object.get("Name").toString().contains("GetCapabilities")) {
                strin = strin + (counter++) + ". You have not exercised at least one " + object.get("node_name").toString() + " request.<br/>";
              } else {
                if (!object.get("Name").toString().contains("GetMap")) {
                  strArray.add(object.get("Name").toString());
                }
              }
            }
            countTestCheck = 0;
          }
          String respondString = "";
          if ((failedCount != 1) && ("".equals(strin)) && (strArray.isEmpty())) {
            respondString = "<b>Result: </b>All the GetCapabilities, GetMap and GetFeatureInfo requests were run run at least once and all the requests Passed.";
          } else {
            respondString = "<b>Reason For Failure: </b><br/><br/>";
            if (failedCount == 1) {
              for (String strTest1 : strTest) {
                respondString = respondString + strTest1 + "<br/>";
              }
            }
            if (!("".equals(strin))) {
              respondString = respondString + strin;
            }
            if (strArray.size() > 0) {
              respondString = respondString + (counter++) + ". You have not exercised the GetMap request to get the layer <br/>";
              for (int indexCount = 0; indexCount < strArray.size(); indexCount++) {
                respondString = respondString + "  <li style='margin-left: 20px;'>" + strArray.get(indexCount) + "</li>";
              }
            }
          }
          failGetCapability = 0;
          failGetFeatureInfo = 0;
          strin = "";
          failedCount = 0;
          strArray.clear();
          strTest.clear();
          return respondString;
        }

        /**
         * Manage getMap node view detail result for their all respective layer.
         */
        public String mapLayerManager(JSONArray responseJson) {
          int count = 0;
          for (mainLoop = 3; mainLoop <= 16; mainLoop++) {
            JSONObject object = (JSONObject) responseJson.get(mainLoop);
            if (!responseJson.isEmpty()) {
              for (subLoop = 18; subLoop < responseJson.size(); subLoop++) {
                JSONObject child = (JSONObject) responseJson.get(subLoop);
                if (object.get("id") == child.get("ParentID")) {
                  count = 1;
                  if (child.get("Name").toString().contains("Failed")) {
                    failedCount = 1;
                  }
                }
              }
            }
            if (count == 0) {
              strArray.add(object.get("Name").toString());
            }
            count = 0;
          }
          String respondString = "";
          if ((failedCount != 1) && (strArray.isEmpty())) {
            respondString = "<b>Result: </b>All the GetMap requests were run at least once and all the requests Passed.";
          } else {
            respondString = "<b>Reason For Failure: </b>";
            if (strArray.size() > 0) {
              respondString = respondString + "You have not exercised the GetMap request to get the layer <br/>";
              for (String strArray1 : strArray) {
                respondString = respondString + "<li style='margin-left: 20px;'>" + strArray1 + "</li>";
              }
            }
          }
          failedCount = 0;
          strArray.clear();
          return respondString;
        }

        /**
         * Manage subTest view detail result.
         */
        public String subLayerManager(JSONArray responseJson, String testNo) {
          int countTestCheck = 0;
          int testNumber = Integer.parseInt(testNo);
          JSONObject object = (JSONObject) responseJson.get(testNumber);
          if (!responseJson.isEmpty()) {
            for (subLoop = 18; subLoop < responseJson.size(); subLoop++) {
              JSONObject child = (JSONObject) responseJson.get(subLoop);
              if (object.get("id") == child.get("ParentID")) {
                countTestCheck = 1;
                if (child.get("Name").toString().contains("Failed")) {
                  failedCount = 1;
                  if (object.get("Name").toString().contains("GetCapabilities")) {
                    if (failGetCapability == 0) {
                      strTest.add("At least one request of " + object.get("node_name").toString() + " request failed");
                      failGetCapability++;
                    }
                  }
                  if (object.get("Name").toString().contains("GetFeatureInfo")) {
                    if (failGetFeatureInfo == 0) {
                      strTest.add("At least one request of " + object.get("node_name").toString() + " request failed");
                      failGetFeatureInfo++;
                    }
                  }
                }
              }
            }
          }
          if (countTestCheck == 0) {
            if (object.get("Name").toString().contains("GetFeatureInfo") || object.get("Name").toString().contains("GetCapabilities")) {
              strin = strin + "You have not exercised at least one " + object.get("node_name").toString() + " request.<br/>";
            } else {
              strArray.add(object.get("Name").toString());
            }
          }
          countTestCheck = 0;
          String respondString = "";
          if ((failedCount != 1) && ("".equals(strin)) && (strArray.isEmpty())) {
            respondString = "<b>Result: </b>All the requests were run at least once and all the requests Passed.";
          } else {
            respondString = "<b>Reason For Failure: </b>";
            if (failedCount == 1) {
              for (String strTest1 : strTest) {
                respondString = respondString + strTest1 + "<br/>";
              }
            }
            if (!("".equals(strin))) {
              respondString = respondString + strin + "<br/>";
            }
            if (strArray.size() > 0) {
              respondString = respondString + "You have not exercised the GetMap request to get the layer ";
              for (String strArray1 : strArray) {
                respondString = respondString + strArray1 + "<br/>";
              }
            }
          }
          failGetCapability = 0;
          failGetFeatureInfo = 0;
          strin = "";
          failedCount = 0;
          strArray.clear();
          strTest.clear();
          return respondString;
        }

      %>
      <h2>Log for test <%=request.getParameter("test")%></h2>
      <%
        try {
          File userlog = new File(Conf.getUsersDir(), request.getRemoteUser());
          String test = request.getParameter("test");
          int index = test.indexOf("/");
          String Clause;
          String Purpose;
          String TESTNAME;
          sessionId = (index > 0) ? test.substring(0, index) : test;
          File fXmlFileClause = new File(userlog + "/" + sessionId + "/test_data/result_clause.xml");
          DocumentBuilderFactory dbFactoryClause = DocumentBuilderFactory.newInstance();
          // Fortify Mod: prevent external entity injection
          dbFactoryClause.setExpandEntityReferences(false);
          DocumentBuilder dBuilderClause = dbFactoryClause.newDocumentBuilder();
          Document docClause = dBuilderClause.parse(fXmlFileClause);
          docClause.getDocumentElement().normalize();
          NodeList nListClause = docClause.getElementsByTagName("Request");
          // Fortify Mod: Close the FileReader once we are done with it.
          // Object obj = parser.parse(new FileReader(userlog + "/" + sessionId + "/test_data/finalResult.txt"));
          FileReader fr = new FileReader(userlog + "/" + sessionId + "/test_data/finalResult.txt");
          Object obj = parser.parse(fr);
          fr.close();
          JSONObject jsonObject = (JSONObject) obj;
          JSONArray resultMsg = (JSONArray) jsonObject.get("Result");
          String reqNo = request.getParameter("reqNo");
          String testNo = request.getParameter("testNo");
          String result = request.getParameter("result");
          if (testNo != null) {
            if ("0".equals(testNo)) {
              String output = rootManager(resultMsg);
              out.println(output);
            } else if ("2".equals(testNo)) {
              for (int temp = 0; temp < nListClause.getLength(); temp++) {
                Node nNode = nListClause.item(temp);
                Element eElement = (Element) nNode;
                if ("2".equals(eElement.getAttribute("no"))) {
                  Clause = eElement.getElementsByTagName("Clause").item(0).getTextContent();
                  Purpose = eElement.getElementsByTagName("Purpose").item(0).getTextContent();
                  TESTNAME=eElement.getElementsByTagName("TESTNAME").item(0).getTextContent();
                  if (!("".equals(Clause) && "".equals(Purpose))) {
                         %><div><b> Test:  </b><%out.println(TESTNAME);%><br/><%
                  %><div><b> Clause: </b><%         out.println(Clause);%><br/><%
                   %><div><b> Test Purpose: </b><%        out.println(Purpose);%><br/><%

                        }
                      }
                    }
                    String output = mapLayerManager(resultMsg);
                    out.println(output);
                    %></div><%
                  } else {
                    if ("1".equals(testNo) || "17".equals(testNo)) {
                      for (int temp = 0; temp < nListClause.getLength(); temp++) {
                        Node nNode = nListClause.item(temp);
                        Element eElement = (Element) nNode;
                        if ("1".equals(testNo)) {
                          if ("1".equals(eElement.getAttribute("no"))) {
                            Clause = eElement.getElementsByTagName("Clause").item(0).getTextContent();
                            Purpose = eElement.getElementsByTagName("Purpose").item(0).getTextContent();
                            TESTNAME=eElement.getElementsByTagName("TESTNAME").item(0).getTextContent();
                            if (!("".equals(Clause) && "".equals(Purpose))) {
%><div><b> Test:  </b><%out.println(TESTNAME);%><br/><%
                      %><div><b> Clause: </b><%  out.println(Clause);%><br/><%
              %><div><b> Test Purpose: </b><%            out.println(Purpose);%><br/><%

                      }
                    }
                          %></div><%
                  } else {
                    if ("3".equals(eElement.getAttribute("no"))) {
                      Clause = eElement.getElementsByTagName("Clause").item(0).getTextContent();
                      Purpose = eElement.getElementsByTagName("Purpose").item(0).getTextContent();
                      TESTNAME=eElement.getElementsByTagName("TESTNAME").item(0).getTextContent();
                      if (!("".equals(Clause) && "".equals(Purpose))) {
                        %><div><b> Test: </b><%out.println(TESTNAME);%><br/><%
                               %><div><b> Clause: </b><% out.println(Clause);%><br/><%
                               %><div><b> Test Purpose: </b><% out.println(Purpose);%><br/><%

                              }
                            }
                    %></div><%
                          }
                        }
                      }
                      String output = subLayerManager(resultMsg, testNo);
                      out.println(output);
                    }
                  } else {
                    String assertionNode = "";
                    String mszNode = "";
                    String urlNode = "";
                    File fXmlFile = new File(userlog + "/" + sessionId + "/test_data/result_log.xml");
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    // Fortify Mod: prevent external entity injection
                    dbFactory.setExpandEntityReferences(false);
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(fXmlFile);
                    doc.getDocumentElement().normalize();
      %><div><b>Note : </b>The URLs shown are not resolvable, since the proxy endpoint "<%out.print(doc.getElementsByTagName("ctl:url").item(0).getTextContent().split("monitor/")[0] + "monitor/..");%>" is not available anymore after stopping the test.<br/><br/></div><%
        NodeList nList = doc.getElementsByTagName("Request");
        for (int temp = 0; temp < nList.getLength(); temp++) {
          String str = "";
          Node nNode = nList.item(temp);
          Element eElement = (Element) nNode;
          if (reqNo.equals(eElement.getAttribute("no"))) {
            assertionNode = eElement.getElementsByTagName("Assertion").item(0).getTextContent();
            if (!("".equals(assertionNode))) {
      %><div style="word-break: break-all;"><b>Assertion: </b><%out.println(eElement.getElementsByTagName("Assertion").item(0).getTextContent());
        %><br/><%
            assertionNode = "";
          }
          urlNode = eElement.getElementsByTagName("URL").item(0).getTextContent();
          if (!("".equals(urlNode))) {
        %><b>URL: </b><%
          NodeList url = eElement.getElementsByTagName("URL");
          for (int j = 0; j < url.getLength(); j++) {
            Node node = url.item(j);

            Element element = (Element) node;
            str = element.getElementsByTagName("ctl:url").item(0).getTextContent() + "?";
            NodeList param = eElement.getElementsByTagName("ctl:param");
            for (int k = 0; k < param.getLength(); k++) {
              Node paramName = param.item(k);
              Element eleParam = (Element) paramName;
              str = str + eleParam.getAttribute("name") + "=" + param.item(k).getTextContent() + "&";
            }
          }
          out.println(str.substring(0, str.length() - 1));
        %><br/><%
            urlNode = "";
          }
          mszNode = eElement.getElementsByTagName("Message").item(0).getTextContent();
          if ((!("".equals(mszNode))) && (!(mszNode.startsWith("cite:"))) && (!(result.startsWith("P")))) {
        %><b>Reason For Failure: </b><%out.println(eElement.getElementsByTagName("Message").item(0).getTextContent());
        %><br/><%
            mszNode = "";
          }
        %><br/></div><%
                }

              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        %>
      <br/>
      <a href="viewSessionLog.jsp?session=<%=sessionId%>">Complete session results</a>
      <br/>
      <a href="viewSessions.jsp"/>Sessions list</a>
      <%@ include file="footer.jsp" %>
    </body>
  </html>
