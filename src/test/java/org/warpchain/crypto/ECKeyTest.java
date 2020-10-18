package org.warpchain.crypto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class ECKeyTest {

	@Test
	void createECKey() {
		ECKey key = ECKey.createECKey();
		assertTrue(key.getPrivateKey().signum() > 0);
		byte[] pubKey = key.getPublicKeyAsBytes();
		assertEquals(33, pubKey.length);
		assertTrue(pubKey[0] == 0x02 || pubKey[0] == 0x03);
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void fromPrivateKey(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		BigInteger privateKey = new BigInteger(hexPrivateKey, 16);
		ECKey key = ECKey.fromPrivateKey(privateKey);
		assertTrue(0 == new BigInteger(hexPublicKey, 16).compareTo(key.getPublicKey()));
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void fromPublicKey(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		ECKey key = ECKey.fromPublicKey(new BigInteger(hexPublicKey, 16));
		assertNull(key.getPrivateKey());
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void getPublicKeyAsBytes(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		ECKey key = ECKey.fromPrivateKey(new BigInteger(hexPrivateKey, 16));
		byte[] pubKey = key.getPublicKeyAsBytes();
		assertEquals(hexPublicKey, ByteUtils.toHexString(pubKey));
	}

	@MethodSource("keyPairs")
	@ParameterizedTest
	void getPublicKey(String hexPrivateKey, String wif, String hexPublicKey, String address) {
		ECKey key = ECKey.fromPrivateKey(new BigInteger(hexPrivateKey, 16));
		BigInteger publicKey = key.getPublicKey();
		assertTrue(0 == new BigInteger(hexPublicKey, 16).compareTo(publicKey));
	}

	@Test
	void sign() {
		String hexPrivateKey = "dc55bc0cbde942349d563b3f845f6fcec7362173442b27b70ae7b20d13325a82";
		byte[] message = HashUtils.sha256("Hello, Bitcoin!".getBytes(StandardCharsets.UTF_8));
		ECKey key = ECKey.fromPrivateKey(new BigInteger(hexPrivateKey, 16));
		ECSignature signature = key.sign(message);
		assertTrue(0 == new BigInteger("cbb597c7faa64835b2ab6d25c64ac29ad6b576a448155a3053cd6c7e78e03192", 16)
				.compareTo(signature.r));
		assertTrue(0 == new BigInteger("58488c561040f29dca019b8791237ee3a2529e724af84c2dcf19db4b91bc9251", 16)
				.compareTo(signature.s));
		byte[] der = signature.toDER();
		assertEquals(
				"3045022100cbb597c7faa64835b2ab6d25c64ac29ad6b576a448155a3053cd6c7e78e03192022058488c561040f29dca019b8791237ee3a2529e724af84c2dcf19db4b91bc9251",
				ByteUtils.toHexString(der));
	}

	@Test
	void verify() {
		byte[] message = HashUtils.sha256("Hello, Bitcoin!".getBytes(StandardCharsets.UTF_8));
		BigInteger r = new BigInteger("cbb597c7faa64835b2ab6d25c64ac29ad6b576a448155a3053cd6c7e78e03192", 16);
		BigInteger s = new BigInteger("58488c561040f29dca019b8791237ee3a2529e724af84c2dcf19db4b91bc9251", 16);
		ECKey key = ECKey.fromPublicKey(
				new BigInteger("02859a42554b255917b971c6d0e322651db76d8e33b7e6686b345f22e57048c750", 16));
		assertTrue(key.verify(message, r, s));
		// verify failed:
		assertFalse(key.verify(message, r.subtract(BigInteger.ONE), s));
		assertFalse(key.verify(message, r, s.subtract(BigInteger.ONE)));
		message[message.length - 1] = (byte) (message[message.length - 1] + 1); // change last byte 118 -> 119
		assertFalse(key.verify(message, r, s));
		assertFalse(key.verify(Arrays.copyOf(message, message.length - 1), r, s)); // change byte array size
	}

	@Test
	void getN() {
		assertEquals(0, new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16)
				.compareTo(ECKey.getN()));
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
}
