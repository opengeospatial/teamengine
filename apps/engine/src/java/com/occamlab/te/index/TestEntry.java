package com.occamlab.te.index;

import org.w3c.dom.Element;
import com.occamlab.te.TECore;  // 2011-04-01 PwD

public class TestEntry extends TemplateEntry {
	int defaultResult = TECore.PASS; // 2011-04-01 PwD
	int result = TECore.PASS;        // 2011-04-07 PwD
	String context;   				 // 2011-03-30 PwD
	String type;       				 // 2011-03-31 PwD
    String assertion;

    public TestEntry() {  // 2011-04-07 PwD made public 
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
        	// begin 2011-03-30 PwD
        	if (usesContext()) {
            		setContext(test.getElementsByTagName("context").item(0).getTextContent());	
        	}
        	// end 2011-03-30 PwD
        	// begin 2011-03-31 PwD
        	setType(test.getElementsByTagName("type").item(0).getTextContent());
        	// end 2011-03-31 PwD
        	setAssertion(test.getElementsByTagName("assertion").item(0).getTextContent());
        	// begin 2011-04-07 PwD
        	String defaultResultName = test.getElementsByTagName("defaultResult").item(0).getTextContent();
        	setDefaultResult(defaultResultName.equals("BestPractice") ? TECore.BEST_PRACTICE : TECore.PASS);
        	setResult(getDefaultResult());
        	// end 2011-04-07 PwD
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }

    // begin 2011-04-01 PwD
    public int getDefaultResult() {
    	return defaultResult;
    }
    
    public void setDefaultResult(int defaultResult) {
    	this.defaultResult = defaultResult;
    }
    // end 2011-04-01 PwD
    // begin 2011-04-07 PwD
    public int getResult() {
    	return result;
    }
    
    public void setResult(int result) {
    	this.result = result;
    }
    // end 2011-04-07 PwD
    // begin 2011-03-30 PwD
    public String getContext() {
    	return context;
    }
    
    public void setContext(String context) {
    	this.context = context;
    }
    // end 2011-03-30 PwD
    // begin 2011-03-31 PwD
    public String getType() {
    	return type;
    }
    
    public void setType(String type) {
    	this.type = type;
    }
    // end 2011-03-31 PwD
    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }
}
