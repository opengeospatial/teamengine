package com.occamlab.te.web;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* creates the main Config File reading the TE_BASE/scripts config files */
public  class ConfigFileCreator {
	
	public static void create(ServletContext servletContext){
		
		try {
			process(servletContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	 private static void addfiles(File input, ArrayList<File> files) {
         if (input.isDirectory()) {
           ArrayList<File> path = new ArrayList<File>(Arrays.asList(input.listFiles()));
           for (int i = 0; i < path.size(); ++i) {
             if (path.get(i).isDirectory()) {
               addfiles(path.get(i), files);
             }
             if (path.get(i).isFile()) {
               files.add(path.get(i));
             }
           }
         }
         if (input.isFile()) {
           files.add(input);
         }
       }
	 
	private static void process(ServletContext servletContext) throws Exception{ 
	 
	 Element rootorganization = null;
       String path = servletContext.getInitParameter("teConfigFile");
       String directory = path.split("config")[0] + "scripts";
       DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
       Document tournaments = builder.parse(new File(path));
       NodeList ndlScripts = tournaments.getElementsByTagName("scripts");
       Element rootscripts = (Element) ndlScripts.item(0);
       if (null != tournaments.getElementsByTagName("organization").item(1)) {
         Node organization = tournaments.getElementsByTagName("organization").item(1);
         organization.getParentNode().removeChild(organization);
       }
       ArrayList<File> files = new ArrayList<File>();
       addfiles(new File(directory), files);
       int counter = 0;
       for (File file1 : files) {
         if ((file1.getName().contains("config.xml")) && !(file1.getName().contains("config.xml~"))) {
           if (counter == 0) {
             rootorganization = tournaments.createElement("organization");
             rootscripts.appendChild(rootorganization);
             Element rootname = tournaments.createElement("name");
             Document tournament = builder.parse(file1);
             NodeList ndname = tournament.getElementsByTagName("name");
             Node tournamentElement = ndname.item(0);
             rootname.appendChild(tournaments.createTextNode(tournamentElement.getFirstChild().getNodeValue()));
             rootorganization.appendChild(rootname);
             counter = counter + 1;
           }
         }
       }
       for (File file : files) {
         if ((file.getName().contains("config.xml")) && !(file.getName().contains("config.xml~"))) {
           Document tournament = builder.parse(file);
           NodeList ndlst = tournament.getElementsByTagName("standard");
           Node tournamentElement = ndlst.item(0);
           Node firstDocImportedNode = tournaments.adoptNode(tournamentElement);
           rootorganization.appendChild(firstDocImportedNode);
         }
       }
       TransformerFactory transformerFactory = TransformerFactory.newInstance();
       Transformer transformer = transformerFactory.newTransformer();
       transformer.setOutputProperty(OutputKeys.INDENT, "yes");
       transformer.transform(new DOMSource(tournaments), new StreamResult(new FileOutputStream(path)));

       File siteFolder = new File(path.split("config")[0] + "resources/site");
       if (siteFolder.exists()){
         String teLocalPath = servletContext.getRealPath(File.separator);
         File siteFolderInTE = new File (teLocalPath+ "site");
         FileUtils.copyDirectory(siteFolder, siteFolderInTE);
       }
	}

}
