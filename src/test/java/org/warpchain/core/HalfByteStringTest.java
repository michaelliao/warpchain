package org.warpchain.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HalfByteStringTest {

	byte[] data16Bytes;
	byte[] data32Bytes;

	@BeforeEach
	void setUp() {
		this.data16Bytes = new byte[] { //
				0x16, 0x00, 0x01, 0x02, 0x10, 0x20, 0x3c, 0x7f, //
				(byte) 0x80, (byte) 0x81, (byte) 0x9e, (byte) 0xab, (byte) 0xb1, (byte) 0xde, (byte) 0xf5, (byte) 0xff //
		};
		assertEquals(16, this.data16Bytes.length);
		this.data32Bytes = new byte[] { //
				0x32, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, //
				0x16, 0x00, 0x01, 0x02, 0x10, 0x20, 0x3c, 0x7f, //
				(byte) 0x80, (byte) 0x81, (byte) 0x9e, (byte) 0xab, (byte) 0xb1, (byte) 0xde, (byte) 0xf5, (byte) 0xff, //
				(byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff //
		};
		// assertEquals(32, this.data32Bytes.length);
	}

	@Test
	void testToString() {
		var hbs = new HalfByteString(data16Bytes);
		assertEquals(32, hbs.length());
		assertEquals("1600010210203c7f80819eabb1def5ff", hbs.toString());

		String hex = "1600010210203c7f80819eabb1def5ff";
		var s2 = new HalfByteString(hex);
		assertEquals(hex, s2.toString());
	}

	@Test
	void testInvalidHexString() {
		assertThrows(IllegalArgumentException.class, () -> {
			new HalfByteString("123");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			new HalfByteString("123s");
		});
	}

	@Test
	void testHashCode() {
		var s32 = new HalfByteString(data16Bytes);
		var s64 = new HalfByteString(data32Bytes);
		var sub32 = s64.substring(16, 48);
		assertEquals(1663895323, s32.hashCode());
		assertEquals(1663895323, sub32.hashCode());
	}

	@Test
	void testEmptyByte() {
		var empty = new HalfByteString();
		assertEquals(0, empty.length());
		assertEquals(0, empty.hashCode());
		assertEquals("", empty.toString());
	}

	@Test
	void testStartsWith() {
		byte[] b1 = new byte[] { 0x12, 0x34, 0x56, 0x78 };
		byte[] b2 = new byte[] { 0x12, 0x34, 0x56 };
		byte[] b3 = new byte[] { 0x34, 0x56 };
		var s1 = new HalfByteString(b1);
		var s2 = new HalfByteString(b2);
		var s3 = new HalfByteString(b3);
		assertTrue(s1.startsWith(s2));
		assertFalse(s1.startsWith(s3));
		assertTrue(s1.startsWith(s3, 2));
		// test same:
		assertTrue(s2.startsWith(s1.substring(0, 6)));
		assertTrue(s2.startsWith(s2));
		// test empty:
		var s0 = new HalfByteString(new byte[0]);
		assertTrue(s2.startsWith(s0));
		assertTrue(s0.startsWith(s0));
	}

	@Test
	void testSharedPrefix() {
		byte[] b1 = new byte[] { 0x12, 0x34, 0x56, 0x78 };
		byte[] b2 = new byte[] { 0x12, 0x3f, 0x56 };
		byte[] b3 = new byte[] { 0x34, 0x56 };
		var s1 = new HalfByteString(b1);
		var s2 = new HalfByteString(b2);
		var s3 = new HalfByteString(b3);
		assertEquals(s1.substring(0, 3), HalfByteString.sharedPrefix(s1, s2));
		assertEquals(HalfByteString.EMPTY, HalfByteString.sharedPrefix(s1, s3));
		assertEquals(HalfByteString.EMPTY, HalfByteString.sharedPrefix(s1, HalfByteString.EMPTY));
		assertEquals(HalfByteString.EMPTY, HalfByteString.sharedPrefix(HalfByteString.EMPTY, HalfByteString.EMPTY));
		assertEquals(s1, HalfByteString.sharedPrefix(s1, s1));
	}

	@Test
	void testSubstring() {
		var s32 = new HalfByteString(data16Bytes);
		var s64 = new HalfByteString(data32Bytes);
		var sub32 = s64.substring(16, 48);
		assertEquals("1600010210203c7f80819eabb1def5ff", sub32.toString());
		assertEquals(sub32, s32);
		assertEquals(sub32.substring(5, 20), s32.substring(5, 20));
		assertEquals(sub32.substring(5), s32.substring(5, 32));
		assertEquals("01021020", sub32.substring(4, 12).toString());
	}

	@Test
	void testValueAt() {
		var s32 = new HalfByteString(data16Bytes);
		var s64 = new HalfByteString(data32Bytes);
		var sub32 = s64.substring(16, 48);
		assertEquals(0x1, s32.valueAt(0));
		assertEquals(0x1, sub32.valueAt(0));
		assertEquals(0x1, s64.valueAt(16));

		assertEquals(0x6, s32.valueAt(1));
		assertEquals(0x6, sub32.valueAt(1));
		assertEquals(0x6, s64.valueAt(1 + 16));

		assertEquals(0xf, s32.valueAt(28));
		assertEquals(0xf, sub32.valueAt(28));
		assertEquals(0xf, s64.valueAt(28 + 16));

		assertEquals(0x5, s32.valueAt(29));
		assertEquals(0x5, sub32.valueAt(29));
		assertEquals(0x5, s64.valueAt(29 + 16));
	}

	@Test
	void testEquals() {
		var s32 = new HalfByteString(data16Bytes);
		var s64 = new HalfByteString(data32Bytes);
		var sub32 = s64.substring(16, 48);
		assertEquals(s32, sub32);
	}

}
