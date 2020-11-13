package org.warpchain.tree;

import org.warpchain.core.BitString;

public abstract class Node {
	
	protected static final String INDENT = "  ";

	public abstract void appendTo(StringBuilder sb);

	public abstract byte[] getMerkleHash();

	public abstract void update(TreeInfo tree, BitString path, byte[] dataHash, byte[] dataValue);

}
