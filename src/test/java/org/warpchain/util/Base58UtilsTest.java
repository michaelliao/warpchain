package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class Base58UtilsTest {

	@Test
	void testEncode() {
		byte[] data = new BigInteger("1b1234ff09091", 16).toByteArray();
		assertEquals("4ih2JerSC", Base58Utils.encode(data));
	}

	@Test
	public void testDecode() {
		byte[] data = Base58Utils.decode("4ih2JerSC");
		assertTrue(0 == new BigInteger("1b1234ff09091", 16).compareTo(new BigInteger(1, data)));
	}
}
