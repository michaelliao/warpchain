package org.warpchain.tree;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class SparseMerkleTree {

	private Logger logger = LoggerFactory.getLogger(getClass());

	final int treeHeight;
	final byte[][] DEFAULT_HEIGHT_OF_HASH;
	private final Node root;
	private final Function<byte[], byte[]> hashFunction;

	public SparseMerkleTree() {
		this(HashUtils::dsha256);
	}

	/**
	 * Construct a specific-height, all-empty leafs tree. The tree height is set to
	 * hash size in bits. bits of hash value.
	 * 
	 * @param hashFunction
	 * @param levels
	 */
	SparseMerkleTree(Function<byte[], byte[]> hashFunction) {
		byte[] hashOfHeight0 = hashFunction.apply(ByteUtils.emptyByteArray());
		this.treeHeight = hashOfHeight0.length;
		DEFAULT_HEIGHT_OF_HASH = new byte[this.treeHeight + 1][];
		DEFAULT_HEIGHT_OF_HASH[0] = hashOfHeight0;
		for (int i = 1; i <= this.treeHeight; i++) {
			DEFAULT_HEIGHT_OF_HASH[i] = hashFunction
					.apply(ByteUtils.concat(DEFAULT_HEIGHT_OF_HASH[i - 1], DEFAULT_HEIGHT_OF_HASH[i - 1]));
		}
		this.hashFunction = hashFunction;
		this.root = new Node(NodeType.FULL, this.treeHeight, BitString.EMPTY, DEFAULT_HEIGHT_OF_HASH[this.treeHeight],
				true);
	}

	public void update(byte[] hash, byte[] value) {
		assert hash.length == this.treeHeight : "Invalid hash size: " + hash;
		this.root.update(new BitString(hash), value);
	}

	public void print() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(4096);
		this.root.appendToString(this.treeHeight, sb);
		return sb.toString();
	}

}
