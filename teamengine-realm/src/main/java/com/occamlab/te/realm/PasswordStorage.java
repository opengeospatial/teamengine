/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.occamlab.te.realm;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.xml.bind.DatatypeConverter;

/**
 * Creates and verifies a password digest using the PBKDF2 function. Original code by
 * <a href="https://github.com/defuse">Taylor Hornby</a> and other contributors is
 * licensed under the terms of a derivative BSD 2-Clause License.
 *
 * <p>
 * The hash format consists of five fields separated by the colon (':') character:
 * <code>algorithm:iterations:hashSize:salt:hash</code>.
 * </p>
 * <ul>
 * <li><em>algorithm</em> - the name of the cryptographic hash function ("sha1")</li>
 * <li><em>iterations</em> - the number of PBKDF2 iterations ("64000")</li>
 * <li><em>hashSize</em> - the length, in bytes, of the hash field (after decoding)</li>
 * <li><em>salt</em> - the salt (base64 encoded)</li>
 * <li><em>hash</em> - the PBKDF2 output (base64 encoded)</li>
 * </ul>
 *
 * @see <a href="https://github.com/defuse/password-hashing">Secure Password Storage
 * v2.0</a>
 */
public class PasswordStorage {

	@SuppressWarnings("serial")
	static public class InvalidHashException extends Exception {

		public InvalidHashException(String message) {
			super(message);
		}

		public InvalidHashException(String message, Throwable source) {
			super(message, source);
		}

	}

	@SuppressWarnings("serial")
	static public class CannotPerformOperationException extends Exception {

		public CannotPerformOperationException(String message) {
			super(message);
		}

		public CannotPerformOperationException(String message, Throwable source) {
			super(message, source);
		}

	}

	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

	// These constants may be changed without breaking existing hashes.
	public static final int SALT_BYTE_SIZE = 24;

	public static final int HASH_BYTE_SIZE = 18;

	public static final int PBKDF2_ITERATIONS = 64000;

	// These constants define the encoding and may not be changed.
	public static final int HASH_SECTIONS = 5;

	public static final int HASH_ALGORITHM_INDEX = 0;

	public static final int ITERATION_INDEX = 1;

	public static final int HASH_SIZE_INDEX = 2;

	public static final int SALT_INDEX = 3;

	public static final int PBKDF2_INDEX = 4;

	/**
	 * Creates a password digest using the PBKDF2 key derivation function (64,000
	 * iterations of SHA1 by default) with a cryptographically-random salt.
	 * @param password The submitted password.
	 * @return A hash value in the following format:
	 * algorithm:iterations:hashSize:salt:hash.
	 * @throws CannotPerformOperationException If the hash cannot be created for some
	 * reason (e.g. random number generator doesn't work).
	 */
	public static String createHash(String password) throws CannotPerformOperationException {
		return createHash(password.toCharArray());
	}

	public static String createHash(char[] password) throws CannotPerformOperationException {
		// Generate a random salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BYTE_SIZE];
		random.nextBytes(salt);

		// Hash the password
		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
		int hashSize = hash.length;

		// format: algorithm:iterations:hashSize:salt:hash
		return "sha1:" + PBKDF2_ITERATIONS + ":" + hashSize + ":" + toBase64(salt) + ":" + toBase64(hash);
	}

	/**
	 * Checks a submitted password against the expected hash value.
	 * @param password The submitted password.
	 * @param correctHash The expected hash value.
	 * @return true if the provided password is correct; false otherwise.
	 * @throws CannotPerformOperationException If password verification failed for some
	 * reason.
	 * @throws InvalidHashException If correctHash was somehow corrupted.
	 */
	public static boolean verifyPassword(String password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException {
		return verifyPassword(password.toCharArray(), correctHash);
	}

	public static boolean verifyPassword(char[] password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException {
		// Decode the hash into its parameters
		String[] params = correctHash.split(":");
		if (params.length != HASH_SECTIONS) {
			throw new InvalidHashException("Fields are missing from the password hash.");
		}

		// Currently, Java only supports SHA1.
		if (!params[HASH_ALGORITHM_INDEX].equals("sha1")) {
			throw new CannotPerformOperationException("Unsupported hash type.");
		}

		int iterations = 0;
		try {
			iterations = Integer.parseInt(params[ITERATION_INDEX]);
		}
		catch (NumberFormatException ex) {
			throw new InvalidHashException("Could not parse the iteration count as an integer.", ex);
		}

		if (iterations < 1) {
			throw new InvalidHashException("Invalid number of iterations. Must be >= 1.");
		}

		byte[] salt = null;
		try {
			salt = fromBase64(params[SALT_INDEX]);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidHashException("Base64 decoding of salt failed.", ex);
		}

		byte[] hash = null;
		try {
			hash = fromBase64(params[PBKDF2_INDEX]);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidHashException("Base64 decoding of pbkdf2 output failed.", ex);
		}

		int storedHashSize = 0;
		try {
			storedHashSize = Integer.parseInt(params[HASH_SIZE_INDEX]);
		}
		catch (NumberFormatException ex) {
			throw new InvalidHashException("Could not parse the hash size as an integer.", ex);
		}

		if (storedHashSize != hash.length) {
			throw new InvalidHashException("Hash length doesn't match stored hash length.");
		}

		// Compute the hash of the provided password, using the same salt,
		// iteration count, and hash length
		byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
		// Compare the hashes in constant time. The password is correct if
		// both hashes match.
		return slowEquals(hash, testHash);
	}

	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		for (int i = 0; i < a.length && i < b.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
			throws CannotPerformOperationException {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
			return skf.generateSecret(spec).getEncoded();
		}
		catch (NoSuchAlgorithmException ex) {
			throw new CannotPerformOperationException("Hash algorithm not supported.", ex);
		}
		catch (InvalidKeySpecException ex) {
			throw new CannotPerformOperationException("Invalid key spec.", ex);
		}
	}

	private static byte[] fromBase64(String hex) throws IllegalArgumentException {
		return DatatypeConverter.parseBase64Binary(hex);
	}

	private static String toBase64(byte[] array) {
		return DatatypeConverter.printBase64Binary(array);
	}

}
