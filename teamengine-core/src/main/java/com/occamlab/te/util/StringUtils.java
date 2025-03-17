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

/**
 * Utilities to do more advanced processing on Strings than what is available in XSLT.
 *
 * @author jparrpearson
 */
public class StringUtils {

	/**
	 * Extracts the path from a file path string (everything before the last '/').
	 * @param filePath the string to parse
	 * @return the path without the filename, or the original path/string if no occurance
	 * of a dir seperator
	 */
	public static String getPathFromString(String filePath) {
		String newPath = filePath;

		int forwardInd = filePath.lastIndexOf("/");
		int backInd = filePath.lastIndexOf("\\");

		if (forwardInd > backInd) {
			newPath = filePath.substring(0, forwardInd + 1);
		}
		else {
			newPath = filePath.substring(0, backInd + 1);
		}

		// still original if no occurance of "/" or "\"
		return newPath;
	}

	/**
	 * Extracts the filename from a path.
	 * @param filePath the string to parse
	 * @return the path containing only the filename itself
	 */
	public static String getFilenameFromString(String filePath) {
		String newPath = filePath;

		int forwardInd = filePath.lastIndexOf("/");
		int backInd = filePath.lastIndexOf("\\");

		if (forwardInd > backInd) {
			newPath = filePath.substring(forwardInd + 1);
		}
		else {
			newPath = filePath.substring(backInd + 1);
		}

		// still original if no occurance of "/" or "\"
		return newPath;
	}

	// Replaces all occurences of match in str with replacement
	public static String replaceAll(String str, String match, String replacement) {
		String newStr = str;
		int i = newStr.indexOf(match);
		while (i >= 0) {
			newStr = newStr.substring(0, i) + replacement + newStr.substring(i + match.length());
			i = newStr.indexOf(match);
		}
		return newStr;
	}

	public static String escapeXML(String str) {
		String ret = str;
		ret = ret.replaceAll("\"", "&quot;");
		ret = ret.replaceAll("&", "&amp;");
		ret = ret.replaceAll("<", "&lt;");
		ret = ret.replaceAll(">", "&gt;");
		ret = ret.replaceAll("'", "&apos;");
		return ret;
	}

}
