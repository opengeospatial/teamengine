package com.occamlab.te.index;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.util.DomUtils;

public class ParserEntry extends IndexEntry {
    boolean initialized;
    String className;
    String method;
    List<Node> classParams = null;

    ParserEntry() {
        super();
    }

    ParserEntry(Element parser) throws Exception {
        super(parser);
        Element e = (Element) parser.getElementsByTagName("java").item(0);
        if (e != null) {
            setClassName(e.getAttribute("class"));
            setMethod(e.getAttribute("method"));
            setInitialized(Boolean.parseBoolean(e.getAttribute("initialized")));
            NodeList nl = e.getElementsByTagName("with-param");
            if (nl.getLength() > 0) {
                setInitialized(true);
                classParams = new ArrayList<Node>();
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    // System.out.println(DomUtils.serializeNode(el));
                    Node value = null;
                    NodeList children = el.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node n = children.item(j);
                        if (n.getNodeType() == Node.TEXT_NODE) {
                            value = n;
                        }
                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            value = DomUtils.createDocument(n);
                            break;
                        }
                    }
                    classParams.add(value);
                }
            }
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Node> getClassParams() {
        return classParams;
    }

    public void setClassParams(List<Node> classParams) {
        this.classParams = classParams;
    }
}
