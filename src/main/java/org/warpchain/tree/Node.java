package org.warpchain.tree;

import java.nio.charset.StandardCharsets;

import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;

public class Node {
	/**
	 * Node type.
	 */
	public NodeType type;

	/**
	 * Node height level.
	 */
	public final int height;

	/**
	 * Node full path.
	 */
	public final BitString path;

	/**
	 * Node merkle hash.
	 */
	public byte[] merkleHash = null;

	/**
	 * For short cut only: end height.
	 */
	public int shortCutEndHeight;

	/**
	 * For short cut only: end hash.
	 */
	public byte[] shortCutEndMerkleHash = null;

	/**
	 * For full / short cut node if height > 0: left node.
	 */
	public Node left = null;

	/**
	 * For full / short cut node if height > 0: right node.
	 */
	public Node right = null;

	/**
	 * Is default node: default node has default merkle hash and its left / right
	 * node is null.
	 */
	public boolean isDefault;

	/**
	 * For leaf / short cut node: store leaf data.
	 */
	public byte[] data;

	Node(NodeType type, int height, BitString path, byte[] merkleHash, boolean isDefault) {
		this.type = type;
		this.height = height;
		this.path = path;
		this.merkleHash = merkleHash;
		this.isDefault = isDefault;
	}

	boolean update(SparseMerkleTree tree, BitString path, byte[] hash, byte[] data) {
		switch (this.type) {
		case FULL:
			int bit = path.bitValueAt(this.height - 1);
			if (bit == 0) {
				if (this.left == null) {
					// no left node, just create a shortcut node to data:
					Node node = new Node(NodeType.SHORTCUT, this.height - 1, path, null, false);
					node.shortCutEndHeight = 0;
					node.shortCutEndMerkleHash = hash;
					node.data = data;
					// calculate merkle:
					int dataBit = path.bitValueAt(tree.treeHeight);
					byte[] leftHash = tree.DEFAULT_HEIGHT_OF_HASH[0];
					byte[] rightHash = tree.DEFAULT_HEIGHT_OF_HASH[0];
					for (int i = 0; i < this.height - 1; i++) {

					}
				}
			} else {
				//
			}
			break;
		case LEAF:
			break;
		case SHORTCUT:
			break;
		default:
			throw new RuntimeException("Invalid node type: " + this.type);
		}
		return false;
	}

	void appendToString(int treeHeight, StringBuilder sb) {
		for (int i = 0; i < treeHeight - height; i++) {
			sb.append(' ');
		}
		switch (this.type) {
		case FULL:
			sb.append("[FULL: height=").append(this.height).append(", isDefault=").append(this.isDefault)
					.append(", merkleHash=").append(ByteUtils.toHexString(this.merkleHash)).append("]\n");
			if (this.left != null) {
				this.left.appendToString(treeHeight, sb);
			}
			if (this.right != null) {
				this.right.appendToString(treeHeight, sb);
			}
			break;
		case LEAF:
			sb.append("[LEAF: height=").append(this.height).append(", isDefault=").append(this.isDefault)
					.append(", merkleHash=").append(ByteUtils.toHexString(this.merkleHash)).append("] data=")
					.append(getDataPrefix()).append('\n');
			break;
		case SHORTCUT:
			sb.append("[SHORTCUT: height=").append(this.height).append(" -> ").append(this.shortCutEndHeight)
					.append(", isDefault=").append(this.isDefault).append(", merkleHash=")
					.append(ByteUtils.toHexString(this.merkleHash)).append("]");
			if (this.shortCutEndHeight == 0) {
				sb.append(getDataPrefix());
			}
			sb.append('\n');
			break;
		default:
			throw new RuntimeException("Invalid node type: " + this.type);
		}
	}

	private String getDataPrefix() {
		if (this.data == null || this.data.length == 0) {
			return "(empty)";
		}
		String s = new String(this.data, StandardCharsets.UTF_8);
		if (s.length() > 20) {
			s = s.substring(0, 20) + "...";
		}
		return "(length=" + this.data.length + ") " + s;
	}
}
