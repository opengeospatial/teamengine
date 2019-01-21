package com.occamlab.te.form;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ImageHandler {

    private final File testLogDir;

    private final String sessionId;

    /**
     * @param testLogDir
     *            the directory of the log files, never <code>null</code>
     * @param sessionId
     *            the id of the current session, never <code>null</code>
     */
    public ImageHandler( File testLogDir, String sessionId ) {
        this.testLogDir = testLogDir;
        this.sessionId = sessionId;
    }

    /**
     * Save images into session if the ets is interactive and it contains the images.
     *
     * @param form
     *            the form element with the images, never <code>null</code>
     * @throws IOException
     *             if the images could not be read/write
     */
    public void saveImages( Element form )
                            throws IOException {
        List<Element> images = DomUtils.getElementsByTagName( form, "img" );
        int imageCount = 1;
        for ( Element image : images ) {
            if ( image.hasAttribute( "src" ) ) {
                String src = image.getAttribute( "src" );
                if(src.contains("http")){
                String imageFormat = parseImageFormat( src );
                String testName = System.getProperty( "TestName" );
                String imgName = parseImageName( testName );
                URL url = new URL( src );
                BufferedImage img = ImageIO.read( url );
                File file = createImageFile( imageCount, imageFormat, imgName );
                if ( !file.exists() ) {
                    file.getParentFile().mkdirs();
                }
                ImageIO.write( img, imageFormat, file );
                imageCount++;
                }
            }
        }
    }

    private File createImageFile( int imageCount, String imageFormat, String imgName ) {
        String downloadPath = testLogDir + File.separator + sessionId + File.separator + "images" + File.separator
                              + imgName + imageCount + "." + imageFormat;
        return new File( downloadPath );
    }

    private String parseImageFormat( String src )
                            throws MalformedURLException, UnsupportedEncodingException {
        if ( src.contains( "FORMAT" ) ) {
            URL url = new URL( URLDecoder.decode( src, "UTF-8" ) );
            String params[] = url.getQuery().split( "&" );
            for ( String param : params ) {
                if ( param.split( "=" )[0].equalsIgnoreCase( "FORMAT" ) ) {
                    return param.split( "=" )[1].split( "/" )[1];
                }
            }
        }
        return "PNG";
    }

    private String parseImageName( String testName ) {
        if ( testName.contains( " " ) ) {
            return testName.substring( testName.indexOf( ":" ) + 1, testName.indexOf( " " ) );
        }
        return testName;
    }

}
