package com.occamlab.te;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SetupOptionsTest {

    static String TE_BASE = "TE_BASE";
    static File baseDir;
    static boolean createdDir;
    static String prevTEBase;

    @BeforeClass
    public static void setSystemProperty() {
        baseDir = new File(System.getProperty("user.home"), UUID.randomUUID().toString());
        createdDir = baseDir.mkdir();
        prevTEBase = System.setProperty(TE_BASE, baseDir.getAbsolutePath());
    }

    @AfterClass
    public static void restoreSystemProperty() {
        if (createdDir) {
            File dir = new File(System.getProperty(TE_BASE));
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                children[i].delete();
            }
            dir.delete();
        }
        if (null != prevTEBase) {
            System.setProperty(TE_BASE, prevTEBase);
        } else {
            System.clearProperty(TE_BASE);
        }
        System.clearProperty("Record");
    }

    @Test
    public void getBaseDir() {
        File baseDir = SetupOptions.getBaseConfigDirectory();
        Assert.assertNotNull("File reference is null.", baseDir);
        Assert.assertTrue("Expected a directory at " + baseDir.getAbsolutePath(), baseDir.isDirectory());
    }

    @Test
    public void getWorkDir() {
        SetupOptions iut = new SetupOptions();
        File workDir = iut.getWorkDir();
        Assert.assertTrue("Expected a directory at " + workDir.getAbsolutePath(), workDir.isDirectory());
    }

    @Test
    public void getScriptSources() {
        SetupOptions iut = new SetupOptions();
        List<File> sources = iut.getSources();
        Assert.assertEquals("Unexpected size", 0, sources.size());
    }

    @Test
    public void testRecordingInfoWithoutConfig() throws ParserConfigurationException, SAXException, IOException {
        File configFile = new File(baseDir, "config.xml");
        assertFalse(configFile.exists());
        assertFalse(SetupOptions.recordingInfo("foo"));
    }
}
