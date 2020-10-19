package org.warpchain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

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

	public static byte[] ripeMd160(byte[] input) {
		MessageDigest digest = new RIPEMD160.Digest();
		digest.update(input);
		return digest.digest();
	}

	public static byte[] keccak256(byte[] input) {
		Keccak.Digest256 digest = new Keccak.Digest256();
		digest.update(input);
		return digest.digest();
	}
}
