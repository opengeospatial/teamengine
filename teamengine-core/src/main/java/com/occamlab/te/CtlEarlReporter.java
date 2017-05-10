package com.occamlab.te;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.occamlab.te.spi.vocabulary.CITE;
import com.occamlab.te.spi.vocabulary.CONTENT;
import com.occamlab.te.spi.vocabulary.EARL;
import com.occamlab.te.spi.vocabulary.HTTP;

public class CtlEarlReporter {

    private String langCode = "en";
    private Resource testRun;
    private int resultCount = 0;
    private Resource assertor;
    private Resource testSubject;
    private Model earlModel;
    private Seq reqs;
    private int cPassCount;
    private int cFailCount;
    private int cSkipCount;
    private int cContinueCount;
    private int cBestPracticeCount;
    private int cNotTestedCount;
    private int cWarningCount;
    private int cInheritedFailureCount;
    private int totalPassCount;
    private int totalFailCount;
    private int totalSkipCount;
    private int totalContinueCount;
    private int totalBestPracticeCount;
    private int totalNotTestedCount;
    private int totalWarningCount;
    private int totalInheritedFailureCount;

    public CtlEarlReporter() {
        this.earlModel = ModelFactory.createDefaultModel();
        totalPassCount = 0;
        totalFailCount = 0;
        totalSkipCount = 0;
        totalContinueCount = 0;
        totalBestPracticeCount = 0;
        totalNotTestedCount = 0;
        totalWarningCount = 0;
        totalInheritedFailureCount = 0;
    }

    public void generateEarlReport(File outputDirectory, File reportFile, String suiteName, String iut) throws UnsupportedEncodingException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        docFactory.setXIncludeAware(true);
        // Source results = null;
        Document document;
        try {
            document = docFactory.newDocumentBuilder().parse(reportFile);

        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        document.getDocumentElement().normalize();

        // Here comes the root node
        Element root = document.getDocumentElement();
        Model model = initializeModel(suiteName, iut);
        addTestInputs(model, iut);
        this.reqs = model.createSeq();

        NodeList executionList = document.getElementsByTagName("execution");

        for (int temp = 0; temp < executionList.getLength(); temp++) {
            Node executionNode = executionList.item(temp);

            Element executionElement = (Element) executionNode;

            NodeList logList = executionElement.getElementsByTagName("log");

            Element logElement = (Element) logList.item(0);

            NodeList starttestList = logElement.getElementsByTagName("starttest");

            Element starttestElement = (Element) starttestList.item(0);

            /*
             * Get list of the <testcall> element.
             */

            NodeList testcallList = logElement.getElementsByTagName("testcall");
            /*
             * Get the subtest result recursively.
             */
            getSubtestResult(model, testcallList, logList, starttestElement.getAttribute("local-name"));

        }

        this.testRun.addProperty(CITE.requirements, this.reqs);
        this.testRun.addLiteral(CITE.testsPassed, new Integer(this.totalPassCount));
        this.testRun.addLiteral(CITE.testsFailed, new Integer(this.totalFailCount));
        this.testRun.addLiteral(CITE.testsSkipped, new Integer(this.totalSkipCount));
        this.testRun.addLiteral(CITE.testsContinue, new Integer(this.totalContinueCount));
        this.testRun.addLiteral(CITE.testsBestPractice, new Integer(this.totalBestPracticeCount));
        this.testRun.addLiteral(CITE.testsNotTested, new Integer(this.totalNotTestedCount));
        this.testRun.addLiteral(CITE.testsWarning, new Integer(this.totalWarningCount));
        this.testRun.addLiteral(CITE.testsInheritedFailure, new Integer(this.totalInheritedFailureCount));
        
        this.earlModel.add(model);

        try {
            writeModel(this.earlModel, outputDirectory, true);
        } catch (IOException iox) {
            throw new RuntimeException("Failed to serialize EARL results to " + outputDirectory.getAbsolutePath(), iox);
        }

    }

    public void getSubtestResult(Model model, NodeList testcallList, NodeList logList, String fTestname)
            throws UnsupportedEncodingException {

        String conformanceClass = "";
        for (int k = 0; k < testcallList.getLength(); k++) {

            // Get current testcall element path attribute
            String testcallPath = "";
            Element testcallElement = (Element) testcallList.item(k);
            testcallPath = testcallElement.getAttribute("path");

            // Iterate the log element list.

            for (int j = 0; j < logList.getLength(); j++) {

                Element logElements = (Element) logList.item(j);
                String decodedBaseURL = "";

                decodedBaseURL = java.net.URLDecoder.decode(logElements.getAttribute("xml:base"), "UTF-8");
                String baseUrl = decodedBaseURL.substring(decodedBaseURL.indexOf("users"));
        		int first = baseUrl.indexOf(System.getProperty("file.separator"));
        		int second = baseUrl.indexOf(System.getProperty("file.separator"), first + 1);
        		String logtestcall = baseUrl.substring(second + 1, baseUrl.lastIndexOf(System.getProperty("file.separator")));

        		if(logtestcall.contains("\\")){
        			logtestcall = logtestcall.replace("\\", "/");
        		}

                // Check sub-testcall is matching with the <log baseURL="">

                if (testcallPath.equals(logtestcall)) {

                    Map<String, String> testinfo = getTestinfo(logElements);

                    if (testinfo.get("isConformanceClass").equals("true")) {
                        conformanceClass = testinfo.get("testName");
                        this.cPassCount = 0;
                        this.cFailCount = 0;
                        this.cSkipCount = 0;
                        this.cContinueCount = 0;
                        this.cBestPracticeCount = 0;
                        this.cNotTestedCount = 0;
                        this.cWarningCount = 0;
                        this.cInheritedFailureCount = 0;
                        addTestRequirements(model, testinfo);
                    }

                    /*
                     * Process Test Result
                     */
                    processTestResults(model, logElements, logList, logtestcall, conformanceClass);

                    Resource testReq = model.createResource(conformanceClass);
                    testReq.addLiteral(CITE.testsPassed, new Integer(this.cPassCount));
                    testReq.addLiteral(CITE.testsFailed, new Integer(this.cFailCount));
                    testReq.addLiteral(CITE.testsSkipped, new Integer(this.cSkipCount));
                    testReq.addLiteral(CITE.testsContinue, new Integer(this.cContinueCount));
                    testReq.addLiteral(CITE.testsBestPractice, new Integer(this.cBestPracticeCount));
                    testReq.addLiteral(CITE.testsNotTested, new Integer(this.cNotTestedCount));
                    testReq.addLiteral(CITE.testsWarning, new Integer(this.cWarningCount));
                    testReq.addLiteral(CITE.testsInheritedFailure, new Integer(this.cInheritedFailureCount));
                    this.totalPassCount += cPassCount;
                    this.totalFailCount += cFailCount;
                    this.totalSkipCount += cSkipCount;
                    this.totalContinueCount += cContinueCount;
                    this.totalBestPracticeCount += cBestPracticeCount;
                    this.totalNotTestedCount += cNotTestedCount;
                    this.totalWarningCount += cWarningCount;
                    this.totalInheritedFailureCount += cInheritedFailureCount;
                    break;
                } // end of sub-testcall
            }

        }

    }

    public Map<String, String> getTestinfo(Element logElements) {

        Map<String, String> attr = new HashMap<String, String>();

        NodeList starttestLists = logElements.getElementsByTagName("starttest");

        Element starttestElements = (Element) starttestLists.item(0);

        Element endtestElements = (Element) logElements.getElementsByTagName("endtest").item(0);
        NodeList assertionElements = logElements.getElementsByTagName("assertion");
        if(assertionElements.getLength() > 0){
        	Element assertionElement =  (Element) assertionElements.item(0);
        	attr.put("assertion", assertionElement.getTextContent());
        } else attr.put("assertion","Null");
        attr.put("testName", starttestElements.getAttribute("local-name"));
        attr.put("result", endtestElements.getAttribute("result"));
        NodeList isConformanceClassList = logElements.getElementsByTagName("conformanceClass");
        String isCC = (isConformanceClassList.getLength() > 0) ? "true" : "false";
        attr.put("isConformanceClass", isCC);
        Element cClass = (Element) isConformanceClassList.item(0);
		if (null != cClass) {
			if (cClass.hasAttribute("isBasic")) {
				attr.put("isBasic", cClass.getAttribute("isBasic"));
			}
		}
		
        return attr;
    }

    Model initializeModel(String suiteName, String iut) {
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put("earl", EARL.NS_URI);
        nsBindings.put("dct", DCTerms.NS);
        nsBindings.put("cite", CITE.NS_URI);
        nsBindings.put("http", HTTP.NS_URI);
        nsBindings.put("cnt", CONTENT.NS_URI);
        model.setNsPrefixes(nsBindings);
        this.testRun = model.createResource(CITE.TestRun);
        this.testRun.addProperty(DCTerms.title, suiteName);
        String nowUTC = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ISO_INSTANT);
        this.testRun.addProperty(DCTerms.created, nowUTC);
        this.assertor = model.createResource("https://github.com/opengeospatial/teamengine", EARL.Assertor);
        this.assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
        this.assertor.addProperty(DCTerms.description,
                "Official test harness of the OGC conformance testing program (CITE).", this.langCode);
        /*
         * Map<String, String> params = suite.getXmlSuite().getAllParameters();
         * String iut = params.get("iut"); if (null == iut) { // non-default
         * parameter refers to test subject--use first URI value for
         * (Map.Entry<String, String> param : params.entrySet()) { try { URI uri
         * = URI.create(param.getValue()); iut = uri.toString(); } catch
         * (IllegalArgumentException e) { continue; } } } if (null == iut) {
         * throw new
         * NullPointerException("Unable to find URI reference for IUT in test run parameters."
         * ); }
         */
       
        this.testSubject = model.createResource(iut, EARL.TestSubject);
        return model;
    }

    /*
     * Add TestRequirements
     */
    void addTestRequirements(Model earl, Map<String, String> testinfo) {
        Resource testReq = earl.createResource(testinfo.get("testName").replaceAll("\\s", "-"), EARL.TestRequirement);
        testReq.addProperty(DCTerms.title, testinfo.get("testName"));
        if(null != testinfo.get("isBasic")){
    		testReq.addProperty(CITE.isBasic, testinfo.get("isBasic"));
    	}
        this.reqs.add(testReq);
    }

    /*
     * Process child tests of Conformance Class and call same method recursively
     * if it has the child tests.
     * 
     */
    public void processTestResults(Model earl, Element logElements, NodeList logList, String logtestcallPath,
            String conformanceClass) throws UnsupportedEncodingException {

        NodeList childtestcallList = logElements.getElementsByTagName("testcall");
        String testcallPath;
        Element childlogElements = null;
        Map<String, String> testDetails;
        String childLogtestcall = "";

        for (int l = 0; l < childtestcallList.getLength(); l++) {

            Element childtestcallElement = (Element) childtestcallList.item(l);
            testcallPath = childtestcallElement.getAttribute("path");

            for (int m = 0; m < logList.getLength(); m++) {

                childlogElements = (Element) logList.item(m);
                String decodedBaseURL = java.net.URLDecoder.decode(childlogElements.getAttribute("xml:base"), "UTF-8");

                String baseUrl = decodedBaseURL.substring(decodedBaseURL.indexOf("users"));
        		int first = baseUrl.indexOf(System.getProperty("file.separator"));
        		int second = baseUrl.indexOf(System.getProperty("file.separator"), first + 1);
        		childLogtestcall = baseUrl.substring(second + 1, baseUrl.lastIndexOf(System.getProperty("file.separator")));

        		if(childLogtestcall.contains("\\")){
        			childLogtestcall = childLogtestcall.replace("\\", "/");
        		}
                if (testcallPath.equals(childLogtestcall)) {
                    break;
                }
            }
            if (!childlogElements.equals(null)) {
                testDetails = getTestinfo(childlogElements);
            } else {
                throw new NullPointerException("Failed to get Test-Info due to null log element.");
            }

            // create earl:Assertion
            GregorianCalendar calTime = new GregorianCalendar(TimeZone.getDefault());
            Resource assertion = earl.createResource("assert-" + ++this.resultCount, EARL.Assertion);
            assertion.addProperty(EARL.mode, EARL.AutomaticMode);
            assertion.addProperty(EARL.assertedBy, this.assertor);
            assertion.addProperty(EARL.subject, this.testSubject);
            // link earl:TestResult to earl:Assertion
            Resource earlResult = earl.createResource("result-" + this.resultCount, EARL.TestResult);
            earlResult.addProperty(DCTerms.date, earl.createTypedLiteral(calTime));
            int res = Integer.parseInt(testDetails.get("result"));

            switch (res) {
            case 0:
                earlResult.addProperty(EARL.outcome, CITE.Continue);
                this.cContinueCount++;
                break;
            case 2:
                earlResult.addProperty(EARL.outcome, CITE.Not_Tested);
                this.cNotTestedCount++;
                break;
            case 6: // Fail
                earlResult.addProperty(EARL.outcome, EARL.Fail);
				Element errorMessage = getErrorMessage(childlogElements);
				if(errorMessage != null){
					earlResult.addProperty(DCTerms.description,errorMessage.getTextContent());
				}
                this.cFailCount++;
                break;
            case 3:
                earlResult.addProperty(EARL.outcome, EARL.NotTested);
                this.cSkipCount++;
                break;
            case 4:
                earlResult.addProperty(EARL.outcome, CITE.Warning);
                this.cWarningCount++;
                break;
            case 5:
                earlResult.addProperty(EARL.outcome, CITE.Inherited_Failure);
                this.cInheritedFailureCount++;
                break;
            default:
                earlResult.addProperty(EARL.outcome, EARL.Pass);
                break;
            }
            
            processResultAttributes(earlResult, childlogElements, earl);
            
            assertion.addProperty(EARL.result, earlResult);
            // link earl:TestCase to earl:Assertion and earl:TestRequirement
            String testName = testDetails.get("testName");
            StringBuilder testCaseId = new StringBuilder(childLogtestcall);
            testCaseId.append('#').append(testName);
            Resource testCase = earl.createResource(testCaseId.toString(), EARL.TestCase);
            testCase.addProperty(DCTerms.title, testName);
            testCase.addProperty(DCTerms.description, testDetails.get("assertion"));
            assertion.addProperty(EARL.test, testCase);
            earl.createResource(conformanceClass).addProperty(DCTerms.hasPart, testCase);
            NodeList testcallLists = childtestcallElement.getElementsByTagName("testcall");

            if (testcallLists.getLength() > 0) {
                processTestResults(earl, childtestcallElement, logList, childLogtestcall, conformanceClass);
            }
        }
    }

    private Element getErrorMessage(Element childlogElements) {
    	Element exceptionElement = null;
    	NodeList exceptionList = childlogElements.getElementsByTagName("exception");
		if (exceptionList.getLength() > 0) {
			exceptionElement = (Element) childlogElements.getElementsByTagName("exception").item(0);
		}
		return exceptionElement;
	}
    
    void processResultAttributes(Resource earlResult, Element childlogElements, Model earl) {
    	
		String httpMethod;
		String reqVal;
		Resource httpReq = null;

		if (null == childlogElements)
			return;
		NodeList requestList = childlogElements.getElementsByTagName("request");
		Element reqElement;

		for (int i = 0; i < requestList.getLength(); i++) {
			httpMethod = "";
			reqVal = "";
			String xmlString = "";

			reqElement = (Element) requestList.item(i);

			NodeList nl = reqElement.getChildNodes();
			for (int j = 0; j < nl.getLength(); j++) {
				String str = nl.item(j).getLocalName();
				if (null != str && str.equalsIgnoreCase("request")) {
					Element currentItem = (Element) nl.item(j);
					NodeList methodList = currentItem.getChildNodes();
					for (int k = 0; k < methodList.getLength(); k++) {
						Element method = (Element) methodList.item(k);

						if (null != method
								&& method.getLocalName().equalsIgnoreCase(
										"method")) {
							httpMethod = method.getTextContent();
						}

						if (null != method
								&& method.getLocalName()
										.equalsIgnoreCase("url")) {
							reqVal = method.getTextContent();
						}
						// Check request method is GET or POST.
						if (httpMethod.equalsIgnoreCase("GET")) {
							if (null != method
									&& method.getLocalName().equalsIgnoreCase(
											"param")) {

								if (reqVal.indexOf("?") == -1) {
									reqVal += "?";
								} else if (!reqVal.endsWith("?")
										&& !reqVal.endsWith("&")) {
									reqVal += "&";
								}

								reqVal += method.getAttribute("name") + "="
										+ method.getTextContent();
							}

							httpReq = earl.createResource(HTTP.Request);
							httpReq.addProperty(HTTP.methodName, httpMethod);
							httpReq.addProperty(HTTP.requestURI, reqVal);

						} else if (httpMethod.equalsIgnoreCase("POST")) {
							// Post method content
							try {
								Transformer transformer = TransformerFactory
										.newInstance().newTransformer();
								transformer.setOutputProperty(
										OutputKeys.INDENT, "yes");

								StreamResult result = new StreamResult(
										new StringWriter());
								DOMSource source = new DOMSource(currentItem);
								transformer.transform(source, result);

								xmlString = result.getWriter().toString();

								Resource reqContent = earl
										.createResource(CONTENT.ContentAsXML);

								reqContent.addProperty(CONTENT.rest, xmlString);
								httpReq.addProperty(HTTP.body, reqContent);
							} catch (Exception e) {
								new RuntimeException(
										"Request content is not well-formatted. "
												+ e.getMessage());
							}
						}
					}
				}
				if (null != str && str.equalsIgnoreCase("response")) {
					Resource httpRsp = earl.createResource(HTTP.Response);
					// safe assumption, but need more response info to know for
					// sure
					Resource rspContent = earl
							.createResource(CONTENT.ContentAsXML);
					rspContent.addProperty(CONTENT.rest, nl.item(j)
							.getTextContent());
					httpRsp.addProperty(HTTP.body, rspContent);
					httpReq.addProperty(HTTP.resp, httpRsp);
				}
			}
		}
		if (null != httpReq) {
			earlResult.addProperty(CITE.message, httpReq);
		}
    }
    
   private void addTestInputs(Model earl, String params) {
	   Bag inputs = earl.createBag();
	    DocumentBuilder docBuild;
	    Document inputDoc = null;
	    if(!params.equals("") && params != null){
		try {
			docBuild = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
	    InputSource inputsrc = new InputSource();
	    inputsrc.setCharacterStream(new StringReader(params));

	    inputDoc = docBuild.parse(inputsrc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		NodeList valuesList = inputDoc.getElementsByTagName("values");
		Element valuesElement = (Element) valuesList.item(0);
		NodeList valueList = valuesElement.getElementsByTagName("value");
		String value = "";
		String key = "";
		
		for (int i = 0; i < valueList.getLength(); i++) {
		      Element valueElement = (Element) valueList.item(i);  
		      key = valueElement.getAttribute("key");
		      NodeList fileList = valueElement.getElementsByTagName("file-entry");
   		  
		      System.out.println("Element Name:  " + valueElement.getNodeName());
		      if(fileList.getLength() > 0){
		    	  Element fileElement = (Element) fileList.item(0);
		    		  System.out.println("Key: " + valueElement.getAttribute("key"));
		    		  value = fileElement.getAttribute("full-path");
		      } else value = valueElement.getTextContent();
		      
		      System.out.println("Key: " + valueElement.getAttribute("key")+ " ==> " + "Value: " + value);
		      Resource testInputs = earl.createResource();
		      testInputs.addProperty(DCTerms.title, key);
		      testInputs.addProperty(DCTerms.description, value);
               inputs.add(testInputs);
		    }
	    }
        this.testRun.addProperty(CITE.inputs, inputs);
    }
    
    /*
     * Write CTL result into EARL report.
     */
    private void writeModel(Model earlModel2, File outputDirectory, boolean abbreviated) throws IOException {

        File outputFile = new File(outputDirectory, "earl-results.rdf");
        if (!outputFile.createNewFile()) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        String syntax = (abbreviated) ? "RDF/XML-ABBREV" : "RDF/XML";
        String baseUri = new StringBuilder("http://example.org/earl/").append(outputDirectory.getName()).append('/')
                .toString();
        OutputStream outStream = new FileOutputStream(outputFile);
        try (Writer writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8)) {
            earlModel2.write(writer, syntax, baseUri);
        }

    }

}
