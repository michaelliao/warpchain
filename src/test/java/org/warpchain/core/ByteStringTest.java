package org.warpchain.core;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.Test;
import org.warpchain.util.HashUtils;

public class ByteStringTest {

	// f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0
	static final byte[] BYTE_DATA = HashUtils.sha1("Hello".getBytes(StandardCharsets.ISO_8859_1));
	static final String HEX_STRING = "[f7 ff 9e 8b 7b b2 e0 9b 70 93 5a 5d 78 5e 0c c5 d9 d0 ab f0]";

	@Test
	void valueAt() {
		var bs = new ByteString(BYTE_DATA);
		assertEquals((byte) 0xf7, bs.valueAt(0));
		assertEquals((byte) 0xff, bs.valueAt(1));
		assertEquals((byte) 0x9e, bs.valueAt(2));
		assertEquals((byte) 0xab, bs.valueAt(18));
		assertEquals((byte) 0xf0, bs.valueAt(19));

		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.valueAt(-1);
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.valueAt(20);
		});
	}

	@Test
	void bitAt() {
		var bs = new ByteString(BYTE_DATA);
		// f7 = 11110111
		assertEquals(1, bs.bitAt(0));
		assertEquals(1, bs.bitAt(1));
		assertEquals(1, bs.bitAt(2));
		assertEquals(1, bs.bitAt(3));
		assertEquals(0, bs.bitAt(4));
		assertEquals(1, bs.bitAt(5));
		assertEquals(1, bs.bitAt(6));
		assertEquals(1, bs.bitAt(7));
		// 9e = 10011110
		assertEquals(1, bs.bitAt(16));
		assertEquals(0, bs.bitAt(17));
		assertEquals(0, bs.bitAt(18));
		assertEquals(1, bs.bitAt(19));
		assertEquals(1, bs.bitAt(20));
		assertEquals(1, bs.bitAt(21));
		assertEquals(1, bs.bitAt(22));
		assertEquals(0, bs.bitAt(23));
		// f0 = 11110000
		assertEquals(1, bs.bitAt(152));
		assertEquals(1, bs.bitAt(153));
		assertEquals(1, bs.bitAt(154));
		assertEquals(1, bs.bitAt(155));
		assertEquals(0, bs.bitAt(156));
		assertEquals(0, bs.bitAt(157));
		assertEquals(0, bs.bitAt(158));
		assertEquals(0, bs.bitAt(159));

		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.bitAt(160);
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.bitAt(-1);
		});
	}

	@Test
	void equalsAndHashCode() {
		var bs = new ByteString(BYTE_DATA);
		// 9e 8b 7b b2 e0 9b
		var bs1 = bs.substring(2, 8);
		var bs2 = bs.substring(2, 6);
		var bs3 = new ByteString(ByteUtils.fromHexString("9e8b7bb2e09b"));
		assertNotEquals(bs1, bs2);
		assertEquals(bs1, bs3);
		assertEquals(bs1.hashCode(), bs3.hashCode());
		assertEquals(-2022616832, bs1.hashCode());
		assertEquals(0, ByteString.EMPTY.hashCode());
	}

	@Test
	void lengthAndIsEmpty() {
		var bs = new ByteString(BYTE_DATA);
		var bs1 = bs.substring(2, 8);
		var bs2 = bs.substring(10, 10);
		assertEquals(20, bs.length());
		assertEquals(6, bs1.length());
		assertEquals(0, bs2.length());

		assertFalse(bs.isEmpty());
		assertFalse(bs1.isEmpty());
		assertTrue(bs2.isEmpty());
	}

	@Test
	void startsWith() {
		var bs = new ByteString(BYTE_DATA);
		assertTrue(bs.startsWith(ByteString.EMPTY));
		assertTrue(bs.startsWith(bs));
		assertTrue(bs.startsWith(bs.substring(0, 5)));
		assertTrue(bs.startsWith(new ByteString(ByteUtils.fromHexString("f7ff9e"))));
		assertTrue(bs.startsWith(new ByteString(ByteUtils.fromHexString("0011f7ff9e")).substring(2)));

		assertFalse(bs.startsWith(new ByteString(ByteUtils.fromHexString("f8"))));
		assertFalse(bs.startsWith(new ByteString(ByteUtils.fromHexString("0011f7ff9e"))));

		// 9e 8b 7b b2 e0 9b
		var bs1 = bs.substring(2, 8);
		assertTrue(bs1.startsWith(ByteString.EMPTY));
		assertTrue(bs1.startsWith(bs1));
		assertTrue(bs1.startsWith(bs1.substring(0, 5)));
		assertTrue(bs1.startsWith(bs.substring(2, 7)));
		assertTrue(bs1.startsWith(new ByteString(ByteUtils.fromHexString("9e8b7b"))));
		assertTrue(bs1.startsWith(new ByteString(ByteUtils.fromHexString("00119e8b7b")).substring(2)));

		assertFalse(bs1.startsWith(bs.substring(2, 9)));
		assertFalse(bs1.startsWith(new ByteString(ByteUtils.fromHexString("9f"))));
		assertFalse(bs1.startsWith(new ByteString(ByteUtils.fromHexString("00119e8b7b"))));
	}

	@Test
	void substring() {
		var bs = new ByteString(BYTE_DATA);
		assertEquals(bs, bs.substring(0));
		assertEquals("5a5d785e0cc5d9d0abf0", bs.substring(10).toHexString());
		assertEquals("5a5d785e0cc5d9d0abf0", bs.substring(10, 20).toHexString());
		assertEquals("5a5d785e0cc5", bs.substring(10, 16).toHexString());
		assertEquals("5a", bs.substring(10, 11).toHexString());
		assertEquals(ByteString.EMPTY, bs.substring(10, 10));

		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.substring(10, 21);
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			bs.substring(11, 10);
		});
	}

	@Test
	void toBytes() {
		var bs = new ByteString(BYTE_DATA);
		assertArrayEquals(BYTE_DATA, bs.toBytes());
		assertArrayEquals(Arrays.copyOf(BYTE_DATA, 6), bs.substring(0, 6).toBytes());
		assertArrayEquals(Arrays.copyOfRange(BYTE_DATA, 6, 10), bs.substring(6, 10).toBytes());
		assertArrayEquals(new byte[0], bs.substring(10, 10).toBytes());
	}
}
