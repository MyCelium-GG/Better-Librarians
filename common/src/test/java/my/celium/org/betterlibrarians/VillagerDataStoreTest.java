package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
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

    @Test
    void brokenFileIsKeptInsteadOfOverwritten(@TempDir Path dir) throws Exception {
        Path data = dir.resolve(VillagerDataStore.FILE_NAME);
        Files.writeString(data, "{ \"villagers\": [ broken");
        VillagerDataStore.load(dir);
        UUID id = UUID.randomUUID();
        VillagerDataStore.setName(id, "Alfred");
        assertEquals("{ \"villagers\": [ broken",
                Files.readString(dir.resolve(VillagerDataStore.FILE_NAME + ".broken")));
        assertEquals("Alfred", VillagerDataStore.getName(id));
    }

    @Test
    void nullBooksDoNotWipeTheFile(@TempDir Path dir) throws Exception {
        UUID id = UUID.randomUUID();
        Files.writeString(dir.resolve(VillagerDataStore.FILE_NAME),
                "{ \"villagers\": { \"" + id + "\": { \"name\": \"Bea\", \"books\": null } } }");
        VillagerDataStore.load(dir);
        assertEquals("Bea", VillagerDataStore.getName(id));
        assertEquals(List.of(), VillagerDataStore.getBooks(id));
    }

    @Test
    void remembersPriceAndPlaceThenForgets() {
        java.util.UUID id = java.util.UUID.randomUUID();
        VillagerDataStore.setBooks(id, java.util.List.of(new VillagerDataStore.StoredBook("Mending", true, 24)));
        VillagerDataStore.setLocation(id, "world:w", "minecraft:overworld", 1, 2, 3);
        VillagerDataStore.StoredVillager stored = VillagerDataStore.entries().get(id.toString());
        org.junit.jupiter.api.Assertions.assertEquals(24, stored.books.get(0).cost);
        org.junit.jupiter.api.Assertions.assertEquals("minecraft:overworld", stored.dimension);
        VillagerDataStore.forgetBooks(id);
        org.junit.jupiter.api.Assertions.assertNull(VillagerDataStore.entries().get(id.toString()));
    }

    @Test
    void forgettingKeepsARename() {
        java.util.UUID id = java.util.UUID.randomUUID();
        VillagerDataStore.setName(id, "Bob");
        VillagerDataStore.setBooks(id, java.util.List.of(new VillagerDataStore.StoredBook("Mending", true, 24)));
        VillagerDataStore.forgetBooks(id);
        org.junit.jupiter.api.Assertions.assertEquals("Bob", VillagerDataStore.getName(id));
        org.junit.jupiter.api.Assertions.assertTrue(VillagerDataStore.getBooks(id).isEmpty());
    }
}
