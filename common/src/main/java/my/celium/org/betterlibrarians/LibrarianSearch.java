package my.celium.org.betterlibrarians;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import my.celium.org.betterlibrarians.VillagerDataStore.StoredBook;
import my.celium.org.betterlibrarians.VillagerDataStore.StoredVillager;

/**
 * Finds who sells what: filters catalogued librarians to the current world and
 * a search text, cheapest matching book first.
 */
public final class LibrarianSearch {
    /** A librarian that matched, and the cheapest book that matched by title (or {@code null}). */
    public record Hit(String id, StoredVillager villager, StoredBook book) {
    }

    private LibrarianSearch() {
    }

    /**
     * @param entries uuid-string to villager
     * @param world   the current world; entries from another world are left out, and entries with
     *                no world (catalogued before locations existed) are kept
     * @param query   text to find in a book's title or the villager's name; blank matches everyone
     */
    public static List<Hit> search(Map<String, StoredVillager> entries, String world, String query) {
        String text = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Hit> hits = new ArrayList<>();
        for (Map.Entry<String, StoredVillager> entry : entries.entrySet()) {
            StoredVillager villager = entry.getValue();
            if (villager.books == null || villager.books.isEmpty()
                    || (villager.world != null && !Objects.equals(villager.world, world))) {
                continue;
            }
            StoredBook best = null;
            for (StoredBook book : villager.books) {
                if (!text.isEmpty() && book.text.toLowerCase(Locale.ROOT).contains(text)
                        && (best == null || priceOrder(book) < priceOrder(best))) {
                    best = book;
                }
            }
            boolean nameMatches = villager.name != null && villager.name.toLowerCase(Locale.ROOT).contains(text);
            if (text.isEmpty() || best != null || nameMatches) {
                hits.add(new Hit(entry.getKey(), villager, best));
            }
        }
        if (!text.isEmpty()) {
            hits.sort(Comparator.comparingInt(hit -> hit.book() == null ? Integer.MAX_VALUE : priceOrder(hit.book())));
        }
        return hits;
    }

    /** Unknown prices sort after every known one. */
    private static int priceOrder(StoredBook book) {
        return book.cost > 0 ? book.cost : Integer.MAX_VALUE - 1;
    }
}
