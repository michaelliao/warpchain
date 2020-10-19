package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ByteArrayOutputTest {

	ByteArrayOutput output;

	@BeforeEach
	void setUp() throws Exception {
		output = new ByteArrayOutput();
	}

	@Test
	void testWriteByte() {
		output.writeByte((byte) 'H');
		output.writeByte((byte) 'e');
		output.writeByte((byte) 'l');
		output.writeByte((byte) 'l');
		output.writeByte((byte) 'o');
		assertEquals(5, output.size());
		ByteAssertions.assertByteArrayEquals("Hello", output.toByteArray());
	}

	@Test
	void testWriteInt8() {
		output.writeInt8('H');
		output.writeInt8('e');
		output.writeInt8('l');
		output.writeInt8('l');
		// 'o' = 0x6f
		output.writeInt8(0xff_6f); // ignore most higher bits
		assertEquals(5, output.size());
		ByteAssertions.assertByteArrayEquals("Hello", output.toByteArray());
	}

	@Test
	void testWriteInt16() {
		// Hell = 0x48_65_6c_6c
		output.writeInt16(0x48_65);
		output.writeInt16(0xff_6c_6c); // ignore most higher bits
		assertEquals(4, output.size());
		ByteAssertions.assertByteArrayEquals("Hell", output.toByteArray());
	}

	@Test
	void testWriteInt24() {
		// HelloW = 0x48_65_6c_6c_6f_57
		output.writeInt24(0x48_65_6c);
		output.writeInt24(0xff_6c_6f_57); // ignore most higher bits
		assertEquals(6, output.size());
		ByteAssertions.assertByteArrayEquals("HelloW", output.toByteArray());
	}

	@Test
	void testWriteInt32() {
		// HelloWor = 0x48_65_6c_6c_6f_57_6f_72
		output.writeInt32(0x48_65_6c_6c);
		output.writeInt32(0x6f_57_6f_72);
		assertEquals(8, output.size());
		ByteAssertions.assertByteArrayEquals("HelloWor", output.toByteArray());
	}

	@Test
	void testWriteLong() {
		// HelloWor = 0x48_65_6c_6c_6f_57_6f_72
		output.writeLong(0x48_65_6c_6c_6f_57_6f_72L);
		assertEquals(8, output.size());
		ByteAssertions.assertByteArrayEquals("HelloWor", output.toByteArray());
	}

	@Test
	void testWriteBytesWithRangeIntInt() throws Exception {
		byte[] buffer = "HelloWorld".getBytes("UTF-8");
		output.writeBytes(buffer, 2, 6);
		assertEquals(6, output.size());
		ByteAssertions.assertByteArrayEquals("lloWor", output.toByteArray());
	}

	@Test
	void testWriteBytes() throws Exception {
		byte[] buffer = "HelloWorld".getBytes("UTF-8");
		output.writeBytes(buffer);
		assertEquals(10, output.size());
		ByteAssertions.assertByteArrayEquals("HelloWorld", output.toByteArray());
	}

	@Test
	void testWriteString() throws Exception {
		output.writeString("HelloWorld");
		ByteAssertions.assertByteArrayEquals("HelloWorld", output.toByteArray());
	}

	@Test
	void testWriteTo() throws Exception {
		var to = new ByteArrayOutputStream();
		output.writeString("HelloWorld");
		output.writeTo(to);
		ByteAssertions.assertByteArrayEquals("HelloWorld", to.toByteArray());
	}

	@Test
	void testReset() {
		output.writeString("Hello");
		ByteAssertions.assertByteArrayEquals("Hello", output.toByteArray());
		output.reset();
		output.writeString("World");
		ByteAssertions.assertByteArrayEquals("World", output.toByteArray());
	}

	@Test
	void testToByteArray() {
		output.writeInt24(0xe4_b8_ad);
		output.writeInt24(0xe6_96_87);
		assertArrayEquals(new byte[] { (byte) 0xe4, (byte) 0xb8, (byte) 0xad, (byte) 0xe6, (byte) 0x96, (byte) 0x87 },
				output.toByteArray());
	}

	@Test
	void testSize() {
		assertEquals(0, output.size());
		output.writeString("Hello");
		assertEquals(5, output.size());
	}

	@Test
	void testToString() {
		// utf8("\u4e2d\u6587") = 0xe4_b8_ad 0xe6_96_87
		output.writeInt24(0xe4_b8_ad);
		output.writeInt24(0xe6_96_87);
		assertEquals("\u4e2d\u6587", output.toString());
	}

	@Test
	void testLargeWrite() throws Exception {
		var sb = new StringBuilder(1024);
		for (int i = 0; i < 1000; i++) {
			String s = "A123456789";
			sb.append(s);
			output.writeBytes(s.getBytes("UTF-8"));
		}
		assertEquals(sb.toString(), output.toString());
	}
}
