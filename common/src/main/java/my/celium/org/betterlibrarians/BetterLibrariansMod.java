package my.celium.org.betterlibrarians;

import my.celium.org.betterlibrarians.restock.RestockPacket;
import my.celium.org.betterlibrarians.restock.RestockServer;
import my.celium.org.network.MycelNetwork;
import my.celium.org.network.PacketDefinition;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.metadata.ModMetadata;

/**
 * Better Librarians: shows a title card with a librarian's enchanted books
 * whenever you look at them - no trading GUI required.
 *
 * <p>Mostly client side. {@link #init()} registers metadata, config and the restock packet with
 * Mycel; each loader's <em>client</em> entrypoint then (a) catalogues trade offers whenever a
 * villager trading screen is open, (b) draws the title card and the restock timer through its native
 * hooks, and (c) registers the librarian list. On a server (or in singleplayer) the mod also sends the
 * exact restock state of nearby villagers ({@code RestockServer}). Dedicated servers never touch client
 * classes.
 */
public final class BetterLibrariansMod {
    public static final String ID = "betterlibrarians";

    public static final ModMetadata META = ModMetadata.builder(ID, "Better Librarians", "1.3.0")
            .author("MyCelium")
            .website("https://www.curseforge.com/minecraft/mc-mods/better-librarians")
            .updateUrl("https://wiki.mycelium-gg.org/updates/betterlibrarians.json")
            .build();

    public static ConfigValue<Boolean> enabled;
    public static ConfigValue<Boolean> showLevel;
    public static ConfigValue<Boolean> highlightMaxLevel;
    public static ConfigValue<Integer> maxBooks;
    public static ConfigValue<Boolean> showUncataloguedHint;
    public static ConfigValue<Boolean> restockTimer;
    public static ConfigValue<Boolean> restockAllJobs;
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
                "Show an \"open its trades once\" hint for librarians whose books are unknown.");
        restockTimer = builder.booleanValue("restockTimer", true,
                "Show when villagers restock, on the look-at card and under the trading screen. Exact when the "
                        + "server has Better Librarians too (always in singleplayer), an estimate otherwise.");
        restockAllJobs = builder.booleanValue("restockAllJobs", true,
                "Show the restock timer for every working villager, not only librarians.");
        config = builder.build();
        LibrarianStats.start();

        // Optional both ways: clients without the mod never get it, servers without it never send.
        RestockServer.packet = PacketDefinition.toClient(RestockPacket.TYPE, RestockPacket.CODEC,
                (packet, context) -> my.celium.org.betterlibrarians.client.RestockClient.receive(packet));
        MycelNetwork.register(META, RestockServer.packet);
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled.get();
    }

    /** Whether this side runs the restock timer (the server sends only when it is on there). */
    public static boolean restockTimer() {
        return restockTimer != null && Mycel.isEnabled(ID) && restockTimer.get();
    }
}
