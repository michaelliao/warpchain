package org.warpchain.tree;

import org.warpchain.core.BitString;
import org.warpchain.core.ByteString; 

public class ShortCutNode extends Node {

	public final BitString path;

	ShortCutNode(BitString path) {
		super(Node.Type.SHORTCUT);
		this.path = path;
	}

	@Override
	void insert(BitString hash, byte[] value) {
		// TODO Auto-generated method stub

	}
}
