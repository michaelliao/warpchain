package org.warpchain.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;

public class ByteAssertions {

	public static void assertByteEquals(char expected, byte actual) {
		Assertions.assertEquals((byte) (0xff & expected), actual, String.format(
				"expected: <0x%02x/'%s'> but was: <0x%02x/%s>", (int) expected, expected, actual & 0xff, actual));
	}

	public static void assertByteEquals(int expected, byte actual) {
		Assertions.assertEquals(expected, actual, String.format("expected: <0x%02x/'%s'> but was: <0x%02x/%s>",
				expected, expected, actual & 0xff, actual));
	}

	public static void assertByteArrayEquals(String expected, byte[] actual) {
		byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
		Assertions.assertArrayEquals(expectedBytes, actual,
				String.format("expected: <0x%02x/\"%s\"> but was: <0x%02x/%s>", new BigInteger(1, expectedBytes),
						expected, new BigInteger(1, actual), Arrays.toString(actual)));
	}
}
