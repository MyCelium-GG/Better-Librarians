package my.celium.org.betterlibrarians.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import my.celium.org.betterlibrarians.restock.RestockInfo;
import my.celium.org.betterlibrarians.restock.RestockRules;
import my.celium.org.client.MycelTheme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Turns stock data into short lines for the look-at card and the trading screen. Exact when the
 * server has the mod; otherwise an estimate from the time of day and the stock seen when trading.
 */
public final class RestockText {
    /** One line of text and its colour. */
    public record Line(String text, int color) {
    }

    /** What the client saw itself in the trading screen. */
    public record Seen(int trades, int soldOut, int used, long seenAt) {
    }

    private RestockText() {
    }

    /**
     * Lines for a villager.
     *
     * @param live    the server's data, or {@code null}
     * @param seen    stock seen in the trading screen, or {@code null}
     * @param clock   overworld clock time now
     * @param now     game time now
     * @param profession the villager's job, for "its lectern" when the block is unknown
     */
    public static List<Line> lines(RestockInfo live, Seen seen, long clock, long now, String profession) {
        List<Line> out = new ArrayList<>();
        if (live != null) {
            exact(out, live, clock, profession);
        } else if (seen != null) {
            estimate(out, seen, clock, now, profession);
        }
        return out;
    }

    private static void exact(List<Line> out, RestockInfo info, long clock, String profession) {
        String station = station(info.workBlock(), profession);
        if (info.used() == 0) {
            out.add(new Line(t("in_stock"), MycelTheme.DIM));
            return;
        }
        if (info.soldOut() > 0) {
            out.add(new Line(soldOut(info.soldOut(), info.trades()), MycelTheme.WARN));
        }
        if (!info.workstation()) {
            out.add(new Line(t("no_station", station), MycelTheme.BAD));
            return;
        }
        if (!info.atWorkstation() && info.distance() >= 2 && RestockRules.isWorkTime(clock)) {
            // Working hours but away from the workstation: in a trading hall cell this never changes.
            out.add(new Line(t("too_far", station, info.distance()), MycelTheme.WARN));
            return;
        }
        long wait = RestockRules.untilAllowed(clock, info.restocksToday(), info.sinceRestock(), info.newDayPending());
        boolean twice = info.restocksToday() >= RestockRules.MAX_PER_DAY && !info.newDayPending();
        if (wait > 0) {
            out.add(new Line(t(twice ? "twice" : "in", time(wait)), MycelTheme.TEXT));
        } else if (info.atWorkstation()) {
            out.add(new Line(t("any_moment"), MycelTheme.TEXT));
        } else {
            out.add(new Line(t("back_at", station), MycelTheme.TEXT));
        }
    }

    private static void estimate(List<Line> out, Seen seen, long clock, long now, String profession) {
        String station = station("", profession);
        if (seen.used() == 0) {
            out.add(new Line(t("were_in_stock"), MycelTheme.DIM));
            return;
        }
        if (seen.soldOut() > 0) {
            out.add(new Line(t("when_traded", soldOut(seen.soldOut(), seen.trades())), MycelTheme.WARN));
        }
        long since = now - seen.seenAt();
        if (since > 0 && workedSince(clock, since)) {
            out.add(new Line(t("probably"), MycelTheme.TEXT));
        } else if (RestockRules.isWorkTime(clock)) {
            out.add(new Line(t("while_works", station), MycelTheme.TEXT));
        } else {
            out.add(new Line(t("work_starts", time(RestockRules.untilWork(clock))), MycelTheme.TEXT));
        }
        out.add(new Line(t("estimate"), MycelTheme.FAINT));
    }

    /** Whether a full chance to restock (at least a minute of work time) lies between then and now. */
    static boolean workedSince(long clock, long since) {
        if (since >= RestockRules.DAY) {
            return true;
        }
        long worked = 0;
        for (long t = clock - since; t < clock; t += 100) {
            if (RestockRules.isWorkTime(t)) {
                worked += 100;
            }
        }
        return worked >= 1200;
    }

    static String soldOut(int soldOut, int trades) {
        if (soldOut == 1) {
            return t(trades > 1 ? "sold_out.one" : "sold_out.all");
        }
        return t("sold_out", soldOut);
    }

    /** The workstation's name, from its block or the profession. */
    static String station(String blockId, String profession) {
        String name = null;
        if (blockId != null && !blockId.isEmpty()) {
            try {
                Identifier id = Identifier.parse(blockId);
                name = BuiltInRegistries.BLOCK.getValue(id).getName().getString();
            } catch (RuntimeException e) {
                name = null;
            }
            if (name != null && (name.isEmpty() || "air".equalsIgnoreCase(name))) {
                name = null;
            }
        }
        if (name == null) {
            name = "librarian".equals(profession)
                    ? net.minecraft.network.chat.Component.translatable("block.minecraft.lectern").getString() : t("workstation");
        }
        return name;
    }

    private static String t(String path, Object... args) {
        return net.minecraft.client.resources.language.I18n.get("betterlibrarians.restock." + path, args);
    }

    /** Ticks as real time: "4:05", "0:12". */
    public static String time(long ticks) {
        long seconds = Math.max(0, (ticks + 19) / 20);
        return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }
}
