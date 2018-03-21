/**
 * ********************************************************************************
 *
 * Version Date: January 8, 2018
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Modifications to address Fortify issues
 *
 * ********************************************************************************
 */

package com.occamlab.te;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

    public void generateEarlReport( File outputDirectory, File reportFile, String suiteName, Map params )
                            throws UnsupportedEncodingException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware( true );
        docFactory.setXIncludeAware( true );
        Document document;
        try {
            document = docFactory.newDocumentBuilder().parse( reportFile );

        } catch ( IOException | SAXException | ParserConfigurationException e ) {
            throw new RuntimeException( e );
        }
        document.getDocumentElement().normalize();

        Model model = initializeModel( suiteName );
        addTestInputs( model, params );
        this.reqs = model.createSeq();

        NodeList executionList = document.getElementsByTagName( "execution" );

        for ( int temp = 0; temp < executionList.getLength(); temp++ ) {
            Node executionNode = executionList.item( temp );
            Element executionElement = (Element) executionNode;
            NodeList logList = executionElement.getElementsByTagName( "log" );
            Element logElement = (Element) logList.item( 0 );
            NodeList testcallList = logElement.getElementsByTagName( "testcall" );
            getSubtestResult( model, testcallList, logList );
        }

        this.testRun.addProperty( CITE.requirements, this.reqs );
        this.testRun.addLiteral( CITE.testsPassed, new Integer( this.totalPassCount ) );
        this.testRun.addLiteral( CITE.testsFailed, new Integer( this.totalFailCount ) );
        this.testRun.addLiteral( CITE.testsSkipped, new Integer( this.totalSkipCount ) );
        this.testRun.addLiteral( CITE.testsContinue, new Integer( this.totalContinueCount ) );
        this.testRun.addLiteral( CITE.testsBestPractice, new Integer( this.totalBestPracticeCount ) );
        this.testRun.addLiteral( CITE.testsNotTested, new Integer( this.totalNotTestedCount ) );
        this.testRun.addLiteral( CITE.testsWarning, new Integer( this.totalWarningCount ) );
        this.testRun.addLiteral( CITE.testsInheritedFailure, new Integer( this.totalInheritedFailureCount ) );
        this.testRun.addLiteral( CITE.testSuiteType, "ctl" );

        this.earlModel.add( model );

        try {
            writeModel( this.earlModel, outputDirectory, true );
        } catch ( IOException iox ) {
            throw new RuntimeException( "Failed to serialize EARL results to " + outputDirectory.getAbsolutePath(), iox );
        }

    }

    private void getSubtestResult( Model model, NodeList testcallList, NodeList logList )
                            throws UnsupportedEncodingException {
        String conformanceClass = "";
        for ( int k = 0; k < testcallList.getLength(); k++ ) {

            Element testcallElement = (Element) testcallList.item( k );
            String testcallPath = testcallElement.getAttribute( "path" );

            Element logElements = findMatchingLogElement( logList, testcallPath );

            if ( logElements != null ) {
                TestInfo testInfo = getTestinfo( logElements );

                if ( testInfo.isConformanceClass ) {
                    conformanceClass = testInfo.testName;
                    this.cPassCount = 0;
                    this.cFailCount = 0;
                    this.cSkipCount = 0;
                    this.cContinueCount = 0;
                    this.cBestPracticeCount = 0;
                    this.cNotTestedCount = 0;
                    this.cWarningCount = 0;
                    this.cInheritedFailureCount = 0;
                    addTestRequirements( model, testInfo );
                }

                processTestResults( model, logElements, logList, conformanceClass, null );

                Resource testReq = model.createResource( conformanceClass );
                testReq.addLiteral( CITE.testsPassed, new Integer( this.cPassCount ) );
                testReq.addLiteral( CITE.testsFailed, new Integer( this.cFailCount ) );
                testReq.addLiteral( CITE.testsSkipped, new Integer( this.cSkipCount ) );
                testReq.addLiteral( CITE.testsContinue, new Integer( this.cContinueCount ) );
                testReq.addLiteral( CITE.testsBestPractice, new Integer( this.cBestPracticeCount ) );
                testReq.addLiteral( CITE.testsNotTested, new Integer( this.cNotTestedCount ) );
                testReq.addLiteral( CITE.testsWarning, new Integer( this.cWarningCount ) );
                testReq.addLiteral( CITE.testsInheritedFailure, new Integer( this.cInheritedFailureCount ) );
                this.totalPassCount += cPassCount;
                this.totalFailCount += cFailCount;
                this.totalSkipCount += cSkipCount;
                this.totalContinueCount += cContinueCount;
                this.totalBestPracticeCount += cBestPracticeCount;
                this.totalNotTestedCount += cNotTestedCount;
                this.totalWarningCount += cWarningCount;
                this.totalInheritedFailureCount += cInheritedFailureCount;
                break;
            }
        }

    }

    private Element findMatchingLogElement( NodeList logList, String testcallPath )
                            throws UnsupportedEncodingException {
        for ( int j = 0; j < logList.getLength(); j++ ) {
            Element logElement = (Element) logList.item( j );
            String decodedBaseURL = java.net.URLDecoder.decode( logElement.getAttribute( "xml:base" ), "UTF-8" );
            String logtestcall = parseLogTestCall( "", decodedBaseURL );
            // Check sub-testcall is matching with the <log baseURL="">
            if ( testcallPath.equals( logtestcall ) ) {
                return logElement;
            }

        }
        return null;
    }

    private TestInfo getTestinfo( Element logElements ) {
        NodeList starttestLists = logElements.getElementsByTagName( "starttest" );
        Element starttestElements = (Element) starttestLists.item( 0 );
        Element endtestElements = (Element) logElements.getElementsByTagName( "endtest" ).item( 0 );
        NodeList assertionElements = logElements.getElementsByTagName( "assertion" );
        String assertion = "Null";
        if ( assertionElements.getLength() > 0 ) {
            Element assertionElement = (Element) assertionElements.item( 0 );
            assertion = assertionElement.getTextContent();
        }
        String testName = starttestElements.getAttribute( "local-name" );
        int result = Integer.parseInt( endtestElements.getAttribute( "result" ) );
        NodeList isConformanceClassList = logElements.getElementsByTagName( "conformanceClass" );
        boolean isCC = ( isConformanceClassList.getLength() > 0 ) ? true : false;
        boolean isBasic = false;
        Element cClass = (Element) isConformanceClassList.item( 0 );
        if ( null != cClass ) {
            if ( cClass.hasAttribute( "isBasic" ) ) {
                isBasic = true;
            }
        }
        return new TestInfo( assertion, testName, result, isCC, isBasic );
    }

    private Model initializeModel( String suiteName ) {
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put( "earl", EARL.NS_URI );
        nsBindings.put( "dct", DCTerms.NS );
        nsBindings.put( "cite", CITE.NS_URI );
        nsBindings.put( "http", HTTP.NS_URI );
        nsBindings.put( "cnt", CONTENT.NS_URI );
        model.setNsPrefixes( nsBindings );
        this.testRun = model.createResource( CITE.TestRun );
        this.testRun.addProperty( DCTerms.title, suiteName );
        String nowUTC = ZonedDateTime.now( ZoneId.of( "Z" ) ).format( DateTimeFormatter.ISO_INSTANT );
        this.testRun.addProperty( DCTerms.created, nowUTC );
        this.assertor = model.createResource( "https://github.com/opengeospatial/teamengine", EARL.Assertor );
        this.assertor.addProperty( DCTerms.title, "OGC TEAM Engine", this.langCode );
        this.assertor.addProperty( DCTerms.description,
                                   "Official test harness of the OGC conformance testing program (CITE).",
                                   this.langCode );
        /*
         * Map<String, String> params = suite.getXmlSuite().getAllParameters(); String iut = params.get("iut"); if (null
         * == iut) { // non-default parameter refers to test subject--use first URI value for (Map.Entry<String, String>
         * param : params.entrySet()) { try { URI uri = URI.create(param.getValue()); iut = uri.toString(); } catch
         * (IllegalArgumentException e) { continue; } } } if (null == iut) { throw new
         * NullPointerException("Unable to find URI reference for IUT in test run parameters." ); }
         */

        this.testSubject = model.createResource( "", EARL.TestSubject );
        return model;
    }

    private void addTestRequirements( Model earl, TestInfo testInfo ) {
        Resource testReq = earl.createResource( testInfo.testName.replaceAll( "\\s", "-" ), EARL.TestRequirement );
        testReq.addProperty( DCTerms.title, testInfo.testName );
        if ( testInfo.isBasic ) {
            testReq.addProperty( CITE.isBasic, "isBasic" );
        }
        this.reqs.add( testReq );
    }

    /*
     * Process child tests of Conformance Class and call same method recursively if it has the child tests.
     */
    private void processTestResults( Model earl, Element logElement, NodeList logList, String conformanceClass,
                                     Resource parentTestCase )
                            throws UnsupportedEncodingException {
        NodeList childtestcallList = logElement.getElementsByTagName( "testcall" );

        for ( int l = 0; l < childtestcallList.getLength(); l++ ) {
            Element childtestcallElement = (Element) childtestcallList.item( l );
            String testcallPath = childtestcallElement.getAttribute( "path" );

            Element childlogElement = findMatchingLogElement( logList, testcallPath );

            if ( childlogElement == null )
                throw new NullPointerException( "Failed to get Test-Info due to null log element." );
            TestInfo testDetails = getTestinfo( childlogElement );

            // create earl:Assertion
            GregorianCalendar calTime = new GregorianCalendar( TimeZone.getDefault() );
            Resource assertion = earl.createResource( "assert-" + ++this.resultCount, EARL.Assertion );
            assertion.addProperty( EARL.mode, EARL.AutomaticMode );
            assertion.addProperty( EARL.assertedBy, this.assertor );
            assertion.addProperty( EARL.subject, this.testSubject );
            // link earl:TestResult to earl:Assertion
            Resource earlResult = earl.createResource( "result-" + this.resultCount, EARL.TestResult );
            earlResult.addProperty( DCTerms.date, earl.createTypedLiteral( calTime ) );

            handleTestResult( childlogElement, testDetails, earlResult );

            processResultAttributes( earlResult, childlogElement, earl );

            assertion.addProperty( EARL.result, earlResult );
            // link earl:TestCase to earl:Assertion and earl:TestRequirement
            String testName = testDetails.testName;
            StringBuilder testCaseId = new StringBuilder( testcallPath );
            testCaseId.append( '#' ).append( testName );
            Resource testCase = earl.createResource( testCaseId.toString(), EARL.TestCase );
            testCase.addProperty( DCTerms.title, testName );
            testCase.addProperty( DCTerms.description, testDetails.assertion );
            assertion.addProperty( EARL.test, testCase );
            
            if ( parentTestCase != null )
                parentTestCase.addProperty( DCTerms.hasPart, testCase );
            else
                earl.createResource( conformanceClass ).addProperty( DCTerms.hasPart, testCase );
            processTestResults( earl, childlogElement, logList, conformanceClass, testCase );
        }
    }

    private void handleTestResult( Element childlogElement, TestInfo testDetails, Resource earlResult ) {
        switch ( testDetails.result ) {
        case 0:
            earlResult.addProperty( EARL.outcome, CITE.Continue );
            this.cContinueCount++;
            break;
        case 2:
            earlResult.addProperty( EARL.outcome, CITE.Not_Tested );
            this.cNotTestedCount++;
            break;
        case 6: // Fail
            earlResult.addProperty( EARL.outcome, EARL.Fail );
            Element errorMessage = getErrorMessage( childlogElement );
            if ( errorMessage != null ) {
                earlResult.addProperty( DCTerms.description, errorMessage.getTextContent() );
            }
            this.cFailCount++;
            break;
        case 3:
            earlResult.addProperty( EARL.outcome, EARL.NotTested );
            this.cSkipCount++;
            break;
        case 4:
            earlResult.addProperty( EARL.outcome, CITE.Warning );
            this.cWarningCount++;
            break;
        case 5:
            earlResult.addProperty( EARL.outcome, CITE.Inherited_Failure );
            this.cInheritedFailureCount++;
            break;
        default:
            earlResult.addProperty( EARL.outcome, EARL.Pass );
            break;
        }
    }

    private Element getErrorMessage( Element childlogElements ) {
        Element exceptionElement = null;
        NodeList exceptionList = childlogElements.getElementsByTagName( "exception" );
        if ( exceptionList.getLength() > 0 ) {
            exceptionElement = (Element) childlogElements.getElementsByTagName( "exception" ).item( 0 );
        }
        return exceptionElement;
    }

    private void processResultAttributes( Resource earlResult, Element childlogElements, Model earl ) {

        String httpMethod;
        String reqVal;
        Resource httpReq = null;

        if ( null == childlogElements )
            return;
        NodeList requestList = childlogElements.getElementsByTagName( "request" );
        Element reqElement;

        for ( int i = 0; i < requestList.getLength(); i++ ) {
            httpMethod = "";
            reqVal = "";

            reqElement = (Element) requestList.item( i );

            NodeList nl = reqElement.getChildNodes();
            for ( int j = 0; j < nl.getLength(); j++ ) {
                String str = nl.item( j ).getLocalName();
                if ( null != str && str.equalsIgnoreCase( "request" ) ) {
                    Element currentItem = (Element) nl.item( j );
                    NodeList methodList = currentItem.getChildNodes();
                    for ( int k = 0; k < methodList.getLength(); k++ ) {
                        Element method = (Element) methodList.item( k );

                        if ( null != method && method.getLocalName().equalsIgnoreCase( "method" ) ) {
                            httpMethod = method.getTextContent();
                        }

                        if ( null != method && method.getLocalName().equalsIgnoreCase( "url" ) ) {
                            reqVal = method.getTextContent();
                        }
                        // Check request method is GET or POST.
                        httpReq = earl.createResource( HTTP.Request );
                        if ( httpMethod.equalsIgnoreCase( "GET" ) ) {
                            if ( null != method && method.getLocalName().equalsIgnoreCase( "param" ) ) {

                                if ( reqVal.indexOf( "?" ) == -1 ) {
                                    reqVal += "?";
                                } else if ( !reqVal.endsWith( "?" ) && !reqVal.endsWith( "&" ) ) {
                                    reqVal += "&";
                                }

                                reqVal += method.getAttribute( "name" ) + "=" + method.getTextContent();
                            }

                            httpReq.addProperty( HTTP.methodName, httpMethod );
                            httpReq.addProperty( HTTP.requestURI, reqVal );

                        } else if ( httpMethod.equalsIgnoreCase( "POST" ) ) {
                            // Post method content
                            try {
                                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                                transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

                                StreamResult result = new StreamResult( new StringWriter() );
                                DOMSource source = new DOMSource( currentItem );
                                transformer.transform( source, result );

                                String xmlString = result.getWriter().toString();
                                // Fortify Mod: Close the Writer. This flushes the content and frees resources which
                                // could be exhausted by the do-loop.
                                result.getWriter().close();

                                Resource reqContent = earl.createResource( CONTENT.ContentAsXML );

                                reqContent.addProperty( CONTENT.rest, xmlString );
                                httpReq.addProperty( HTTP.body, reqContent );
                            } catch ( Exception e ) {
                                new RuntimeException( "Request content is not well-formatted. " + e.getMessage() );
                            }
                        }
                    }
                }
                if ( httpReq != null && "response".equalsIgnoreCase( str ) ) {
                    Resource httpRsp = earl.createResource( HTTP.Response );
                    // safe assumption, but need more response info to know for
                    // sure
                    Resource rspContent = earl.createResource( CONTENT.ContentAsXML );
                    rspContent.addProperty( CONTENT.rest, nl.item( j ).getTextContent() );
                    httpRsp.addProperty( HTTP.body, rspContent );
                    httpReq.addProperty( HTTP.resp, httpRsp );
                }
            }
        }
        if ( null != httpReq ) {
            earlResult.addProperty( CITE.message, httpReq );
        }
    }

    /**
     * This method is used to add test inputs in to earl report.
     *
     * @param earl
     *            Model object to add the result into it.
     * @param params
     *            The variable is type of Map with all the user input.
     */
    private void addTestInputs( Model earl, Map<String, String> params ) {
        Bag inputs = earl.createBag();
        if ( !params.equals( "" ) && params != null ) {
            String value = "";
            for ( String key : params.keySet() ) {
                value = params.get( key );
                Resource testInputs = earl.createResource();
                testInputs.addProperty( DCTerms.title, key );
                testInputs.addProperty( DCTerms.description, value );
                inputs.add( testInputs );
            }
        }
        this.testRun.addProperty( CITE.inputs, inputs );
    }

    /*
     * Write CTL result into EARL report.
     */
    private void writeModel( Model earlModel2, File outputDirectory, boolean abbreviated )
                            throws IOException {

        File outputFile = new File( outputDirectory, "earl-results.rdf" );
        if ( !outputFile.createNewFile() ) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        String syntax = ( abbreviated ) ? "RDF/XML-ABBREV" : "RDF/XML";
        String baseUri = new StringBuilder( "http://example.org/earl/" ).append( outputDirectory.getName() ).append( '/' ).toString();
        OutputStream outStream = new FileOutputStream( outputFile );
        try (Writer writer = new OutputStreamWriter( outStream, StandardCharsets.UTF_8 )) {
            earlModel2.write( writer, syntax, baseUri );
        }

    }

    private String parseLogTestCall( String logtestcall, String decodedBaseURL ) {
        if ( decodedBaseURL.contains( "users" ) ) {
            String baseUrl = decodedBaseURL.substring( decodedBaseURL.indexOf( "users" ) );
            int first = baseUrl.indexOf( System.getProperty( "file.separator" ) );
            int second = baseUrl.indexOf( System.getProperty( "file.separator" ), first + 1 );
            logtestcall = baseUrl.substring( second + 1, baseUrl.lastIndexOf( System.getProperty( "file.separator" ) ) );
        } else if ( decodedBaseURL.contains( "temp" ) ) {
            String baseUrl = decodedBaseURL.substring( decodedBaseURL.indexOf( "temp" ) );
            logtestcall = baseUrl.substring( baseUrl.indexOf( System.getProperty( "file.separator" ) ) + 1,
                                             baseUrl.lastIndexOf( System.getProperty( "file.separator" ) ) );
        }
        if ( logtestcall.contains( "\\" ) ) {
            logtestcall = logtestcall.replace( "\\", "/" );
        }
        return logtestcall;
    }

    private class TestInfo {
        private String assertion;

        private String testName;

        private int result;

        private boolean isConformanceClass;

        private boolean isBasic;

        public TestInfo( String assertion, String testName, int result, boolean isConformanceClass, boolean isBasic ) {
            this.assertion = assertion;
            this.testName = testName;
            this.result = result;
            this.isConformanceClass = isConformanceClass;
            this.isBasic = isBasic;
        }
    }
}