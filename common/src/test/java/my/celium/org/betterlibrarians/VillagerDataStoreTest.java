package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.betterlibrarians.VillagerDataStore.StoredBook;

/** Headless tests for the JSON store (files only, no Minecraft). */
class VillagerDataStoreTest {
    @AfterEach
    void clear() {
        VillagerDataStore.clearForTesting();
    }

    @Test
    void namesRoundTrip(@TempDir Path dir) {
        VillagerDataStore.load(dir);
        UUID id = UUID.randomUUID();
        assertNull(VillagerDataStore.getName(id));
        VillagerDataStore.setName(id, "&aBob");
        assertEquals("&aBob", VillagerDataStore.getName(id));

        // Survives a reload from disk.
        VillagerDataStore.load(dir);
        assertEquals("&aBob", VillagerDataStore.getName(id));

        VillagerDataStore.setName(id, null);
        assertNull(VillagerDataStore.getName(id));
    }

    @Test
    void booksRoundTrip(@TempDir Path dir) {
        VillagerDataStore.load(dir);
        UUID id = UUID.randomUUID();
        assertEquals(List.of(), VillagerDataStore.getBooks(id));
        var books = List.of(new StoredBook("Sharpness V", true), new StoredBook("Mending", false));
        VillagerDataStore.setBooks(id, books);

        VillagerDataStore.load(dir);
        assertEquals(books, VillagerDataStore.getBooks(id));
    }

    @Test
    void invalidEntriesIgnored(@TempDir Path dir) throws Exception {
        java.nio.file.Files.writeString(dir.resolve(VillagerDataStore.FILE_NAME),
                "{\"villagers\":{\"nope\":{\"name\":\"x\"}}}");
        VillagerDataStore.load(dir);
        assertEquals(0, VillagerDataStore.namedEntries().size());
    }
}
