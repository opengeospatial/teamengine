package com.occamlab.te.parsers;

import java.io.PrintWriter;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BinaryPayloadParser {

    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Element root = doc.createElement("payload");

        Transformer t = TransformerFactory.newInstance().newTransformer();

        return doc;
    }

}
