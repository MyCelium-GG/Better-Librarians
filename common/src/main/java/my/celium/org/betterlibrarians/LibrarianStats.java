package my.celium.org.betterlibrarians;

import java.util.ArrayList;
import java.util.List;

import my.celium.org.stats.ModStats;

/**
 * Anonymous stats for the FastStats project {@code better-librarians}, sent through Mycel under
 * its consent: how many librarians are catalogued and renamed, the settings, and which features
 * get used. Names and book texts are never sent.
 */
public final class LibrarianStats {
    private static final String TOKEN = "b0134c089f72df5be020ae5389f939cb";

    private static volatile ModStats stats;

    private LibrarianStats() {
    }

    static void start() {
        stats = ModStats.builder(BetterLibrariansMod.META, TOKEN)
                .number("catalogued_librarians", LibrarianStats::catalogued)
                .number("renamed_villagers", () -> VillagerDataStore.namedEntries().size())
                .number("max_level_books", LibrarianStats::maxLevelBooks)
                .number("max_books", () -> BetterLibrariansMod.maxBooks.get())
                .stringArray("settings_off", LibrarianStats::settingsOff)
                .errors("my.celium.org.betterlibrarians.")
                .start();
    }

    /**
     * Counts a feature use: {@code list_key}, {@code list_command}, {@code search}, {@code find},
     * {@code forget}, {@code rename}, {@code rename_clear}, {@code catalogue}.
     */
    public static void count(String what) {
        ModStats current = stats;
        if (current != null) {
            current.count(what);
        }
    }

    private static int catalogued() {
        int count = 0;
        for (VillagerDataStore.StoredVillager villager : VillagerDataStore.entries().values()) {
            if (villager.books != null && !villager.books.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static int maxLevelBooks() {
        int count = 0;
        for (VillagerDataStore.StoredVillager villager : VillagerDataStore.entries().values()) {
            if (villager.books != null) {
                for (VillagerDataStore.StoredBook book : villager.books) {
                    if (book.maxLevel) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static String[] settingsOff() {
        List<String> off = new ArrayList<>();
        if (!BetterLibrariansMod.enabled.get()) {
            off.add("enabled");
        }
        if (!BetterLibrariansMod.showLevel.get()) {
            off.add("showLevel");
        }
        if (!BetterLibrariansMod.highlightMaxLevel.get()) {
            off.add("highlightMaxLevel");
        }
        if (!BetterLibrariansMod.showUncataloguedHint.get()) {
            off.add("showUncataloguedHint");
        }
        return off.isEmpty() ? null : off.toArray(String[]::new);
    }
}
