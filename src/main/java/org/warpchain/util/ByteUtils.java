package org.warpchain.util;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ByteUtils {

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public static byte[] emptyByteArray() {
		return EMPTY_BYTE_ARRAY;
	}

	public static byte[] stringToBytes(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public static String bytesToString(byte[] bs) {
		return new String(bs, StandardCharsets.UTF_8);
	}

	public static String toHexString(byte[] bs) {
		return toHexString(bs, 0, bs.length);
	}

	public static String toHexString(byte[] bs, int offset, int length) {
		Objects.checkFromIndexSize(offset, bs.length, length);
		if (bs.length == 0) {
			return "";
		}
		if (bs.length == 1) {
			return toHexString(bs[offset]);
		}
		StringBuilder sb = new StringBuilder(bs.length * 2);
		for (int i = offset; i < offset + length; i++) {
			sb.append(toHexString(bs[i]));
		}
		return sb.toString();
	}

	public static String toHexString(byte b) {
		return BYTE_TO_HEX[b & 0xff];
	}

	public static byte[] copy(byte[] bs) {
		Objects.requireNonNull(bs, "byte array is null");
		if (bs.length == 0) {
			return bs; // no need copy zero-length array
		}
		byte[] buffer = new byte[bs.length];
		if (bs.length > 0) {
			System.arraycopy(bs, 0, buffer, 0, bs.length);
		}
		return buffer;
	}

	/**
	 * Concat array { 'A', 'B', 'C' } and array { 'X', 'Y' } to new array { 'A',
	 * 'B', 'C', 'X', 'Y' }.
	 * 
	 * @param bs1 the first byte array.
	 * @param bs2 the second byte array.
	 * @return the new byte array.
	 */
	public static byte[] concat(byte[] bs1, byte[] bs2) {
		Objects.requireNonNull(bs1, "byte array is null");
		Objects.requireNonNull(bs2, "byte array is null");
		byte[] buffer = new byte[bs1.length + bs2.length];
		if (bs1.length > 0) {
			System.arraycopy(bs1, 0, buffer, 0, bs1.length);
		}
		if (bs2.length > 0) {
			System.arraycopy(bs2, 0, buffer, bs1.length, bs2.length);
		}
		return buffer;
	}

	/**
	 * Concat byte 'X' and byte 'Y' to new array { 'X', 'Y' }.
	 * 
	 * @param b1 the first byte.
	 * @param b2 the second byte.
	 * @return the new byte array.
	 */
	public static byte[] concat(byte b1, byte b2) {
		byte[] buffer = new byte[2];
		buffer[0] = b1;
		buffer[1] = b2;
		return buffer;
	}

	/**
	 * Concat byte 'X' and array { 'A', 'B', 'C' } to new array { 'X', 'A', 'B', 'C'
	 * }.
	 * 
	 * @param b  the byte.
	 * @param bs the byte array.
	 * @return the new byte array.
	 */
	public static byte[] concat(byte b, byte[] bs) {
		Objects.requireNonNull(bs, "byte array is null");
		byte[] buffer = new byte[bs.length + 1];
		buffer[0] = b;
		if (bs.length > 0) {
			System.arraycopy(bs, 0, buffer, 1, bs.length);
		}
		return buffer;
	}

	/**
	 * Concat array { 'A', 'B', 'C' } and byte 'X' to new array { 'A', 'B', 'C', 'X'
	 * }.
	 * 
	 * @param bs the byte array.
	 * @param b  the byte.
	 * @return the new byte array.
	 */
	public static byte[] concat(byte[] bs, byte b) {
		Objects.requireNonNull(bs, "byte array is null");
		byte[] buffer = new byte[bs.length + 1];
		if (bs.length > 0) {
			System.arraycopy(bs, 0, buffer, 0, bs.length);
		}
		buffer[buffer.length - 1] = b;
		return buffer;
	}

	public static byte[] fromHexString(String hex) {
		if (hex == null) {
			throw new IllegalArgumentException("Hex string is null.");
		}
		if (hex.startsWith("0x")) {
			hex = hex.substring(2);
		}
		if (hex.length() == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		if (hex.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid length of hex string.");
		}
		final int bytes = hex.length() / 2;
		byte[] results = new byte[bytes];
		for (int i = 0; i < bytes; i++) {
			int pos = i << 1;
			char c1 = hex.charAt(pos);
			int n1 = HEX_CHARS.indexOf(c1);
			if (n1 < 0) {
				throw new IllegalArgumentException("Invalid char: " + c1);
			}
			char c2 = hex.charAt(pos + 1);
			int n2 = HEX_CHARS.indexOf(c2);
			if (n2 < 0) {
				throw new IllegalArgumentException("Invalid char: " + c2);
			}
			results[i] = (byte) ((n1 << 4) + n2);
		}
		return results;
	}

	private static final String HEX_CHARS = "0123456789abcdef";
	private static final String[] BYTE_TO_HEX;

	static {
		String[] ss = new String[256];
		for (int i = 0; i < 256; i++) {
			ss[i] = String.format("%02x", i);
		}
		BYTE_TO_HEX = ss;
	}
}
