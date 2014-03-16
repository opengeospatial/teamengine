/****************************************************************************

 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): 
 		Paul Daisey (Image Matters LLC)
 		2011-05-13 add getImageWidth(), getImageHeight()
 		2011-08-23 add try/catch block to processFrame()to return image format
		2011-08-24 add case to processFrame() to return image transparency for parsers:transparency tag
		2011-08-24 add checkTransparentNodata(); call from processBufferedImage() for parsers:model/parsers:transparentNodata tag
		2011-09-08 add getBase64Data(), parseBase64(), formatName param to processBufferedImage()
		
 ****************************************************************************/
package com.occamlab.te.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
// 2011-11-15 PwD uncomment for Java 1.7 import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Transparency; // 2011-08-24 PwD

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extracts the body of a response message and treats it as an image resource.
 * If the entity does not correspond to a supported image format, an exception
 * is reported in the test log.
 * 
 */
public class ImageParser {
    private static Logger jlogger = Logger
            .getLogger("com.occamlab.te.parsers.ImageParser");

    private static class ImageTracker implements ImageObserver {
        volatile boolean done = false;

        public synchronized boolean imageUpdate(Image img, int infoflags,
                int x, int y, int width, int height) {
            if ((infoflags & (ABORT | ERROR)) != 0) {
                done = true;
            } else {
                done = ((infoflags & (ALLBITS | FRAMEBITS)) != 0);
            }
            return !done;
        }
    }

    private Element initialInstruction = null;

    public ImageParser(Document init) {
        initialInstruction = init.getDocumentElement();
    }

    // Same as Thread.sleep, except it ignores exceptions
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // not an error
        }
    }

    private static String HexString(int num, int digits) {
        String s = Integer.toHexString(num);
        if (digits > s.length()) {
            return "00000000".substring(0, digits - s.length()) + s;
        } else {
            return s;
        }
    }

    /*
     * 2011-09-08 PwD
     * 
     * @return image data as a base64 encoded string
     */
    private static String getBase64Data(BufferedImage buffImage,
            String formatName, Node node) throws Exception {
        int numBytes = buffImage.getWidth() * buffImage.getHeight() * 4;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(numBytes);
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        boolean status = ImageIO.write(buffImage, formatName, ios);
        if (!status) {
            throw new Exception(
                    "No suitable ImageIO writer found by ImageParser.getBase64Data() for image format "
                            + formatName);
        }
        byte[] imageData = baos.toByteArray();
        // String base64String = Base64.encodeBase64String(imageData); this is
        // unchunked (no line breaks) which is unwieldy
        byte[] base64Data = Base64.encodeBase64Chunked(imageData);
        // 2011-11-15 PwD uncomment for Java 1.7 String base64String = new
        // String(base64Data, StandardCharsets.UTF_8);
        String base64String = new String(base64Data, "UTF-8"); // 2011-11-15 PwD
                                                               // for Java 1.6;
                                                               // remove for
                                                               // Java 1.7
        return base64String;
    }

    /*
     * 2011-08-24 PwD check that all pixels in image with no data are
     * transparent
     * 
     * @return NA if all pixels contain data
     * 
     * @return true if all pixels with no data are transparent (alpha channel
     * value 0)
     * 
     * @return false if any pixel with no data is non-transparent
     */
    private static String checkTransparentNodata(BufferedImage buffImage,
            Node node) throws Exception {
        String transparentNodata = "NA";
        boolean noData = false;
        boolean transparent = true;
        int[] bandIndexes = new int[4];
        bandIndexes[0] = 3; // A
        bandIndexes[1] = 2; // B
        bandIndexes[2] = 1; // G
        bandIndexes[3] = 0; // R
        Raster raster = buffImage.getRaster();
        int minx = raster.getMinX();
        int maxx = minx + raster.getWidth();
        int miny = raster.getMinY();
        int maxy = miny + raster.getHeight();
        int bands[][] = new int[bandIndexes.length][raster.getWidth()];
        for (int y = miny; y < maxy; y++) {
            for (int i = 0; i < bandIndexes.length; i++) {
                raster.getSamples(minx, y, maxx, 1, bandIndexes[i], bands[i]);
            }
            for (int x = minx; x < maxx; x++) {
                int a = bands[0][x];
                int b = bands[1][x];
                int g = bands[2][x];
                int r = bands[3][x];
                if (b == 0 && g == 0 && r == 0) {
                    noData = true;
                    if (a != 0) {
                        transparent = false;
                    }
                }
            }
        }
        transparentNodata = (noData) ? (transparent) ? "true" : "false" : "NA";
        return transparentNodata;
    }

    /*
     * Process buffered image obtained from Reader 2011-09-08 PwD added
     * formatName param to support getBase64Data()
     */
    private static void processBufferedImage(BufferedImage buffimage,
            String formatName, NodeList nodes) throws Exception {
        HashMap<Object, Object> bandMap = new HashMap<Object, Object>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getLocalName().equals("subimage")) {
                    Element e = (Element) node;
                    int x = Integer.parseInt(e.getAttribute("x"));
                    int y = Integer.parseInt(e.getAttribute("y"));
                    int w = Integer.parseInt(e.getAttribute("width"));
                    int h = Integer.parseInt(e.getAttribute("height"));
                    processBufferedImage(buffimage.getSubimage(x, y, w, h),
                            formatName, e.getChildNodes());
                } else if (node.getLocalName().equals("checksum")) {
                    CRC32 checksum = new CRC32();
                    Raster raster = buffimage.getRaster();
                    DataBufferByte buffer;
                    if (node.getParentNode().getLocalName().equals("subimage")) {
                        WritableRaster outRaster = raster
                                .createCompatibleWritableRaster();
                        buffimage.copyData(outRaster);
                        buffer = (DataBufferByte) outRaster.getDataBuffer();
                    } else {
                        buffer = (DataBufferByte) raster.getDataBuffer();
                    }
                    int numbanks = buffer.getNumBanks();
                    for (int j = 0; j < numbanks; j++) {
                        checksum.update(buffer.getData(j));
                    }
                    Document doc = node.getOwnerDocument();
                    node.appendChild(doc.createTextNode(Long.toString(checksum
                            .getValue())));
                } else if (node.getLocalName().equals("count")) {
                    String band = ((Element) node).getAttribute("bands");
                    String sample = ((Element) node).getAttribute("sample");
                    if (sample.equals("all")) {
                        bandMap.put(band, null);
                    } else {
                        HashMap<Object, Object> sampleMap = (HashMap<Object, Object>) bandMap
                                .get(band);
                        if (sampleMap == null) {
                            if (!bandMap.containsKey(band)) {
                                sampleMap = new HashMap<Object, Object>();
                                bandMap.put(band, sampleMap);
                            }
                        }
                        sampleMap.put(Integer.decode(sample), new Integer(0));
                    }
                } else if (node.getLocalName().equals("transparentNodata")) { // 2011-08-24
                                                                              // PwD
                    String transparentNodata = checkTransparentNodata(
                            buffimage, node);
                    node.setTextContent(transparentNodata);
                }
            }
        }

        Iterator bandIt = bandMap.keySet().iterator();
        while (bandIt.hasNext()) {
            String band_str = (String) bandIt.next();
            int band_indexes[];
            if (buffimage.getType() == BufferedImage.TYPE_BYTE_BINARY
                    || buffimage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
                band_indexes = new int[1];
                band_indexes[0] = 0;
            } else {
                band_indexes = new int[band_str.length()];
                for (int i = 0; i < band_str.length(); i++) {
                    if (band_str.charAt(i) == 'A')
                        band_indexes[i] = 3;
                    if (band_str.charAt(i) == 'B')
                        band_indexes[i] = 2;
                    if (band_str.charAt(i) == 'G')
                        band_indexes[i] = 1;
                    if (band_str.charAt(i) == 'R')
                        band_indexes[i] = 0;
                }
            }

            Raster raster = buffimage.getRaster();
            java.util.HashMap sampleMap = (java.util.HashMap) bandMap
                    .get(band_str);
            boolean addall = (sampleMap == null);
            if (sampleMap == null) {
                sampleMap = new java.util.HashMap();
                bandMap.put(band_str, sampleMap);
            }

            int minx = raster.getMinX();
            int maxx = minx + raster.getWidth();
            int miny = raster.getMinY();
            int maxy = miny + raster.getHeight();
            int bands[][] = new int[band_indexes.length][raster.getWidth()];

            for (int y = miny; y < maxy; y++) {
                for (int i = 0; i < band_indexes.length; i++) {
                    raster.getSamples(minx, y, maxx, 1, band_indexes[i],
                            bands[i]);
                }
                for (int x = minx; x < maxx; x++) {
                    int sample = 0;
                    for (int i = 0; i < band_indexes.length; i++) {
                        sample |= bands[i][x] << ((band_indexes.length - i - 1) * 8);
                    }

                    Integer sampleObj = new Integer(sample);

                    boolean add = addall;
                    if (!addall) {
                        add = sampleMap.containsKey(sampleObj);
                    }
                    if (add) {
                        Integer count = (Integer) sampleMap.get(sampleObj);
                        if (count == null) {
                            count = new Integer(0);
                        }
                        count = new Integer(count.intValue() + 1);
                        sampleMap.put(sampleObj, count);
                    }
                }
            }
        }

        Node node = nodes.item(0);
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getLocalName().equals("count")) {
                    String band = ((Element) node).getAttribute("bands");
                    String sample = ((Element) node).getAttribute("sample");
                    HashMap sampleMap = (HashMap) bandMap.get(band);
                    Document doc = node.getOwnerDocument();
                    if (sample.equals("all")) {
                        Node parent = node.getParentNode();
                        Node prevSibling = node.getPreviousSibling();
                        Iterator sampleIt = sampleMap.keySet().iterator();
                        Element countnode = null;
                        int digits;
                        String prefix;
                        switch (buffimage.getType()) {
                        case BufferedImage.TYPE_BYTE_BINARY:
                            digits = 1;
                            prefix = "";
                            break;
                        case BufferedImage.TYPE_BYTE_GRAY:
                            digits = 2;
                            prefix = "0x";
                            break;
                        default:
                            prefix = "0x";
                            digits = band.length() * 2;
                        }
                        while (sampleIt.hasNext()) {
                            countnode = doc.createElementNS(
                                    node.getNamespaceURI(), "count");
                            Integer sampleInt = (Integer) sampleIt.next();
                            Integer count = (Integer) sampleMap.get(sampleInt);
                            if (band.length() > 0) {
                                countnode.setAttribute("bands", band);
                            }
                            countnode.setAttribute("sample", prefix
                                    + HexString(sampleInt.intValue(), digits));
                            Node textnode = doc
                                    .createTextNode(count.toString());
                            countnode.appendChild(textnode);
                            parent.insertBefore(countnode, node);
                            if (sampleIt.hasNext()) {
                                if (prevSibling != null
                                        && prevSibling.getNodeType() == Node.TEXT_NODE) {
                                    parent.insertBefore(
                                            prevSibling.cloneNode(false), node);
                                }
                            }
                        }
                        parent.removeChild(node);
                        node = countnode;
                    } else {
                        Integer count = (Integer) sampleMap.get(Integer
                                .decode(sample));
                        if (count == null)
                            count = new Integer(0);
                        Node textnode = doc.createTextNode(count.toString());
                        node.appendChild(textnode);
                    }
                }
            }
            node = node.getNextSibling();
        }
    }

    /**
     * Determines the width of the first image in an image file in pixels.
     * 
     * @param imageLoc
     *            the string location of the image (uri syntax expected)
     * @return int the image width in pixels, or -1 if unable.
     * @author Paul Daisey added 2011-05-13 to support WMTS ETS
     */
    public static int getImageWidth(String imageLoc) {

        // Get the image as an InputStream
        InputStream is = null;
        try {
            URI imageUri = new URI(imageLoc);
            URL imageUrl = imageUri.toURL();
            is = imageUrl.openStream();
        } catch (Exception e) {
            jlogger.log(Level.SEVERE, "getImageWidth", e);

            return -1;
        }
        // Determine the image width
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(is);

            // Find all image readers that recognize the image format
            Iterator iter = ImageIO.getImageReaders(iis);

            // No readers found
            if (!iter.hasNext()) {
                return -1;
            }

            // Use the first reader
            ImageReader reader = (ImageReader) iter.next();
            reader.setInput(iis, true);
            int width = reader.getWidth(0);
            iis.close();

            return width;
        } catch (IOException e) {
            jlogger.log(Level.SEVERE, "getImageWidth", e);
            // The image could not be read
        }
        return -1;
    }

    /**
     * Determines the height of the first image in an image file in pixels.
     * 
     * @param imageLoc
     *            the string location of the image (uri syntax expected)
     * @return int the image width in pixels, or -1 if unable.
     * @author Paul Daisey added 2011-05-13 to support WMTS ETS
     */
    public static int getImageHeight(String imageLoc) {

        // Get the image as an InputStream
        InputStream is = null;
        try {
            URI imageUri = new URI(imageLoc);
            URL imageUrl = imageUri.toURL();
            is = imageUrl.openStream();
        } catch (Exception e) {
            jlogger.log(Level.SEVERE, "getImageWidth", e);

            return -1;
        }
        // Determine the image width
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(is);

            // Find all image readers that recognize the image format
            Iterator iter = ImageIO.getImageReaders(iis);

            // No readers found
            if (!iter.hasNext()) {
                return -1;
            }

            // Use the first reader
            ImageReader reader = (ImageReader) iter.next();
            reader.setInput(iis, true);
            int height = reader.getHeight(0);
            iis.close();

            return height;
        } catch (IOException e) {
            jlogger.log(Level.SEVERE, "getImageWidth", e);
            // The image could not be read
        }
        return -1;
    }

    /**
     * Determines the type of image, or null if not a valid image type
     * 
     * @param imageLoc
     *            the string location of the image (uri syntax expected)
     * @return String the name of the image type/format, or null if not valid
     */
    public static String getImageType(String imageLoc) {

        // Get the image as an InputStream
        InputStream is = null;
        try {
            URI imageUri = new URI(imageLoc);
            URL imageUrl = imageUri.toURL();
            is = imageUrl.openStream();
        } catch (Exception e) {
            jlogger.log(Level.SEVERE, "getImageType", e);

            return null;
        }

        // Determine the image type and return it if valid
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(is);

            // Find all image readers that recognize the image format
            Iterator iter = ImageIO.getImageReaders(iis);

            // No readers found
            if (!iter.hasNext()) {
                return null;
            }

            // Use the first reader
            ImageReader reader = (ImageReader) iter.next();
            iis.close();

            // Return the format name
            return reader.getFormatName();
        } catch (IOException e) {
            jlogger.log(Level.SEVERE, "getImageType", e);

        }

        // The image could not be read
        return null;
    }

    /**
     * Gets the supported image types in the ImageIO class (gives a
     * comma-seperated list)
     */
    public static String getSupportedImageTypes() {
        String[] readers = ImageIO.getReaderFormatNames();
        ArrayList<String> imageArray = new ArrayList<String>();
        String str = "";
        for (int i = 0; i < readers.length; i++) {
            String current = readers[i].toLowerCase();
            if (!imageArray.contains(current)) {
                imageArray.add(current);
                str += current + ",";
            }
        }
        return str.substring(0, str.lastIndexOf(","));
    }

    public static Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) {
        Document doc = null;
        InputStream is = null;
        try {
            is = uc.getInputStream();
            doc = parse(is, instruction, logger);
        } catch (Exception e) {
            String msg = String.format(
                    "Failed to parse %s resource from %s \n %s",
                    uc.getContentType(), uc.getURL(), e.getMessage());
            jlogger.warning(msg);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return doc;
    }

    public Document parseAsInitialized(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        if (initialInstruction == null) {
            throw new Exception("Parser was not initialized");
        } else {
            return parse(uc, initialInstruction, logger);
        }
    }

    private static Node processFrame(ImageReader reader, int frame,
            NodeList nodes, PrintWriter logger) throws Exception {
        if (nodes.getLength() == 0) {
            return null;
        }
        String formatName = reader.getFormatName().toLowerCase(); // 2011-09-08
                                                                  // PwD
        BufferedImage image = reader.read(frame);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // System.out.println(node.getLocalName());
                if (node.getLocalName().equals("type")) {
                    node.setTextContent(formatName); // 2011-09-08 PwD was
                                                     // reader.getFormatName().toLowerCase()
                } else if (node.getLocalName().equals("height")) {
                    node.setTextContent(Integer.toString(image.getHeight()));
                } else if (node.getLocalName().equals("width")) {
                    node.setTextContent(Integer.toString(image.getWidth()));
                } else if (node.getLocalName().equals("metadata")) {
                    try { // 2011--08-23 PwD
                        IIOMetadata metadata = reader.getImageMetadata(frame);
                        if (metadata != null) {
                            String format = ((Element) node)
                                    .getAttribute("format");
                            if (format.length() == 0) {
                                format = metadata.getNativeMetadataFormatName();
                            }
                            Node tree = metadata.getAsTree(format);
                            TransformerFactory tf = TransformerFactory
                                    .newInstance();
                            Transformer t = tf.newTransformer();
                            t.transform(new DOMSource(tree),
                                    new DOMResult(node));
                        }
                    } catch (javax.imageio.IIOException e) { // 2011--08-23 PwD
                        DocumentBuilderFactory dbf = DocumentBuilderFactory
                                .newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.newDocument();
                        String format = reader.getFormatName().toLowerCase();
                        String formatEltName = "javax_imageio_" + format
                                + "_1.0";
                        Element formatElt = doc.createElement(formatEltName);
                        TransformerFactory tf = TransformerFactory
                                .newInstance();
                        Transformer t = tf.newTransformer();
                        t.transform(new DOMSource(formatElt), new DOMResult(
                                node));
                    }
                } else if (node.getLocalName().equals("model")) {
                    int imagetype = -1;
                    String model = ((Element) node).getAttribute("value");
                    if (model.equals("MONOCHROME")) {
                        imagetype = BufferedImage.TYPE_BYTE_BINARY;
                    } else if (model.equals("GRAY")) {
                        imagetype = BufferedImage.TYPE_BYTE_GRAY;
                    } else if (model.equals("RGB")) {
                        imagetype = BufferedImage.TYPE_3BYTE_BGR;
                    } else if (model.equals("ARGB")) {
                        imagetype = BufferedImage.TYPE_4BYTE_ABGR;
                    } else {
                        model = "CUSTOM";
                    }
                    ((Element) node).setAttribute("value", model);
                    BufferedImage buffImage = image;
                    if (image.getType() != imagetype && imagetype != -1) {
                        buffImage = new BufferedImage(image.getWidth(),
                                image.getHeight(), imagetype);
                        Graphics2D g2 = buffImage.createGraphics();
                        ImageTracker tracker = new ImageTracker();
                        boolean done = g2.drawImage(image, 0, 0, tracker);
                        if (!done) {
                            while (!tracker.done) {
                                sleep(50);
                            }
                        }
                    }
                    processBufferedImage(buffImage, formatName,
                            node.getChildNodes());
                } else if (node.getLocalName().equals("transparency")) { // 2011-08-24
                                                                         // PwD
                    int transparency = image.getTransparency();
                    String transparencyName = null;
                    switch (transparency) {
                    case Transparency.OPAQUE: {
                        transparencyName = "Opaque";
                        break;
                    }
                    case Transparency.BITMASK: {
                        transparencyName = "Bitmask";
                        break;
                    }
                    case Transparency.TRANSLUCENT: {
                        transparencyName = "Translucent";
                        break;
                    }
                    default: {
                        transparencyName = "Unknown";
                    }
                    }
                    node.setTextContent(transparencyName);

                } else if (node.getLocalName().equals("base64Data")) { // 2011-09-08
                                                                       // PwD
                    String base64Data = getBase64Data(image, formatName, node);
                    node.setTextContent(base64Data);
                } else {
                    logger.println("ImageParser Error: Invalid tag "
                            + node.getNodeName());
                }
            }
        }
        return null;
    }

    /*
     * Parse a string of base64 encoded image data. 2011-09-08 PwD
     */
    public static Document parseBase64(String base64Data, Element instruction)
            throws Exception {
        byte[] imageData = Base64.decodeBase64(base64Data);
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        StringWriter swLogger = new StringWriter();
        PrintWriter pwLogger = new PrintWriter(swLogger);
        return parse(bais, instruction, pwLogger);
    }

    private static Document parse(InputStream source, Element instruction,
            PrintWriter logger) throws Exception {
        ImageReader reader;
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(source);
            reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis);
        } catch (Exception e) {
            logger.println("No image handlers available for the data stream. "
                    + e.getMessage());
            throw e;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.transform(new DOMSource(instruction), new DOMResult(doc));

        Element new_instruction = doc.getDocumentElement();

        int framesRead = 0;
        boolean containsFrames = false;
        Element framesElement = null;
        Element metadataElement = null;

        NodeList nodes = new_instruction.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // System.out.println(node.getLocalName());
                if (node.getLocalName().equals("type")) {
                    node.setTextContent(reader.getFormatName().toLowerCase());
                } else if (node.getLocalName().equals("frames")) {
                    framesElement = (Element) node;
                    containsFrames = true;
                } else if (node.getLocalName().equals("metadata")) {
                    metadataElement = (Element) node;
                } else if (node.getLocalName().equals("frame")) {
                    int frame;
                    String frameStr = ((Element) node).getAttribute("num");
                    if (frameStr.length() == 0) {
                        frame = framesRead;
                        framesRead++;
                        ((Element) node).setAttribute("num",
                                Integer.toString(frame));
                    } else {
                        frame = Integer.parseInt(frameStr);
                        framesRead = frame + 1;
                    }
                    processFrame(reader, frame, node.getChildNodes(), logger);
                    containsFrames = true;
                }
            }
        }

        if (containsFrames) {
            if (metadataElement != null) {
                IIOMetadata metadata = reader.getStreamMetadata();
                if (metadata != null) {
                    String format = metadataElement.getAttribute("format");
                    if (format.length() == 0) {
                        format = metadata.getNativeMetadataFormatName();
                    }
                    Node tree = metadata.getAsTree(format);
                    t.transform(new DOMSource(tree), new DOMResult(
                            metadataElement));
                }
            }
            if (framesElement != null) {
                boolean allowSearch = !reader.isSeekForwardOnly();
                int frames = reader.getNumImages(allowSearch);
                if (frames == -1) {
                    try {
                        while (true) {
                            reader.read(framesRead);
                            framesRead++;
                        }
                    } catch (Exception e) {
                        jlogger.log(Level.SEVERE, "", e);

                        frames = framesRead + 1;
                    }
                }
                framesElement.setTextContent(Integer.toString(frames));
            }
        } else {
            processFrame(reader, 0, nodes, logger);
            framesRead = 1;
        }

        // t.transform(new DOMSource(doc), new StreamResult(System.out));
        return doc;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Parameters: xml_url image_url");
            return;
        }

        java.net.URL xml_url;
        try {
            xml_url = new java.net.URL(args[0]);
        } catch (Exception e) {
            jlogger.log(Level.INFO,
                    "Error building xmlurl, will prefix file://", e);

            xml_url = new java.net.URL("file://" + args[0]);
        }

        java.net.URL image_url;
        try {
            image_url = new java.net.URL(args[1]);
        } catch (Exception e) {
            jlogger.log(Level.INFO,
                    "Error building xmlurl, will prefix file://", e);

            image_url = new java.net.URL("file://" + args[1]);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml_url.openStream());
        // Element instruction = (Element)
        // doc.getElementsByTagNameNS("http://www.occamlab.com/te/parsers",
        // "ImageParser").item(0);
        Element instruction = (Element) doc.getDocumentElement();

        PrintWriter logger = new PrintWriter(System.out);
        InputStream image_is = image_url.openConnection().getInputStream();

        Document result = parse(image_is, instruction, logger);
        logger.flush();

        if (result != null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("http://saxon.sf.net/feature/strip-whitespace",
                        "all");
            } catch (IllegalArgumentException e) {
                jlogger.log(
                        Level.INFO,
                        "setAttribute(\"http://saxon.sf.net/feature/strip-whitespace\", \"all\");",
                        e);

            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(result), new StreamResult(System.out));
        }

        System.exit(0);
    }
}
