package org.warpchain.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BitStringTest {

	// 12 34 56 70 ff 34 56 60
	static final byte[] BYTE_DATA = new byte[] { 0x12, 0x34, 0x56, 0x70, (byte) 0xff, 0x34, 0x56, 0x60 };

	static final String BIT_STR = "000" + Long.toBinaryString(0x12345670_ff345660L);

	static final String BIT_STR_34_56_70 = "00" + Long.toBinaryString(0x345670L);
	static final String BIT_STR_34_56_60 = "00" + Long.toBinaryString(0x345660L);

	@Test
	void testBitValueAt() {
		BitString bs = new BitString(BYTE_DATA);
		// 0x12 = 0001 0010
		assertEquals(0, bs.bitValueAt(0));
		assertEquals(0, bs.bitValueAt(1));
		assertEquals(0, bs.bitValueAt(2));
		assertEquals(1, bs.bitValueAt(3));
		assertEquals(0, bs.bitValueAt(4));
		assertEquals(0, bs.bitValueAt(5));
		assertEquals(1, bs.bitValueAt(6));
		assertEquals(0, bs.bitValueAt(7));
		// 0x34 = 0011 0100
		assertEquals(0, bs.bitValueAt(8));
		assertEquals(0, bs.bitValueAt(9));
		assertEquals(1, bs.bitValueAt(10));
		assertEquals(1, bs.bitValueAt(11));
		assertEquals(0, bs.bitValueAt(12));
		assertEquals(1, bs.bitValueAt(13));
		assertEquals(0, bs.bitValueAt(14));
		assertEquals(0, bs.bitValueAt(15));
		// 0x23 = 0010 0011
		BitString bs2 = bs.substring(4);
		assertEquals(0, bs2.bitValueAt(0));
		assertEquals(0, bs2.bitValueAt(1));
		assertEquals(1, bs2.bitValueAt(2));
		assertEquals(0, bs2.bitValueAt(3));
		assertEquals(0, bs2.bitValueAt(4));
		assertEquals(0, bs2.bitValueAt(5));
		assertEquals(1, bs2.bitValueAt(6));
		assertEquals(1, bs2.bitValueAt(7));
	}

	@Test
	void testLength() {
		BitString bs = new BitString(BYTE_DATA);
		assertEquals(64, bs.length());
		assertEquals(63, bs.substring(1).length());
		assertEquals(63, bs.substring(1, 64).length());
		assertEquals(62, bs.substring(1, 63).length());
		assertEquals(4, bs.substring(2, 6).length());
		assertEquals(1, bs.substring(2, 3).length());
		assertEquals(0, bs.substring(2, 2).length());
	}

	@Test
	void testToString() {
		BitString bs = new BitString(BYTE_DATA);
		assertEquals(BIT_STR, bs.toString());
	}

	@Test
	void testSubString() {
		BitString bs1 = new BitString(BYTE_DATA).substring(8, 8 + 24);
		assertEquals(BIT_STR_34_56_70, bs1.toString());
		BitString bs2 = new BitString(BYTE_DATA).substring(40, 40 + 24);
		assertEquals(BIT_STR_34_56_60, bs2.toString());

		BitString fullBits = new BitString(BYTE_DATA);
		String fullStr = fullBits.toString();
		for (int i = 0; i <= 64; i++) {
			for (int j = i; j <= 64; j++) {
				assertEquals(fullStr.substring(i, j), fullBits.substring(i, j).toString());
			}
		}
	}

	@Test
	void testStartsWith() {
		// FIXME:
	}

	@Test
	void testEquals() {
		BitString bs = new BitString(BYTE_DATA);
		BitString bs1 = bs.substring(+8, 32); // 34_56_01110000
		BitString bs2 = bs.substring(40, 64); // 34_56_01100000
		assertFalse(bs1.equals(bs2));

		BitString bs3 = bs.substring(+8, 32 - 5); // 34_56_01110000
		BitString bs4 = bs.substring(40, 64 - 5); // 34_56_01100000
		assertTrue(bs3.equals(bs4));
	}

	@Test
	void testSharedPrefix() {
		BitString bs = new BitString(BYTE_DATA);
		BitString bs1 = bs.substring(+8, 32); // 34_56_01110000
		BitString bs2 = bs.substring(40, 64); // 34_56_01100000
		assertEquals("0011010001010110" + "011", BitString.sharedPrefix(bs1, bs2).toString());
	}

}
