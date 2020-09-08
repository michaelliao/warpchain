package org.warpchain.trie;

import org.warpchain.core.HalfByteString;
import org.warpchain.util.HashUtils;

public class Node {

	final NodeType type;
	Node[] slots;
	HalfByteString key;
	byte[] value;

	Node(NodeType type, byte[] rawKey, byte[] value) {
		this.type = type;
		this.key = new HalfByteString(HashUtils.sha256(rawKey));
		this.value = value;
	}

	public HalfByteString getKey() {
		return this.key;
	}

	public byte[] getValue() {
		return value;
	}
}
