package org.warpchain.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class PersistStorageTest {

	PersistStorage db;

	final Path dbPath = Paths.get("./test.db.tmp").toAbsolutePath().normalize();

	@BeforeEach
	void init() throws IOException {
		if (Files.exists(dbPath)) {
			FileSystemUtils.deleteRecursively(dbPath);
		}
		db = new PersistStorage(dbPath.toString());
	}

	@AfterEach
	void tearDown() {
		db.close();
	}

	@Test
	void testSetAndGet() {
		String key1 = "key12345678-9999";
		String key2 = "abc12345678-qwer";
		assertNull(db.getStringValue(key1));
		assertNull(db.getStringValue(key2));
		db.setValue(key1, "Hello");
		assertEquals("Hello", db.getStringValue(key1));
		db.setValue(key2, "World");
		assertEquals("World", db.getStringValue(key2));
		db.setValue(key1, "Changed");
		assertEquals("Changed", db.getStringValue(key1));
		db.removeValue(key2);
		assertNull(db.getStringValue(key2));
	}
}
