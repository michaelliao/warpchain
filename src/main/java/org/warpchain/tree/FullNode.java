package org.warpchain.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;

public class FullNode extends Node {

	private static final Logger logger = LoggerFactory.getLogger(FullNode.class);

	/**
	 * Path from root to current node.
	 */
	private BitString path;

	/**
	 * Bit value of current node: 0=left, 1=right.
	 */
	private int bit;

	/**
	 * Height of this node.
	 */
	private int height;

	/**
	 * Merkle hash of this node.
	 */
	private byte[] merkleHash;
	
	/**
	 * 16 nodes represents 4-depth sub-tree
	 */
	private Node[] children;

	/**
	 * Left child node.
	 */
	private Node left;

	/**
	 * Right child node.
	 */
	private Node right;

	FullNode(TreeInfo tree, int height, BitString path) {
		assert tree.getTreeHeight() > height + 1
				: "cannot set full node to height " + height + " when tree height is " + tree.getTreeHeight();
		this.path = path;
		this.bit = path.bitValueAt(height);
		this.height = height;
		this.merkleHash = tree.getDefaultHashAtHeight(height);
		this.left = null;
		this.right = null;
	}

	@Override
	public byte[] getMerkleHash() {
		return merkleHash;
	}

	@Override
	public void update(TreeInfo tree, BitString path, byte[] dataHash, byte[] dataValue) {
		int dataBit = path.bitValueAt(this.height + 1);
		if (dataBit == 0) {
			if (this.left == null) {
				// no left node, create new node:
				if (this.height == tree.getTreeHeight() - 2) {
					// create leaf node:
					this.left = new LeafNode(tree, this.height + 1, path, dataHash, dataValue);
					this.merkleHash = generateMerkleHash(tree);
					logger.info("insert left child of {}: new short cut node: {}", this, this.left);
				} else {
					// create short-cut node to data:
					this.left = new ShortCutNode(tree, this.height + 1, path, dataHash, dataValue);
					this.merkleHash = generateMerkleHash(tree);
					logger.info("insert left child of {}: new short cut node: {}", this, this.left);
				}
			} else {
				// left node exist:
				logger.info("update left child of {}", this);
			}
		} else { // dataBit == 1
			if (this.right == null) {
				// no right node, create new node:
				if (this.height == tree.getTreeHeight() - 2) {
					// create leaf node:
					this.right = new LeafNode(tree, this.height + 1, path, dataHash, dataValue);
					this.merkleHash = generateMerkleHash(tree);
					logger.info("insert right child of {}: new short cut node: {}", this, this.right);
				} else {
					// create short-cut node to data:
					this.right = new ShortCutNode(tree, this.height + 1, path, dataHash, dataValue);
					this.merkleHash = generateMerkleHash(tree);
					logger.info("insert right child of {}: new short cut node: {}", this, this.right);
				}
			} else {
				// right node exist:
				logger.info("update right child of {}", this);
			}
		}
	}

	private byte[] generateMerkleHash(TreeInfo tree) {
		byte[] leftChildHash = left == null ? tree.getDefaultHashAtHeight(this.height + 1) : left.getMerkleHash();
		byte[] rightChildHash = right == null ? tree.getDefaultHashAtHeight(this.height + 1) : right.getMerkleHash();
		return tree.generateMerkleHash(leftChildHash, rightChildHash);
	}

	@Override
	public void appendTo(StringBuilder sb) {
		for (int i = 0; i <= this.height; i++) {
			sb.append(INDENT);
		}
		sb.append(this.bit == 0 ? 'L' : 'R').append("=[FULL: height=").append(this.height).append(", path=")
				.append(this.path).append(", merkleHash=").append(ByteUtils.toHexString(this.merkleHash)).append("]\n");
		if (this.left != null) {
			this.left.appendTo(sb);
		}
		if (this.right != null) {
			this.right.appendTo(sb);
		}
	}

	@Override
	public String toString() {
		return String.format("FullNode(height=%s, bit=%s, path=%s, merkleHash=%s)", this.height, this.bit, this.path,
				ByteUtils.toHexString(this.merkleHash));
	}
}
