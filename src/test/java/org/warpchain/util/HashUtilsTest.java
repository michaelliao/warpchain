package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class HashUtilsTest {

	@ParameterizedTest
	@CsvSource({ // test data
			"hello, 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
			"Hello, 185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969",
			"tdsyipbjpbaehbeurwzbasyjricvlkutmajspixe, 9bf10aff38b5db7dacb97f3985c3d0dc034e03ec58988917ddc4374b0bc23923" })
	void sha256(String inputString, String expectedHash) {
		byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
		byte[] hash = HashUtils.sha256(input);
		String actualHash = ByteUtils.toHexString(hash);
		assertEquals(expectedHash, actualHash);
	}

	@ParameterizedTest
	@CsvSource({ // test data
			"hello, 9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50",
			"Hello, 70bc18bef5ae66b72d1995f8db90a583a60d77b4066e4653f1cead613025861c",
			"tdsyipbjpbaehbeurwzbasyjricvlkutmajspixe, fd1c463d317c0696dce634936cbf94d4c4cf79af3f258841f6adfeae5f0aa9ba" })
	void dsha256(String inputString, String expectedHash) {
		byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
		byte[] hash = HashUtils.dsha256(input);
		String actualHash = ByteUtils.toHexString(hash);
		assertEquals(expectedHash, actualHash);
	}

	@ParameterizedTest
	@CsvSource({ // test data
			"hello, 108f07b8382412612c048d07d13f814118445acd",
			"Hello, d44426aca8ae0a69cdbc4021c64fa5ad68ca32fe",
			"tdsyipbjpbaehbeurwzbasyjricvlkutmajspixe, 87de78260083c9c08a220722447d8487de3107ef" })
	void ripeMd160(String inputString, String expectedHash) {
		byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
		byte[] hash = HashUtils.ripeMd160(input);
		String actualHash = ByteUtils.toHexString(hash);
		assertEquals(expectedHash, actualHash);
	}

	@ParameterizedTest
	@CsvSource({ // test data
			"hello, 1c8aff950685c2ed4bc3174f3472287b56d9517b9c948127319a09a7a36deac8",
			"Hello, 06b3dfaec148fb1bb2b066f10ec285e7c9bf402ab32aa78a5d38e34566810cd2",
			"tdsyipbjpbaehbeurwzbasyjricvlkutmajspixe, a1cc624cc87d855c6f951b7cf805892aa5d901c7cff0042d240fa2c4ac60a351" })
	void keccak256(String inputString, String expectedHash) {
		byte[] input = inputString.getBytes(StandardCharsets.UTF_8);
		byte[] hash = HashUtils.keccak256(input);
		String actualHash = ByteUtils.toHexString(hash);
		assertEquals(expectedHash, actualHash);
	}

}
