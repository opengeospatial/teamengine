package com.occamlab.te.form;

import com.occamlab.te.util.DomUtils;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ImageHandler {

    private final File testLogDir;

    private final String sessionId;

    public ImageHandler(File testLogDir, String sessionId) {
        this.testLogDir = testLogDir;
        this.sessionId = sessionId;
    }

    public void saveImages(Element form) throws IOException {
        // Save images into session if the ets is interactive and it contains the
        // images.
        List<Element> images = DomUtils.getElementsByTagName(form, "img");
        if (images.size() > 0) {
            int imageCount = 1;
            for (Element image : images) {
                if (image.hasAttribute("src")) {
                    String src = image.getAttribute("src");
                    String imageFormat = null;
                    String imgName = null;
                    if (src.contains("FORMAT")) {
                        URL url = new URL(URLDecoder.decode(src, "UTF-8"));
                        String params[] = url.getQuery().split("&");
                        for (String param : params) {
                            if (param.split("=")[0].equalsIgnoreCase("FORMAT")) {
                                imageFormat = param.split("=")[1].split("/")[1];
                            }
                        }
                        if (null == imageFormat) {
                            imageFormat = "PNG";
                        }
                    }
                    String testName = System.getProperty("TestName");
                    if (testName.contains(" ")) {
                        imgName = testName.substring(testName.indexOf(":") + 1,
                                testName.indexOf(" "));
                    } else {
                        imgName = testName;
                    }
                    String downloadPath = testLogDir + File.separator
                            + sessionId + File.separator + "images" + File.separator
                            + imgName + imageCount + "." + imageFormat;
                    URL url = new URL(src);
                    BufferedImage img = ImageIO.read(url);
                    File file = new File(downloadPath);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }
                    ImageIO.write(img, imageFormat, file);
                    imageCount++;
                }
            }
        }
    }

}
