package org.warpchain.util;

import java.math.BigInteger;
import java.util.Arrays;

import org.warpchain.crypto.ECKey;

public class BitcoinUtils {

	public static String toWIF(BigInteger privateKey) {
		if (privateKey.signum() <= 0 || privateKey.compareTo(ECKey.getN()) >= 0) {
			throw new IllegalArgumentException("Invalid private key.");
		}
		byte[] key = ByteUtils.bigIntegerToBytes(privateKey, 32);
		byte[] extendedKey = ByteUtils.concat(PRIVATE_KEY_PREFIX_ARRAY, key, PRIVATE_KEY_SUFFIX_ARRAY);
		byte[] hash = HashUtils.dsha256(extendedKey);
		byte[] checksum = Arrays.copyOfRange(hash, 0, 4);
		byte[] extendedKeyWithChecksum = ByteUtils.concat(extendedKey, checksum);
		return Base58Utils.encode(extendedKeyWithChecksum);
	}

	public static String toWIF(ECKey key) {
		BigInteger privateKey = key.getPrivateKey();
		if (privateKey == null) {
			throw new IllegalArgumentException("No private key.");
		}
		return toWIF(privateKey);
	}

	public static ECKey fromWIF(String wif) {
		char first = wif.charAt(0);
		if (first != 'L' && first != 'K') {
			throw new IllegalArgumentException("Invalid WIF: must start with L or K.");
		}
		byte[] data = Base58Utils.decodeChecked(wif);
		if (data[0] != PRIVATE_KEY_PREFIX) {
			throw new IllegalArgumentException("Invalid WIF: leading byte is not 0x80.");
		}
		if (data[data.length - 1] != PRIVATE_KEY_SUFFIX) {
			throw new IllegalArgumentException("Invalid WIF: ending byte is not 0x01.");
		}
		// remove first 0x80 and last 0x01:
		byte[] privateKey = Arrays.copyOfRange(data, 1, data.length - 1);
		return ECKey.fromPrivateKey(new BigInteger(1, privateKey));
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
		if (pubKey == null || pubKey.length != 33 || (pubKey[0] != 0x02 && pubKey[0] != 0x03)) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		byte[] hash160 = HashUtils.ripeMd160(HashUtils.sha256(pubKey));
		byte[] hashWithNetworkId = ByteUtils.concat(NETWORK_ID_ARRAY, hash160);
		byte[] checksum = HashUtils.dsha256(hashWithNetworkId);
		byte[] address = ByteUtils.concat(hashWithNetworkId, Arrays.copyOfRange(checksum, 0, 4));
		return Base58Utils.encode(address);
	}

	public static String toBech32Address(ECKey key) {
		return toBech32Address(key.getPublicKeyAsBytes());
	}

	public static String toBech32Address(BigInteger publicKey) {
		if (publicKey == null || publicKey.signum() <= 0) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		return toBech32Address(ByteUtils.bigIntegerToBytes(publicKey, 33));
	}

	public static String toBech32Address(byte[] pubKey) {
		if (pubKey == null || pubKey.length != 33 || (pubKey[0] != 0x02 && pubKey[0] != 0x03)) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		byte[] hash160 = HashUtils.ripeMd160(HashUtils.sha256(pubKey));
		byte[] b32data = Bech32Utils.convertBits(hash160, 8, 5, false);
		byte[] b32dataWithVersion = new byte[b32data.length + 1];
		b32dataWithVersion[0] = 0;
		System.arraycopy(b32data, 0, b32dataWithVersion, 1, b32data.length);
		return Bech32Utils.encode("bc", b32dataWithVersion);
	}

	/**
	 * Network ID: 0x00 = main network.
	 */
	private static final byte NETWORK_ID = 0x00;
	private static final byte[] NETWORK_ID_ARRAY = { NETWORK_ID };

	/**
	 * Private key prefix: 0x80.
	 */
	private static final byte PRIVATE_KEY_PREFIX = (byte) 0x80;
	private static final byte[] PRIVATE_KEY_PREFIX_ARRAY = { PRIVATE_KEY_PREFIX };

	private static final byte PRIVATE_KEY_SUFFIX = 0x01;
	private static final byte[] PRIVATE_KEY_SUFFIX_ARRAY = { PRIVATE_KEY_SUFFIX };
}
