package org.warpchain.db;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.warpchain.exception.StoreException;

public class PersistDb implements Closeable {

	static {
		RocksDB.loadLibrary();
	}

	final String path;
	Options options;
	RocksDB rdb;

	public PersistDb(String dbPath) {
		this.path = Paths.get(dbPath).toAbsolutePath().normalize().toString();
		this.options = new Options();
		this.options.setCreateIfMissing(true);
		try {
			this.rdb = RocksDB.open(path);
		} catch (RocksDBException e) {
			new RuntimeException(e);
		}
	}

	public byte[] getValue(String key) {
		return getValue(key.getBytes(StandardCharsets.UTF_8));
	}

	public byte[] getValue(byte[] key) {
		try {
			return this.rdb.get(key);
		} catch (RocksDBException e) {
			throw new StoreException(e);
		}
	}

	public String getStringValue(String key) {
		return new String(getValue(key), StandardCharsets.UTF_8);
	}

	@Override
	public void close() {
		if (rdb != null) {
			rdb.close();
			rdb = null;
		}
		if (options != null) {
			options.close();
			options = null;
		}
	}
}
