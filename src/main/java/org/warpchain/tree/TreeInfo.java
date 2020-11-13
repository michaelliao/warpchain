package org.warpchain.tree;

public interface TreeInfo {

	int getTreeHeight();

	byte[] generateMerkleHash(byte[] left, byte[] right);

	byte[] getDefaultHashAtHeight(int height);
}
