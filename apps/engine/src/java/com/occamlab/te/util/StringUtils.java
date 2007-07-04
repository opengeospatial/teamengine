package com.occamlab.te.util;

/**
 * Utilities to do more advanced processing on Strings than what is available in XSLT.
 *
 * @author jparrpearson
 */
public class StringUtils {

    /**
     * Extracts the path from a file path string (everything before the last '/').
     *
     * @param filePath
     *            the string to parse
     * @return the path without the filename, or the original path/string if
     *		no occurance of a dir seperator
     */
    public static String getPathFromString(String filePath) {
	String newPath = filePath;

	int forwardInd = filePath.lastIndexOf("/");
	int backInd = filePath.lastIndexOf("\\");

	if (forwardInd > backInd) {
		newPath = filePath.substring(0, forwardInd+1);
	}
	else {
		newPath = filePath.substring(0, backInd+1);
	}

	// still original if no occurance of "/" or "\"
	return newPath;
    }

    /**
     * Extracts the filename from a path.
     *
     * @param filePath
     *            the string to parse
     * @return the path containing only the filename itself
     */
    public static String getFilenameFromString(String filePath) {
	String newPath = filePath;

	int forwardInd = filePath.lastIndexOf("/");
	int backInd = filePath.lastIndexOf("\\");

	if (forwardInd > backInd) {
		newPath = filePath.substring(forwardInd+1);
	}
	else {
		newPath = filePath.substring(backInd+1);
	}

	// still original if no occurance of "/" or "\"
	return newPath;
    }

}