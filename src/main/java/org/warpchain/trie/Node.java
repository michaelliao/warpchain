package org.warpchain.trie;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.warpchain.core.HalfByteString;
import org.warpchain.util.ByteUtils;

public class Node {

	final NodeType type;

	Node left; // when type == FULL
	Node right; // when type == FULL
	Node[] children; // when type==FULL
	HalfByteString key; // when type==LEAF or SHORT
	Node next; // when type=SHORT
	byte[] value; // when type==LEAF or FULL

	Node(NodeType type, HalfByteString key, byte[] value) {
		this.type = type;
		this.key = key;
		this.value = value;
		if (type == NodeType.FULL) {
			this.children = new Node[16];
		}
	}

	public HalfByteString getKey() {
		return this.key;
	}

	public byte[] getValue() {
		return value;
	}

	byte[] recursiveGetValue(HalfByteString suffixKey) {
		switch (this.type) {
		case FULL:
			if (suffixKey.isEmpty()) {
				return this.value;
			}
			Node child = this.children[suffixKey.valueAt(0)];
			if (child != null) {
				return child.recursiveGetValue(suffixKey.substring(1));
			}
			return null;
		case LEAF:
			if (this.key.equals(suffixKey)) {
				return this.value;
			}
			return null;
		case SHORT:
			if (suffixKey.startsWith(this.key)) {
				return this.next.recursiveGetValue(suffixKey.substring(this.key.length()));
			}
			return null;
		default:
			throw new RuntimeException("Unknown node type: " + this.type);
		}
	}

	void print(PrintStream ps, int depth) {
		ps.print("  ".repeat(depth));
		ps.print("[");
		ps.print(type);
		ps.print("]");
		switch (this.type) {
		case FULL:
			ps.print(" value = ");
			ps.println(valueAsString());
			for (int i = 0; i < 16; i++) {
				Node child = this.children[i];
				ps.print("  ".repeat(depth + 1));
				if (child == null) {
					ps.println(String.format("[%x] => null ", i));
				} else {
					ps.println(String.format("[%x] =>", i));
					child.print(ps, depth + 1);
				}
			}
			break;
		case SHORT:
			ps.println();
			break;
		case LEAF:
			ps.print(" key = ");
			ps.print(this.key.toString());
			ps.print(", value = ");
			ps.println(valueAsString());
			break;
		default:
			throw new RuntimeException("Unknown node type: " + this.type);
		}
		ps.println();
	}

	private String valueAsString() {
		if (this.value == null) {
			return "null";
		}
		return String.format("0x%s \"%s\"", ByteUtils.toHexString(this.value),
				new String(this.value, StandardCharsets.UTF_8));
	}
}
