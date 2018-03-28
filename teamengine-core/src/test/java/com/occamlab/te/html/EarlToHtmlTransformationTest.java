package com.occamlab.te.html;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class EarlToHtmlTransformationTest {

    @Test
    public void testEarlHtmlReport_TestNG()
                            throws Exception {
        String outputDirectory = createTempDirectroryAndCopyResources( "earl-results_testng.rdf" );

        EarlToHtmlTransformation earlToHtmlTransformation = new EarlToHtmlTransformation();
        earlToHtmlTransformation.earlHtmlReport( outputDirectory );

        assertThat( new File( outputDirectory, "result/index.html" ).exists(), is( true ) );
    }

    @Test
    public void testEarlHtmlReport_Ctl_WithHierarchy()
                            throws Exception {
        String outputDirectory = createTempDirectroryAndCopyResources( "earl-results_ctl_withHierarchy.rdf" );

        EarlToHtmlTransformation earlToHtmlTransformation = new EarlToHtmlTransformation();
        earlToHtmlTransformation.earlHtmlReport( outputDirectory );

        assertThat( new File( outputDirectory, "result/index.html" ).exists(), is( true ) );
    }

    private String createTempDirectroryAndCopyResources( String earlReport )
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

}
