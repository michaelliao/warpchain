package org.warpchain.tree;

import org.warpchain.core.BitString;

public class FullNode extends Node {

	private Node left;
	private Node right;

	FullNode() {
		super(Node.Type.FULL);
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}

	@Override
	void insert(BitString hash, byte[] value) {
		// TODO Auto-generated method stub

	}
}
