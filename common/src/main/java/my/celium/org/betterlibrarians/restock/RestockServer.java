package my.celium.org.betterlibrarians.restock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.mixin.VillagerAccessor;
import my.celium.org.network.MycelNetwork;
import my.celium.org.network.PacketDefinition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.timeline.Timelines;

/**
 * Sends players with the mod the stock of the villagers around them, once a second. Players
 * without the mod, and servers without it, simply get the client's estimate instead.
 */
public final class RestockServer {
    /** How far around the player villagers are reported. */
    static final double RANGE = 12;
    /** Vanilla's reach to the workstation in WorkAtPoi. */
    static final double WORK_REACH = 1.73;

    public static PacketDefinition<RestockPacket> packet;

    private RestockServer() {
    }

    /** Server tick, from each loader's tick event. */
    public static void tick(MinecraftServer server) {
        if (server == null || packet == null || server.getTickCount() % 20 != 0 || !BetterLibrariansMod.restockTimer()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            try {
                if (MycelNetwork.canSend(player, packet)) {
                    List<RestockInfo> list = around(player);
                    if (!list.isEmpty()) {
                        MycelNetwork.sendToPlayer(player, packet, new RestockPacket(list));
                    }
                }
            } catch (RuntimeException e) {
                // A timer is a nicety; never break the server tick.
            }
        }
    }

    private static List<RestockInfo> around(ServerPlayer player) {
        ServerLevel level = player.level();
        AABB box = player.getBoundingBox().inflate(RANGE);
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, box,
                villager -> !villager.isRemoved() && !villager.isBaby());
        villagers.sort(Comparator.comparingDouble(player::distanceToSqr));
        List<RestockInfo> out = new ArrayList<>();
        for (Villager villager : villagers) {
            if (out.size() >= RestockPacket.MAX) {
                break;
            }
            RestockInfo info = info(level, villager);
            if (info != null) {
                out.add(info);
            }
        }
        return out;
    }

    static RestockInfo info(ServerLevel level, Villager villager) {
        if (villager.getOffers().isEmpty()) {
            return null;
        }
        int trades = 0;
        int soldOut = 0;
        int used = 0;
        for (MerchantOffer offer : villager.getOffers()) {
            trades++;
            if (offer.isOutOfStock()) {
                soldOut++;
            }
            if (offer.needsRestock()) {
                used++;
            }
        }
        VillagerAccessor counters = (VillagerAccessor) villager;
        long gameTime = level.getGameTime();
        long since = gameTime - counters.betterlibrarians$lastRestock();
        // Same new-day test as Villager.shouldRestock, without changing anything.
        long day = level.registryAccess().get(Timelines.OVERWORLD_DAY)
                .map(timeline -> timeline.value().getPeriodCount(level.clockManager())).orElse(0).longValue();
        long lastDay = counters.betterlibrarians$lastCheckDay();
        boolean newDay = since > RestockRules.HALF_DAY || (lastDay > 0 && day > lastDay);

        Optional<GlobalPos> site = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        boolean hasSite = site.isPresent();
        boolean here = hasSite && site.get().dimension() == level.dimension();
        boolean at = here && site.get().pos().closerToCenterThan(villager.position(), WORK_REACH);
        int distance = here ? (int) Math.round(Math.sqrt(net.minecraft.world.phys.Vec3.atCenterOf(site.get().pos()).distanceToSqr(villager.position()))) : -1;
        String block = "";
        if (here && level.isLoaded(site.get().pos())) {
            Identifier key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(site.get().pos()).getBlock());
            block = key == null ? "" : key.toString();
        }
        return new RestockInfo(villager.getUUID(), trades, soldOut, used, counters.betterlibrarians$restocksToday(),
                since, newDay, hasSite, at, distance, block);
    }
}
