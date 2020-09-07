package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.UncheckedIOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ByteArrayInputTest {

	private ByteArrayInput input;

	@BeforeEach
	void setUp() throws Exception {
		input = new ByteArrayInput("HelloWorld".getBytes("UTF-8"));
	}

	@Test
	void testReadByte() throws Exception {
		ByteAssertions.assertByteEquals('H', input.readByte());
		ByteAssertions.assertByteEquals('e', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		ByteAssertions.assertByteEquals('W', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		ByteAssertions.assertByteEquals('r', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('d', input.readByte());
		assertThrows(UncheckedIOException.class, () -> {
			input.readByte();
		});
	}

	@Test
	void testReadByte2() throws Exception {
		input = new ByteArrayInput(new byte[] { 1, 2, 3, 0, -1, -2, 127, -127, -128 });
		ByteAssertions.assertByteEquals(1, input.readByte());
		ByteAssertions.assertByteEquals(2, input.readByte());
		ByteAssertions.assertByteEquals(3, input.readByte());
		ByteAssertions.assertByteEquals(0, input.readByte());
		ByteAssertions.assertByteEquals(-1, input.readByte());
		ByteAssertions.assertByteEquals(-2, input.readByte());
		ByteAssertions.assertByteEquals(127, input.readByte());
		ByteAssertions.assertByteEquals(-127, input.readByte());
		ByteAssertions.assertByteEquals(-128, input.readByte());
		assertThrows(UncheckedIOException.class, () -> {
			input.readByte();
		});
	}

	@Test
	void testRead() {
		byte[] buffer1 = new byte[5];
		assertEquals(5, input.read(buffer1, 0, 5));
		ByteAssertions.assertByteArrayEquals("Hello", buffer1);

		byte[] buffer2 = new byte[3];
		assertEquals(2, input.read(buffer2, 1, 2));
		assertArrayEquals(new byte[] { 0, 'W', 'o' }, buffer2);

		byte[] buffer3 = new byte[6];
		assertEquals(3, input.read(buffer3, 2, 4));
		assertArrayEquals(new byte[] { 0, 0, 'r', 'l', 'd', 0 }, buffer3);
	}

	@Test
	void testReadAllBytes() {
		ByteAssertions.assertByteArrayEquals("HelloWorld", input.readAllBytes());
		assertEquals(0, input.available());
		ByteAssertions.assertByteArrayEquals("", input.readAllBytes());
	}

	@Test
	void testReadAllBytes2() {
		input.readNBytes(2);
		ByteAssertions.assertByteArrayEquals("lloWorld", input.readAllBytes());
		assertEquals(0, input.available());
		ByteAssertions.assertByteArrayEquals("", input.readAllBytes());
	}

	@Test
	void testReadNBytes() {
		assertEquals(10, input.available());
		ByteAssertions.assertByteArrayEquals("He", input.readNBytes(2));
		assertEquals(8, input.available());
		ByteAssertions.assertByteArrayEquals("lloW", input.readNBytes(4));
		assertEquals(4, input.available());
		assertThrows(UncheckedIOException.class, () -> {
			input.readNBytes(5);
		});
	}

	@Test
	void testReadInt8() {
		// "He" = 0x48_65
		assertEquals(0x48, input.readInt8());
		assertEquals(0x65, input.readInt8());
		assertEquals(8, input.available());
	}

	@Test
	void testReadInt16() {
		// "He" = 0x48_65
		assertEquals(0x48_65, input.readInt16());
		assertEquals(8, input.available());
	}

	@Test
	void testReadInt24() {
		// "Hel" = 0x48_65_6c
		assertEquals(0x48_65_6c, input.readInt24());
		assertEquals(7, input.available());
	}

	@Test
	void testReadInt32() {
		// "Hell" = 0x48_65_6c_6c
		assertEquals(0x48_65_6c_6c, input.readInt32());
		assertEquals(6, input.available());
	}

	@Test
	void testReadLong() {
		// "HelloWor" = 0x48_65_6c_6c_6f_57_6f_72
		assertEquals(0x48_65_6c_6c_6f_57_6f_72L, input.readLong());
		assertEquals(2, input.available());
	}

	@Test
	void testSkip() {
		assertEquals(2, input.skip(2)); // skip 'He'
		assertEquals(8, input.available());
		ByteAssertions.assertByteEquals('l', input.readByte());
		assertEquals(4, input.skip(4)); // skip 'loWo'
		assertEquals(3, input.available());
		assertEquals(3, input.skip(4)); // skip 'rld' and EOF
		assertEquals(0, input.available());
	}

	@Test
	void testAvailable() {
		assertEquals(10, input.available());
		ByteAssertions.assertByteEquals('H', input.readByte());
		ByteAssertions.assertByteEquals('e', input.readByte());
		assertEquals(8, input.available());
		input.skip(2);
		assertEquals(6, input.available());
		ByteAssertions.assertByteEquals('o', input.readByte());
		assertEquals(5, input.available());
		ByteAssertions.assertByteEquals('W', input.readByte());
		assertEquals(4, input.available());
		ByteAssertions.assertByteArrayEquals("orld", input.readNBytes(4));
		assertEquals(0, input.available());
	}

	@Test
	void testMark() {
		ByteAssertions.assertByteEquals('H', input.readByte());
		ByteAssertions.assertByteEquals('e', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		input.mark();
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		input.reset();
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		ByteAssertions.assertByteArrayEquals("World", input.readAllBytes());
		assertThrows(UncheckedIOException.class, () -> {
			input.readByte();
		});
	}

	@Test
	void testReset() {
		ByteAssertions.assertByteEquals('H', input.readByte());
		ByteAssertions.assertByteEquals('e', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		input.reset();
		ByteAssertions.assertByteEquals('H', input.readByte());
		ByteAssertions.assertByteEquals('e', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		input.mark();
		ByteAssertions.assertByteArrayEquals("World", input.readNBytes(5));
		input.reset();
		ByteAssertions.assertByteEquals('W', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		ByteAssertions.assertByteArrayEquals("rld", input.readAllBytes());
		assertThrows(UncheckedIOException.class, () -> {
			input.readByte();
		});
	}

	@Test
	void testReset2() throws Exception {
		input = new ByteArrayInput("HelloWorld".getBytes("UTF-8"), 2, 6); // valid buffer is "lloWor"
		assertEquals(6, input.available());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		assertEquals(4, input.available());
		input.reset();
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('l', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		input.mark();
		ByteAssertions.assertByteArrayEquals("Wor", input.readAllBytes());
		input.reset();
		ByteAssertions.assertByteEquals('W', input.readByte());
		ByteAssertions.assertByteEquals('o', input.readByte());
		ByteAssertions.assertByteArrayEquals("r", input.readAllBytes());
		assertEquals(0, input.available());
		assertThrows(UncheckedIOException.class, () -> {
			input.readByte();
		});
	}

	@Test
	void testReset3() throws Exception {
		input = new ByteArrayInput("HelloWorld".getBytes("UTF-8"), 2, 10); // valid buffer is "lloWorld"
		assertEquals(8, input.available());
		ByteAssertions.assertByteArrayEquals("lloWorld", input.readAllBytes());
		assertEquals(0, input.available());
	}
}
