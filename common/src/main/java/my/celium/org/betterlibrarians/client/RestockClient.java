package my.celium.org.betterlibrarians.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import my.celium.org.betterlibrarians.restock.RestockInfo;
import my.celium.org.betterlibrarians.restock.RestockPacket;
import net.minecraft.client.Minecraft;

/** What the server last said about each nearby villager's stock (only when the server has the mod). */
public final class RestockClient {
    /** Server data older than this (ticks) counts as gone: the villager left the range, or the server stopped. */
    static final long FRESH = 60;

    private static final Map<UUID, Entry> LIVE = new ConcurrentHashMap<>();

    private record Entry(RestockInfo info, long receivedAt) {
    }

    private RestockClient() {
    }

    public static void receive(RestockPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        for (RestockInfo info : packet.villagers()) {
            LIVE.put(info.villager(), new Entry(info, now));
        }
    }

    /** The server's data for a villager, aged to now, or {@code null} when there is none. */
    public static RestockInfo live(UUID villager) {
        Minecraft minecraft = Minecraft.getInstance();
        Entry entry = villager == null ? null : LIVE.get(villager);
        if (entry == null || minecraft.level == null) {
            return null;
        }
        long age = minecraft.level.getGameTime() - entry.receivedAt;
        if (age < 0 || age > FRESH) {
            LIVE.remove(villager);
            return null;
        }
        RestockInfo i = entry.info;
        return new RestockInfo(i.villager(), i.trades(), i.soldOut(), i.used(), i.restocksToday(), i.sinceRestock() + age,
                i.newDayPending(), i.workstation(), i.atWorkstation(), i.distance(), i.workBlock());
    }

    /** Whether the server sent anything lately, so it has the mod. */
    public static boolean serverHasMod() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        long now = minecraft.level.getGameTime();
        return LIVE.values().stream().anyMatch(entry -> now - entry.receivedAt <= FRESH);
    }

    /** Forgets everything, when leaving a world. */
    public static void clear() {
        LIVE.clear();
    }
}
