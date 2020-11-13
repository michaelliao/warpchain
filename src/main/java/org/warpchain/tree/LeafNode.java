package org.warpchain.tree;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;

public class LeafNode extends Node {

	private static final Logger logger = LoggerFactory.getLogger(LeafNode.class);

	/**
	 * Full path from root to current node.
	 */
	private BitString path;

	/**
	 * Bit value of current node: 0=left, 1=right.
	 */
	private int bit;

	/**
	 * height of this node.
	 */
	private int height;

	/**
	 * Data hash of this node.
	 */
	private byte[] merkleHash;

	/**
	 * Data of this node.
	 */
	private byte[] data;

	LeafNode(TreeInfo tree, int height, BitString path, byte[] dataHash, byte[] dataValue) {
		assert tree.getTreeHeight() == height + 1
				: "Leaf node's height must be " + (tree.getTreeHeight() - 1) + " but is set to " + height;
		assert tree.getTreeHeight() == path.length()
				: "Leaf node's path must be " + tree.getTreeHeight() + " bits but is set to " + path.length();
		assert 8 * dataHash.length == tree.getTreeHeight()
				: "data hash bits is expected as " + tree.getTreeHeight() + " but actual is " + (8 * dataHash.length);
		assert dataValue != null : "data value is null";
		assert dataValue.length > 0 : "data value is empty byte array";

		this.path = path;
		this.bit = path.bitValueAt(height);
		this.height = height;
		this.merkleHash = dataHash;
		this.data = dataValue;
	}

	@Override
	public byte[] getMerkleHash() {
		return merkleHash;
	}

	@Override
	public void appendTo(StringBuilder sb) {
		for (int i = 0; i <= this.height; i++) {
			sb.append(INDENT);
		}
		sb.append(bit == 0 ? 'L' : 'R').append("=[LEAF: height=").append(this.height).append(", bit=").append(this.bit)
				.append(", path=").append(this.path).append(", dataHash=")
				.append(ByteUtils.toHexString(this.merkleHash)).append(", dataValue=")
				.append(new String(this.data, StandardCharsets.UTF_8)).append("]\n");
	}

	@Override
	public String toString() {
		return String.format("LeafNode(height=%s, bit=%s, path=%s, dataHash=%s, dataValue=%s)", this.height, this.bit,
				this.path, ByteUtils.toHexString(this.merkleHash), new String(this.data, StandardCharsets.UTF_8));
	}

	@Override
	public void update(TreeInfo tree, BitString path, byte[] dataHash, byte[] dataValue) {
		throw new UnsupportedOperationException("Cannot update leaf node!");
	}
}
