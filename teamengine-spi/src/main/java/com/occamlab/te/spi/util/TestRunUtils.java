package com.occamlab.te.spi.util;

/*-
 * #%L
 * TEAM Engine - Service Providers
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.occamlab.te.spi.jaxrs.TestSuiteController;

public class TestRunUtils {

	public static String getUserName(List<String> authCredentials) {
		String authCredential = authCredentials.get(0);
		final String encodedUserPassword = authCredential.replaceFirst("Basic" + " ", "");
		String usernameAndPassword = null;

		byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
		usernameAndPassword = new String(decodedBytes, StandardCharsets.UTF_8);

		final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
		final String username = tokenizer.nextToken();
		return username;
	}

	public static void save(File outputDir, String sessionId, String sourcesId) {
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}
		File sessionDir = new File(outputDir, sessionId);
		sessionDir.mkdir();

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);

		PrintStream out = null;
		try {
			out = new PrintStream(new File(sessionDir, "session.xml"));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.println(
				"<session id=\"" + sessionId + "\" sourcesId=\"" + sourcesId + "\" date=\"" + currentDate + "\"  >");
		out.println("</session>");
		out.close();
	}

	public static String getSourcesId(TestSuiteController controller) {
		return "OGC_" + controller.getTitle() + "_" + controller.getVersion();
	}

}
