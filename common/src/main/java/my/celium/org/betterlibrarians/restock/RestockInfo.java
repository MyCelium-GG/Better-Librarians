package my.celium.org.betterlibrarians.restock;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

/**
 * What the server knows about one villager's stock, sent to players who have the mod.
 *
 * @param villager       the villager
 * @param trades         number of trades
 * @param soldOut        trades that cannot be used until a restock
 * @param used           trades used at least once since the last restock (vanilla restocks only then)
 * @param restocksToday  restocks since the day started
 * @param sinceRestock   ticks since the last restock
 * @param newDayPending  a new day started, so the daily count resets at the next check
 * @param workstation    whether it has a workstation at all
 * @param atWorkstation  whether it stands close enough to it to restock (1.73 blocks)
 * @param distance       blocks to its workstation, rounded; -1 when it has none or it is elsewhere
 * @param workBlock      the workstation's block id, or "" when unknown
 */
public record RestockInfo(UUID villager, int trades, int soldOut, int used, int restocksToday, long sinceRestock,
        boolean newDayPending, boolean workstation, boolean atWorkstation, int distance, String workBlock) {

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(villager);
        buf.writeVarInt(trades);
        buf.writeVarInt(soldOut);
        buf.writeVarInt(used);
        buf.writeVarInt(restocksToday);
        buf.writeVarLong(Math.max(0, sinceRestock));
        buf.writeByte((newDayPending ? 1 : 0) | (workstation ? 2 : 0) | (atWorkstation ? 4 : 0));
        buf.writeVarInt(distance + 1);
        buf.writeUtf(workBlock, 128);
    }

    public static RestockInfo read(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        int trades = buf.readVarInt();
        int soldOut = buf.readVarInt();
        int used = buf.readVarInt();
        int restocks = buf.readVarInt();
        long since = buf.readVarLong();
        int flags = buf.readByte();
        int distance = buf.readVarInt() - 1;
        String block = buf.readUtf(128);
        return new RestockInfo(id, trades, soldOut, used, restocks, since, (flags & 1) != 0, (flags & 2) != 0,
                (flags & 4) != 0, distance, block);
    }
}
