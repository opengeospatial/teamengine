package com.occamlab.te.spi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;

/**
 * 
 * This class is used to process HTML result. 
 * It will transform EARL result into HTML report 
 * and return the HTML result with zip file.
 *
 */
public class HtmlReport {

	/**
	 * This method will return the HTML result with zip file.
	 * @param outputDirectory
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static File getHtmlResultZip (String outputDirectory) throws FileNotFoundException{
		File htmlResultFile = null;
		File htmlResult = earlHtmlReport(outputDirectory);
		htmlResultFile = new File(outputDirectory, "result.zip");
//        File resultDir = new File(outputDirectory, "result");
        try {
			zipDir(htmlResultFile, htmlResult);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return htmlResultFile;
		
	}
	
	/**
     * Convert EARL result into HTML report.
     * 
     * @param outputDir
     * 		Location of the test result.
     * @return 
     * 		Return the output file.
     * @throws FileNotFoundException
     * 		Throws exception if file is not available. 
     */
    public static File earlHtmlReport(String outputDir) throws FileNotFoundException {

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String resourceDir = cl.getResource("com/occamlab/te/earl/lib").getPath();
		String earlXsl = cl.getResource("com/occamlab/te/earl_html_report.xsl").toString();
		File earlResult = null;
		File htmlOutput = null;
	
			htmlOutput = new File(outputDir,"result");
			earlResult = new File(outputDir, "earl-results.rdf");

		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(new StreamSource(earlXsl));
			transformer.setParameter("outputDir", htmlOutput);
			transformer.transform(new StreamSource(earlResult),
					new StreamResult(new FileOutputStream("index.html")));
			FileUtils.copyDirectory(new File(resourceDir), htmlOutput);
		} catch (Exception e) {
			System.out.println(e.getMessage() + e.getCause());
		}
		if(!htmlOutput.exists()){
			throw new FileNotFoundException("HTML results not found at " + htmlOutput.getAbsolutePath());
		}
		return htmlOutput;
	}
    
    /**
     *  Zips the directory and all of it's sub directories
     * @param zipFile
     * 			Path of zip file.
     * @param dirObj
     * 			Location of test result
     * @throws Exception
     *  		It will throw this exception if file not found.
     */
    public static void zipDir(File zipFile, File dirObj) throws Exception {
        // File dirObj = new File(dir);
        if (!dirObj.isDirectory()) {
            System.err.println(dirObj.getName() + " is not a directory");
            System.exit(1);
        }

        try {

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipFile));

            System.out.println("Creating : " + zipFile);

            addDir(dirObj, out);
            // Complete the ZIP file
            out.close();

        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    /**
     *  Add directory to zip file
     * @param dirObj
     * @param out
     * @throws IOException
     */
    private static void addDir(File dirObj, ZipOutputStream out)
            throws IOException {
        File[] dirList = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < dirList.length; i++) {
            if (dirList[i].isDirectory()) {
                addDir(dirList[i], out);
                continue;
            }

            FileInputStream in = new FileInputStream(
                    dirList[i].getAbsolutePath());
            System.out.println(" Adding: " + dirList[i].getAbsolutePath());

            out.putNextEntry(new ZipEntry(dirList[i].getAbsolutePath()));

            // Transfer from the file to the ZIP file
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
            in.close();
        }
    }
}
