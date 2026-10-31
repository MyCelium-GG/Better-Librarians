package my.celium.org.betterlibrarians;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import my.celium.org.Mycel;
import my.celium.org.data.MycelDataFile;
import my.celium.org.logging.MycelLog;

/**
 * Persistent client storage for villager names and catalogued books.
 *
 * <p>Lives in {@code config/betterlibrarians_data.json} (alongside Mycel's
 * own config dir), so renames and catalogues survive restarts. Small enough
 * to rewrite wholesale on every mutation. {@link MycelDataFile} keeps a
 * broken file aside instead of letting the next save overwrite it.
 */
public final class VillagerDataStore {
    static final String FILE_NAME = "betterlibrarians_data.json";
    /** Maximum villagers kept; oldest entries are dropped first. */
    static final int MAX_VILLAGERS = 256;

    private static final Map<String, StoredVillager> DATA = new LinkedHashMap<>();
    private static MycelDataFile file;

    private VillagerDataStore() {
    }

    /** One catalogued book: display text, whether it is max level, and its price. */
    public static final class StoredBook {
        public String text = "";
        public boolean maxLevel;
        /** Emeralds when last traded with; 0 when unknown (catalogued before prices existed). */
        public int cost;

        public StoredBook() {
        }

        public StoredBook(String text, boolean maxLevel) {
            this(text, maxLevel, 0);
        }

        public StoredBook(String text, boolean maxLevel, int cost) {
            this.text = text == null ? "" : text;
            this.maxLevel = maxLevel;
            this.cost = Math.max(0, cost);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof StoredBook that)) {
                return false;
            }
            return maxLevel == that.maxLevel && cost == that.cost && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, maxLevel, cost);
        }
    }

    /** One stored villager: optional rename, catalogued books and where they were traded with. */
    public static final class StoredVillager {
        public String name;
        public List<StoredBook> books = new ArrayList<>();
        /** The server or singleplayer world, {@code null} until traded with since locations existed. */
        public String world;
        public String dimension;
        public int x;
        public int y;
        public int z;

        public boolean isEmpty() {
            return (name == null || name.isBlank()) && (books == null || books.isEmpty());
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
        file = dir == null ? null : MycelDataFile.at(BetterLibrariansMod.META, dir.resolve(FILE_NAME));
        if (file == null) {
            return;
        }
        Root root = file.read(Root.class, Root::new);
        if (root.villagers != null) {
            for (Map.Entry<String, StoredVillager> entry : root.villagers.entrySet()) {
                try {
                    UUID.fromString(entry.getKey());
                } catch (IllegalArgumentException bad) {
                    continue;
                }
                StoredVillager villager = entry.getValue();
                if (villager == null) {
                    continue;
                }
                if (villager.books == null) {
                    villager.books = new ArrayList<>();
                }
                villager.books.removeIf(Objects::isNull);
                if (!villager.isEmpty()) {
                    DATA.put(entry.getKey(), villager);
                }
            }
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

    /** Remembers where a villager was traded with. Persists when it changed. */
    public static synchronized void setLocation(UUID id, String world, String dimension, int x, int y, int z) {
        StoredVillager stored = id == null ? null : DATA.get(id.toString());
        if (stored == null || (Objects.equals(world, stored.world) && Objects.equals(dimension, stored.dimension)
                && x == stored.x && y == stored.y && z == stored.z)) {
            return;
        }
        stored.world = world;
        stored.dimension = dimension;
        stored.x = x;
        stored.y = y;
        stored.z = z;
        save();
    }

    /** Forgets a villager's books and location, keeping a rename. Persists. */
    public static synchronized void forgetBooks(UUID id) {
        StoredVillager stored = id == null ? null : DATA.get(id.toString());
        if (stored == null) {
            return;
        }
        stored.books = new ArrayList<>();
        stored.world = null;
        stored.dimension = null;
        if (stored.isEmpty()) {
            DATA.remove(id.toString());
        }
        save();
    }

    /** Snapshot of uuid-string to villager for everyone stored. */
    public static synchronized Map<String, StoredVillager> entries() {
        return new LinkedHashMap<>(DATA);
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
        file = null;
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
        if (file == null) {
            return;
        }
        Root root = new Root();
        root.villagers.putAll(DATA);
        file.write(root);
    }
}
