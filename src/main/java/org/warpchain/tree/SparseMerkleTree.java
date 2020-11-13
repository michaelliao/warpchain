package org.warpchain.tree;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class SparseMerkleTree implements TreeInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final Function<byte[], byte[]> hashFunction;
	private final int treeHeight;
	private final byte[][] DEFAULT_HASH_AT_HEIGHT;
	private byte[] rootMerkleHash;
	private Node left;
	private Node right;

	/**
	 * Default sparse merkle tree using DSHA-256 as hash function.
	 */
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
		this.hashFunction = hashFunction;
		final byte[] hashOfLeaf = hashFunction.apply(ByteUtils.emptyByteArray());
		this.treeHeight = 8 * hashOfLeaf.length;
		DEFAULT_HASH_AT_HEIGHT = new byte[this.treeHeight][];
		DEFAULT_HASH_AT_HEIGHT[this.treeHeight - 1] = hashOfLeaf;
		for (int i = this.treeHeight - 2; i >= 0; i--) {
			DEFAULT_HASH_AT_HEIGHT[i] = hashFunction
					.apply(ByteUtils.concat(DEFAULT_HASH_AT_HEIGHT[i + 1], DEFAULT_HASH_AT_HEIGHT[i + 1]));
		}
		this.left = new FullNode(this, 0, BitString.BIT_0);
		this.right = new FullNode(this, 0, BitString.BIT_1);
		this.rootMerkleHash = generateRootMerkleHash();
		logger.info("Init empty sparse markle tree: root merkle hash = {}", getRootMerkleHash());
	}

	@Override
	public byte[] getDefaultHashAtHeight(int height) {
		return DEFAULT_HASH_AT_HEIGHT[height];
	}

	@Override
	public int getTreeHeight() {
		return this.treeHeight;
	}

	@Override
	public byte[] generateMerkleHash(byte[] left, byte[] right) {
		byte[] data = ByteUtils.concat(left, right);
		return this.hashFunction.apply(data);
	}

	private byte[] generateRootMerkleHash() {
		byte[] leftHash = this.left == null ? this.getDefaultHashAtHeight(0) : this.left.getMerkleHash();
		byte[] rightHash = this.right == null ? this.getDefaultHashAtHeight(0) : this.right.getMerkleHash();
		return generateMerkleHash(leftHash, rightHash);
	}

	public void update(byte[] value) {
		update(this.hashFunction.apply(value), value);
	}

	public void update(byte[] hash, byte[] value) {
		BitString path = new BitString(hash);
		assert path.length() == this.treeHeight : "Invalid hash size: " + hash;

		int firstBit = path.bitValueAt(0);
		if (firstBit == 0) {
			this.left.update(this, path, hash, value);
		} else {
			this.right.update(this, path, hash, value);
		}
		this.rootMerkleHash = generateRootMerkleHash();
	}

	public String getRootMerkleHash() {
		return ByteUtils.toHexString(this.rootMerkleHash);
	}

	public void print() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("[ROOT: merkleHash=").append(getRootMerkleHash()).append("]\n");
		this.left.appendTo(sb);
		this.right.appendTo(sb);
		return sb.toString();
	}
}
