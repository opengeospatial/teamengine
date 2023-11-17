package com.occamlab.te;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CtlEarlReporterTest {

    @Test
    public void testGenerateEarlReport()
                            throws Exception {
        CtlEarlReporter ctlEarlReporter = new CtlEarlReporter();

        ByteArrayOutputStream earlReport = new ByteArrayOutputStream();
        InputStream reportStream = CtlEarlReporterTest.class.getResourceAsStream( "unittest/session/report_logs.xml" );

        String testSuiteName = "OGC_Web Map Service (WMS)_1.3.0_1.23-SNAPSHOT";

        Map<String, String> parames = new HashMap<>();
        parames.put( "queryable", "queryable" );
        parames.put( "low-updatesequence", "" );
        parames.put( "capabilities-url",
                     "http://cite.deegree.org/deegree-webservices-3.4-RC3/services/wms130?request=GetCapabilities&service=WMS" );
        parames.put( "high-updatesequence", "" );
        parames.put( "updatesequence", "auto" );
        parames.put( "basic", "basic" );

        ctlEarlReporter.generateEarlReport( earlReport, reportStream, testSuiteName, parames );

        assertThat(earlReport.toString(), hasXPath("/rdf:RDF").withNamespaceContext(nsContext()));
        assertThat(earlReport.toString(),
                hasXPath("count(//earl:TestRequirement)", equalTo("4")).withNamespaceContext(nsContext()));
        assertThat(earlReport.toString(),
                hasXPath("//earl:TestRequirement[@rdf:about='queryable']/cite:isBasic", equalTo("false")).withNamespaceContext(nsContext()));
        assertThat(earlReport.toString(),
                hasXPath("//earl:TestRequirement[@rdf:about='basic']/cite:isBasic", equalTo("true")).withNamespaceContext(nsContext()));
    }

    private Map<String, String> nsContext() {
        Map<String, String> nsContext = new HashMap<>();
        nsContext.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsContext.put("earl", "http://www.w3.org/ns/earl#");
        nsContext.put("cite", "http://cite.opengeospatial.org/");
        return nsContext;
    }

}
