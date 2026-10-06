package my.celium.org.betterlibrarians;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import my.celium.org.Mycel;
import my.celium.org.logging.MycelLog;

/**
 * Persistent client storage for villager names and catalogued books.
 *
 * <p>Lives in {@code config/betterlibrarians_data.json} (alongside Mycel's
 * own config dir), so renames and catalogues survive restarts. Small enough
 * to rewrite wholesale on every mutation. No Minecraft classes - only Gson
 * and files - so it is fully unit-testable.
 */
public final class VillagerDataStore {
    static final String FILE_NAME = "betterlibrarians_data.json";
    /** Maximum villagers kept; oldest entries are dropped first. */
    static final int MAX_VILLAGERS = 256;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, StoredVillager> DATA = new LinkedHashMap<>();
    private static Path loadedFrom;

    private VillagerDataStore() {
    }

    /** One catalogued book: display text plus whether it is max level. */
    public static final class StoredBook {
        public String text = "";
        public boolean maxLevel;

        public StoredBook() {
        }

        public StoredBook(String text, boolean maxLevel) {
            this.text = text == null ? "" : text;
            this.maxLevel = maxLevel;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof StoredBook that)) {
                return false;
            }
            return maxLevel == that.maxLevel && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, maxLevel);
        }
    }

    /** One stored villager: optional rename plus catalogued books. */
    public static final class StoredVillager {
        public String name;
        public List<StoredBook> books = new ArrayList<>();

        public boolean isEmpty() {
            return (name == null || name.isBlank()) && books.isEmpty();
        }
    }

    private static final class Root {
        Map<String, StoredVillager> villagers = new LinkedHashMap<>();
    }

    /** Loads from Mycel's config dir (client). */
    public static synchronized void load() {
        try {
            load(Mycel.getConfigDir());
        } catch (RuntimeException e) {
            MycelLog.warnMod(BetterLibrariansMod.ID, "Could not locate config dir: {}", e.toString());
        }
    }

    /** Loads from an explicit directory (used by tests). */
    public static synchronized void load(Path dir) {
        DATA.clear();
        loadedFrom = dir == null ? null : dir.resolve(FILE_NAME);
        if (loadedFrom == null || !Files.isRegularFile(loadedFrom)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(loadedFrom, StandardCharsets.UTF_8)) {
            Root root = GSON.fromJson(reader, Root.class);
            if (root != null && root.villagers != null) {
                for (Map.Entry<String, StoredVillager> entry : root.villagers.entrySet()) {
                    try {
                        UUID.fromString(entry.getKey());
                    } catch (IllegalArgumentException bad) {
                        continue;
                    }
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        DATA.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            MycelLog.warnMod(BetterLibrariansMod.ID, "Could not read villager data, starting fresh: {}",
                    e.toString());
            DATA.clear();
        }
    }

    /** Current rename for a villager, or {@code null} when unnamed. */
    public static synchronized String getName(UUID id) {
        StoredVillager stored = id == null ? null : DATA.get(id.toString());
        return stored == null ? null : stored.name;
    }

    /** Sets (or clears, when blank) a villager's rename. Persists. */
    public static synchronized void setName(UUID id, String raw) {
        if (id == null) {
            return;
        }
        StoredVillager stored = DATA.computeIfAbsent(id.toString(), key -> new StoredVillager());
        stored.name = raw == null || raw.isBlank() ? null : raw;
        prune(id.toString());
        save();
    }

    /** Catalogued books for a villager (never {@code null}). */
    public static synchronized List<StoredBook> getBooks(UUID id) {
        StoredVillager stored = id == null ? null : DATA.get(id.toString());
        if (stored == null || stored.books == null) {
            return List.of();
        }
        return List.copyOf(stored.books);
    }

    /**
     * Replaces a villager's catalogued books. Returns whether anything
     * changed (and was persisted).
     */
    public static synchronized boolean setBooks(UUID id, List<StoredBook> books) {
        if (id == null) {
            return false;
        }
        List<StoredBook> fresh = books == null ? List.of() : List.copyOf(books);
        StoredVillager stored = DATA.computeIfAbsent(id.toString(), key -> new StoredVillager());
        if (fresh.equals(stored.books)) {
            if (stored.isEmpty()) {
                DATA.remove(id.toString());
            }
            return false;
        }
        stored.books = new ArrayList<>(fresh);
        prune(id.toString());
        save();
        return true;
    }

    /** Snapshot of uuid-string to raw name for every renamed villager. */
    public static synchronized Map<String, String> namedEntries() {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, StoredVillager> entry : DATA.entrySet()) {
            if (entry.getValue().name != null && !entry.getValue().name.isBlank()) {
                out.put(entry.getKey(), entry.getValue().name);
            }
        }
        return out;
    }

    /** Forgets everything (used by tests). Does not touch disk. */
    static synchronized void clearForTesting() {
        DATA.clear();
        loadedFrom = null;
    }

    private static void prune(String keepKey) {
        StoredVillager kept = DATA.remove(keepKey);
        while (DATA.size() > MAX_VILLAGERS - 1) {
            var oldest = DATA.keySet().iterator();
            if (!oldest.hasNext()) {
                break;
            }
            oldest.next();
            oldest.remove();
        }
        if (kept != null && !kept.isEmpty()) {
            DATA.put(keepKey, kept);
        }
    }

    private static void save() {
        if (loadedFrom == null) {
            return;
        }
        try {
            Files.createDirectories(loadedFrom.getParent());
            Root root = new Root();
            root.villagers.putAll(DATA);
            try (Writer writer = Files.newBufferedWriter(loadedFrom, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException | RuntimeException e) {
            MycelLog.warnMod(BetterLibrariansMod.ID, "Could not save villager data: {}", e.toString());
        }
    }
}
