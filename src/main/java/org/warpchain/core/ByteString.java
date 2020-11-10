package org.warpchain.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable byte array.
 * 
 * @author liaoxuefeng
 */
public class ByteString {

	private static final byte[] EMPTY_BYTES = new byte[0];
	public static final ByteString EMPTY = new ByteString(EMPTY_BYTES, 0, 0);

	private final byte[] value;
	private final int offset;
	private final int count;
	private int hash;

	private ByteString(byte[] value, int offset, int count) {
		this.value = value;
		this.offset = offset;
		this.count = count;
	}

	public ByteString(byte[] value) {
		this(Arrays.copyOf(value, value.length), 0, value.length);
	}

	public static ByteString unsafe(byte[] value) {
		return new ByteString(value, 0, value.length);
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public int length() {
		return count;
	}

	public ByteString substring(int beginIndex) {
		return substring(beginIndex, this.count);
	}

	public ByteString substring(int beginIndex, int endIndex) {
		if (beginIndex == 0 && endIndex == this.count) {
			return this;
		}
		Objects.checkFromToIndex(beginIndex, endIndex, this.count);
		return new ByteString(this.value, this.offset + beginIndex, endIndex - beginIndex);
	}

	public int intValueAt(int index) {
		return valueAt(index) & 0xff;
	}

	public byte valueAt(int index) {
		Objects.checkIndex(index, this.count);
		return this.value[this.offset + index];
	}

	public int bitAt(int bitIndex) {
		byte b = valueAt(bitIndex >> 3);
		return (b & BIT_INDEX_MASK[bitIndex % 8]) == 0 ? 0 : 1;
	}

	private static int[] BIT_INDEX_MASK = { //
			0b10000000, // bit at 0
			0b01000000, // bit at 1
			0b00100000, // bit at 2
			0b00010000, // bit at 3
			0b00001000, // bit at 4
			0b00000100, // bit at 5
			0b00000010, // bit at 6
			0b00000001 // bit at 7
	};

	/**
	 * Tests if this string starts with the specified prefix.
	 *
	 * @param prefix the prefix.
	 * @return <code>true</code> if the character sequence represented by the
	 *         argument is a prefix of the character sequence represented by this
	 *         string; <code>false</code> otherwise.
	 */
	public boolean startsWith(ByteString prefix) {
		return startsWith(prefix, 0);
	}

	/**
	 * Tests if the substring of this string beginning at the specified index starts
	 * with the specified prefix.
	 * 
	 * @param prefix  prefix the prefix.
	 * @param toffset toffset where to begin looking in this string.
	 * @return <code>true</code> if the character sequence represented by the
	 *         argument is a prefix of the substring of this object starting at
	 *         index <code>toffset</code>; <code>false</code> otherwise.
	 */
	public boolean startsWith(ByteString prefix, int toffset) {
		byte[] ta = this.value;
		int to = this.offset + toffset;
		byte[] pa = prefix.value;
		int po = prefix.offset;
		int pc = prefix.count;
		// Note: toffset might be near -1>>>1.
		if ((toffset < 0) || (toffset > count - pc)) {
			return false;
		}
		while (--pc >= 0) {
			if (ta[to++] != pa[po++]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get shared prefix as many as possible. Example: "1a2b3c" and "1a2f4d" share
	 * "1a2".
	 */
	public static ByteString sharedPrefix(ByteString s1, ByteString s2) {
		int max = Math.min(s1.length(), s2.length());
		if (max == 0) {
			return EMPTY;
		}
		int n = 0;
		while (n < max) {
			if (s1.valueAt(n) != s2.valueAt(n)) {
				return s1.substring(0, n);
			}
			n++;
		}
		return s1.substring(0, max);
	}

	/**
	 * Return a copy of byte array that represent this ByteString.
	 * 
	 * @return Byte array.
	 */
	public byte[] toBytes() {
		if (this.count == 0) {
			return EMPTY_BYTES;
		}
		return Arrays.copyOfRange(this.value, this.offset, this.offset + this.count);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof ByteString) {
			ByteString bs = (ByteString) o;
			return Arrays.equals(this.value, this.offset, this.offset + this.count, bs.value, bs.offset,
					bs.offset + bs.count);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = this.hash;
		if (h == 0 && this.count > 0) {
			h = 1;
			int off = this.offset;
			for (int i = 0; i < this.count; i++) {
				h = 31 * h + this.value[off++];
			}
			this.hash = h;
		}
		return h;
	}

	@Override
	public String toString() {
		if (this.count == 0) {
			return "";
		}
		var sb = new StringBuilder(this.count * 3 + 2);
		sb.append('[');
		int off = this.offset;
		for (int i = 0; i < this.count; i++) {
			sb.append(HEX[this.value[off++] & 0xff]);
			sb.append(' ');
		}
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	public String toHexString() {
		if (this.count == 0) {
			return "";
		}
		var sb = new StringBuilder(this.count * 2);
		int off = this.offset;
		for (int i = 0; i < this.count; i++) {
			sb.append(HEX[this.value[off++] & 0xff]);
		}
		return sb.toString();
	}

	private static final String[] HEX = initHex();

	private static String[] initHex() {
		String[] ss = new String[256];
		String hex = "0123456789abcdef";
		for (int i = 0; i < ss.length; i++) {
			int h = (i & 0xf0) >> 4;
			int l = i & 0x0f;
			ss[i] = hex.charAt(h) + "" + hex.charAt(l);
		}
		return ss;
	}

}
