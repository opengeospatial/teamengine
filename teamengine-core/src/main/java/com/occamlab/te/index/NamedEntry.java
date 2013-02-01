package com.occamlab.te.index;

import javax.xml.namespace.QName;

public interface NamedEntry {
    public String getName();

    public String getLocalName();

    public String getNamespaceURI();

    public String getPrefix();

    public QName getQName();

    public void setQName(QName qname);

    public String getId();
}
