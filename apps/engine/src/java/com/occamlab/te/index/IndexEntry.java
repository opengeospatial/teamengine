package com.occamlab.te.index;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public class IndexEntry implements NamedEntry {
    QName qname = null;
    
    IndexEntry() {
    }

    IndexEntry(Element el) {
        String prefix = el.getAttribute("prefix");
        String namespaceUri = el.getAttribute("namespace-uri");
        String localName = el.getAttribute("local-name");
        setQName(new QName(namespaceUri, localName, prefix));
    }
    
//    public void persistAttributes(PrintWriter out) {
//        out.print(" prefix=\"" + getPrefix() + "\"" + 
//                  " namespace-uri=\"" + getNamespaceURI() + "\"" +
//                  " local-name=\"" + getLocalName() + "\"");
//    }
//    
//    public void persistTags(PrintWriter out) {
//    }
//    
//    public void persist(PrintWriter out, String tagname) {
//        out.print("<" + tagname);
//        persistAttributes(out);
//        out.println(">");
//        persistTags(out);
//        out.println("</" + tagname + ">");
//    }
//
//    public abstract void persist(PrintWriter out);

    public String getName() {
        String prefix = qname.getPrefix();
        if (prefix == null) {
            return qname.getLocalPart();
        } else {
            return prefix + ":" + qname.getLocalPart();
        }
    }

    public String getLocalName() {
        return qname.getLocalPart();
    }

    public String getNamespaceURI() {
        return qname.getNamespaceURI();
    }

    public String getPrefix() {
        return qname.getPrefix();
    }

    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }

    public String getId() {
        return "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart();
    }
}

