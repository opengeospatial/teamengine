package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.occamlab.te.Engine;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;

public class HttpParserTest {

    private static final String DOCBOOK_NS = "http://docbook.org/ns/docbook";
    private static TECore teCore;
    private StringWriter strWriter;
    private PrintWriter logger;

    @BeforeClass
    public static void setUpClass() throws Exception {
        teCore = new TECore(new Engine(), new Index(), new RuntimeOptions());
    }

    @Before
    public void initWriter() {
        this.strWriter = new StringWriter();
        this.logger = new PrintWriter(strWriter);
    }

    @Test
    public void parseXmlEntityWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/article.xml");
        URLConnection urlConn = url.openConnection();
        Document rsp = HTTPParser.parse(urlConn, null, logger, teCore);
        assertNotNull("Expected article element.",
                rsp.getElementsByTagNameNS(DOCBOOK_NS, "article").item(0));
        assertTrue("Expected empty log.", strWriter.getBuffer().length() == 0);
    }

    @Test
    public void parseTextEntityWithNullInstruction() throws Throwable {
        URL url = this.getClass().getResource("/jabberwocky.txt");
        URLConnection urlConn = url.openConnection();
        Document rsp = HTTPParser.parse(urlConn, null, logger, teCore);
        Element content = (Element) rsp.getElementsByTagName("content").item(0);
        assertNotNull("Expected content element.", content);
        assertTrue("Expected text content starting with 'Twas brillig", content
                .getTextContent().startsWith("'Twas brillig"));
    }

    static String writeNodeToString(Node node) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException {
        DOMImplementationRegistry registry = DOMImplementationRegistry
                .newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry
                .getDOMImplementation("LS");
        LSSerializer serializer = impl.createLSSerializer();
        return serializer.writeToString(node);
    }
}
