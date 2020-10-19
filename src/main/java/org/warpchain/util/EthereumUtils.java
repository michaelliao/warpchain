package org.warpchain.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.bouncycastle.math.ec.ECPoint;
import org.warpchain.crypto.ECKey;

public class EthereumUtils {

	public static String exportPrivateKey(BigInteger privateKey) {
		if (privateKey.signum() <= 0 || privateKey.compareTo(ECKey.getN()) >= 0) {
			throw new IllegalArgumentException("Invalid private key.");
		}
		byte[] key = ByteUtils.bigIntegerToBytes(privateKey, 32);
		return "0x" + ByteUtils.toHexString(key);
	}

	public static String exportPrivateKey(ECKey key) {
		BigInteger privateKey = key.getPrivateKey();
		if (privateKey == null) {
			throw new IllegalArgumentException("No private key.");
		}
		return exportPrivateKey(privateKey);
	}

	public static ECKey fromPrivateKey(String hexPrivateKey) {
		if (hexPrivateKey == null) {
			throw new IllegalArgumentException("Invalid private key.");
		}
		if (hexPrivateKey.startsWith("0x")) {
			hexPrivateKey = hexPrivateKey.substring(2);
		}
		if (hexPrivateKey.length() != 64) {
			throw new IllegalArgumentException("Invalid private key.");
		}
		return ECKey.fromPrivateKey(new BigInteger(hexPrivateKey, 16));
	}

	public static String toAddress(ECKey key) {
		return toAddress(key.getPublicKeyAsBytes());
	}

	public static String toAddress(BigInteger publicKey) {
		if (publicKey == null || publicKey.signum() <= 0) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		return toAddress(ByteUtils.bigIntegerToBytes(publicKey, 33));
	}

	public static String toAddress(byte[] pubKey) {
		if (pubKey == null) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		if (pubKey.length == 33 && (pubKey[0] == 0x02 || pubKey[0] == 0x03)) {
			// compressed public key to uncompressed public key:
			pubKey = toUncompressed(pubKey);
		} else if (pubKey.length != 65 || pubKey[0] != 0x04) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		// IMPORTANT: Only hash the 64 bytes public key (remove first prefix 0x04):
		byte[] pubKeyHash = HashUtils.keccak256(Arrays.copyOfRange(pubKey, 1, 65));
		String pubKeyHashHex = ByteUtils.toHexString(pubKeyHash);
		String addressHex = pubKeyHashHex.substring(pubKeyHashHex.length() - 40);
		String addressHash = ByteUtils
				.toHexString(HashUtils.keccak256(addressHex.getBytes(StandardCharsets.ISO_8859_1)));
		StringBuilder sb = new StringBuilder(42);
		sb.append("0x");
		for (int i = 0; i < 40; i++) {
			char ch = addressHex.charAt(i);
			if (ch >= 'a' && addressHash.charAt(i) >= '8') {
				sb.append(Character.toUpperCase(ch));
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	static byte[] toUncompressed(byte[] compressed) {
		ECPoint point = ECKey.getCurve().decodePoint(compressed).normalize();
		byte[] x = point.getXCoord().getEncoded();
		byte[] y = point.getYCoord().getEncoded();
		// concat 0x04, x, and y:
		return ByteUtils.concat(UNCOMPRESSED_PREFIX, x, y);
	}

	private static final byte[] UNCOMPRESSED_PREFIX = new byte[] { 0x04 };
}
