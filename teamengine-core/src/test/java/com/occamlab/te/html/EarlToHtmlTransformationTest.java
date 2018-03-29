package com.occamlab.te.html;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.hasXPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xmlmatchers.namespace.SimpleNamespaceContext;
import org.xmlmatchers.transform.XmlConverters;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class EarlToHtmlTransformationTest {

    @Test
    public void testEarlHtmlReport_TestNG()
                            throws Exception {
        String outputDirectory = createTempDirectoryAndCopyResources( "earl-results_testng.rdf" );

        EarlToHtmlTransformation earlToHtmlTransformation = new EarlToHtmlTransformation();
        earlToHtmlTransformation.earlHtmlReport( outputDirectory );

        assertThat( new File( outputDirectory, "result/index.html" ).exists(), is( true ) );
    }

    @Test
    public void testEarlHtmlReport_Ctl_WithHierarchy()
                            throws Exception {
        String outputDirectory = createTempDirectoryAndCopyResources( "earl-results_ctl_withHierarchy.rdf" );

        EarlToHtmlTransformation earlToHtmlTransformation = new EarlToHtmlTransformation();
        earlToHtmlTransformation.earlHtmlReport( outputDirectory );

        assertThat( new File( outputDirectory, "result/index.html" ).exists(), is( true ) );
    }

    @Test
    public void testEarlHtmlReport_Ctl_WithHierarchy_MissingElements()
                            throws Exception {
        String outputDirectory = createTempDirectoryAndCopyResources( "earl-results_ctl_withHierarchy_MissingElements.rdf" );

        EarlToHtmlTransformation earlToHtmlTransformation = new EarlToHtmlTransformation();
        earlToHtmlTransformation.earlHtmlReport( outputDirectory );

        File indexHtmlFile = new File( outputDirectory, "result/index.html" );
        assertThat( indexHtmlFile.exists(), is( true ) );

        Source indexHtml = the( indexHtmlFile );
        // 7 rows plus the header row
        assertThat( indexHtml,
                    hasXPath( "count(//html:table[@id='queryable']/html:tbody/html:tr)", equalTo( "8" ), nsContext() ) );

        assertThat( indexHtml, hasXPath( "//html:span[@id='testsPassed_queryable']", equalTo( "7" ), nsContext() ) );
        assertThat( indexHtml, hasXPath( "//html:span[@id='testsFailed_queryable']", equalTo( "0" ), nsContext() ) );
        assertThat( indexHtml, hasXPath( "//html:span[@id='testsSkipped_queryable']", equalTo( "0" ), nsContext() ) );
        assertThat( indexHtml, hasXPath( "//html:span[@id='testsInTotal_queryable']", equalTo( "7" ), nsContext() ) );
    }

    private Source the( File indexHtmlFile )
                            throws Exception {
        OutputStream indexHtml = new ByteArrayOutputStream();
        IOUtils.copy( new FileInputStream( indexHtmlFile ), indexHtml );
        indexHtml.close();
        return XmlConverters.the( indexHtml.toString() );
    }

    private String createTempDirectoryAndCopyResources( String earlReport )
                            throws IOException {
        File outputDirectory = Files.createTempDirectory( "EarlToHtmlTransformationTest" ).toFile();
        if ( outputDirectory.exists() )
            FileUtils.deleteDirectory( outputDirectory );
        outputDirectory.mkdir();

        InputStream rdfResource = EarlToHtmlTransformationTest.class.getResourceAsStream( earlReport );
        File earlResultToTransform = new File( outputDirectory, "earl-results.rdf" );
        FileOutputStream fileOutputStream = new FileOutputStream( earlResultToTransform );
        IOUtils.copy( rdfResource, fileOutputStream );
        fileOutputStream.close();
        return outputDirectory.getAbsolutePath();
    }

    private NamespaceContext nsContext() {
        SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
        nsContext = nsContext.withBinding( "html", "http://www.w3.org/1999/xhtml" );
        return nsContext;
    }
}
