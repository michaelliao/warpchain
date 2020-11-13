package org.warpchain.tree;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.HalfByteString;
import org.warpchain.util.ByteUtils;

public class LeafNode extends Node {

	private static final Logger logger = LoggerFactory.getLogger(LeafNode.class);

	/**
	 * Full path from root to leaf node.
	 */
	private HalfByteString path;

	/**
	 * start height of this node.
	 */
	private int height;

	/**
	 * Merkle hash of this node.
	 */
	private byte[] merkleHash;

	/**
	 * Data hash of this node.
	 */
	private final byte[] dataHash;

	/**
	 * Data of this node.
	 */
	private final byte[] dataValue;

	LeafNode(TreeInfo tree, int height, HalfByteString path, byte[] dataHash, byte[] dataValue) {
		assert height > 0 && height <= tree.getTreeHeight() && (height & 0x3) == 0
				: "Invalid height for leaf node: " + height;
		assert 8 * dataHash.length == tree.getTreeHeight() : "Invalid data hash: " + ByteUtils.toHexString(dataHash);
		assert 4 * path.length() == tree.getTreeHeight() : "Invalid path length for leaf node: " + path;
		assert dataValue != null && dataValue.length > 0 : "data value is null or empty";

		this.path = path;
		this.height = height;
		this.dataHash = dataHash;
		this.dataValue = dataValue;
		this.updateMerkleHash(tree);
	}

	private void updateMerkleHash(TreeInfo tree) {
		if (this.height == tree.getTreeHeight()) {
			// it is only leaf:
			this.merkleHash = this.dataHash;
			return;
		}
		int endIndex = tree.getTreeHeight() / 4 - 1;
		int startIndex = this.height / 4;
		byte[] left;
		byte[] right;
		byte[] merkle = this.dataHash;
		logger.info("height={}, merkle={}", tree.getTreeHeight(), ByteUtils.toHexString(merkle));
		for (int i = endIndex; i >= startIndex; i--) {
			int hb = this.path.valueAt(i);
			int bitIndex = i << 2;
			int bit0 = hb & 0b1000;
			int bit1 = hb & 0b0100;
			int bit2 = hb & 0b0010;
			int bit3 = hb & 0b0001;
			left = bit3 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 4);
			right = bit3 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 4);
			merkle = tree.generateMerkleHash(left, right);
			logger.info("height={}, merkle={}", bitIndex + 3, ByteUtils.toHexString(merkle));

			left = bit2 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 3);
			right = bit2 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 3);
			merkle = tree.generateMerkleHash(left, right);

			left = bit1 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 2);
			right = bit1 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 2);
			merkle = tree.generateMerkleHash(left, right);

			left = bit0 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 1);
			right = bit0 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 1);
			merkle = tree.generateMerkleHash(left, right);
		}
		this.merkleHash = merkle;
	}

	@Override
	public byte[] getMerkleHash() {
		return this.merkleHash;
	}

	@Override
	public Node update(TreeInfo tree, HalfByteString dataPath, byte[] dataHash, byte[] dataValue) {
		logger.info("update {}...", dataPath);
		if (this.path.equals(dataPath)) {
			logger.info("data not change for path {}", dataPath);
			return this;
		}
		HalfByteString prefix = HalfByteString.sharedPrefix(this.path, dataPath);
		logger.info("shared prefix: {}", prefix);
		final int parentHeight = this.height;
		final int childHeight = (prefix.length() + 1) * 4;
		final FullNode parent = new FullNode(tree, parentHeight, prefix);
		logger.info("build full node for current node and new node: {}", parent);
		final int currentSlot = this.path.valueAt(childHeight / 4 - 1);
		// move leaf height to next level:
		this.height = childHeight;
		this.updateMerkleHash(tree);
		parent.setChild(currentSlot, this);
		final int newSlot = dataPath.valueAt(childHeight / 4 - 1);
		parent.setChild(newSlot, new LeafNode(tree, childHeight, dataPath, dataHash, dataValue));
		parent.updateMerkleHash(tree);
		return parent;
	}

	@Override
	public void appendTo(StringBuilder sb, int slot) {
		for (int i = 0; i < this.height; i++) {
			sb.append(INDENT);
		}
		sb.append(String.format("%x", slot)).append(":[LEAF: height=").append(this.height).append(", path=")
				.append(this.path).append(", merkleHash=").append(ByteUtils.toHexString(this.merkleHash))
				.append(", dataHash=").append(ByteUtils.toHexString(this.dataHash)).append(", dataValue=")
				.append(new String(this.dataValue, StandardCharsets.UTF_8)).append("]\n");
	}

	@Override
	public String toString() {
		return String.format("LeafNode(height=%s, path=%s, merkleHash=%s, dataHash=%s, dataValue=%s)", this.height,
				this.path, ByteUtils.toHexString(this.merkleHash), ByteUtils.toHexString(this.dataHash),
				new String(this.dataValue, StandardCharsets.UTF_8));
	}
}
