package org.warpchain.tree;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class SparseMerkleTreeTest {

	@Test
	void hash() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		assertEquals("9595c9", ByteUtils.toHexString(tree.hash("hello".getBytes())));
		assertEquals("9595c9", ByteUtils.toHexString(tree.hash("duplicate-20495739".getBytes())));
		assertEquals("9595c5", ByteUtils.toHexString(tree.hash("abc-2120105".getBytes())));
		assertEquals("95958a", ByteUtils.toHexString(tree.hash("xyz-50318".getBytes())));
		assertEquals("959df6", ByteUtils.toHexString(tree.hash("hi-5515".getBytes())));
		assertEquals("95ac9f", ByteUtils.toHexString(tree.hash("op-416".getBytes())));
		assertEquals("9a7948", ByteUtils.toHexString(tree.hash("t-60".getBytes())));
		assertEquals("63e5c1", ByteUtils.toHexString(tree.hash("world".getBytes())));
	}

	@Test
	void emptyTree() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		tree.print();
		assertEquals(verifyMerkle(tree), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertRootSlot9() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c9
		tree.update("hello".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafWithoutSharedPath() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9 595c9
		tree.update("hello".getBytes());
		tree.print();
		// H("t-60") = 9 a7948
		tree.update("t-60".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		Node sub = root.getChild(9);
		assertNotNull(sub);
		assertTrue(sub instanceof FullNode);
		FullNode fn = (FullNode) sub;
		assertNotNull(fn.getChild(5));
		assertNotNull(fn.getChild(0xa));
		assertEquals(verifyMerkle(tree, "hello", "t-60"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafWithSharedPath1() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 95 95c9
		tree.update("hello".getBytes());
		tree.print();
		// H("op-416") = 95 ac9f
		tree.update("op-416".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "op-416"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafWithSharedPath2() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 959 5c9
		tree.update("hello".getBytes());
		tree.print();
		// H("hi-5515") = 959 df6
		tree.update("hi-5515".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "hi-5515"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafWithSharedPath3() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595 c9
		tree.update("hello".getBytes());
		tree.print();
		// H("xyz-50318") = 9595 8a
		tree.update("xyz-50318".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "xyz-50318"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafWithSharedPath4() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c 9
		tree.update("hello".getBytes());
		tree.print();
		// H("abc-2120105") = 9595c 5
		tree.update("abc-2120105".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "abc-2120105"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafsWithSharedPath() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c 9
		tree.update("hello".getBytes());
		tree.print();
		// H("abc-2120105") = 9595c 5
		tree.update("abc-2120105".getBytes());
		tree.print();
		// H("xyz-50318") = 9595 8a
		tree.update("xyz-50318".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "abc-2120105", "xyz-50318"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafsWithSharedPath2() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c 9
		tree.update("hello".getBytes());
		tree.print();
		// H("abc-2120105") = 9595c 5
		tree.update("abc-2120105".getBytes());
		tree.print();
		// H("hi-5515") = 959 df6
		tree.update("hi-5515".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "abc-2120105", "hi-5515"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafsWithSharedPath3() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c 9
		tree.update("hello".getBytes());
		tree.print();
		// H("abc-2120105") = 9595c 5
		tree.update("abc-2120105".getBytes());
		tree.print();
		// H("hi-5515") = 959 df6
		tree.update("hi-5515".getBytes());
		// H("op-416") = 95 ac9f
		tree.update("op-416".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello", "abc-2120105", "hi-5515", "op-416"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertLeafsWithDiffOrders() {
		String[] data = { "hello", "abc-2120105", "hi-5515", "op-416", "t-60" };
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		String merkle = verifyMerkle(tree, data);

		var tree1 = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		Arrays.stream(data).map(String::getBytes).forEach(tree1::update);
		tree1.print();
		assertEquals(merkle, tree1.getRootMerkleHashAsString());

		var tree2 = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		Arrays.stream(data).sorted().map(String::getBytes).forEach(tree2::update);
		tree2.print();
		assertEquals(merkle, tree2.getRootMerkleHashAsString());

		var tree3 = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		Arrays.stream(data).sorted((s1, s2) -> Integer.compare(s1.length(), s2.length())).map(String::getBytes)
				.forEach(tree3::update);
		tree3.print();
		assertEquals(merkle, tree3.getRootMerkleHashAsString());
	}

	@Test
	void updateRandom() {
		Random rnd = new Random(123456);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 60; i++) {
			StringBuilder sb = new StringBuilder(8);
			for (int j = 0; j < 8; j++) {
				int ch = 'A' + rnd.nextInt(26);
				sb.append((char) ch);
			}
			list.add(sb.toString());
		}
		String[] data = list.toArray(String[]::new);
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		Arrays.stream(data).map(String::getBytes).forEach(tree::update);
		String merkle = verifyMerkle(tree, data);
		assertEquals(merkle, tree.getRootMerkleHashAsString());
		tree.print();
	}

	@Test
	void insertSameLeaf() {
		var tree = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		// H("hello") = 9595c9
		tree.update("hello".getBytes());
		tree.print();
		// H("duplicate-20495739") = 9595c9
		tree.update("duplicate-20495739".getBytes());
		tree.print();
		FullNode root = (FullNode) tree.getRootNode();
		assertNotNull(root.getChild(9));
		assertEquals(verifyMerkle(tree, "hello"), tree.getRootMerkleHashAsString());
	}

	@Test
	void insertRootSlot6() {
		var smt = new SparseMerkleTree(SparseMerkleTreeTest::hash24bits);
		smt.update("world".getBytes());
		smt.print();
		FullNode root = (FullNode) smt.getRootNode();
		assertNotNull(root.getChild(6));
	}

	static byte[] hash24bits(byte[] input) {
		byte[] hash = HashUtils.dsha256(input);
		return Arrays.copyOfRange(hash, 0, 3);
	}

	static String verifyMerkle(TreeInfo tree, String... dataSet) {
		Map<Integer, byte[]> dataHashes = new HashMap<>();
		for (String data : dataSet) {
			byte[] hash = tree.hash(data.getBytes());
			int index = new BigInteger(1, hash).intValueExact();
			dataHashes.put(index, hash);
			System.out.printf("height = %s, index = %s, hash = %s\n", tree.getTreeHeight(), index,
					ByteUtils.toHexString(hash));
		}
		// calculate merkle:
		Map<Integer, byte[]> lower = dataHashes;
		Map<Integer, byte[]> upper;
		for (int height = tree.getTreeHeight(); height > 0; height--) {
			upper = new HashMap<>();
			int endIndex = 1 << height;
			for (int i = 0; i < endIndex; i += 2) {
				byte[] left = lower.get(i);
				byte[] right = lower.get(i + 1);
				byte[] merkle = null;
				if (left != null || right != null) {
					if (left == null) {
						left = tree.getDefaultHashAtHeight(height);
					}
					if (right == null) {
						right = tree.getDefaultHashAtHeight(height);
					}
					merkle = tree.generateMerkleHash(left, right);
				}
				if (merkle != null) {
					int index = i / 2;
					upper.put(index, merkle);
					System.out.printf("height = %s, index = %s, hash = %s\n", height - 1, index,
							ByteUtils.toHexString(merkle));
				}
			}
			lower = upper;
		}
		byte[] root = lower.get(0);
		if (root == null) {
			root = tree.getDefaultHashAtHeight(0);
		}
		return ByteUtils.toHexString(root);
	}
}
