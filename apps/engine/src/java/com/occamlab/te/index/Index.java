/*
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
 */

package com.occamlab.te.index;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Index {
    List<File> dependencies = new ArrayList<File>();
    Map<String, FunctionEntry> functionMap = new HashMap<String, FunctionEntry>();
    Map<String, ParserEntry> parserMap = new HashMap<String, ParserEntry>();
    Map<String, SuiteEntry> suiteMap = new HashMap<String, SuiteEntry>();
    Map<String, TestEntry> testMap = new HashMap<String, TestEntry>();
    
    public Index() {
    }
    
    public Index(File indexFile) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(indexFile);
        Element index = (Element)doc.getDocumentElement();
        NodeList nodes = index.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)node;
                String name = el.getNodeName();
                if (name.equals("dependency")) {
                    File file = new File(el.getAttribute("file"));
                    dependencies.add(file);
                } else if (name.equals("suite")) {
                    SuiteEntry se = new SuiteEntry(el);
                    suiteMap.put(se.getId(), se);
                } else if (name.equals("test")) {
                    TestEntry te = new TestEntry(el);
                    testMap.put(te.getId(), te);
                } else if (name.equals("function")) {
                    FunctionEntry fe = new FunctionEntry(el);
                    functionMap.put(fe.getId(), fe);
                } else if (name.equals("parser")) {
                    ParserEntry pe = new ParserEntry(el);
                    parserMap.put(pe.getId(), pe);
                }
            }
        }
    }
    
    public boolean outOfDate() {
        //TODO: determine this by comparing file dates
        return true;
    }
    
    public void add(Index index) {
        dependencies.addAll(index.dependencies);
        functionMap.putAll(index.functionMap);
        suiteMap.putAll(index.suiteMap);
        testMap.putAll(index.testMap);
        parserMap.putAll(index.parserMap);
    }
    
    public FunctionEntry getFunction(String name) {
        return (FunctionEntry)getEntry(functionMap, name);
    }
    
    public FunctionEntry getFunction(QName qname) {
        return (FunctionEntry)getEntry(functionMap, qname);
    }
    
    public ParserEntry getParser(String name) {
        return (ParserEntry)getEntry(parserMap, name);
    }

    public ParserEntry getParser(QName qname) {
        return (ParserEntry)getEntry(parserMap, qname);
    }
    
    public SuiteEntry getSuite(String name) {
        return (SuiteEntry)getEntry(suiteMap, name);
    }

    public SuiteEntry getSuite(QName qname) {
        return (SuiteEntry)getEntry(suiteMap, qname);
    }
    
    public Set<String> getSuiteKeys() {
        return suiteMap.keySet();
    }

    public TestEntry getTest(String name) {
        return (TestEntry)getEntry(testMap, name);
    }

    public TestEntry getTest(QName qname) {
        return (TestEntry)getEntry(testMap, qname);
    }

    private IndexEntry getEntry(Map<String, ? extends IndexEntry> map, QName qname) {
        return getEntry(map, "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart());
    }

    private IndexEntry getEntry(Map<String, ? extends IndexEntry> map, String name) {
        if (name == null) {
            return map.values().iterator().next();
        }
        
        if (name.startsWith("{")) {
            return map.get(name);
        }

        int i = name.lastIndexOf(',');
        if (i >= 0) {
            String key = "{" + name.substring(0, i) + "}" + name.substring(i + 1);
            return map.get(key);
        }

        String prefix = null;
        String localName = name;
        i = name.indexOf(':');
        if (i >= 0) {
            prefix = name.substring(0, i);
            localName = name.substring(i);
        }
        
        Iterator<? extends IndexEntry> it = map.values().iterator();
        while (it.hasNext()) {
            IndexEntry entry = it.next();
            if (entry.getLocalName().equals(localName)) {
                if (prefix == null) {
                    return entry;
                } else {
                    if (entry.getPrefix().equals(prefix)) {
                        return entry;
                    }
                }
            }
        }

        return null;
    }
}
