package org.warpchain.tree;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.core.BitString;
import org.warpchain.util.ByteUtils;

public class ShortCutNode extends Node {

	private static final Logger logger = LoggerFactory.getLogger(ShortCutNode.class);

	/**
	 * Path from root to end height of current node.
	 */
	private final BitString path;

	/**
	 * Bit value of current node: 0=left, 1=right.
	 */
	private final int bit;

	/**
	 * Start height of this node.
	 */
	private final int startHeight;

	/**
	 * End height of this node.
	 */
	private final int endHeight;

	/**
	 * Merkle hash at start height of this node.
	 */
	private byte[] startMerkleHash;

	/**
	 * Merkle hash at end height of this node.
	 */
	private byte[] endMerkleHash;

	private Node left;

	private Node right;

	/**
	 * Data if end height is leaf.
	 */
	private final byte[] data;

	ShortCutNode(TreeInfo tree, int height, BitString path) {
		assert path.length() > height - 1;
		assert path.length() < tree.getTreeHeight();

		this.path = path;
		this.bit = path.bitValueAt(height);
		this.startHeight = height;
		this.endHeight = path.length() - 1;
		this.startMerkleHash = tree.getDefaultHashAtHeight(height);
		this.endMerkleHash = tree.getDefaultHashAtHeight(endHeight);
		this.data = null;
	}

	ShortCutNode(TreeInfo tree, int height, BitString path, byte[] dataHash, byte[] dataValue) {
		assert dataValue != null && dataValue.length > 0;
		assert path.length() == tree.getTreeHeight();
		assert path.length() == 8 * dataHash.length;

		this.path = path;
		this.bit = path.bitValueAt(height);
		this.startHeight = height;
		this.endHeight = path.length() - 1;
		this.endMerkleHash = dataHash;
		this.data = dataValue;
		this.startMerkleHash = generateStartMerkleHashFromEnd(tree);
	}

	@Override
	public byte[] getMerkleHash() {
		return startMerkleHash;
	}

	private byte[] generateStartMerkleHashFromEnd(TreeInfo tree) {
		byte[] merkle = this.endMerkleHash;
		for (int i = endHeight - 1; i >= startHeight; i--) {
			int dataBit = path.bitValueAt(i + 1);
			byte[] leftHash = dataBit == 0 ? merkle : tree.getDefaultHashAtHeight(i + 1);
			byte[] rightHash = dataBit == 1 ? merkle : tree.getDefaultHashAtHeight(i + 1);
			merkle = tree.generateMerkleHash(leftHash, rightHash);
		}
		return merkle;
	}

	private void updateMerkleHashByChildren(TreeInfo tree) {
		byte[] leftChildHash = left == null ? tree.getDefaultHashAtHeight(this.endHeight + 1) : left.getMerkleHash();
		byte[] rightChildHash = right == null ? tree.getDefaultHashAtHeight(this.endHeight + 1) : right.getMerkleHash();
		this.endMerkleHash = tree.generateMerkleHash(leftChildHash, rightChildHash);
		this.startMerkleHash = generateStartMerkleHashFromEnd(tree);
	}

	@Override
	public void update(TreeInfo tree, BitString path, byte[] dataHash, byte[] dataValue) {
		// FIXME:
	}

	void updateLeftChild() {
		//
	}

	void updateRightChild() {
		//
	}

	@Override
	public void appendTo(StringBuilder sb) {
		for (int i = 0; i <= this.startHeight; i++) {
			sb.append(INDENT);
		}
		sb.append(bit == 0 ? 'L' : 'R').append("=[SHORTCUT: height=").append(this.startHeight).append(" -> ")
				.append(this.endHeight).append(", bit=").append(this.bit).append(", path=").append(this.path)
				.append(", merkleHash=").append(ByteUtils.toHexString(this.startMerkleHash)).append(" -> ")
				.append(ByteUtils.toHexString(this.endMerkleHash));
		if (this.data != null) {
			sb.append(", data=").append(new String(this.data, StandardCharsets.UTF_8));
		} else {
			if (this.left != null) {
				this.left.appendTo(sb);
			}
			if (this.right != null) {
				this.right.appendTo(sb);
			}
		}
		sb.append("]\n");
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return String.format("ShortCutNode(height=%s -> %s, bit=%s, path=%s, merkleHash=%s -> %s)",
					this.startHeight, this.endHeight, ByteUtils.toHexString(this.startMerkleHash),
					ByteUtils.toHexString(this.endMerkleHash));
		}
		return String.format("ShortCutNode(height=%s -> %s, bit=%s, path=%s, merkleHash=%s -> %s, data=%s)",
				this.startHeight, this.endHeight, this.bit, this.path, ByteUtils.toHexString(this.startMerkleHash),
				ByteUtils.toHexString(this.endMerkleHash), new String(this.data, StandardCharsets.UTF_8));
	}
}
