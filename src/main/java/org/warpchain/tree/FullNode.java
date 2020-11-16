package org.warpchain.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.NibbleString;
import org.warpchain.util.ByteUtils;

public class FullNode extends Node {

	private static final Logger logger = LoggerFactory.getLogger(FullNode.class);

	/**
	 * Path from root to current node.
	 */
	private final NibbleString path;

	/**
	 * Height of this node.
	 */
	private final int height;

	/**
	 * Merkle hash of this node.
	 */
	private byte[] merkleHash;

	/**
	 * 16 nodes represents 4-depth sub-tree
	 */
	private Node[] children;

	FullNode(TreeInfo tree, int height, NibbleString path) {
		assert height >= 0 && height < tree.getTreeHeight() && (height & 0x3) == 0
				: "invalid height for full node: " + height;
		assert path.length() <= (height << 2) : "invalid path length for full node: " + path;

		this.height = height;
		this.path = path;
		this.merkleHash = tree.getDefaultHashAtHeight(height);
		this.children = null;
	}

	@Override
	public byte[] getMerkleHash() {
		return merkleHash;
	}

	@Override
	public Node update(TreeInfo tree, NibbleString dataPath, byte[] dataHash, byte[] dataValue) {
		NibbleString prefix = NibbleString.sharedPrefix(this.path, dataPath);
		logger.info("shared path prefix: {}", prefix);
		if (prefix.length() == this.path.length()) {
			logger.info("updated data path has same prefix for current path, just update child node.");
			int childHeight = this.path.length() * 4 + 4;
			int childHalfByteIndex = childHeight >> 2;
			int childSlotIndex = dataPath.valueAt(childHalfByteIndex - 1);
			Node child = this.getChild(childSlotIndex);
			if (child == null) {
				// no child node, create leaf node:
				Node created = new LeafNode(tree, childHeight, dataPath, dataHash, dataValue);
				logger.info("set new node at slot {}: {}", childSlotIndex, created);
				this.setChild(childSlotIndex, created);
				this.updateMerkleHash(tree);
				logger.info("update merkle to {}: {}", ByteUtils.toHexString(this.merkleHash), this);
			} else {
				// child node exist, update:
				Node updated = child.update(tree, dataPath, dataHash, dataValue);
				logger.info("set updated node at slot {}: {}", childSlotIndex, updated);
				this.setChild(childSlotIndex, updated);
				this.updateMerkleHash(tree);
				logger.info("update merkle to {}: {}", ByteUtils.toHexString(this.merkleHash), this);
			}
			return this;
		}
		FullNode parent = new FullNode(tree, this.height, prefix);
		logger.info("cannot update child node direct. split new parent node for path {}: {}", prefix, parent);
		FullNode currentChild = new FullNode(tree, prefix.length() * 4 + 4, this.path);
		logger.info("copy and modify current node to new full node: {}", currentChild);
		copyChildren(this, currentChild);
		currentChild.updateMerkleHash(tree);
		int currentChildSlotIndex = this.path.valueAt(prefix.length());
		parent.setChild(currentChildSlotIndex, currentChild);

		// create new child LeafNode:
		LeafNode leaf = new LeafNode(tree, prefix.length() * 4 + 4, dataPath, dataHash, dataValue);
		logger.info("add new leaf node: {}", leaf);
		int newLeafSlotIndex = dataPath.valueAt(prefix.length());
		parent.setChild(newLeafSlotIndex, leaf);
		parent.updateMerkleHash(tree);
		return parent;
	}

	private void copyChildren(FullNode fromNode, FullNode toNode) {
		if (fromNode.children == null) {
			toNode.children = null;
		} else {
			toNode.children = new Node[16];
			System.arraycopy(fromNode.children, 0, toNode.children, 0, 16);
		}
	}

	Node getChild(int index) {
		assert index >= 0 && index < 16 : "Invalid child index: " + index;
		if (this.children == null) {
			return null;
		}
		return this.children[index];
	}

	void setChild(int index, Node child) {
		assert index >= 0 && index < 16 : "Invalid child index: " + index;
		if (this.children == null) {
			this.children = new Node[16];
		}
		this.children[index] = child;
	}

	void updateMerkleHash(TreeInfo tree) {
		int subtreeHeight = this.path.length() * 4;
		if (this.children == null) {
			this.merkleHash = tree.getDefaultHashAtHeight(subtreeHeight);
		}

		// init 16 hashes, set null if default:
		byte[][] merkles16 = new byte[16][];
		for (int i = 0; i < 16; i++) {
			Node node = this.children[i];
			merkles16[i] = node == null ? null : node.getMerkleHash();
			if (merkles16[i] != null) {
				logger.info("for subtree: 16 nodes: {} merkle = {}", i, ByteUtils.toHexString(merkles16[i]));
			}
		}

		// 16 hashes -> 8 hashes:
		int merkleHeight = subtreeHeight + 4;
		byte[][] merkles8 = new byte[8][];
		for (int i = 0; i < 8; i++) {
			int n = i << 1;
			byte[] left = merkles16[n];
			byte[] right = merkles16[n + 1];
			if (left == null && right == null) {
				merkles8[i] = null;
			} else {
				if (left == null) {
					left = tree.getDefaultHashAtHeight(merkleHeight);
				}
				if (right == null) {
					right = tree.getDefaultHashAtHeight(merkleHeight);
				}
				merkles8[i] = tree.generateMerkleHash(left, right);
				logger.info("for subtree: 8 nodes: {} merkle = {}", i, ByteUtils.toHexString(merkles8[i]));
			}
		}

		// 8 hashes -> 4 hashes:
		merkleHeight--;
		byte[][] merkles4 = new byte[4][];
		for (int i = 0; i < 4; i++) {
			int n = i << 1;
			byte[] left = merkles8[n];
			byte[] right = merkles8[n + 1];
			if (left == null && right == null) {
				merkles4[i] = null;
			} else {
				if (left == null) {
					left = tree.getDefaultHashAtHeight(merkleHeight);
				}
				if (right == null) {
					right = tree.getDefaultHashAtHeight(merkleHeight);
				}
				merkles4[i] = tree.generateMerkleHash(left, right);
				logger.info("for subtree: 4 nodes: {} merkle = {}", i, ByteUtils.toHexString(merkles4[i]));
			}
		}

		// 4 hashes -> 2 hashes:
		merkleHeight--;
		byte[][] merkles2 = new byte[2][];
		for (int i = 0; i < 2; i++) {
			int n = i << 1;
			byte[] left = merkles4[n];
			byte[] right = merkles4[n + 1];
			if (left == null && right == null) {
				merkles2[i] = null;
			} else {
				if (left == null) {
					left = tree.getDefaultHashAtHeight(merkleHeight);
				}
				if (right == null) {
					right = tree.getDefaultHashAtHeight(merkleHeight);
				}
				merkles2[i] = tree.generateMerkleHash(left, right);
				logger.info("for subtree: 2 nodes: {} merkle = {}", i, ByteUtils.toHexString(merkles2[i]));
			}
		}

		// 2 hashes -> 1 hash:
		merkleHeight--;
		byte[] merkle = null;
		byte[] left = merkles2[0];
		byte[] right = merkles2[1];
		if (left != null || right != null) {
			if (left == null) {
				left = tree.getDefaultHashAtHeight(merkleHeight);
			}
			if (right == null) {
				right = tree.getDefaultHashAtHeight(merkleHeight);
			}
			merkle = tree.generateMerkleHash(left, right);
		}

		// root hash of subtree:
		merkleHeight--;
		if (merkle == null) {
			merkle = tree.getDefaultHashAtHeight(merkleHeight);
		}
		logger.info("for subtree: root: height = {}, merkle = {}", subtreeHeight, ByteUtils.toHexString(merkle));

		// continue calculate merkle for shared path:
		if (subtreeHeight > this.height) {
			int startIndex = this.height / 4;
			int endIndex = this.path.length() - 1;
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
				logger.info("for shared path: height = {}, merkle = {}", bitIndex + 3, ByteUtils.toHexString(merkle));

				left = bit2 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 3);
				right = bit2 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 3);
				merkle = tree.generateMerkleHash(left, right);

				left = bit1 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 2);
				right = bit1 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 2);
				merkle = tree.generateMerkleHash(left, right);

				left = bit0 == 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 1);
				right = bit0 != 0 ? merkle : tree.getDefaultHashAtHeight(bitIndex + 1);
				merkle = tree.generateMerkleHash(left, right);
				logger.info("for shared path: height = {}, merkle = {}", bitIndex, ByteUtils.toHexString(merkle));
			}
		}

		this.merkleHash = merkle;
	}

	@Override
	public void appendTo(StringBuilder sb, int slot) {
		for (int i = 0; i < this.height; i++) {
			sb.append(INDENT);
		}
		if (slot >= 0) {
			sb.append(String.format("%x", slot)).append(':');
		}
		sb.append("[FULL: height=").append(this.height).append(", path=").append(this.path).append(", merkleHash=")
				.append(ByteUtils.toHexString(this.merkleHash)).append("]\n");
		if (this.children != null) {
			for (int i = 0; i < 16; i++) {
				Node child = this.children[i];
				if (child != null) {
					child.appendTo(sb, i);
				}
			}
		}
	}

	@Override
	public String toString() {
		return String.format("FullNode(height=%s, path=%s, merkleHash=%s)", this.height, this.path,
				ByteUtils.toHexString(this.merkleHash));
	}
}
