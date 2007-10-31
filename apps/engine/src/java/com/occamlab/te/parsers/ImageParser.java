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

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.parsers;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.ArrayList;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.*;

import com.occamlab.te.util.IOUtils;

/**
 * Extracts the body of a response message and treats it as an image resource.
 * If the entity does not correspond to a supported image format, an exception
 * is reported in the test log.
 *
 */
public class ImageParser {

    private static class ImageTracker implements ImageObserver {

        private boolean done;
        private String type;
        private Image image;

        public ImageTracker(Image image, boolean wait) {
            this.image = image;

            if (wait) {
                ImageTracker tracker = new ImageTracker(image, false);
                tracker.waitForImage();
                type = tracker.getImageType();
                done = true;
            } else {
                type = null;
                done = false;
            }
        }

        public ImageTracker(Image image) {
            this(image, true);
        }

        public String getImageType() {
            if (type == null) {
                while (image.getWidth(this) == -1 && !done) {
                    sleep(50);
                }
            }
            return type;
        }

        public boolean isDone() {
            return done;
        }

        public void waitForImage() {
            while (image.getWidth(this) == -1 && !done) {
                sleep(50);
            }

            while (image.getHeight(this) == -1 && !done) {
                sleep(50);
            }

            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            if (tk.prepareImage(image, -1, -1, this)) {
                done = true;
            }

            while (!done) {
                sleep(50);
            }
        }

        /**
         * Determines image type by parsing a stack trace to find out which
         * ImageDecoder class is being used. This is admittedly a rather
         * contrived method, but it seems to be the only way to retrieve the
         * information from an Image created with an ImageProducer class.
         *
         * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int,
         *      int, int, int, int)
         */
        public synchronized boolean imageUpdate(Image img, int infoflags,
                int x, int y, int width, int height) {
            if (type == null) {
                try {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    new Throwable("").printStackTrace(new java.io.PrintWriter(
                            sw));
                    String stackTrace = sw.toString();
                    int i = stackTrace.indexOf("ImageDecoder.produceImage");
                    int j = stackTrace.lastIndexOf(".", i - 1);
                    type = stackTrace.substring(j + 1, i).toLowerCase();
                } catch (Exception e) {
                    type = "Could not determine";
                }
            }

            if ((infoflags & (ABORT | ERROR)) != 0) {
                done = true;
            } else {
                done = ((infoflags & (ALLBITS | FRAMEBITS)) != 0);
            }
            return !done;
        }
    }

    // Same as Thread.sleep, except it ignores exceptions
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
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

    private static void processBufferedImage(BufferedImage buffimage,
            NodeList nodes) throws Exception {
        java.util.HashMap<Object, Object> bandMap = new java.util.HashMap<Object, Object>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getLocalName().equals("subimage")) {
                    Element e = (Element) node;
                    int x = Integer.parseInt(e.getAttribute("x"));
                    int y = Integer.parseInt(e.getAttribute("y"));
                    int w = Integer.parseInt(e.getAttribute("width"));
                    int h = Integer.parseInt(e.getAttribute("height"));
                    processBufferedImage(buffimage.getSubimage(x, y, w, h), e
                            .getChildNodes());
                } else if (node.getLocalName().equals("checksum")) {
                    java.util.zip.CRC32 checksum = new java.util.zip.CRC32();
                    Raster raster = buffimage.getRaster();
                    java.awt.image.DataBufferByte buffer = (java.awt.image.DataBufferByte) raster
                            .getDataBuffer();
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
                        java.util.HashMap<Object, Object> sampleMap = (java.util.HashMap<Object, Object>) bandMap
                                .get(band);
                        if (sampleMap == null) {
                            if (!bandMap.containsKey(band)) {
                                sampleMap = new java.util.HashMap<Object, Object>();
                                bandMap.put(band, sampleMap);
                            }
                        }
                        sampleMap.put(Integer.decode(sample), new Integer(0));
                    }
                }
            }
        }

        java.util.Iterator bandIt = bandMap.keySet().iterator();
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
                        if (count == null)
                            count = new Integer(0);
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
                    java.util.HashMap sampleMap = (java.util.HashMap) bandMap
                            .get(band);
                    Document doc = node.getOwnerDocument();
                    if (sample.equals("all")) {
                        Node parent = node.getParentNode();
                        Node prevSibling = node.getPreviousSibling();
                        // Node nextSibling = node.getNextSibling();
                        java.util.Iterator sampleIt = sampleMap.keySet()
                                .iterator();
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
                            countnode = doc.createElement("count");
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
                                if (prevSibling.getNodeType() == Node.TEXT_NODE) {
                                    parent.insertBefore(prevSibling
                                            .cloneNode(false), node);
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
            PrintWriter logger) throws Exception {
        return parse(uc.getContent(), instruction, logger);
    }
/*
    public static Document parse(HttpResponse resp, Element instruction,
            PrintWriter logger) throws Exception {
        return parse(IOUtils.inputStreamToBytes(resp.getEntity().getContent()), instruction, logger);
    }
*/
    private static Document parse(Object source, Element instruction,
            PrintWriter logger) throws Exception {
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }

        try {
            Image image;
            try {
                if (source instanceof InputStream) {
                    byte[] imgBytes = IOUtils.inputStreamToBytes((InputStream)source);
                    image = java.awt.Toolkit.getDefaultToolkit().createImage(imgBytes);
                } else {
                    image = java.awt.Toolkit.getDefaultToolkit().createImage((ImageProducer)source);
                }
            } catch (Exception e) {
                logger.println("ImageParser Error: Could not get the image");
                return null;
            }

            // Document doc = instruction.getOwnerDocument();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.transform(new DOMSource(instruction), new DOMResult(doc));

            Element new_instruction = (Element) doc.getElementsByTagNameNS(
                    "http://www.occamlab.com/te/parsers", "ImageParser")
                    .item(0);

            ImageTracker tracker = new ImageTracker(image);
            String type = tracker.getImageType();
            int height = image.getHeight(tracker);
            int width = image.getWidth(tracker);

            NodeList nodes = new_instruction.getChildNodes();
            // System.out.println(Integer.toString(nodes.getLength()));
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // System.out.println(node.getLocalName());
                    if (node.getLocalName().equals("type")) {
                        node.appendChild(doc.createTextNode(type));
                    } else if (node.getLocalName().equals("height")) {
                        // System.out.println("height");
                        node.appendChild(doc.createTextNode(Integer
                                .toString(height)));
                    } else if (node.getLocalName().equals("width")) {
                        node.appendChild(doc.createTextNode(Integer
                                .toString(width)));
                    } else if (node.getLocalName().equals("model")) {
                        int imagetype;
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
                            imagetype = BufferedImage.TYPE_CUSTOM;
                        }
                        BufferedImage buffImage = new BufferedImage(width,
                                height, imagetype);
                        Graphics2D g2 = buffImage.createGraphics();
                        g2.drawImage(image, 0, 0, tracker);
                        processBufferedImage(buffImage, node.getChildNodes());
                    } else {
                        logger.println("ImageParser Error: Invalid tag "
                                + node.getNodeName());
                    }
                }
            }

            // t.transform(new DOMSource(doc), new StreamResult(System.out));
            return doc;
        } catch (Exception e) {
            logger.println("ImageParser " + e.getClass().toString() + ": "
                    + e.getMessage());
            return null;
        }
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
            xml_url = new java.net.URL("file://" + args[0]);
        }

        java.net.URL image_url;
        try {
            image_url = new java.net.URL(args[1]);
        } catch (Exception e) {
            image_url = new java.net.URL("file://" + args[1]);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml_url.openStream());
        Element instruction = (Element) doc.getElementsByTagNameNS(
                "http://www.occamlab.com/te/parsers", "ImageParser").item(0);

        PrintWriter logger = new PrintWriter(System.out);
        InputStream image_is = image_url.openConnection().getInputStream();

        Document result = parse(image_is, instruction, logger);
        logger.flush();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(result), new StreamResult(System.out));

        System.exit(0);
    }
}
