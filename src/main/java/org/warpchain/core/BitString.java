package org.warpchain.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable bit array.
 * 
 * @author liaoxuefeng
 */
public class BitString {

	private static final byte[] EMPTY_BITS = new byte[0];

	/**
	 * Empty bit string ''.
	 */
	public static final BitString EMPTY = new BitString(EMPTY_BITS, 0, 0);

	/**
	 * Single bit string of '0'.
	 */
	public static final BitString BIT_0 = new BitString(new byte[] { 0x0 }, 0, 1);

	/**
	 * Single bit string of '1'.
	 */
	public static final BitString BIT_1 = new BitString(new byte[] { (byte) 0x80 }, 0, 1);

	private final byte[] value;
	private final int offset;
	private final int count;
	private int hash;

	private BitString(byte[] value, int offset, int count) {
		this.value = value;
		this.offset = offset;
		this.count = count;
	}

	public BitString(byte[] value) {
		this(Arrays.copyOf(value, value.length), 0, 8 * value.length); // 1 byte = 8 bits
	}

	public static BitString unsafe(byte[] value) {
		return new BitString(value, 0, 8 * value.length);
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public int length() {
		return count;
	}

	public BitString substring(int beginIndex) {
		return substring(beginIndex, this.count);
	}

	public BitString substring(int beginIndex, int endIndex) {
		if (beginIndex == 0 && endIndex == this.count) {
			return this;
		}
		Objects.checkFromToIndex(beginIndex, endIndex, this.count);
		return new BitString(this.value, this.offset + beginIndex, endIndex - beginIndex);
	}

	public int bitValueAt(int index) {
		Objects.checkIndex(index, this.count);
		int indexOfBit = this.offset + index;
		int indexOfByte = indexOfBit >> 3;
		int offsetOfBit = indexOfBit % 8;
		return (this.value[indexOfByte] & BIT_INDEX_MASK[offsetOfBit]) == 0 ? 0 : 1;
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
	public boolean startsWith(BitString prefix) {
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
	public boolean startsWith(BitString prefix, int toffset) {
		int to = this.offset + toffset;
		int po = prefix.offset;
		int pc = prefix.count;
		// Note: toffset might be near -1>>>1.
		if ((toffset < 0) || (toffset > count - pc)) {
			return false;
		}
		while (--pc >= 0) {
			if (bitValueAt(to++) != prefix.bitValueAt(po++)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get shared prefix as many as possible. Example: "1a2b3c" and "1a2f4d" share
	 * "1a2".
	 */
	public static BitString sharedPrefix(BitString s1, BitString s2) {
		int max = Math.min(s1.length(), s2.length());
		if (max == 0) {
			return EMPTY;
		}
		int n = 0;
		while (n < max) {
			if (s1.bitValueAt(n) != s2.bitValueAt(n)) {
				return s1.substring(0, n);
			}
			n++;
		}
		return s1.substring(0, max);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof BitString) {
			BitString bs = (BitString) o;
			if (this.count == bs.count) {
				for (int i = 0; i < this.count; i++) {
					if (this.bitValueAt(i) != bs.bitValueAt(i)) {
						return false;
					}
				}
				return true;
			}
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
				h = 31 * h + this.bitValueAt(off++);
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
		var sb = new StringBuilder(this.count);
		for (int i = 0; i < this.count; i++) {
			sb.append(this.bitValueAt(i) == 0 ? '0' : '1');
		}
		return sb.toString();
	}
}
