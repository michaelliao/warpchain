package org.warpchain.tree;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.NibbleString;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class SparseMerkleTree implements TreeInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final Function<byte[], byte[]> hashFunction;
	private final int treeHeight;
	private final byte[][] DEFAULT_HASH_AT_HEIGHT;
	private Node root;

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
	SparseMerkleTree(final Function<byte[], byte[]> hashFunction) {
		this.hashFunction = hashFunction;
		final byte[] hashOfLeaf = hashFunction.apply(ByteUtils.emptyByteArray());
		this.treeHeight = 8 * hashOfLeaf.length;
		DEFAULT_HASH_AT_HEIGHT = new byte[this.treeHeight + 1][];
		DEFAULT_HASH_AT_HEIGHT[this.treeHeight] = hashOfLeaf;
		for (int i = this.treeHeight - 1; i >= 0; i--) {
			DEFAULT_HASH_AT_HEIGHT[i] = hashFunction
					.apply(ByteUtils.concat(DEFAULT_HASH_AT_HEIGHT[i + 1], DEFAULT_HASH_AT_HEIGHT[i + 1]));
		}
		logger.info("init tree: height = {}, root merkle hash = {}, leaf merkle hash = {}", this.treeHeight,
				ByteUtils.toHexString(DEFAULT_HASH_AT_HEIGHT[0]),
				ByteUtils.toHexString(DEFAULT_HASH_AT_HEIGHT[this.treeHeight]));
		this.root = new FullNode(this, 0, NibbleString.EMPTY);
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
	public byte[] hash(byte[] data) {
		return this.hashFunction.apply(data);
	}

	@Override
	public byte[] generateMerkleHash(byte[] left, byte[] right) {
		byte[] data = ByteUtils.concat(left, right);
		return this.hashFunction.apply(data);
	}

	public Node getRootNode() {
		return this.root;
	}

	public void update(byte[] dataValue) {
		update(this.hashFunction.apply(dataValue), dataValue);
	}

	void update(byte[] dataHash, byte[] dataValue) {
		NibbleString path = new NibbleString(dataHash);
		update(path, dataHash, dataValue);
	}

	void update(NibbleString dataPath, byte[] dataHash, byte[] dataValue) {
		assert 4 * dataPath.length() == this.treeHeight : "Invalid path size: " + dataPath;

		logger.info("update tree: path={}, data={}", dataPath, new String(dataValue, StandardCharsets.UTF_8));
		this.root = this.root.update(this, dataPath, dataHash, dataValue);
	}

	public byte[] getRootMerkleHash() {
		return this.root.getMerkleHash();
	}

	public String getRootMerkleHashAsString() {
		return ByteUtils.toHexString(this.root.getMerkleHash());
	}

	public void print() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("== SparseMerkleTree(height=").append(this.treeHeight).append(") ==\n\n");
		this.root.appendTo(sb, -1);
		sb.append("\n== END SparseMerkleTree ==\n");
		return sb.toString();
	}
}
