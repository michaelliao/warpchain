package org.warpchain.tree;

import org.warpchain.core.BitString;

public abstract class Node {

	public static enum Type {
		FULL, SHORTCUT, LEAF;
	}

	public final Type type;
	public byte[] cryptoHash = null;

	Node(Type type) {
		this.type = type;
	}

	public static LeafNode createLeafNode(BitString key, byte[] value) {
		return new LeafNode(key, value);
	}

	public static ShortCutNode createShortCutNode(BitString path) {
		return new ShortCutNode(path);
	}

	public static FullNode createFullNode() {
		return new FullNode();
	}

	abstract void insert(BitString hash, byte[] value);
}
