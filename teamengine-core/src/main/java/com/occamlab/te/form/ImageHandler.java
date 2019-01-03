package com.occamlab.te.form;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        if ( images.size() > 0 ) {
            int imageCount = 1;
            for ( Element image : images ) {
                if ( image.hasAttribute( "src" ) ) {
                    String src = image.getAttribute( "src" );
                    String imageFormat = null;
                    String imgName = null;
                    if ( src.contains( "FORMAT" ) ) {
                        URL url = new URL( URLDecoder.decode( src, "UTF-8" ) );
                        String params[] = url.getQuery().split( "&" );
                        for ( String param : params ) {
                            if ( param.split( "=" )[0].equalsIgnoreCase( "FORMAT" ) ) {
                                imageFormat = param.split( "=" )[1].split( "/" )[1];
                            }
                        }
                        if ( null == imageFormat ) {
                            imageFormat = "PNG";
                        }
                    }
                    String testName = System.getProperty( "TestName" );
                    if ( testName.contains( " " ) ) {
                        imgName = testName.substring( testName.indexOf( ":" ) + 1, testName.indexOf( " " ) );
                    } else {
                        imgName = testName;
                    }
                    String downloadPath = testLogDir + File.separator + sessionId + File.separator + "images"
                                          + File.separator + imgName + imageCount + "." + imageFormat;
                    URL url = new URL( src );
                    BufferedImage img = ImageIO.read( url );
                    File file = new File( downloadPath );
                    if ( !file.exists() ) {
                        file.getParentFile().mkdirs();
                    }
                    ImageIO.write( img, imageFormat, file );
                    imageCount++;
                }
            }
        }
    }

}
