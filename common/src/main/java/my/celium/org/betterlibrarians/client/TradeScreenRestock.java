package my.celium.org.betterlibrarians.client;

import java.util.List;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.restock.RestockInfo;
import my.celium.org.client.MycelTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** The restock timer under the trading screen. Client only; never throws. */
public final class TradeScreenRestock {
    /** Vanilla's trading screen size. */
    static final int GUI_HEIGHT = 166;

    private TradeScreenRestock() {
    }

    public static void render(MerchantScreen screen, GuiGraphicsExtractor gfx) {
        if (screen == null || gfx == null) {
            return;
        }
        try {
            renderInner(screen, gfx);
        } catch (RuntimeException e) {
            // The timer must never break the trading screen.
        }
    }

    private static void renderInner(MerchantScreen screen, GuiGraphicsExtractor gfx) {
        if (!BetterLibrariansMod.isModEnabled() || !BetterLibrariansMod.restockTimer.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !screen.getMenu().canRestock()) {
            return;
        }
        MerchantOffers offers = screen.getMenu().getOffers();
        AbstractVillager trader = VillagerCatalog.trader(minecraft);
        if (offers == null || offers.isEmpty() || !(trader instanceof Villager villager)) {
            return;
        }
        // The open screen shows the stock exactly, right now.
        int trades = 0;
        int soldOut = 0;
        int used = 0;
        for (MerchantOffer offer : offers) {
            trades++;
            soldOut += offer.isOutOfStock() ? 1 : 0;
            used += offer.needsRestock() ? 1 : 0;
        }
        RestockInfo live = RestockClient.live(villager.getUUID());
        if (live != null) {
            live = new RestockInfo(live.villager(), trades, soldOut, used, live.restocksToday(), live.sinceRestock(),
                    live.newDayPending(), live.workstation(), live.atWorkstation(), live.distance(), live.workBlock());
        }
        long now = minecraft.level.getGameTime();
        List<RestockText.Line> lines = RestockText.lines(live,
                new RestockText.Seen(trades, soldOut, used, now), minecraft.level.getOverworldClockTime(), now,
                LookTitleCard.professionPath(villager));
        if (lines.isEmpty()) {
            return;
        }
        Font font = minecraft.font;
        int width = 0;
        for (RestockText.Line line : lines) {
            width = Math.max(width, font.width(line.text()));
        }
        int centerX = screen.width / 2;
        int top = (screen.height + GUI_HEIGHT) / 2 + 4;
        int x0 = centerX - width / 2 - 6;
        int x1 = centerX + width / 2 + 6;
        int y1 = top + 4 + lines.size() * 11;
        gfx.fill(x0, top, x1, y1, MycelTheme.BORDER);
        gfx.fill(x0 + 1, top + 1, x1 - 1, y1 - 1, LookTitleCard.BOX_COLOR);
        int y = top + 3;
        for (RestockText.Line line : lines) {
            gfx.centeredText(font, line.text(), centerX, y, line.color());
            y += 11;
        }
    }
}
