package com.occamlab.te.index;

import javax.xml.namespace.QName;

public interface NamedEntry {
    String getName();

    String getLocalName();

    String getNamespaceURI();

    String getPrefix();

    QName getQName();

    void setQName(QName qname);

    String getId();
}
