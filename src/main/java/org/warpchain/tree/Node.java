package org.warpchain.tree;

import org.warpchain.core.HalfByteString;

public abstract class Node {

	public abstract void appendTo(StringBuilder sb, int slot);

	public abstract byte[] getMerkleHash();

	public abstract Node update(TreeInfo tree, HalfByteString path, byte[] dataHash, byte[] dataValue);

	protected static final String INDENT = " ";

}
