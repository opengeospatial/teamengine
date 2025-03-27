package com.occamlab.te.util;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	/**
	 * Zips the directory and all of it's sub directories
	 * @param zipFile the file to write the files into, never <code>null</code>
	 * @param directoryToZip the directory to zip, never <code>null</code>
	 * @throws Exception if the zip file could not be created
	 * @throws IllegalArgumentException if the directoryToZip is not a directory
	 */
	public static void zipDir(File zipFile, File directoryToZip) throws Exception {
		if (!directoryToZip.isDirectory()) {
			throw new IllegalArgumentException("Directory to zip is not a directory");
		}
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
			File parentDir = new File(directoryToZip.toURI().resolve(".."));
			for (File file : directoryToZip.listFiles()) {
				zip(parentDir, file, out);
			}
			out.flush();
			out.close();
		}
		catch (IOException e) {
			throw new Exception(e.getMessage());
		}

	}

	private static void zip(File parentDir, File fileOrDirectoryToZip, ZipOutputStream out) throws IOException {
		String filePath = parentDir.toURI().relativize(fileOrDirectoryToZip.toURI()).getPath();
		if (fileOrDirectoryToZip.isDirectory()) {
			for (File file : fileOrDirectoryToZip.listFiles()) {
				zip(parentDir, file, out);
			}
		}
		else {
			out.putNextEntry(new ZipEntry(filePath));
			FileInputStream in = new FileInputStream(fileOrDirectoryToZip);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.closeEntry();
			in.close();
		}
	}

}
