package org.warpchain.tree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SparseMerkleTreeTest {

	@Test
	void emptyTree() {
		var smt = new SparseMerkleTree(SparseMerkleTreeTest::hash8, 8);
	}

	static byte[] hash8(byte[] input) {
		if (input == null || input.length == 0) {
			return new byte[] { 0 };
		}
		byte result = 0;
		for (byte b : input) {
			result = (byte) (result * 31 + b);
		}
		return new byte[] { result };
	}
}
