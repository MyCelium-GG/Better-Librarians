package my.celium.org.betterlibrarians.client;

import java.util.UUID;

import my.celium.org.betterlibrarians.ColorCodes;
import my.celium.org.betterlibrarians.VillagerDataStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Shared backs for the {@code /vname} client command on every loader:
 * resolves the crosshair-targeted villager and mutates
 * {@link VillagerDataStore}. Each loader delivers the resulting message
 * through its own command source. Client only.
 */
public final class RenameActions {
    static final int MAX_NAME_LENGTH = 48;

    /** Outcome of a rename action: brigadier code plus chat message. */
    public record Result(int code, Component message, boolean success) {
    }

    private RenameActions() {
    }

    /** Renames the targeted villager (supports {@code &} and {@code &#} codes). */
    public static Result setName(String raw) {
        UUID target = crosshairTarget();
        if (target == null) {
            return new Result(0, ColorCodes.parse("&cLook at a villager to rename it."), false);
        }
        String clamped = ColorCodes.clamp(raw, MAX_NAME_LENGTH);
        if (clamped.isEmpty() || ColorCodes.parse(clamped).getString().isBlank()) {
            return new Result(0, ColorCodes.parse("&cName has no visible text."), false);
        }
        VillagerDataStore.setName(target, clamped);
        return new Result(1, Component.literal("Named villager: ").append(ColorCodes.parse(clamped)),
                true);
    }

    /** Clears the targeted villager's rename. */
    public static Result clearName() {
        UUID target = crosshairTarget();
        if (target == null) {
            return new Result(0, ColorCodes.parse("&cLook at a villager to rename it."), false);
        }
        VillagerDataStore.setName(target, null);
        return new Result(1, Component.literal("Cleared villager name."), true);
    }

    static UUID crosshairTarget() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.level == null) {
                return null;
            }
            if (minecraft.hitResult instanceof EntityHitResult entityHit
                    && entityHit.getEntity() instanceof AbstractVillager villager
                    && !villager.isRemoved()) {
                return villager.getUUID();
            }
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
