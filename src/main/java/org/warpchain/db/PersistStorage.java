package org.warpchain.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.warpchain.exception.StoreException;

/**
 * Persist key-value using RocksDB.
 * 
 * @author liaoxuefeng
 */
public class PersistStorage implements KeyValueStorage {

	static {
		RocksDB.loadLibrary();
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String path;
	private Options options;
	private RocksDB rdb;

	public PersistStorage(String dbPath) {
		this.path = Paths.get(dbPath).toAbsolutePath().normalize().toString();
		logger.info("try open rocksdb: {}", this.path);
		this.options = new Options();
		this.options.setCreateIfMissing(true);
		try {
			this.rdb = RocksDB.open(options, path);
		} catch (RocksDBException e) {
			new RuntimeException(e);
		}
	}

	@Override
	public byte[] getValue(String key) {
		return getValue(key.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public byte[] getValue(byte[] key) {
		try {
			return this.rdb.get(key);
		} catch (RocksDBException e) {
			throw new StoreException(e);
		}
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
		try {
			this.rdb.put(key, value);
		} catch (RocksDBException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void removeValue(String key) {
		removeValue(key.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void removeValue(byte[] key) {
		try {
			this.rdb.delete(key);
		} catch (RocksDBException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void close() {
		if (this.rdb != null) {
			logger.info("closing rocksdb: {}", this.path);
			this.rdb.close();
			this.rdb = null;
		}
		if (this.options != null) {
			this.options.close();
			this.options = null;
		}
	}
}
