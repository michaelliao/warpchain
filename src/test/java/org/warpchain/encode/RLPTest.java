package org.warpchain.encode;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.warpchain.exception.DecodeException;
import org.warpchain.exception.EncodeException;
import org.warpchain.util.ByteArrayInput;
import org.warpchain.util.ByteArrayOutput;
import org.warpchain.util.ByteUtils;

public class RLPTest {

	static final byte[] rlpEmptyString = new byte[] { (byte) 0x80 };

	static final byte[] rlpH = "H".getBytes(StandardCharsets.UTF_8);

	static final byte[] rlp0x80 = ByteUtils.concat((byte) 0x81, (byte) 0x80);

	static final byte[] rlpHello = ByteUtils.concat((byte) 0x85, "Hello".getBytes(StandardCharsets.UTF_8));

	// byte[] rlpHello = ByteUtils.concat((byte) 0x85,
	// "Hello".getBytes(StandardCharsets.UTF_8));

	ByteArrayOutput output;

	@Test
	void encodeSingleByte_0x00_to_0x79() {
		for (int i = 0; i <= 0x79; i++) {
			byte b = (byte) i;
			output = new ByteArrayOutput();
			RLP.encode(b, output);
			assertArrayEquals(new byte[] { b }, output.toByteArray());
		}
	}

	@Test
	void encodeSingleByte_0x80_to_0xff() {
		for (int i = 0x80; i <= 0xff; i++) {
			byte b = (byte) i;
			output = new ByteArrayOutput();
			RLP.encode(b, output);
			assertArrayEquals(new byte[] { (byte) 0x81, b }, output.toByteArray());
		}
	}

	@Test
	void encodeEmptyByteArray() {
		output = new ByteArrayOutput();
		RLP.encode(new byte[0], output);
		assertArrayEquals(new byte[] { (byte) 0x80 }, output.toByteArray());
	}

	@Test
	void encodeByteArrayForLength_1_to_59() throws Exception {
		// string length = 26 + 26 + 10 = 62
		String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		byte[] one = "a".getBytes("UTF-8");
		output = new ByteArrayOutput();
		RLP.encode(one, output);
		assertArrayEquals(one, output.toByteArray());
		for (int i = 2; i <= 59; i++) {
			byte[] data = s.substring(0, i).getBytes("UTF-8");
			assertEquals(i, data.length);
			output = new ByteArrayOutput();
			RLP.encode(data, output);
			assertArrayEquals(ByteUtils.concat((byte) (0x80 + i), data), output.toByteArray());
		}
	}

	@Test
	void encodeByteArrayForLength_60_to_255() throws Exception {
		// string length = 32 * 8 = 256
		String s = "abcdefghijklmnopqrstuvwxyzABCDEF".repeat(8);
		for (int i = 60; i <= 255; i++) {
			byte[] data = s.substring(0, i).getBytes("UTF-8");
			assertEquals(i, data.length);
			output = new ByteArrayOutput();
			RLP.encode(data, output);
			assertArrayEquals(ByteUtils.concat(new byte[] { (byte) 0xbc, (byte) i }, data), output.toByteArray());
		}
	}

	@Test
	void encodeByteArrayForLength_256_to_65535() throws Exception {
		// string length = 32 * 2048 = 65536
		String s = "abcdefghijklmnopqrstuvwxyzABCDEF".repeat(2048);
		for (int i = 256; i <= 65535; i += 29) {
			byte[] data = s.substring(0, i).getBytes("UTF-8");
			assertEquals(i, data.length);
			output = new ByteArrayOutput();
			RLP.encode(data, output);
			assertArrayEquals(
					ByteUtils.concat(new byte[] { (byte) 0xbd, (byte) ((i & 0xff00) >> 8), (byte) (i & 0xff) }, data),
					output.toByteArray());
		}
	}

	@Test
	void encodeByteArrayForLength_65536_to_16777215() throws Exception {
		// string length = 32 * 524288 = 16777216
		String s = "abcdefghijklmnopqrstuvwxyzABCDEF".repeat(524288);
		for (int i = 65536; i <= 16777215; i += 451667) {
			byte[] data = s.substring(0, i).getBytes("UTF-8");
			assertEquals(i, data.length);
			output = new ByteArrayOutput();
			RLP.encode(data, output);
			assertArrayEquals(ByteUtils.concat(new byte[] { (byte) 0xbe, (byte) ((i & 0xff0000) >> 16),
					(byte) ((i & 0xff00) >> 8), (byte) (i & 0xff) }, data), output.toByteArray());
		}
	}

	@Test
	void encodeByteArrayForLength_16777216_to_268435455() throws Exception {
		// string length = 32 * 8388608 = 268435456
		String s = "abcdefghijklmnopqrstuvwxyzABCDEF".repeat(8388608);
		for (int i = 16777216; i <= 268435455; i += 35951177) {
			byte[] data = s.substring(0, i).getBytes("UTF-8");
			assertEquals(i, data.length);
			output = new ByteArrayOutput();
			RLP.encode(data, output);
			assertArrayEquals(
					ByteUtils.concat(new byte[] { (byte) 0xbf, (byte) ((i & 0xff000000) >> 24),
							(byte) ((i & 0xff0000) >> 16), (byte) ((i & 0xff00) >> 8), (byte) (i & 0xff) }, data),
					output.toByteArray());
		}
	}

	@Test
	void encodeByteArrayTooLarge() {
		assertThrows(EncodeException.class, () -> {
			byte[] data = new byte[0xfff_ffff + 1];
			output = new ByteArrayOutput();
			RLP.encode(data, output);
		});
	}

	@Test
	void encodeEmptyString() {
		output = new ByteArrayOutput();
		RLP.encode("", output);
		assertArrayEquals(new byte[] { (byte) 0x80 }, output.toByteArray());
	}

	@Test
	void encodeEmptyList() {
		output = new ByteArrayOutput();
		RLP.encode(List.of(), output);
		assertArrayEquals(new byte[] { (byte) 0xc0 }, output.toByteArray());
	}

	@Test
	void encodeListForElements_1_to_59() {
		for (int i = 1; i <= 59; i++) {
			List<String> list = Arrays.asList("A".repeat(i).split(""));
			output = new ByteArrayOutput();
			RLP.encode(list, output);
			byte[] result = output.toByteArray();
			assertNotNull(result);
			assertEquals(i + 1, result.length);
			assertEquals((byte) (0xc0 + i), result[0]);
			for (int j = 1; j < result.length; j++) {
				assertEquals((byte) 'A', result[j]);
			}
		}
	}

	@Test
	void encodeListForElements_60_to_255() {
		List<String> data = List.of("A".repeat(255).split(""));
		for (int i = 60; i <= 255; i++) {
			List<String> list = data.subList(0, i);
			output = new ByteArrayOutput();
			RLP.encode(list, output);
			byte[] result = output.toByteArray();
			assertNotNull(result);
			assertEquals(i + 2, result.length);
			assertEquals((byte) 0xfc, result[0]);
			assertEquals((byte) i, result[1]);
			for (int j = 2; j < result.length; j++) {
				assertEquals((byte) 'A', result[j]);
			}
		}
	}

	@Test
	void encodeListForElements_256_to_65535() {
		List<String> data = List.of("A".repeat(65535).split(""));
		for (int i = 256; i <= 65535; i += 2551) {
			List<String> list = data.subList(0, i);
			output = new ByteArrayOutput();
			RLP.encode(list, output);
			byte[] result = output.toByteArray();
			assertNotNull(result);
			assertEquals(i + 3, result.length);
			assertEquals((byte) 0xfd, result[0]);
			assertEquals((byte) ((i & 0xff00) >> 8), result[1]);
			assertEquals((byte) (i & 0xff), result[2]);
			for (int j = 3; j < result.length; j++) {
				assertEquals((byte) 'A', result[j]);
			}
		}
	}

	@Test
	void encodeListForLength_65536_to_16777215() throws Exception {
		List<String> data = List.of("A".repeat(16777215).split(""));
		for (int i = 65536; i <= 16777215; i += 451667) {
			List<String> list = data.subList(0, i);
			output = new ByteArrayOutput();
			RLP.encode(list, output);
			byte[] result = output.toByteArray();
			assertNotNull(result);
			assertEquals(i + 4, result.length);
			assertEquals((byte) 0xfe, result[0]);
			assertEquals((byte) ((i & 0xff0000) >> 16), result[1]);
			assertEquals((byte) ((i & 0xff00) >> 8), result[2]);
			assertEquals((byte) (i & 0xff), result[3]);
			for (int j = 4; j < result.length; j++) {
				assertEquals((byte) 'A', result[j]);
			}
		}
	}

	@Disabled("This test may consume huge memory and may cause OutOfMemoryError.")
	@Test
	void encodeListForLength_16777216_to_268435455() throws Exception {
		List<String> data = List.of("A".repeat(268435455).split(""));
		for (int i = 16777216; i <= 268435455; i += 35951177) {
			List<String> list = data.subList(0, i);
			output = new ByteArrayOutput();
			RLP.encode(list, output);
			byte[] result = output.toByteArray();
			assertNotNull(result);
			assertEquals(i + 5, result.length);
			assertEquals((byte) 0xff, result[0]);
			assertEquals((byte) ((i & 0xff000000) >> 24), result[1]);
			assertEquals((byte) ((i & 0xff0000) >> 16), result[2]);
			assertEquals((byte) ((i & 0xff00) >> 8), result[3]);
			assertEquals((byte) (i & 0xff), result[4]);
			for (int j = 3; j < result.length; j++) {
				assertEquals((byte) 'A', result[j]);
			}
		}
	}

	@Disabled("This test may consume huge memory and may cause OutOfMemoryError.")
	@Test
	void encodeListTooLarge() {
		assertThrows(EncodeException.class, () -> {
			String s = "abcdefghijklmnopqrstuvwxyzABCDEF".repeat(8388608);
			List<String> list = List.of(s.split(""));
			output = new ByteArrayOutput();
			RLP.encode(list, output);
		});
	}

	@ParameterizedTest
	@MethodSource
	void encodeStringSamples(String input, byte[] result) {
		output = new ByteArrayOutput();
		RLP.encode(input, output);
		assertArrayEquals(result, output.toByteArray());
	}

	static List<Arguments> encodeStringSamples() {
		return List.of(
				// "A" -> 0x41
				Arguments.arguments("A", new byte[] { (byte) 0x41 }),
				// "AB" -> 0x82 0x41_43
				Arguments.arguments("AB", new byte[] { (byte) 0x82, 0x41, 0x42 }),
				// "DOG" -> 0x83 0x44_4f_47
				Arguments.arguments("DOG", new byte[] { (byte) 0x83, 0x44, 0x4f, 0x47 }),
				// "" -> 0x80
				Arguments.arguments("", new byte[] { (byte) 0x80 }));
	}

	@ParameterizedTest
	@MethodSource("decodedListAndEncodedBytes")
	void encodeListSamples(List<Object> list, byte[] rlpEncoded) {
		output = new ByteArrayOutput();
		RLP.encode(list, output);
		assertArrayEquals(rlpEncoded, output.toByteArray());
	}

	@Test
	void decodeAsSingleByte() {
		assertEquals(0x00, RLP.decodeAsSingleByte(prepareInput("00")));
		assertEquals(0x01, RLP.decodeAsSingleByte(prepareInput("01")));
		assertEquals(0x02, RLP.decodeAsSingleByte(prepareInput("02")));
		assertEquals(0x1f, RLP.decodeAsSingleByte(prepareInput("1f")));
		assertEquals(0x7f, RLP.decodeAsSingleByte(prepareInput("7f")));

		assertEquals((byte) 0x80, RLP.decodeAsSingleByte(prepareInput("81 80")));
		assertEquals((byte) 0x81, RLP.decodeAsSingleByte(prepareInput("81 81")));
		assertEquals((byte) 0x82, RLP.decodeAsSingleByte(prepareInput("81 82")));
		assertEquals((byte) 0xff, RLP.decodeAsSingleByte(prepareInput("81 ff")));

		// NOTE 0x81 + single byte is also OK:
		assertEquals(0x00, RLP.decodeAsSingleByte(prepareInput("81 00")));
		assertEquals(0x01, RLP.decodeAsSingleByte(prepareInput("81 01")));
		assertEquals(0x02, RLP.decodeAsSingleByte(prepareInput("81 02")));
		assertEquals(0x7f, RLP.decodeAsSingleByte(prepareInput("81 7f")));
	}

	@Test
	void decodeSingleByteFailed() {
		assertThrows(DecodeException.class, () -> {
			RLP.decodeAsSingleByte(prepareInput("82 00 01"));
		});
		assertThrows(DecodeException.class, () -> {
			RLP.decodeAsSingleByte(prepareInput("83 00 01 02"));
		});
		assertThrows(DecodeException.class, () -> {
			RLP.decodeAsSingleByte(prepareInput("80"));
		});
	}

	@Test
	void decodeByteArray() throws Exception {
		assertArrayEquals(new byte[] {}, RLP.decodeAsByteArray(prepareInput("80")));

		assertArrayEquals(new byte[] { 0x41 }, RLP.decodeAsByteArray(prepareInput("41")));
		assertArrayEquals(new byte[] { 0x41 }, RLP.decodeAsByteArray(prepareInput("81 41")));
		assertArrayEquals(new byte[] { 0x41, 0x42 }, RLP.decodeAsByteArray(prepareInput("82 41 42")));
		assertArrayEquals(new byte[] { 0x41, 0x42, 0x43 }, RLP.decodeAsByteArray(prepareInput("83 41 42 43")));

		assertArrayEquals("A".repeat(59).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bb " + "41".repeat(59))));

		assertArrayEquals("A".repeat(60).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bc 3c" + "41".repeat(60))));
		assertArrayEquals("A".repeat(61).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bc 3d" + "41".repeat(61))));
		assertArrayEquals("A".repeat(255).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bc ff" + "41".repeat(255))));

		assertArrayEquals("A".repeat(256).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bd 01 00" + "41".repeat(256))));
		assertArrayEquals("A".repeat(257).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bd 01 01" + "41".repeat(257))));
		assertArrayEquals("A".repeat(65535).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bd ff ff" + "41".repeat(65535))));

		assertArrayEquals("A".repeat(65536).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("be 01 00 00" + "41".repeat(65536))));
		assertArrayEquals("A".repeat(65537).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("be 01 00 01" + "41".repeat(65537))));
		assertArrayEquals("A".repeat(16777215).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("be ff ff ff" + "41".repeat(16777215))));

		assertArrayEquals("A".repeat(16777216).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bf 01 00 00 00" + "41".repeat(16777216))));
		assertArrayEquals("A".repeat(268435455).getBytes("UTF-8"),
				RLP.decodeAsByteArray(prepareInput("bf 0f ff ff ff" + "41".repeat(268435455))));
	}

	@Test
	void decodeByteArrayAsString() {
		assertEquals("", RLP.decode(prepareInput("80"), true));
		assertEquals("A", RLP.decode(prepareInput("41"), true));
		assertEquals("A", RLP.decode(prepareInput("81 41"), true));
		assertEquals("AB", RLP.decode(prepareInput("82 41 42"), true));
		assertEquals("ABC", RLP.decode(prepareInput("83 41 42 43"), true));
	}

	@Test
	void decodeAsString() {
		assertEquals("", RLP.decodeAsString(prepareInput("80")));
		assertEquals("A", RLP.decodeAsString(prepareInput("41")));
		assertEquals("A", RLP.decodeAsString(prepareInput("81 41")));
		assertEquals("AB", RLP.decodeAsString(prepareInput("82 41 42")));
		assertEquals("ABC", RLP.decodeAsString(prepareInput("83 41 42 43")));
	}

	@Test
	void decodeAsList() {
		assertEquals(List.of(), RLP.decodeAsList(prepareInput("c0"), true));
		assertEquals(List.of("A"), RLP.decodeAsList(prepareInput("c1 41"), true));
		assertEquals(List.of("A"), RLP.decodeAsList(prepareInput("c1 81 41"), true));
		assertEquals(List.of("A", "B"), RLP.decodeAsList(prepareInput("c2 41 42"), true));
		assertEquals(List.of("A", "B", "C"), RLP.decodeAsList(prepareInput("c3 41 42 43"), true));
		assertEquals(listByElement("A", 59), RLP.decodeAsList(prepareInput("fb " + "41".repeat(59)), true));

		assertEquals(listByElement("A", 60), RLP.decodeAsList(prepareInput("fc 3c " + "41".repeat(60)), true));
		assertEquals(listByElement("A", 255), RLP.decodeAsList(prepareInput("fc ff " + "41".repeat(255)), true));

		assertEquals(listByElement("A", 256), RLP.decodeAsList(prepareInput("fd 01 00 " + "41".repeat(256)), true));
		assertEquals(listByElement("A", 65535), RLP.decodeAsList(prepareInput("fd ff ff " + "41".repeat(65535)), true));

		assertEquals(listByElement("A", 65536),
				RLP.decodeAsList(prepareInput("fe 01 00 00 " + "41".repeat(65536)), true));
		assertEquals(listByElement("A", 16777215),
				RLP.decodeAsList(prepareInput("fe ff ff ff " + "41".repeat(16777215)), true));

		assertEquals(listByElement("A", 16777216),
				RLP.decodeAsList(prepareInput("ff 01 00 00 00 " + "41".repeat(16777216)), true));
		// OutOfMemoryError:
		// assertEquals(listByElement("A", 268435455),
		// RLP.decodeAsList(
		// prepareInput("ff 0f ff ff ff " + "41".repeat(268435455)), true));
	}

	@ParameterizedTest
	@MethodSource("decodedListAndEncodedBytes")
	void decodeAsListWithNestedList(List<Object> list, byte[] rlpEncoded) {
		var input = new ByteArrayInput(rlpEncoded);
		List<Object> result = RLP.decodeAsList(input, true);
		assertEquals(list, result);
	}

	static List<Arguments> decodedListAndEncodedBytes() {
		return List.of(
				// [""] -> 0xc1 0x80
				Arguments.arguments(List.of(""), new byte[] { (byte) 0xc1, (byte) 0x80 }),
				// ["A"] -> 0xc1 0x41
				Arguments.arguments(List.of("A"), new byte[] { (byte) 0xc1, 0x41 }),
				// ["DOG"] -> 0xc1 0x83:44_4f_47
				Arguments.arguments(List.of("DOG"), new byte[] { (byte) 0xc1, (byte) 0x83, 0x44, 0x4f, 0x47 }),
				// ["A", "DOG"] -> 0xc2 0x41 0x83:44_4f_47
				Arguments.arguments(List.of("A", "DOG"),
						new byte[] { (byte) 0xc2, 0x41, (byte) 0x83, 0x44, 0x4f, 0x47 }),
				// ["A", ["DOG"]] -> 0xc2 0x41 0xc1 0x83:44_4f_47
				Arguments.arguments(List.of("A", List.of("DOG")),
						new byte[] { (byte) 0xc2, 0x41, (byte) 0xc1, (byte) 0x83, 0x44, 0x4f, 0x47 }),
				// ["A", [], ["DOG"]] -> 0xc3 0x41 0xc0 0xc1 0x83:44_4f_47
				Arguments.arguments(List.of("A", List.of(), List.of("DOG")),
						new byte[] { (byte) 0xc3, 0x41, (byte) 0xc0, (byte) 0xc1, (byte) 0x83, 0x44, 0x4f, 0x47 }),
				// ["A", [], ["DOG", ["CAT"]]] -> 0xc3 0x41 0xc0 0xc2 0x83:44_4f_47 0xc1
				// 0x83:43_41_54
				Arguments.arguments(List.of("A", List.of(), List.of("DOG", List.of("CAT"))),
						new byte[] { (byte) 0xc3, 0x41, (byte) 0xc0, (byte) 0xc2, (byte) 0x83, 0x44, 0x4f, 0x47,
								(byte) 0xc1, (byte) 0x83, 0x43, 0x41, 0x54 }),
				// [[[[[]]]]] -> 0xc1 0xc1 0xc1 0xc1 0xc0
				Arguments.arguments(List.of(List.of(List.of(List.of(List.of())))),
						new byte[] { (byte) 0xc1, (byte) 0xc1, (byte) 0xc1, (byte) 0xc1, (byte) 0xc0 }),
				// [] -> 0xc0
				Arguments.arguments(List.of(), new byte[] { (byte) 0xc0 }));
	}

	static ByteArrayInput prepareInput(String hex) {
		hex = hex.replace(" ", "").replace("_", "");
		byte[] input = ByteUtils.fromHexString(hex);
		return new ByteArrayInput(input);
	}

	static List<String> listByElement(String s, int repeat) {
		List<String> list = new ArrayList<>(repeat);
		for (int i = 0; i < repeat; i++) {
			list.add(s);
		}
		return list;
	}
}
