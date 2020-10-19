package org.warpchain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.warpchain.crypto.ECKey;

public class BitcoinUtilsTest {

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toWIF(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		String actualWif = BitcoinUtils.toWIF(new BigInteger(hexPrivateKey, 16));
		assertEquals(wif, actualWif);
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void fromWIF(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		ECKey key = BitcoinUtils.fromWIF(wif);
		assertTrue(0 == new BigInteger(hexPrivateKey, 16).compareTo(key.getPrivateKey()));
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toAddressByBigInteger(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		String actualAddr = BitcoinUtils.toAddress(new BigInteger(hexPublicKey, 16));
		assertEquals(address, actualAddr);
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toAddressByByteArray(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		String actualAddr = BitcoinUtils.toAddress(ByteUtils.fromHexString(hexPublicKey));
		assertEquals(address, actualAddr);
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void toAddressByECKey(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		ECKey key = BitcoinUtils.fromWIF(wif);
		String actualAddr = BitcoinUtils.toAddress(key);
		assertEquals(address, actualAddr);
	}

	@Test
	void fromWIFFailed() {
		assertThrows(IllegalArgumentException.class, () -> {
			// old WIF format:
			BitcoinUtils.fromWIF("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ");
		});
	}

	@Test
	void toAddressFailed() {
		assertThrows(IllegalArgumentException.class, () -> {
			// not start with 02 or 03:
			BitcoinUtils.toAddress(
					new BigInteger("04859a42554b255917b971c6d0e322651db76d8e33b7e6686b345f22e57048c750", 16));
		});
		assertThrows(IllegalArgumentException.class, () -> {
			// empty byte array:
			BitcoinUtils.toAddress(new byte[0]);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			// not 33 bytes
			BitcoinUtils
					.toAddress(new BigInteger("02859a42554b255917b971c6d0e322651db76d8e33b7e6686b345f22e57048c7", 16));
		});
	}

	/**
	 * Mnemonic words: beach silent bind deputy hero glad boring rocket gain
	 */
	@MethodSource
	static List<Arguments> keyPairs() {
		// m/44'/0'/0'/0/0 ~ 5:
		return List.of(
				// L4c1iSf8e3gW3rEAbKktLhbCFuCWnFfVfsgQW26c6QoaHQGwthCG
				Arguments.of("dc55bc0cbde942349d563b3f845f6fcec7362173442b27b70ae7b20d13325a82",
						"L4c1iSf8e3gW3rEAbKktLhbCFuCWnFfVfsgQW26c6QoaHQGwthCG",
						"02859a42554b255917b971c6d0e322651db76d8e33b7e6686b345f22e57048c750",
						"1KNdhXP6ZVPzEDBXtEfcJM8YcYiR4Ni8Qo"),
				// KwUefgTV5FEmMkHtXteAPzNgmj26ayTTyZ5MuNMCC2mzUW14A7tD
				Arguments.of("07af74bc9c0b73d84c24b2de0f82babcb8c208d142539c0776e5e29d9472cfe8",
						"KwUefgTV5FEmMkHtXteAPzNgmj26ayTTyZ5MuNMCC2mzUW14A7tD",
						"02bb6ae99eed56005ed7a49dfd0ba540f4592f050d8cb2bb9f6aa1c10d643d5362",
						"1CczufdcQFberpwi6umLA4aUShuWRV7BB8"),
				// Ky2PYhNC7qs4SEBTQP6dAVozEQfu153CCn2Bd4BDAAoh1drYxSDQ
				Arguments.of("35d9c595f126e0d3876609f46b274a24400cbbd82a61078178a4926d997d3b1a",
						"Ky2PYhNC7qs4SEBTQP6dAVozEQfu153CCn2Bd4BDAAoh1drYxSDQ",
						"03b91d0a4de9b893eb6f3693088540807a73467b82b1d370ba7e90b4d8dc675767",
						"12udDTnX1EhUa9YuQG3Qhto4VFaj4xD9Xy"),
				// KxtiC1y1Nr1sRcFvyYJA1A3Vx3yzVyLfwf6kZwuNBrqNnY2b1a3W
				Arguments.of("31e6890a53ff64e82ceffac238aa435e488ce552644693def445b80051da634f",
						"KxtiC1y1Nr1sRcFvyYJA1A3Vx3yzVyLfwf6kZwuNBrqNnY2b1a3W",
						"0254a58625017dd7339b17cd7d2a8468d28cfa0dcf5e3eee9198d776cd0faf0ad7",
						"14dMxhd2566hmtB4Q5hcPSiyiKpnCgR4RG"),
				// Kwn2ofhF63ahDEU8LxsWAxP1BTrL9DLRgKY9vgeyMdJCEktwke34
				Arguments.of("10a08e554cff37443a29e659feeb921d966baf4e4c079152f13820e31081e534",
						"Kwn2ofhF63ahDEU8LxsWAxP1BTrL9DLRgKY9vgeyMdJCEktwke34",
						"03ff345b530f24877f4db5405202497af5a263fe7ba0646444ef56f930eebd07a3",
						"1F1A5DFkrPiCZFSZLF6ocTAiarv5gFr4JW"),
				// L26HpKaVXifDTEn11L4pQ7WJ2ZPY7jagyWsdQBrKZZW9cx1jXLTs
				Arguments.of("915eaa2b553d7e4c8dd9823be0d0897cbb819ce5dd9bfc9eaa3142c527ec69a6",
						"L26HpKaVXifDTEn11L4pQ7WJ2ZPY7jagyWsdQBrKZZW9cx1jXLTs",
						"039ab753a8481d965af517e2c01db595b539398052404bc077ff798b8ddce49c94",
						"1CWHy4hSWz4YDjqYKpDMTopRkxuWMy84mp"));
	}

	@MethodSource
	static List<Arguments> bech32Addresses() {
		// m/84'/0'/0'/0/0 ~ 5:
		return List.of(
				// bc1q3a9fejgzwyq3v2lr5avedpm7kw9ght30weazgd
				Arguments.of("03b1ff66b2adf06a7931fb9d6409b97c66064256c5c0bd310754ce8dc554859fca",
						"bc1q3a9fejgzwyq3v2lr5avedpm7kw9ght30weazgd"),
				// bc1qdnm43xkcnvpyvldv5t69g34y3vrmcnm8rhrvps
				Arguments.of("029e5b3eaba308af95fe3aecdfa8063e67466af1da71f4518245063fb6fd485606",
						"bc1qdnm43xkcnvpyvldv5t69g34y3vrmcnm8rhrvps"),
				// bc1q3lfvp6fa3wrmu5leljxpcfg2n7fx0wkgmg7lde
				Arguments.of("037731685c5806c14c6146685d35b1c760def3dd5be6c40863a1b2c4afb02de15b",
						"bc1q3lfvp6fa3wrmu5leljxpcfg2n7fx0wkgmg7lde"),
				// bc1qenl0hklhhaxtrf4kkgsxexm6lh06xg54vjzd4v
				Arguments.of("02591114689cb534e0b408b7d60707728a32959cb014bdff263060d163be5f3d3c",
						"bc1qenl0hklhhaxtrf4kkgsxexm6lh06xg54vjzd4v"),
				// bc1qfpxajv78msyss6j54w8ygqpp6ft675ptzarcxw
				Arguments.of("02c442b0fbba5b39b13643080d78d7430601d8fbf93c83f9112ac13b6e63ed932b",
						"bc1qfpxajv78msyss6j54w8ygqpp6ft675ptzarcxw"),
				// bc1qf9qu8za8usmsa23rvjm65xx2l6nqpcafgcaddh
				Arguments.of("02840cdaa98dbf271e98c31750636d3a55e72fe620e5f02fb5bb4a995fb16e6051",
						"bc1qf9qu8za8usmsa23rvjm65xx2l6nqpcafgcaddh"));
	}
}
