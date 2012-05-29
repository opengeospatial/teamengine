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

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.*;

/**
 * Extracts the body of a response message and treats it as an image resource.
 * If the entity does not correspond to a supported image format, an exception
 * is reported in the test log.
 * 
 */
public class ImageParser {
    private static Logger jlogger = Logger.getLogger("com.occamlab.te.parsers.ImageParser");

    private static class ImageTracker implements ImageObserver {
        volatile boolean done = false;
        
        public synchronized boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
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

    private static void processBufferedImage(BufferedImage buffimage, NodeList nodes) throws Exception {
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
                    processBufferedImage(buffimage.getSubimage(x, y, w, h), e.getChildNodes());
                } else if (node.getLocalName().equals("checksum")) {
                    CRC32 checksum = new CRC32();
                    Raster raster = buffimage.getRaster();
                    DataBufferByte buffer;
                    if (node.getParentNode().getLocalName().equals("subimage")) {
                        WritableRaster outRaster = raster.createCompatibleWritableRaster();
                        buffimage.copyData(outRaster);
                        buffer = (DataBufferByte)outRaster.getDataBuffer();
                    } else {
                        buffer = (DataBufferByte)raster.getDataBuffer();
                    }
                    int numbanks = buffer.getNumBanks();
                    for (int j = 0; j < numbanks; j++) {
                        checksum.update(buffer.getData(j));
                    }
                    Document doc = node.getOwnerDocument();
                    node.appendChild(doc.createTextNode(Long.toString(checksum.getValue())));
                } else if (node.getLocalName().equals("count")) {
                    String band = ((Element) node).getAttribute("bands");
                    String sample = ((Element) node).getAttribute("sample");
                    if (sample.equals("all")) {
                        bandMap.put(band, null);
                    } else {
                        HashMap<Object, Object> sampleMap = (HashMap<Object, Object>) bandMap.get(band);
                        if (sampleMap == null) {
                            if (!bandMap.containsKey(band)) {
                                sampleMap = new HashMap<Object, Object>();
                                bandMap.put(band, sampleMap);
                            }
                        }
                        sampleMap.put(Integer.decode(sample), new Integer(0));
                    }
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
            java.util.HashMap sampleMap = (java.util.HashMap) bandMap.get(band_str);
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
                    raster.getSamples(minx, y, maxx, 1, band_indexes[i], bands[i]);
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
                            countnode = doc.createElementNS(node.getNamespaceURI(), "count");
                            Integer sampleInt = (Integer) sampleIt.next();
                            Integer count = (Integer) sampleMap.get(sampleInt);
                            if (band.length() > 0) {
                                countnode.setAttribute("bands", band);
                            }
                            countnode.setAttribute("sample", prefix + HexString(sampleInt.intValue(), digits));
                            Node textnode = doc.createTextNode(count.toString());
                            countnode.appendChild(textnode);
                            parent.insertBefore(countnode, node);
                            if (sampleIt.hasNext()) {
                                if (prevSibling != null && prevSibling.getNodeType() == Node.TEXT_NODE) {
                                    parent.insertBefore(prevSibling.cloneNode(false), node);
                                }
                            }
                        }
                        parent.removeChild(node);
                        node = countnode;
                    } else {
                        Integer count = (Integer) sampleMap.get(Integer.decode(sample));
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
            jlogger.log(Level.SEVERE,"getImageType",e);

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
            jlogger.log(Level.SEVERE,"getImageType",e);

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

    public static Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
        return parse(uc.getInputStream(), instruction, logger);
    }

    public Document parseAsInitialized(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
        if (initialInstruction == null) {
            throw new Exception("Parser was not initialized");
        } else {
            return parse(uc, initialInstruction, logger);
        }
    }
    
    private static Node processFrame(ImageReader reader, int frame, NodeList nodes, PrintWriter logger) throws Exception {
        if (nodes.getLength() == 0) {
            return null;
        }

        BufferedImage image = reader.read(frame);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // System.out.println(node.getLocalName());
                if (node.getLocalName().equals("type")) {
                    node.setTextContent(reader.getFormatName().toLowerCase());
                } else if (node.getLocalName().equals("height")) {
                    node.setTextContent(Integer.toString(image.getHeight()));
                } else if (node.getLocalName().equals("width")) {
                    node.setTextContent(Integer.toString(image.getWidth()));
                } else if (node.getLocalName().equals("metadata")) {
                    IIOMetadata metadata = reader.getImageMetadata(frame);
                    if (metadata != null) {
                        String format = ((Element)node).getAttribute("format");
                        if (format.length() == 0) {
                            format = metadata.getNativeMetadataFormatName();
                        }
                        Node tree = metadata.getAsTree(format);
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer t = tf.newTransformer();
                        t.transform(new DOMSource(tree), new DOMResult(node));
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
                    ((Element)node).setAttribute("value", model);
                    BufferedImage buffImage = image;
                    if (image.getType() != imagetype && imagetype != -1) {
                        buffImage = new BufferedImage(image.getWidth(), image.getHeight(), imagetype);
                        Graphics2D g2 = buffImage.createGraphics();
                        ImageTracker tracker = new ImageTracker();
                        boolean done = g2.drawImage(image, 0, 0, tracker);
                        if (!done) {
                            while (!tracker.done) {
                                sleep(50);
                            }
                        }
                    }
                    processBufferedImage(buffImage, node.getChildNodes());
                } else {
                    logger.println("ImageParser Error: Invalid tag " + node.getNodeName());
                }
            }
        }
        return null;
    }

    private static Document parse(InputStream source, Element instruction, PrintWriter logger) throws Exception {
        ImageReader reader;
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(source);
            reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis);
        } catch (Exception e) {
            logger.println("No image handlers available for the data stream.");
            return null;
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
                    framesElement = (Element)node;
                    containsFrames = true;
                } else if (node.getLocalName().equals("metadata")) {
                    metadataElement = (Element)node;
                } else if (node.getLocalName().equals("frame")) {
                    int frame;
                    String frameStr = ((Element) node).getAttribute("num");
                    if (frameStr.length() == 0) {
                        frame = framesRead;
                        framesRead++;
                        ((Element) node).setAttribute("num", Integer.toString(frame));
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
                    t.transform(new DOMSource(tree), new DOMResult(metadataElement));
                }
            }
            if (framesElement != null) {
                boolean allowSearch = !reader.isSeekForwardOnly(); 
                int frames = reader.getNumImages(allowSearch);
                if (frames == -1) {
                    try {
                        while(true) {
                            reader.read(framesRead);
                            framesRead++;
                        }
                    } catch (Exception e) {
                        jlogger.log(Level.SEVERE,"",e);

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
            jlogger.log(Level.INFO,"Error building xmlurl, will prefix file://",e);

            xml_url = new java.net.URL("file://" + args[0]);
        }

        java.net.URL image_url;
        try {
            image_url = new java.net.URL(args[1]);
        } catch (Exception e) {
            jlogger.log(Level.INFO,"Error building xmlurl, will prefix file://",e);

            image_url = new java.net.URL("file://" + args[1]);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml_url.openStream());
//        Element instruction = (Element) doc.getElementsByTagNameNS("http://www.occamlab.com/te/parsers", "ImageParser").item(0);
        Element instruction = (Element) doc.getDocumentElement();

        PrintWriter logger = new PrintWriter(System.out);
        InputStream image_is = image_url.openConnection().getInputStream();

        Document result = parse(image_is, instruction, logger);
        logger.flush();

        if (result != null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("http://saxon.sf.net/feature/strip-whitespace", "all");
            } catch (IllegalArgumentException e) {
                jlogger.log(Level.INFO,"setAttribute(\"http://saxon.sf.net/feature/strip-whitespace\", \"all\");",e);

            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(result), new StreamResult(System.out));
        }

        System.exit(0);
    }
}
