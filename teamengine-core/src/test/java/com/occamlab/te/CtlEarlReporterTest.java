package com.occamlab.te;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.junit.Test;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

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

        assertThat( the( earlReport.toString() ), hasXPath( "/rdf:RDF", nsContext() ) );
        assertThat( the( earlReport.toString() ),
                    hasXPath( "count(//earl:TestRequirement)", equalTo( "4" ), nsContext() ) );
        assertThat( the( earlReport.toString() ),
                    hasXPath( "//earl:TestRequirement[@rdf:about='queryable']/cite:isBasic", equalTo( "false" ), nsContext() ) );
        assertThat( the( earlReport.toString() ),
                    hasXPath( "//earl:TestRequirement[@rdf:about='basic']/cite:isBasic", equalTo( "true" ), nsContext() ) );
    }

    private NamespaceContext nsContext() {
        SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
        nsContext = nsContext.withBinding( "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
        nsContext = nsContext.withBinding( "earl", "http://www.w3.org/ns/earl#" );
        nsContext = nsContext.withBinding( "cite", "http://cite.opengeospatial.org/" );
        return nsContext;
    }

}
