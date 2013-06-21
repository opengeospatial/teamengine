package com.occamlab.te;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import com.occamlab.te.index.Index;

public class TECoreTest {

    static Engine engine;
    static Index index;
    static RuntimeOptions runOpts;

    @BeforeClass
    public static void setupFixture() throws Exception {
        engine = new Engine();
        index = new Index();
        runOpts = new RuntimeOptions();
    }

    @Test
    public void parseXmlWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/article.xml");
        URLConnection urlConn = url.openConnection();
        TECore iut = new TECore(engine, index, runOpts);
        Element result = iut.parse(urlConn, null);
        Element content = (Element) result.getElementsByTagName("content")
                .item(0);
        assertNotNull("Expected content element.", content);
        assertTrue("Expected XML content ", content.hasChildNodes());
    }

    @Test
    public void parseTextWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/jabberwocky.txt");
        URLConnection urlConn = url.openConnection();
        TECore iut = new TECore(engine, index, runOpts);
        Element result = iut.parse(urlConn, null);
        Element content = (Element) result.getElementsByTagName("content")
                .item(0);
        assertNotNull("Expected content element.", content);
        assertTrue("Expected text content starting with 'Twas brillig", content
                .getTextContent().startsWith("'Twas brillig"));
    }

}
