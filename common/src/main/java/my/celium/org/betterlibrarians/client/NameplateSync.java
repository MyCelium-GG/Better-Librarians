package my.celium.org.betterlibrarians.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import my.celium.org.betterlibrarians.ColorCodes;
import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

public final class NameplateSync {
    private static final Map<AbstractVillager, Applied> APPLIED = new WeakHashMap<>();

    private NameplateSync() {
    }

    private record Applied(String raw, Component parsed, Component original, boolean originalVisible) {
    }

    public static void tickClient(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }
        try {
            tickClientInner(minecraft);
        } catch (RuntimeException ignored) {
            APPLIED.clear();
        }
    }

    private static void tickClientInner(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            APPLIED.clear();
            return;
        }
        if (!BetterLibrariansMod.isModEnabled()) {
            restoreAll();
            return;
        }
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof AbstractVillager villager && !villager.isRemoved()) {
                syncVillager(villager);
            }
        }
        pruneRemoved();
    }

    private static void syncVillager(AbstractVillager villager) {
        String raw = VillagerDataStore.getName(villager.getUUID());
        Applied applied = APPLIED.get(villager);
        if (raw == null) {
            if (applied != null) {
                restore(villager, applied);
                APPLIED.remove(villager);
            }
            return;
        }
        if (applied == null || !applied.raw().equals(raw)) {
            applied = createApplied(villager, applied, raw);
            APPLIED.put(villager, applied);
        }
        if (!Objects.equals(villager.getCustomName(), applied.parsed())) {
            villager.setCustomName(applied.parsed());
        }
        if (!villager.isCustomNameVisible()) {
            villager.setCustomNameVisible(true);
        }
    }

    private static Applied createApplied(AbstractVillager villager, Applied previous, String raw) {
        Component original = previous == null ? villager.getCustomName() : previous.original();
        boolean originalVisible = previous == null ? villager.isCustomNameVisible() : previous.originalVisible();
        return new Applied(raw, ColorCodes.parse(raw), original, originalVisible);
    }

    private static void restore(AbstractVillager villager, Applied applied) {
        villager.setCustomName(applied.original());
        villager.setCustomNameVisible(applied.originalVisible());
    }

    private static void restoreAll() {
        for (Map.Entry<AbstractVillager, Applied> entry : APPLIED.entrySet()) {
            restore(entry.getKey(), entry.getValue());
        }
        APPLIED.clear();
    }

    private static void pruneRemoved() {
        Iterator<AbstractVillager> iterator = APPLIED.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isRemoved()) {
                iterator.remove();
            }
        }
    }
}