package org.warpchain.trie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MerklePatriciaTrieTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testMerklePatriciaTrie() {
		var mpt = new MerklePatriciaTrie();
		mpt.createNode("1a2b3c", "hello".getBytes());
		mpt.createNode("3f4d", "world".getBytes());
		mpt.print(System.out);
		// mpt.createNode("1a32ff", "world".getBytes());
		// mpt.print(System.out);
		System.err.println(mpt.getValueAsString("1a2b3c"));
		System.err.println(mpt.getValueAsString("3f4d"));
	}
}
