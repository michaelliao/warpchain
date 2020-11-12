package org.warpchain.tree;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;

public class SparseMerkleTree {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final int HEIGHT;
	private final int HASH_SIZE;
	private final byte[][] DEFAULT_HASH_LEVELS;
	private final Node root;
	private final Function<byte[], byte[]> hashFunction;

	/**
	 * Construct a specific-height, all-empty leafs tree.
	 * 
	 * @param hashFunction
	 * @param levels
	 */
	public SparseMerkleTree(Function<byte[], byte[]> hashFunction, int height) {
		DEFAULT_HASH_LEVELS = new byte[height + 1][];
		DEFAULT_HASH_LEVELS[0] = hashFunction.apply(ByteUtils.emptyByteArray());
		HEIGHT = height;
		HASH_SIZE = DEFAULT_HASH_LEVELS[0].length;
		for (int i = 0; i <= height; i++) {
			DEFAULT_HASH_LEVELS[i] = hashFunction
					.apply(ByteUtils.concat(DEFAULT_HASH_LEVELS[i - 1], DEFAULT_HASH_LEVELS[i - 1]));
		}
		this.hashFunction = hashFunction;
		this.root = Node.createFullNode();
	}

	public void insert(byte[] hash, byte[] value) {
		if (hash.length != HASH_SIZE) {
			throw new IllegalArgumentException("Invalid hash size.");
		}
		this.root.insert(new BitString(hash), value);
	}

	@Override
	public String toString() {
		return "";
	}
}
