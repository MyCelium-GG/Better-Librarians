package my.celium.org.betterlibrarians.client;

import my.celium.org.betterlibrarians.VillagerDataStore.StoredVillager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/** Where a catalogued villager is, relative to the player. Client only. */
final class LibrarianPlaces {
    private static final String[] COMPASS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    private LibrarianPlaces() {
    }

    /** A name for the current server or singleplayer world, so lists stay per world. */
    static String currentWorld(Minecraft minecraft) {
        ServerData server = minecraft.getCurrentServer();
        if (server != null) {
            return "server:" + server.ip;
        }
        return minecraft.getSingleplayerServer() != null
                ? "world:" + minecraft.getSingleplayerServer().getWorldData().getLevelName()
                : "unknown";
    }

    static String dimension(Entity entity) {
        return entity.level().dimension().identifier().toString();
    }

    /** {@code "12 m NW"}, {@code "In the Nether"}, or {@code ""} when the place is unknown. */
    static String describe(LocalPlayer player, StoredVillager villager) {
        if (villager.dimension == null) {
            return "";
        }
        if (!villager.dimension.equals(dimension(player))) {
            return switch (villager.dimension) {
                case "minecraft:the_nether" -> "In the Nether";
                case "minecraft:the_end" -> "In the End";
                case "minecraft:overworld" -> "In the Overworld";
                default -> "In another dimension";
            };
        }
        double dx = villager.x + 0.5 - player.getX();
        double dz = villager.z + 0.5 - player.getZ();
        long distance = Math.round(Math.sqrt(dx * dx + dz * dz));
        if (distance < 2) {
            return "Right here";
        }
        int sector = (int) Math.floorMod(Math.round(Math.toDegrees(Math.atan2(-dx, dz)) / 45), 8L);
        return distance + " m " + COMPASS[sector];
    }

    /** Sort key: blocks away, unknown and other dimensions last. */
    static double distance(LocalPlayer player, StoredVillager villager) {
        if (villager.dimension == null || !villager.dimension.equals(dimension(player))) {
            return Double.MAX_VALUE;
        }
        return player.distanceToSqr(villager.x + 0.5, villager.y, villager.z + 0.5);
    }
}
