package org.warpchain.trie;

public class MerklePatriciaTrie {

	private Node root = null;

	public MerklePatriciaTrie() {
	}

	public Node createNode(byte[] key, byte[] value) {
		if (value == null) {
			removeNode(key);
			return null;
		}
		if (root == null) {
			root = createLeafNode(key, value);
			return root;
		}

		return null;
	}

	public void removeNode(byte[] key) {
		// TODO Auto-generated method stub

	}

	private Node createLeafNode(byte[] key, byte[] value) {
		return new Node(NodeType.LEAF, key, value);
	}

}
