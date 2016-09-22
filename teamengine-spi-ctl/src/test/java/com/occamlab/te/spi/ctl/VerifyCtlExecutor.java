package com.occamlab.te.spi.ctl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;

public class VerifyCtlExecutor {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass() throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void extractTwoArguments() throws SAXException, IOException {
        SetupOptions opts = new SetupOptions();
        CtlExecutor iut = new CtlExecutor(opts);
        Document args = docBuilder.parse(getClass().getResourceAsStream("/test-run-props.xml"));
        RuntimeOptions runOpts = iut.extractTestRunArguments(args);
        List<String> params = runOpts.getParams();
        assertEquals("Unexpected number of parameters.", 2, params.size());
        String nvp = params.get(0);
        assertEquals("Expected parameter format: {name}={value}.", 2, nvp.split("=").length);
    }

    @Test
    public void extractNoArguments() throws SAXException, IOException {
        SetupOptions opts = new SetupOptions();
        CtlExecutor iut = new CtlExecutor(opts);
        Document args = docBuilder.parse(getClass().getResourceAsStream("/test-run-props-empty.xml"));
        RuntimeOptions runOpts = iut.extractTestRunArguments(args);
        List<String> params = runOpts.getParams();
        assertEquals("Unexpected number of parameters.", 0, params.size());
    }
}
