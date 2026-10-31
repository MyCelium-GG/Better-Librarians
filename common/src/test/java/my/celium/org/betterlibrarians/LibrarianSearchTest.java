package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import my.celium.org.betterlibrarians.VillagerDataStore.StoredBook;
import my.celium.org.betterlibrarians.VillagerDataStore.StoredVillager;

/** {@link LibrarianSearch} finds who sells what, cheapest first, per world. */
class LibrarianSearchTest {
    private static StoredVillager villager(String world, String name, StoredBook... books) {
        StoredVillager out = new StoredVillager();
        out.world = world;
        out.name = name;
        out.books = List.of(books);
        return out;
    }

    private static Map<String, StoredVillager> entries() {
        Map<String, StoredVillager> map = new LinkedHashMap<>();
        map.put("expensive", villager("w", null, new StoredBook("Mending", true, 38)));
        map.put("cheap", villager("w", "Bob", new StoredBook("Mending", true, 12), new StoredBook("Unbreaking III", true, 20)));
        map.put("unknown", villager("w", null, new StoredBook("Mending", true, 0)));
        map.put("elsewhere", villager("other", null, new StoredBook("Mending", true, 1)));
        map.put("old", villager(null, null, new StoredBook("Sharpness V", true, 0)));
        map.put("renamedOnly", villager("w", "Sam"));
        return map;
    }

    @Test
    void cheapestFirstThenUnknownPrices() {
        List<LibrarianSearch.Hit> hits = LibrarianSearch.search(entries(), "w", "mend");
        assertEquals(List.of("cheap", "expensive", "unknown"), hits.stream().map(LibrarianSearch.Hit::id).toList());
        assertEquals(12, hits.get(0).book().cost);
    }

    @Test
    void blankListsThisWorldAndOldEntriesWithBooks() {
        assertEquals(List.of("expensive", "cheap", "unknown", "old"),
                LibrarianSearch.search(entries(), "w", " ").stream().map(LibrarianSearch.Hit::id).toList());
    }

    @Test
    void findsByVillagerName() {
        List<LibrarianSearch.Hit> hits = LibrarianSearch.search(entries(), "w", "bob");
        assertEquals("cheap", hits.get(0).id());
        assertNull(hits.get(0).book());
    }
}
