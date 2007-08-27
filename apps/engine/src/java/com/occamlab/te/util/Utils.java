package com.occamlab.te.util;

import java.util.Random;

import java.security.MessageDigest;

/**
 * Provides various utility methods (general collection).
 * 
 * @author jparrpearson
 */
public class Utils {

	/**
	 * Returns a random string of a certain length
	 *
	 */
	public static String randomString(int len, Random random) {
		if (len < 1) {
			return "";
		}
		int start = ' ';
		int end = 'z' + 1;

		StringBuffer buffer = new StringBuffer();
		int gap = end - start;

		while (len-- != 0) {
			char ch;
			ch = (char) (random.nextInt(gap) + start);

			if (Character.isLetterOrDigit(ch)) {
				buffer.append(ch);
			} else {
				len++;
			}
		}
		return buffer.toString();
	}

	/**
	 * Uses MD5 to create a hash value for the given String
	 *
	 */
	public static String generateMD5(String text) {
		byte[] md5hash = null;
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md5hash = new byte[8];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			md5hash = md.digest();
		} catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage());
			return "";
		}
		return convertToHex(md5hash);
	}

	/**
	 * Converts a String to Hex digits
	 *
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				}
				else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		}
		return buf.toString();
	}

}