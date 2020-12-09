package org.warpchain.db;

import java.io.Closeable;

public interface KeyValueStorage extends Closeable {

	byte[] getValue(String key);

	byte[] getValue(byte[] key);

	String getStringValue(String key);

	void setValue(byte[] key, byte[] value);

	void setValue(String key, byte[] value);

	void setValue(String key, String value);

	void removeValue(byte[] key);

	void removeValue(String key);
}
