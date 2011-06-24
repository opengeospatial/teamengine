package com.occamlab.te.index;

import org.w3c.dom.Element;

public class TestEntry extends TemplateEntry {
    String assertion;

    TestEntry() {
        super();
    }

    TestEntry(Element test) {
        super(test);
//        try {
//            setTemplateFile(new File(new URI(test.getAttribute("file"))));
//            NodeList nl = test.getElementsByTagName("param");
//            if (nl.getLength() > 0) {
//                params = new ArrayList<QName>();
//                for (int i = 0; i < nl.getLength(); i++) {
//                    Element el = (Element)nl.item(i);
//                    String prefix = el.getAttribute("prefix");
//                    String namespaceUri = el.getAttribute("namespace-uri");
//                    String localName = el.getAttribute("local-name");
//                    params.add(new QName(namespaceUri, localName, prefix));
//                }
//            }
//            setUsesContext(Boolean.parseBoolean(test.getAttribute("uses-context")));
            setAssertion(test.getElementsByTagName("assertion").item(0).getTextContent());
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }
}
