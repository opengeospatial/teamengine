package com.occamlab.te.html;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.occamlab.te.util.Utils;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class EarlToHtmlTransformation {

	private static final Logger LOGR = Logger.getLogger(EarlToHtmlTransformation.class.getName());

	/**
	 * Transform EARL result into HTML report using XSLT.
	 * @param outputDir
	 */
	public File earlHtmlReport(String outputDir) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL resourceDirUrl = cl.getResource("com/occamlab/te/earl/lib");
		String earlXsl = cl.getResource("com/occamlab/te/earl_html_report.xsl").toString();
		File htmlOutput = new File(outputDir, "result");
		htmlOutput.mkdir();

		File earlResult = findEarlResultFile(outputDir);
		LOGR.log(FINE, "Try to transform earl result file '" + earlResult + "' to directory " + htmlOutput);

		try {
			if (earlResult != null && earlResult.exists()) {
				Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(earlXsl));
				transformer.setParameter("outputDir", htmlOutput);
				File indexHtml = new File(htmlOutput, "index.html");
				indexHtml.createNewFile();

				// Fortify Mod: Make sure the FileOutputStream is closed when we are done
				// with it.
				// transformer.transform( new StreamSource( earlResult ),
				// new StreamResult( new FileOutputStream( indexHtml ) ) );
				FileOutputStream fo = new FileOutputStream(indexHtml);
				transformer.transform(new StreamSource(earlResult), new StreamResult(fo));
				fo.close();
				Utils.copyResourceDir(resourceDirUrl, htmlOutput);
				return htmlOutput;
			}
		}
		catch (Exception e) {
			LOGR.log(SEVERE, "Transformation of EARL to HTML failed.", e);
		}
		return null;
	}

	public File findEarlResultFile(String outputDir) {
		File testngDir = new File(outputDir, "testng");
		if (!testngDir.exists()) {
			return new File(outputDir, "earl-results.rdf");
		}
		else {

			String[] dir = testngDir.list();
			File testngUuidDirectory = new File(testngDir, dir[0]);
			if (testngUuidDirectory.isDirectory()) {
				return new File(testngUuidDirectory, "earl-results.rdf");
			}
		}
		return null;
	}

}
