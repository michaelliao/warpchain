package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.warpchain.crypto.ECKey;

public class EthereumUtilsTest {

	@Test
	void exportPrivateKey() {
		String hex = "15c5b377eb48b2ab984e77bf3072009096a7c292390dc66ce68b86043047650c";
		BigInteger privateKey = new BigInteger(hex, 16);
		assertEquals("0x" + hex, EthereumUtils.exportPrivateKey(privateKey));
		assertEquals("0x" + hex, EthereumUtils.exportPrivateKey(ECKey.fromPrivateKey(privateKey)));
	}

	@Test
	void fromPrivateKey() {
		String hex = "15c5b377eb48b2ab984e77bf3072009096a7c292390dc66ce68b86043047650c";
		ECKey key1 = EthereumUtils.fromPrivateKey(hex);
		ECKey key2 = EthereumUtils.fromPrivateKey("0x" + hex);
		assertEquals("0x" + hex, EthereumUtils.exportPrivateKey(key1));
		assertEquals("0x" + hex, EthereumUtils.exportPrivateKey(key2));
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toAddressByECKey(String hexPrivateKey, String hexPublicKey, String address) {
		BigInteger privateKey = new BigInteger(hexPrivateKey.substring(2), 16);
		assertEquals(address, EthereumUtils.toAddress(ECKey.fromPrivateKey(privateKey)));
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toAddressByBigInteger(String hexPrivateKey, String hexPublicKey, String address) {
		BigInteger publicKey = new BigInteger(hexPublicKey.substring(2), 16);
		assertEquals(address, EthereumUtils.toAddress(publicKey));
	}

	@Test
	void toAddressByteArray() {
		byte[] compressed = ByteUtils
				.fromHexString("03ee248749a502eda277c355248c7910cd3d141be474898821a213f81ba1b88700");
		byte[] uncompressed = ByteUtils.fromHexString(
				"04ee248749a502eda277c355248c7910cd3d141be474898821a213f81ba1b8870094abf48255a96c458898d030da9d852901477d4f1fb7d118f4c360b561771a7b");
		String address = "0x0f923C2b18D13765e2f2003875b120b468F210cA";
		assertEquals(address, EthereumUtils.toAddress(compressed));
		assertEquals(address, EthereumUtils.toAddress(uncompressed));
	}

	/**
	 * Mnemonic words: beach silent bind deputy hero glad boring rocket gain
	 */
	@MethodSource
	static List<Arguments> keyPairs() {
		// m/44'/60'/0'/0/0 ~ 5:
		return List.of(
				// 0x0f923C2b18D13765e2f2003875b120b468F210cA
				Arguments.of("0x15c5b377eb48b2ab984e77bf3072009096a7c292390dc66ce68b86043047650c",
						"0x03ee248749a502eda277c355248c7910cd3d141be474898821a213f81ba1b88700",
						"0x0f923C2b18D13765e2f2003875b120b468F210cA"),
				// 0x083b414Fc2E3BDbd79935319FA1e0c9D7D271957
				Arguments.of("0xb3f68fc99e976c5819321f21c27cdca74891986b69394715cc1ceb14f1aaa345",
						"0x036ac926ff62d1fff8ed73811532b4a20536940a56d72e7c199fd51a951352cc98",
						"0x083b414Fc2E3BDbd79935319FA1e0c9D7D271957"),
				// 0x9714906D271A8B31b5df3dDd4737231C7919741d
				Arguments.of("0xf32591e963a681c1f372b3938943e8cffa68176a046a202f1ac5df7b6bfb1b25",
						"0x02e6c166e2ed21330ee7cf462af22049a0e8932f139ab7c0618b8c47fdd53da8c5",
						"0x9714906D271A8B31b5df3dDd4737231C7919741d"),
				// 0xE813bF68F94579c2BaDF06b11A9e49e4046429B7
				Arguments.of("0xe2064f8df42bc58c189ce797709b9603cc1652c5128fa2432874430c5e95bf71",
						"0x02303558705575ac2e92b78b4e270b94e882a39973db3a0e58cf652decc55a6409",
						"0xE813bF68F94579c2BaDF06b11A9e49e4046429B7"),
				// 0xfC78eDD56c0784453792047EAb40f552bEB2C66d
				Arguments.of("0xa8f22c5bc4911711443120fd065731165fd72125d222a7a214a52b3853446583",
						"0x02f9e0c80d5cf22e4d3bb70b4a0cfbef43cc8ff9b34aa7ded641d7030c56a8a074",
						"0xfC78eDD56c0784453792047EAb40f552bEB2C66d"),
				// 0x9DD60433BD70f8103085d7A2f78b890c9Bf8f054
				Arguments.of("0xec3f67c2543726779f048ff4661a640d9b7b3855ac1f645228674deacf252f08",
						"0x02be7483b79a6eff5b48a9f5a82f3be5c632ca958549f969656e26bc9b9f24306c",
						"0x9DD60433BD70f8103085d7A2f78b890c9Bf8f054"));
	}
}
