package org.warpchain.tree;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.warpchain.util.HashUtils;

public class SparseMerkleTreeTest {

	@Test
	void emptyTree() {
		var smt = new SparseMerkleTree(SparseMerkleTreeTest::hash8);
		smt.print();
	}

	static byte[] hash8(byte[] input) {
		byte[] hash = HashUtils.dsha256(input);
		return Arrays.copyOfRange(hash, 0, 8);
	}
}
