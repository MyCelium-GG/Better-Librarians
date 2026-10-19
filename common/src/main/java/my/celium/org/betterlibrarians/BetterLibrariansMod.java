package my.celium.org.betterlibrarians;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.metadata.ModMetadata;

/**
 * Better Librarians: shows a title card with a librarian's enchanted books
 * whenever you look at them - no trading GUI required.
 *
 * <p>Client-side only. {@link #init()} registers metadata and config with
 * Mycel; each loader's <em>client</em> entrypoint then (a) catalogues trade
 * offers whenever a villager trading screen is open, and (b) draws the title
 * card through its native HUD hook. Dedicated servers never touch client
 * classes.
 */
public final class BetterLibrariansMod {
    public static final String ID = "betterlibrarians";

    public static final ModMetadata META = ModMetadata.builder(ID, "Better Librarians", "1.0.1")
            .author("MyCelium")
            .website("https://www.curseforge.com/minecraft/mc-mods/better-librarians")
            .updateUrl("https://wiki.mycelium-gg.org/updates/betterlibrarians.json")
            .build();

    public static ConfigValue<Boolean> enabled;
    public static ConfigValue<Boolean> showLevel;
    public static ConfigValue<Boolean> highlightMaxLevel;
    public static ConfigValue<Integer> maxBooks;
    public static ConfigValue<Boolean> showUncataloguedHint;
    public static MycelConfig config;

    private BetterLibrariansMod() {
    }

    /** Called once from each loader's entrypoint. */
    public static void init() {
        Mycel.initialize();
        Mycel.registerMod(META);

        ConfigBuilder builder = Mycel.config(META);
        enabled = builder.booleanValue("enabled", true,
                "Whether the look-at title card is shown.");
        showLevel = builder.booleanValue("showLevel", true,
                "Append the enchantment level (e.g. \"Sharpness V\").");
        highlightMaxLevel = builder.booleanValue("highlightMaxLevel", true,
                "Draw max-level books in gold, lower levels in white.");
        maxBooks = builder.intValue("maxBooks", 6, 1, 12,
                "Maximum enchanted books listed on the title card.");
        showUncataloguedHint = builder.booleanValue("showUncataloguedHint", true,
                "Show a \"trade once to catalogue\" hint for villagers whose books are unknown.");
        config = builder.build();
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled.get();
    }
}
