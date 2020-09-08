package org.warpchain.trie;

public class Node {

	final NodeType type;
	Node[] slots;
	byte[] key;
	byte[] value;

	Node(NodeType type, byte[] key, byte[] value) {
		this.type = type;
		this.key = key;
		this.value = value;
	}
}
