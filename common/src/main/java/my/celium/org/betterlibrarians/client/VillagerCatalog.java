package my.celium.org.betterlibrarians.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import my.celium.org.betterlibrarians.TradeEnchantText;
import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.VillagerDataStore.StoredBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Client-side catalogue of villagers' enchanted books, backed by
 * {@link VillagerDataStore} so it survives restarts.
 *
 * <p>Why a cache at all? The client only learns a villager's offers when
 * their trading screen is open (vanilla syncs offers to the open menu, never
 * to the villager entity itself). So whenever a {@link MerchantScreen} shows
 * non-empty offers, the books are stored under the villager the crosshair is
 * on, which is the one being traded with. The nearest villager is only the
 * fallback, since a trading hall packs them close together.
 */
public final class VillagerCatalog {
    /** How far (blocks) a villager may be to receive freshly opened offers. */
    static final double LINK_RADIUS = 6.0;

    private static final Map<UUID, Integer> RECORDED_SIZES = new ConcurrentHashMap<>();

    private VillagerCatalog() {
    }

    /**
     * Records the offers of an open trading screen under the nearest nearby
     * villager. Cheap and idempotent: rebuilds only while the offer list is
     * still unknown or changed (offers can arrive a tick after the screen).
     * Never throws.
     */
    public static void recordScreen(MerchantScreen screen) {
        if (screen == null) {
            return;
        }
        try {
            recordScreenInner(screen);
        } catch (RuntimeException e) {
            // Cataloguing must never break the screen.
        }
    }

    private static void recordScreenInner(MerchantScreen screen) {
        MerchantOffers offers = screen.getMenu().getOffers();
        if (offers == null || offers.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        AbstractVillager villager = minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof AbstractVillager looked && !looked.isRemoved()
                ? looked : nearestVillager(player);
        if (villager == null) {
            return;
        }
        UUID id = villager.getUUID();
        Integer known = RECORDED_SIZES.get(id);
        if (known != null && known == offers.size()) {
            return;
        }
        RECORDED_SIZES.put(id, offers.size());
        boolean showLevel;
        try {
            showLevel = my.celium.org.betterlibrarians.BetterLibrariansMod.showLevel.get();
        } catch (RuntimeException e) {
            showLevel = true;
        }
        List<StoredBook> books = new ArrayList<>();
        for (MerchantOffer offer : offers) {
            if (offer == null) {
                continue;
            }
            ItemStack result = offer.getResult();
            if (!TradeEnchantText.isEnchantedBookResult(result)) {
                continue;
            }
            Component text = TradeEnchantText.label(result, showLevel);
            if (text == null) {
                continue;
            }
            ItemStack price = offer.getCostA();
            int cost = price.getItem() == Items.EMERALD ? price.getCount() : 0;
            books.add(new StoredBook(text.getString(), TradeEnchantText.isMaxLevel(result), cost));
        }
        VillagerDataStore.setBooks(id, books);
        VillagerDataStore.setLocation(id, LibrarianPlaces.currentWorld(minecraft),
                LibrarianPlaces.dimension(villager), villager.getBlockX(), villager.getBlockY(), villager.getBlockZ());
    }

    /** The closest villager near the player, or {@code null} when none. */
    static AbstractVillager nearestVillager(LocalPlayer player) {
        AABB box = player.getBoundingBox().inflate(LINK_RADIUS);
        List<AbstractVillager> nearby =
                player.level().getEntitiesOfClass(AbstractVillager.class, box, e -> !e.isRemoved());
        AbstractVillager best = null;
        double bestDist = Double.MAX_VALUE;
        for (AbstractVillager candidate : nearby) {
            double dist = player.distanceTo(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }
}
