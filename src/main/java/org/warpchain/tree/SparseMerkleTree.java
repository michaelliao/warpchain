package org.warpchain.tree;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.HalfByteString;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class SparseMerkleTree implements TreeInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final Function<byte[], byte[]> hashFunction;
	private final int treeHeight;
	private final byte[][] DEFAULT_HASH_AT_HEIGHT;
	private final FullNode root;

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
		this.root = new FullNode(this, 0, HalfByteString.EMPTY);
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

	public FullNode getRootNode() {
		return this.root;
	}

	public void update(byte[] value) {
		update(this.hashFunction.apply(value), value);
	}

	public void update(byte[] hash, byte[] value) {
		HalfByteString path = new HalfByteString(hash);
		assert 4 * path.length() == this.treeHeight : "Invalid hash size: " + hash;

		logger.info("update tree: path={}, data={}", path, new String(value, StandardCharsets.UTF_8));
		this.root.update(this, path, hash, value);
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
