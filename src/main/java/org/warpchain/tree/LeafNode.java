package org.warpchain.tree;

import org.warpchain.core.BitString;

public class LeafNode extends Node {

	public final BitString key;
	public final byte[] valueHash;

	LeafNode(BitString key, byte[] valueHash) {
		super(Node.Type.LEAF);
		this.key = key;
		this.valueHash = valueHash;
	}

	@Override
	void insert(BitString hash, byte[] value) {
		// TODO Auto-generated method stub

	}
}
