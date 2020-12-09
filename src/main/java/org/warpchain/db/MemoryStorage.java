package org.warpchain.db;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Memory key-value db using hash map.
 * 
 * @author liaoxuefeng
 */
public class MemoryStorage implements KeyValueStorage {

	private final Map<byte[], byte[]> kv = new HashMap<>();

	public MemoryStorage() {
	}

	@Override
	public byte[] getValue(String key) {
		return getValue(key.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public byte[] getValue(byte[] key) {
		return this.kv.get(key);
	}

	@Override
	public String getStringValue(String key) {
		byte[] value = getValue(key);
		if (value == null) {
			return null;
		}
		return new String(value, StandardCharsets.UTF_8);
	}

	@Override
	public void setValue(String key, String value) {
		setValue(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void setValue(String key, byte[] value) {
		setValue(key.getBytes(StandardCharsets.UTF_8), value);
	}

	@Override
	public void setValue(byte[] key, byte[] value) {
		this.kv.put(key, value);
	}

	@Override
	public void removeValue(String key) {
		removeValue(key.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void removeValue(byte[] key) {
		this.kv.remove(key);
	}

	@Override
	public void close() {
		this.kv.clear();
	}
}
