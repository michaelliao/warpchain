package org.warpchain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static byte[] sha256(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return md.digest(input);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] dsha256(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] round1 = md.digest(input);
			md.reset();
			return md.digest(round1);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
