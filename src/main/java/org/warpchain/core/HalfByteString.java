package org.warpchain.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable half-byte-string. Each element is in range of 0 ~ 0xf.
 */
public final class HalfByteString {

	private static final int[] EMPTY = new int[0];
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	private final int[] value;
	private final int offset;
	private final int count;
	private int hash;

	public HalfByteString() {
		this(EMPTY, 0, 0);
	}

	public HalfByteString(byte[] original) {
		if (original.length == 0) {
			this.value = EMPTY;
			this.offset = 0;
			this.count = 0;
		} else {
			int len = original.length;
			int[] val = new int[len << 1];
			for (int i = 0; i < len; i++) {
				int offset = i << 1;
				byte b = original[i];
				val[offset] = (b & 0xf0) >>> 4;
				val[offset + 1] = b & 0x0f;
			}
			this.value = val;
			this.offset = 0;
			this.count = val.length;
		}
	}

	private HalfByteString(int[] original, int offset, int count) {
		this.value = original;
		this.offset = offset;
		this.count = count;
	}

	public HalfByteString substring(int beginIndex) {
		return substring(beginIndex, this.count);
	}

	public HalfByteString substring(int beginIndex, int endIndex) {
		if (beginIndex == 0 && endIndex == this.count) {
			return this;
		}
		return new HalfByteString(this.value, this.offset + beginIndex, endIndex - beginIndex);
	}

	public int length() {
		return this.count;
	}

	public int valueAt(int index) {
		Objects.checkIndex(index, this.count);
		return this.value[this.offset + index];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof HalfByteString) {
			HalfByteString bs = (HalfByteString) o;
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
		var sb = new StringBuilder(this.count);
		int off = this.offset;
		for (int i = 0; i < this.count; i++) {
			sb.append(HEX_CHARS[this.value[off++]]);
		}
		return sb.toString();
	}
}
