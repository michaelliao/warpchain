package org.warpchain.encode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.warpchain.exception.DecodeException;
import org.warpchain.exception.EncodeException;
import org.warpchain.util.ByteArrayInput;
import org.warpchain.util.ByteArrayOutput;
import org.warpchain.util.ByteUtils;

public class RLP {

	public static final int MAX_ITEMS = 0xf_ff_ff_ff;

	private static final int INT_BYTEARRAY_SIZE_0 = 0x80;
	private static final int INT_BYTEARRAY_SIZE_1 = 0x81;
	private static final int INT_BYTEARRAY_SIZE_59 = 0xbb;

	private static final int INT_BYTEARRAY_LENGTH_1_BYTE = 0xbc;
	private static final int INT_BYTEARRAY_LENGTH_2_BYTES = 0xbd;
	private static final int INT_BYTEARRAY_LENGTH_3_BYTES = 0xbe;
	private static final int INT_BYTEARRAY_LENGTH_4_BYTES = 0xbf;

	private static final byte BYTE_BYTEARRAY_LENGTH_1 = (byte) INT_BYTEARRAY_LENGTH_1_BYTE;
	private static final byte BYTE_BYTEARRAY_LENGTH_2 = (byte) INT_BYTEARRAY_LENGTH_2_BYTES;
	private static final byte BYTE_BYTEARRAY_LENGTH_3 = (byte) INT_BYTEARRAY_LENGTH_3_BYTES;
	private static final byte BYTE_BYTEARRAY_LENGTH_4 = (byte) INT_BYTEARRAY_LENGTH_4_BYTES;

	private static final int INT_LIST_SIZE_0 = 0xc0;
	private static final int INT_LIST_SIZE_59 = 0xfb;

	private static final int INT_LIST_LENGTH_1_BYTE = 0xfc;
	private static final int INT_LIST_LENGTH_2_BYTES = 0xfd;
	private static final int INT_LIST_LENGTH_3_BYTES = 0xfe;
	private static final int INT_LIST_LENGTH_4_BYTES = 0xff;

	private static final byte BYTE_LIST_LENGTH_1 = (byte) INT_LIST_LENGTH_1_BYTE;
	private static final byte BYTE_LIST_LENGTH_2 = (byte) INT_LIST_LENGTH_2_BYTES;
	private static final byte BYTE_LIST_LENGTH_3 = (byte) INT_LIST_LENGTH_3_BYTES;
	private static final byte BYTE_LIST_LENGTH_4 = (byte) INT_LIST_LENGTH_4_BYTES;

	public static void encode(Object o, ByteArrayOutput output) {
		if (o instanceof Byte) {
			byte b = (Byte) o;
			encode(b, output);
		} else if (o instanceof byte[]) {
			byte[] bs = (byte[]) o;
			encode(bs, output);
		} else if (o instanceof String) {
			String s = (String) o;
			encode(s.getBytes(StandardCharsets.UTF_8), output);
		} else if (o instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) o;
			encode(list, output);
		} else {
			throw new EncodeException(
					"Object type " + (o == null ? "null" : o.getClass().getName()) + " is not supported.");
		}
	}

	public static void encode(byte b, ByteArrayOutput output) {
		if (b >= 0) {
			output.writeByte(b); // single byte: 0 ~ 127
		} else {
			output.writeInt8(0x81); // 0x80 + length=1 = 0x81
			output.writeByte(b); // single byte
		}
	}

	public static void encode(byte[] bs, ByteArrayOutput output) {
		int length = bs.length;
		if (length == 0) {
			output.writeByte((byte) INT_BYTEARRAY_SIZE_0); // empty bytes
		} else if (length == 1) {
			encode(bs[0], output); // single byte
		} else if (length <= 59) {
			output.writeInt8(INT_BYTEARRAY_SIZE_0 + length); // prefix = 0x81 ~ 0xbb
			output.writeBytes(bs); // bytes data
		} else {
			if (length <= 0xff) {
				output.writeByte(BYTE_BYTEARRAY_LENGTH_1); // length bytes = 1
				output.writeByte((byte) length);
			} else if (length <= 0xffff) {
				output.writeByte(BYTE_BYTEARRAY_LENGTH_2); // length bytes = 2
				output.writeInt16(length);
			} else if (length <= 0xff_ffff) {
				output.writeByte(BYTE_BYTEARRAY_LENGTH_3); // length bytes = 3
				output.writeInt24(length);
			} else if (length <= MAX_ITEMS) {
				output.writeByte(BYTE_BYTEARRAY_LENGTH_4); // length bytes = 4
				output.writeInt32(length);
			} else {
				throw new EncodeException("Bytes array is too large: " + Integer.toUnsignedString(length));
			}
			output.writeBytes(bs);
		}
	}

	public static void encode(List<Object> list, ByteArrayOutput output) {
		int length = list.size();
		if (length == 0) {
			output.writeByte((byte) INT_LIST_SIZE_0); // empty list
		} else if (length <= 59) {
			output.writeInt8(INT_LIST_SIZE_0 + length); // prefix = 0xc0 ~ 0xfb
		} else {
			if (length <= 0xff) {
				output.writeByte(BYTE_LIST_LENGTH_1);
				output.writeInt8(length);
			} else if (length <= 0xffff) {
				output.writeByte(BYTE_LIST_LENGTH_2);
				output.writeInt16(length);
			} else if (length <= 0xff_ffff) {
				output.writeByte(BYTE_LIST_LENGTH_3);
				output.writeInt24(length);
			} else if (length <= MAX_ITEMS) {
				output.writeByte(BYTE_LIST_LENGTH_4);
				output.writeInt32(length);
			} else {
				throw new EncodeException("List is too large: " + Integer.toUnsignedString(length));
			}
		}
		for (Object o : list) {
			encode(o, output);
		}
	}

	public static Object decode(ByteArrayInput input) {
		return decode(input, false);
	}

	public static Object decode(ByteArrayInput input, boolean treatBytesAsString) {
		// try decode as byte[1] or single character String:
		byte b = input.readByte();
		if (b >= 0) {
			byte[] data = new byte[] { b };
			return treatBytesAsString ? new String(data, StandardCharsets.UTF_8) : data;
		}
		// try decode as empty byte[] or empty String
		int n = b & 0xff;
		if (n == INT_BYTEARRAY_SIZE_0) {
			return treatBytesAsString ? "" : ByteUtils.emptyByteArray();
		}
		// try decode as bytes[] or String:
		int length = -1;
		if (n > INT_BYTEARRAY_SIZE_0 && n <= INT_BYTEARRAY_SIZE_59) {
			length = n - INT_BYTEARRAY_SIZE_0;
		}
		if (n == INT_BYTEARRAY_LENGTH_1_BYTE) {
			length = input.readInt8();
		}
		if (n == INT_BYTEARRAY_LENGTH_2_BYTES) {
			length = input.readInt16();
		}
		if (n == INT_BYTEARRAY_LENGTH_3_BYTES) {
			length = input.readInt24();
		}
		if (n == INT_BYTEARRAY_LENGTH_4_BYTES) {
			length = input.readInt32();
			if (length < 0 || n > MAX_ITEMS) {
				throw new DecodeException("Too many items: " + Integer.toUnsignedString(length));
			}
		}
		if (length >= 0) {
			byte[] data = input.readNBytes(length);
			return treatBytesAsString ? new String(data, StandardCharsets.UTF_8) : data;
		}
		if (n == INT_LIST_SIZE_0) {
			return new ArrayList<>(0);
		}
		int size;
		if (n > INT_LIST_SIZE_0 && n <= INT_LIST_SIZE_59) {
			size = n - INT_LIST_SIZE_0;
		} else if (n == INT_LIST_LENGTH_1_BYTE) {
			size = input.readInt8();
		} else if (n == INT_LIST_LENGTH_2_BYTES) {
			size = input.readInt16();
		} else if (n == INT_LIST_LENGTH_3_BYTES) {
			size = input.readInt24();
		} else if (n == INT_LIST_LENGTH_4_BYTES) {
			size = input.readInt32();
			if (size < 0 || size > MAX_ITEMS) {
				throw new DecodeException("Too many items: " + Integer.toUnsignedString(size));
			}
		} else {
			throw new DecodeException(
					"Unexpect leading byte when decode as list: 0x" + ByteUtils.toHexString((byte) n));
		}
		List<Object> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Object o = decode(input, treatBytesAsString);
			list.add(o);
		}
		return list;
	}

	public static byte decodeAsSingleByte(ByteArrayInput input) {
		byte b = input.readByte();
		if (b >= 0) { // 0 ~ 127
			return b;
		}
		if ((b & 0xff) == INT_BYTEARRAY_SIZE_1) {
			return input.readByte();
		}
		throw new DecodeException("Unexpect leading byte when decode as byte: 0x" + ByteUtils.toHexString(b));
	}

	public static String decodeAsString(ByteArrayInput input) {
		byte[] bs = decodeAsByteArray(input);
		if (bs.length == 0) {
			return "";
		}
		return new String(bs, StandardCharsets.UTF_8);
	}

	public static byte[] decodeAsByteArray(ByteArrayInput input) {
		byte b = input.readByte();
		if (b >= 0) {
			return new byte[] { b };
		}
		int n = b & 0xff;
		if (n == INT_BYTEARRAY_SIZE_0) {
			return ByteUtils.emptyByteArray();
		}
		if (n > INT_BYTEARRAY_SIZE_0 && n <= INT_BYTEARRAY_SIZE_59) {
			int len = n - INT_BYTEARRAY_SIZE_0;
			return input.readNBytes(len);
		}
		if (n == INT_BYTEARRAY_LENGTH_1_BYTE) {
			int len = input.readInt8();
			return input.readNBytes(len);
		}
		if (n == INT_BYTEARRAY_LENGTH_2_BYTES) {
			int len = input.readInt16();
			return input.readNBytes(len);
		}
		if (n == INT_BYTEARRAY_LENGTH_3_BYTES) {
			int len = input.readInt24();
			return input.readNBytes(len);
		}
		if (n == INT_BYTEARRAY_LENGTH_4_BYTES) {
			int len = input.readInt32();
			if (len < 0 || len > 0xf_ff_ff_ff) {
				throw new DecodeException("Too many items.");
			}
			return input.readNBytes(len);
		}
		throw new DecodeException("Unexpect leading byte when decode as byte array: 0x" + ByteUtils.toHexString(b));
	}

	public static List<Object> decodeAsList(ByteArrayInput input) {
		return decodeAsList(input, false);
	}

	public static List<Object> decodeAsList(ByteArrayInput input, boolean treatBytesAsString) {
		int n = input.readInt8();
		if (n == INT_LIST_SIZE_0) {
			return new ArrayList<>(0);
		}
		int size;
		if (n > INT_LIST_SIZE_0 && n <= INT_LIST_SIZE_59) {
			size = n - INT_LIST_SIZE_0;
		} else if (n == INT_LIST_LENGTH_1_BYTE) {
			size = input.readInt8();
		} else if (n == INT_LIST_LENGTH_2_BYTES) {
			size = input.readInt16();
		} else if (n == INT_LIST_LENGTH_3_BYTES) {
			size = input.readInt24();
		} else if (n == INT_LIST_LENGTH_4_BYTES) {
			size = input.readInt32();
			if (size < 0) {
				throw new OutOfMemoryError("Length of int32 is too large: " + Integer.toUnsignedString(size));
			}
		} else {
			throw new DecodeException(
					"Unexpect leading byte when decode as list: 0x" + ByteUtils.toHexString((byte) n));
		}
		List<Object> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Object o = decode(input, treatBytesAsString);
			list.add(o);
		}
		return list;
	}
}
