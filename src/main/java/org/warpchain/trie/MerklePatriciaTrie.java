package org.warpchain.trie;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.warpchain.core.HalfByteString;
import org.warpchain.util.ByteUtils;
import org.warpchain.util.HashUtils;

public class MerklePatriciaTrie {

	private Node root = null;

	public MerklePatriciaTrie() {
	}

	public Node createNode(String hexKey, String hexValue) {
		return createNode(toKey(hexKey), ByteUtils.fromHexString(hexValue));
	}

	public Node createNode(String hexKey, byte[] value) {
		return createNode(toKey(hexKey), value);
	}

	public Node createNode(byte[] rawKey, byte[] value) {
		return createNode(toKey(rawKey), value);
	}

	public String getValueAsString(String hexKey) {
		byte[] value = getValue(hexKey);
		return value == null ? null : new String(value, StandardCharsets.UTF_8);
	}

	public byte[] getValue(String hexKey) {
		if (this.root == null) {
			return null;
		}
		HalfByteString key = toKey(hexKey);
		return root.recursiveGetValue(key);
	}

	Node createNode(HalfByteString key, byte[] value) {
		if (value == null) {
			return removeNode(key);
		}
		if (this.root == null) {
			this.root = createFullNode(key, null);
		}
		return insertNode(this.root, key, key, value);
	}

	private static Node insertNode(Node target, HalfByteString fullKey, HalfByteString suffixKey, byte[] value) {
		switch (target.type) {
		case FULL:
			if (suffixKey.isEmpty()) {
				// update node value:
				target.value = value;
			} else {
				// find a slot:
				int slot = suffixKey.valueAt(0);
				Node child = target.children[slot];
				if (child == null) {
					// insert new leaf into this slots:
					child = createLeafNode(suffixKey.substring(1), value);
					target.children[slot] = child;
				} else {
					//
				}
				return child;
			}
			break;
		case SHORT:
			break;
		case LEAF:
			break;
		default:
			break;
		}
		return null;
	}

	public Node removeNode(HalfByteString key) {
		return null;
	}

	private static Node createLeafNode(HalfByteString key, byte[] value) {
		return new Node(NodeType.LEAF, key, value);
	}

	private static Node createFullNode(HalfByteString key, byte[] value) {
		return new Node(NodeType.FULL, key, value);
	}

	private static HalfByteString toKey(byte[] rawKey) {
		return new HalfByteString(HashUtils.sha256(rawKey));
	}

	private HalfByteString toKey(String hexKey) {
		return new HalfByteString(ByteUtils.fromHexString(hexKey));
	}

	public void print(PrintStream ps) {
		if (this.root == null) {
			ps.println("null MPT");
		} else {
			root.print(ps, 0);
		}
	}
}
