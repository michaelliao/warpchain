package org.warpchain.util;

import java.io.EOFException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * A non-synchronized version of ByteArrayInputStream.
 * 
 * @author liaoxuefeng
 */
public class ByteArrayInput {

	/**
	 * An array of bytes that was provided by the creator of the stream. Elements
	 * {@code buf[0]} through {@code buf[count-1]} are the only bytes that can ever
	 * be read from the stream; element {@code buf[pos]} is the next byte to be
	 * read.
	 */
	protected byte buf[];

	/**
	 * The index of the next character to read from the input stream buffer. This
	 * value should always be nonnegative and not larger than the value of
	 * {@code count}. The next byte to be read from the input stream buffer will be
	 * {@code buf[pos]}.
	 */
	protected int pos;

	/**
	 * The currently marked position in the stream. ByteArrayInputStream objects are
	 * marked at position zero by default when constructed. They may be marked at
	 * another position within the buffer by the {@code mark()} method. The current
	 * buffer position is set to this point by the {@code reset()} method.
	 * <p>
	 * If no mark has been set, then the value of mark is the offset passed to the
	 * constructor (or 0 if the offset was not supplied).
	 *
	 * @since 1.1
	 */
	protected int mark = 0;

	/**
	 * The index one greater than the last valid character in the input stream
	 * buffer. This value should always be nonnegative and not larger than the
	 * length of {@code buf}. It is one greater than the position of the last byte
	 * within {@code buf} that can ever be read from the input stream buffer.
	 */
	protected int count;

	/**
	 * Creates a {@code ByteArrayInputStream} so that it uses {@code buf} as its
	 * buffer array. The buffer array is not copied. The initial value of
	 * {@code pos} is {@code 0} and the initial value of {@code count} is the length
	 * of {@code buf}.
	 *
	 * @param buf the input buffer.
	 */
	public ByteArrayInput(byte buf[]) {
		this(buf, 0, buf.length);
	}

	/**
	 * Creates {@code ByteArrayInputStream} that uses {@code buf} as its buffer
	 * array. The initial value of {@code pos} is {@code offset} and the initial
	 * value of {@code count} is the minimum of {@code offset+length} and
	 * {@code buf.length}. The buffer array is not copied. The buffer's mark is set
	 * to the specified offset.
	 *
	 * @param buf    the input buffer.
	 * @param offset the offset in the buffer of the first byte to read.
	 * @param length the maximum number of bytes to read from the buffer.
	 */
	public ByteArrayInput(byte buf[], int offset, int length) {
		this.buf = buf;
		this.pos = offset;
		this.count = Math.min(offset + length, buf.length);
		this.mark = offset;
	}

	/**
	 * Reads the next byte of data from this input stream. Throw unchecked
	 * EOFException if no more data.
	 */
	public byte readByte() {
		if (pos < count) {
			return buf[pos++];
		}
		throw new UncheckedIOException("EOF", new EOFException());
	}

	/**
	 * Reads up to {@code len} bytes of data into an array of bytes from this input
	 * stream. If {@code pos} equals {@code count}, then {@code -1} is returned to
	 * indicate end of file. Otherwise, the number {@code k} of bytes read is equal
	 * to the smaller of {@code len} and {@code count-pos}. If {@code k} is
	 * positive, then bytes {@code buf[pos]} through {@code buf[pos+k-1]} are copied
	 * into {@code b[off]} through {@code b[off+k-1]} in the manner performed by
	 * {@code System.arraycopy}. The value {@code k} is added into {@code pos} and
	 * {@code k} is returned.
	 * <p>
	 * This {@code read} method cannot block.
	 *
	 * @param b   the buffer into which the data is read.
	 * @param off the start offset in the destination array {@code b}
	 * @param len the maximum number of bytes read.
	 * @return the total number of bytes read into the buffer, or {@code -1} if
	 *         there is no more data because the end of the stream has been reached.
	 * @throws NullPointerException      If {@code b} is {@code null}.
	 * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is
	 *                                   negative, or {@code len} is greater than
	 *                                   {@code b.length - off}
	 */
	public int read(byte b[], int off, int len) {
		Objects.checkFromIndexSize(off, len, b.length);

		if (pos >= count) {
			return -1;
		}

		int avail = count - pos;
		if (len > avail) {
			len = avail;
		}
		if (len <= 0) {
			return 0;
		}
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
		return len;
	}

	public byte[] readAllBytes() {
		byte[] result = Arrays.copyOfRange(buf, pos, count);
		pos = count;
		return result;
	}

	public byte[] readNBytes(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("Invalid argument: " + n);
		}
		if (count - pos < n) {
			throw new UncheckedIOException("There is no " + n + " bytes avaiable to read.", new EOFException());
		}
		byte[] copy = Arrays.copyOfRange(buf, pos, pos + n);
		pos += n;
		return copy;
	}

	public int readInt8() {
		if (pos < count) {
			return 0xff & buf[pos++];
		}
		throw new UncheckedIOException("EOF", new EOFException());
	}

	public int readInt16() {
		if (count - pos < 2) {
			throw new UncheckedIOException("There is no 2 bytes avaiable to read.", new EOFException());
		}
		byte b1 = buf[pos++];
		byte b2 = buf[pos++];
		return ((b1 & 0xff) << 8) + (b2 & 0xff);
	}

	public int readInt24() {
		if (count - pos < 3) {
			throw new UncheckedIOException("There is no 3 bytes avaiable to read.", new EOFException());
		}
		byte b1 = buf[pos++];
		byte b2 = buf[pos++];
		byte b3 = buf[pos++];
		return ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) + (b3 & 0xff);
	}

	public int readInt32() {
		if (count - pos < 4) {
			throw new UncheckedIOException("There is no 4 bytes avaiable to read.", new EOFException());
		}
		byte b1 = buf[pos++];
		byte b2 = buf[pos++];
		byte b3 = buf[pos++];
		byte b4 = buf[pos++];
		return ((b1 & 0xff) << 24) | ((b2 & 0xff) << 16) | ((b3 & 0xff) << 8) | (b4 & 0xff);
	}

	public long readLong() {
		if (count - pos < 8) {
			throw new UncheckedIOException("There is no 8 bytes avaiable to read.", new EOFException());
		}
		byte b1 = buf[pos++];
		byte b2 = buf[pos++];
		byte b3 = buf[pos++];
		byte b4 = buf[pos++];
		byte b5 = buf[pos++];
		byte b6 = buf[pos++];
		byte b7 = buf[pos++];
		byte b8 = buf[pos++];
		return ((b1 & 0xffL) << 56) | ((b2 & 0xffL) << 48) | ((b3 & 0xffL) << 40) | ((b4 & 0xffL) << 32)
				| ((b5 & 0xffL) << 24) | ((b6 & 0xffL) << 16) | ((b7 & 0xffL) << 8) | (b8 & 0xffL);
	}

	/**
	 * Skips {@code n} bytes of input from this input stream. Fewer bytes might be
	 * skipped if the end of the input stream is reached. The actual number
	 * {@code k} of bytes to be skipped is equal to the smaller of {@code n} and
	 * {@code count-pos}. The value {@code k} is added into {@code pos} and
	 * {@code k} is returned.
	 *
	 * @param n the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 */
	public int skip(int n) {
		int k = count - pos;
		if (n < k) {
			k = n < 0 ? 0 : n;
		}
		pos += k;
		return k;
	}

	/**
	 * Returns the number of remaining bytes that can be read (or skipped over) from
	 * this input stream.
	 * <p>
	 * The value returned is {@code count - pos}, which is the number of bytes
	 * remaining to be read from the input buffer.
	 *
	 * @return the number of remaining bytes that can be read (or skipped over) from
	 *         this input stream without blocking.
	 */
	public int available() {
		return count - pos;
	}

	/**
	 * Set the current marked position in the stream. ByteArrayInputStream objects
	 * are marked at position zero by default when constructed. They may be marked
	 * at another position within the buffer by this method.
	 * <p>
	 * If no mark has been set, then the value of the mark is the offset passed to
	 * the constructor (or 0 if the offset was not supplied).
	 */
	public void mark() {
		mark = pos;
	}

	/**
	 * Resets the buffer to the marked position. The marked position is 0 unless
	 * another position was marked or an offset was specified in the constructor.
	 */
	public void reset() {
		pos = mark;
	}
}
