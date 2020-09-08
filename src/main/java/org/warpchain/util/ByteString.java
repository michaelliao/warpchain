package org.warpchain.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable byte-string.
 */
public final class ByteString {

	private static final byte[] EMPTY_BYTES = new byte[0];

	private final byte[] value;
	private final int offset;
	private final int count;
	private int hash;

	public ByteString() {
		this.value = EMPTY_BYTES;
		this.offset = 0;
		this.count = 0;
	}

	public ByteString(byte[] original) {
		this.value = original;
		this.offset = 0;
		this.count = original.length;
	}

	public ByteString(byte[] original, int offset, int count) {
		Objects.checkFromIndexSize(offset, original.length, count);
		this.value = original;
		this.offset = offset;
		this.count = count;
	}

	public byte[] toBytes() {
		if (this.offset == 0 && this.value.length == this.count) {
			return this.value;
		}
		return Arrays.copyOfRange(this.value, this.offset, this.offset + this.count);
	}

	public ByteString substring(int beginIndex) {
		return substring(beginIndex, this.count);
	}

	public ByteString substring(int beginIndex, int endIndex) {
		if (beginIndex == 0 && endIndex == this.count) {
			return this;
		}
		return new ByteString(this.value, this.offset + beginIndex, endIndex - beginIndex);
	}

	public byte byteAt(int index) {
		Objects.checkIndex(index, this.count);
		return this.value[this.offset + index];
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
		if (h == 0) {
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
		return ByteUtils.toHexString(this.value, this.offset, this.count);
	}
}
