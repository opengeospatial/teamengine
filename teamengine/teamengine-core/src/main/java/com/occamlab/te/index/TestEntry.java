package com.occamlab.te.index;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.occamlab.te.TECore;

/**
 * Describes a test in a test suite. It corresponds to a &lt;test&gt; element in
 * an index file.
 */
public class TestEntry extends TemplateEntry {

    private static final Logger LOGR = Logger.getLogger(TestEntry.class
            .getName());
    int defaultResult = TECore.PASS;
    int result = TECore.PASS;
    String context;
    String type;
    String assertion;

    public TestEntry() {
        super();
    }

    TestEntry(Element test) {
        super(test);
        if (usesContext()) {
            setContext(test.getElementsByTagName("context").item(0)
                    .getTextContent());
        }
        setType(test.getElementsByTagName("type").item(0).getTextContent());
        setAssertion(test.getElementsByTagName("assertion").item(0)
                .getTextContent());
        String defaultResultName = test.getElementsByTagName("defaultResult")
                .item(0).getTextContent();
        setDefaultResult(defaultResultName.equals("BestPractice") ? TECore.BEST_PRACTICE
                : TECore.PASS);
        setResult(getDefaultResult());
    }

    public int getDefaultResult() {
        return defaultResult;
    }

    public void setDefaultResult(int defaultResult) {
        this.defaultResult = defaultResult;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        if (LOGR.isLoggable(Level.FINE)) {
            LOGR.fine(String.format("Setting test result for %s: %d",
                    getQName(), result));
        }
        this.result = result;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }
}
