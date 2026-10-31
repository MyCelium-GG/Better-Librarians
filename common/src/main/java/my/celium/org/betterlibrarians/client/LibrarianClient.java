package my.celium.org.betterlibrarians.client;

import java.lang.reflect.Method;
import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;

import my.celium.org.client.MycelScreens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.phys.EntityHitResult;

/**
 * The way into the librarian list (key and {@code /librarians}) and the glow
 * behind its Find button. Client only.
 */
public final class LibrarianClient {
    /** The list's client command. */
    public static final String COMMAND = "librarians";
    private static final int GLOWING_FLAG = 6;
    private static final int FIND_TICKS = 20 * 30;

    /** Registered by each loader. */
    public static final KeyMapping OPEN_KEY = new KeyMapping("key.betterlibrarians.open", InputConstants.KEY_K,
            KeyMapping.Category.GAMEPLAY);

    private static boolean listRequested;
    private static UUID glowing;
    private static int glowTicks;

    private LibrarianClient() {
    }

    /** Opens the list next tick, so it survives the chat screen closing after a command. */
    public static int requestList() {
        listRequested = true;
        return 1;
    }

    /** Runs every client tick on every loader. */
    public static void tick(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }
        NameplateSync.tickClient(minecraft);
        if (minecraft.player == null) {
            listRequested = false;
            glowing = null;
            return;
        }
        while (OPEN_KEY.consumeClick()) {
            listRequested = true;
        }
        if (listRequested) {
            listRequested = false;
            UUID looked = minecraft.hitResult instanceof EntityHitResult hit
                    && hit.getEntity() instanceof AbstractVillager villager ? villager.getUUID() : null;
            MycelScreens.open(new LibrarianScreen(looked));
        }
        tickGlow(minecraft);
    }

    /** Makes a villager glow for a while. Returns false when it is not loaded nearby. */
    static boolean find(Minecraft minecraft, UUID id) {
        stopGlow(minecraft);
        if (entity(minecraft, id) == null) {
            return false;
        }
        glowing = id;
        glowTicks = FIND_TICKS;
        return true;
    }

    private static void tickGlow(Minecraft minecraft) {
        if (glowing == null) {
            return;
        }
        if (--glowTicks <= 0) {
            stopGlow(minecraft);
            return;
        }
        Entity entity = entity(minecraft, glowing);
        if (entity != null) {
            // A server update can reset the flag, so it is applied every tick.
            setGlow(entity, true);
        }
    }

    private static void stopGlow(Minecraft minecraft) {
        if (glowing != null) {
            Entity entity = entity(minecraft, glowing);
            if (entity != null) {
                setGlow(entity, false);
            }
            glowing = null;
        }
    }

    private static Entity entity(Minecraft minecraft, UUID id) {
        if (minecraft.level == null || id == null) {
            return null;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (id.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }

    /** On clients the glow is the synced shared flag, which is protected. Cosmetic: failures are ignored. */
    private static void setGlow(Entity entity, boolean on) {
        try {
            Method method = Entity.class.getDeclaredMethod("setSharedFlag", int.class, boolean.class);
            method.setAccessible(true);
            method.invoke(entity, GLOWING_FLAG, on);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Finding a villager is a nicety; never break the tick.
        }
    }
}
