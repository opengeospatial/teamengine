package com.occamlab.te.form;

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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ImageHandler {

	private static final Logger LOG = Logger.getLogger(ImageHandler.class.getName());

	private final File testLogDir;

	private final String sessionId;

	/**
	 * @param testLogDir the directory of the log files, never <code>null</code>
	 * @param sessionId the id of the current session, never <code>null</code>
	 */
	public ImageHandler(File testLogDir, String sessionId) {
		this.testLogDir = testLogDir;
		this.sessionId = sessionId;
	}

	/**
	 * Save images into session if the ets is interactive and it contains the images.
	 * @param form the form element with the images, never <code>null</code>
	 * @throws IOException if the images could not be read/write
	 */
	public void saveImages(Element form) throws IOException {
		List<Element> images = DomUtils.getElementsByTagName(form, "img");
		int imageCount = 1;
		for (Element image : images) {
			if (image.hasAttribute("src")) {
				String src = image.getAttribute("src");
				if (src.startsWith("http")) {
					String imageFormat = parseImageFormat(src);
					String imgName = parseImageName();
					File target = createImageFile(imageCount, imageFormat, imgName);
					saveImage(src, imageFormat, target);
					imageCount++;
				}
			}
		}
	}

	private void saveImage(String src, String imageFormat, File file) {
		try {
			URL url = new URL(src);
			BufferedImage img = ImageIO.read(url);
			ImageIO.write(img, imageFormat, file);
		}
		catch (Exception e) {
			LOG.warning("Could not write image " + src + " to " + file);
		}
	}

	private File createImageFile(int imageCount, String imageFormat, String imgName) {
		String downloadPath = testLogDir + File.separator + sessionId + File.separator + "images" + File.separator
				+ imgName + imageCount + "." + imageFormat;
		File target = new File(downloadPath);
		if (!target.exists()) {
			target.getParentFile().mkdirs();
		}
		return target;
	}

	private String parseImageFormat(String src) throws MalformedURLException, UnsupportedEncodingException {
		if (src.contains("FORMAT")) {
			URL url = new URL(URLDecoder.decode(src, StandardCharsets.UTF_8));
			String params[] = url.getQuery().split("&");
			for (String param : params) {
				if (param.split("=")[0].equalsIgnoreCase("FORMAT")) {
					return param.split("=")[1].split("/")[1];
				}
			}
		}
		return "PNG";
	}

	private String parseImageName() {
		String testName = System.getProperty("TestName");
		if (testName.contains(" ")) {
			return testName.substring(testName.indexOf(":") + 1, testName.indexOf(" "));
		}
		return testName;
	}

}
