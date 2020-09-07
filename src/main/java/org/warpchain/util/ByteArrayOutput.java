package org.warpchain.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A non-synchronized version of ByteArrayOutputStream.
 * 
 * @author liaoxuefeng
 */
public class ByteArrayOutput {

	/**
	 * The buffer where data is stored.
	 */
	protected byte buf[];

	/**
	 * The number of valid bytes in the buffer.
	 */
	protected int count;

	/**
	 * Creates a new {@code ByteArrayOutputStream}. The buffer capacity is initially
	 * 32 bytes, though its size increases if necessary.
	 */
	public ByteArrayOutput() {
		this(32);
	}

	/**
	 * Creates a new {@code ByteArrayOutputStream}, with a buffer capacity of the
	 * specified size, in bytes.
	 *
	 * @param size the initial size.
	 * @throws IllegalArgumentException if size is negative.
	 */
	public ByteArrayOutput(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		buf = new byte[size];
	}

	/**
	 * Increases the capacity if necessary to ensure that it can hold at least the
	 * number of elements specified by the minimum capacity argument.
	 *
	 * @param minCapacity the desired minimum capacity
	 * @throws OutOfMemoryError if {@code minCapacity < 0}. This is interpreted as a
	 *                          request for the unsatisfiably large capacity
	 *                          {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
	 */
	private void ensureCapacity(int minCapacity) {
		if (minCapacity > MAX_ARRAY_SIZE) {
			throw new IllegalArgumentException("Cannot set capacity more than " + MAX_ARRAY_SIZE);
		}
		if (minCapacity > buf.length) {
			int newCapacity = buf.length << 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			buf = Arrays.copyOf(buf, newCapacity);
		}
	}

	/**
	 * The maximum size of array to allocate. 1G
	 */
	private static final int MAX_ARRAY_SIZE = 1024 * 1024 * 1024;

	/**
	 * Writes the specified byte to this {@code ByteArrayOutputStream}.
	 *
	 * @param b the byte to be written.
	 */
	public void writeByte(byte b) {
		ensureCapacity(count + 1);
		buf[count] = b;
		count++;
	}

	/**
	 * Write int8 as 1 byte. The most highest 24 bits are ignored.
	 */
	public void writeInt8(int n) {
		ensureCapacity(count + 1);
		buf[count] = (byte) (n & 0xff);
		count++;
	}

	/**
	 * Write int16 as 2 bytes. The most highest 16 bits are ignored.
	 */
	public void writeInt16(int n) {
		ensureCapacity(count + 2);
		buf[count] = (byte) ((n & 0xff00) >> 8);
		count++;
		buf[count] = (byte) (n & 0xff);
		count++;
	}

	/**
	 * Write int24 as 3 bytes. The most highest 8 bits are ignored.
	 */
	public void writeInt24(int n) {
		ensureCapacity(count + 3);
		buf[count] = (byte) ((n & 0xff0000) >> 16);
		count++;
		buf[count] = (byte) ((n & 0xff00) >> 8);
		count++;
		buf[count] = (byte) (n & 0xff);
		count++;
	}

	/**
	 * Write int32 as 4 bytes.
	 */
	public void writeInt32(int n) {
		ensureCapacity(count + 2);
		buf[count] = (byte) ((n & 0xff000000) >> 24);
		count++;
		buf[count] = (byte) ((n & 0xff0000) >> 16);
		count++;
		buf[count] = (byte) ((n & 0xff00) >> 8);
		count++;
		buf[count] = (byte) (n & 0xff);
		count++;
	}

	public void writeLong(long n) {
		ensureCapacity(count + 2);
		buf[count] = (byte) ((n & 0xff000000_00000000L) >> 56);
		count++;
		buf[count] = (byte) ((n & 0xff0000_00000000L) >> 48);
		count++;
		buf[count] = (byte) ((n & 0xff00_00000000L) >> 40);
		count++;
		buf[count] = (byte) ((n & 0xff_00000000L) >> 32);
		count++;
		buf[count] = (byte) ((n & 0xff000000L) >> 24);
		count++;
		buf[count] = (byte) ((n & 0xff0000L) >> 16);
		count++;
		buf[count] = (byte) ((n & 0xff00L) >> 8);
		count++;
		buf[count] = (byte) (n & 0xffL);
		count++;
	}

	/**
	 * Writes {@code len} bytes from the specified byte array starting at offset
	 * {@code off} to this {@code ByteArrayOutputStream}.
	 *
	 * @param b   the data.
	 * @param off the start offset in the data.
	 * @param len the number of bytes to write.
	 * @throws NullPointerException      if {@code b} is {@code null}.
	 * @throws IndexOutOfBoundsException if {@code off} is negative, {@code len} is
	 *                                   negative, or {@code len} is greater than
	 *                                   {@code b.length - off}
	 */
	public void writeBytes(byte b[], int off, int len) {
		Objects.checkFromIndexSize(off, len, b.length);
		ensureCapacity(count + len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	/**
	 * Writes the complete contents of the specified byte array to this
	 * {@code ByteArrayOutput}.
	 *
	 * @apiNote This method is equivalent to {@link #write(byte[],int,int) write(b,
	 *          0, b.length)}.
	 *
	 * @param b the data.
	 * @throws NullPointerException if {@code b} is {@code null}.
	 * @since 11
	 */
	public void writeBytes(byte b[]) {
		writeBytes(b, 0, b.length);
	}

	/**
	 * Writes the content of the specified string to this {@code ByteArrayOutput}
	 * with UTF-8 charset.
	 * 
	 * @param s the data.
	 */
	public void writeString(String s) {
		writeBytes(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Writes the complete contents of this ByteArrayBuffer to the specified output
	 * stream argument, as if by calling the output stream's write method using
	 * {@code out.write(buf, 0, count)}.
	 *
	 * @param out the output stream to which to write the data.
	 * @throws NullPointerException if {@code out} is {@code null}.
	 * @throws IOException          if an I/O error occurs.
	 */
	public void writeTo(OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}

	/**
	 * Resets the {@code count} field of this {@code ByteArrayOutputStream} to zero,
	 * so that all currently accumulated output in the output stream is discarded.
	 * The output stream can be used again, reusing the already allocated buffer
	 * space.
	 */
	public void reset() {
		count = 0;
	}

	/**
	 * Creates a newly allocated byte array. Its size is the current size of this
	 * output stream and the valid contents of the buffer have been copied into it.
	 *
	 * @return the current contents of this output stream, as a byte array.
	 * @see java.io.ByteArrayOutputStream#size()
	 */
	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}

	/**
	 * Returns the current size of the buffer.
	 *
	 * @return the value of the {@code count} field, which is the number of valid
	 *         bytes in this output stream.
	 * @see java.io.ByteArrayOutputStream#count
	 */
	public int size() {
		return count;
	}

	/**
	 * Converts the buffer's contents into a string decoding bytes using the default
	 * UTF-8 character set.
	 */
	public String toString() {
		return new String(buf, 0, count, StandardCharsets.UTF_8);
	}

	/**
	 * Converts the buffer's contents into a string by decoding the bytes using the
	 * named {@link java.nio.charset.Charset charset}.
	 */
	public String toString(String charsetName) throws UnsupportedEncodingException {
		return new String(buf, 0, count, charsetName);
	}

	/**
	 * Converts the buffer's contents into a string by decoding the bytes using the
	 * specified {@link java.nio.charset.Charset charset}.
	 */
	public String toString(Charset charset) {
		return new String(buf, 0, count, charset);
	}
}
