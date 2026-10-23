package my.celium.org.betterlibrarians.client;

import java.util.ArrayList;
import java.util.List;

import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.client.MycelTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Draws the look-at title card: when the crosshair rests on a villager, a
 * small centered card lists the enchanted books that villager trades.
 *
 * <p>Books come from {@link VillagerCatalog} (filled whenever a trading
 * screen is open). Villagers never traded with yet show a one-line hint
 * instead, so the mechanic is discoverable. Client only.
 */
public final class LookTitleCard {
    static final int COLOR_TITLE = MycelTheme.ACCENT;
    /** Base color for parsed renames (styled spans override it). */
    static final int COLOR_NAME_FALLBACK = MycelTheme.HEADING;
    static final int COLOR_SUBTITLE = MycelTheme.DIM;
    static final int COLOR_BOOK = MycelTheme.TEXT;
    static final int COLOR_BOOK_MAX = MycelTheme.WARN;
    static final int COLOR_HINT = MycelTheme.DIM;
    static final int COLOR_MORE = MycelTheme.DIM;
    static final int BOX_COLOR = 0xC0000000;

    static final int TOP_Y = 14;
    static final int PAD_X = 10;
    static final int PAD_Y = 6;
    static final int LINE_GAP = 3;
    static final int MAX_LINE_WIDTH = 240;

    private LookTitleCard() {
    }

    /** Draws the card when the player looks at a villager. Never throws. */
    public static void render(GuiGraphicsExtractor gfx) {
        if (gfx == null) {
            return;
        }
        try {
            renderInner(gfx);
        } catch (RuntimeException e) {
            // HUD must never crash the frame.
        }
    }

    private static void renderInner(GuiGraphicsExtractor gfx) {
        if (!BetterLibrariansMod.isModEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (minecraft.gui.screen() != null) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult entityHit)) {
            return;
        }
        if (!(entityHit.getEntity() instanceof AbstractVillager villager) || villager.isRemoved()) {
            return;
        }
        // The book card is a librarian feature; other professions show nothing.
        if (!isLibrarian(villager)) {
            return;
        }

        int maxBooks = readInt(() -> BetterLibrariansMod.maxBooks.get(), 6);
        boolean showHint = readFlag(() -> BetterLibrariansMod.showUncataloguedHint.get(), true);
        boolean highlightMax =
                readFlag(() -> BetterLibrariansMod.highlightMaxLevel.get(), true);

        List<VillagerDataStore.StoredBook> stored =
                VillagerDataStore.getBooks(villager.getUUID());

        Font font = minecraft.font;
        if (font == null) {
            return;
        }

        List<Line> lines = new ArrayList<>();
        String rename = VillagerDataStore.getName(villager.getUUID());
        if (rename == null || rename.isBlank()) {
            lines.add(new Line(professionTitle(villager), COLOR_TITLE));
        } else {
            lines.add(new Line(fitWidth(font,
                    my.celium.org.betterlibrarians.ColorCodes.parse(rename), MAX_LINE_WIDTH),
                    COLOR_NAME_FALLBACK));
            lines.add(new Line(professionTitle(villager), COLOR_SUBTITLE));
        }
        if (stored.isEmpty()) {
            if (!showHint) {
                return;
            }
            lines.add(new Line(Component.literal("Trade once to catalogue books"), COLOR_HINT));
        } else {
            int shown = Math.min(stored.size(), Math.max(1, maxBooks));
            for (int i = 0; i < shown; i++) {
                VillagerDataStore.StoredBook book = stored.get(i);
                int color = book.maxLevel && highlightMax ? COLOR_BOOK_MAX : COLOR_BOOK;
                lines.add(new Line(fitWidth(font, Component.literal(book.text), MAX_LINE_WIDTH),
                        color));
            }
            if (stored.size() > shown) {
                lines.add(new Line(
                        Component.literal("…and " + (stored.size() - shown) + " more"), COLOR_MORE));
            }
        }

        int lineHeight = font.lineHeight + LINE_GAP;
        int width = 0;
        for (Line line : lines) {
            width = Math.max(width, font.width(line.text()));
        }
        int centerX = gfx.guiWidth() / 2;
        int boxX1 = centerX - width / 2 - PAD_X;
        int boxX2 = centerX + (width + 1) / 2 + PAD_X;
        int boxY1 = TOP_Y;
        int boxY2 = TOP_Y + PAD_Y + lines.size() * lineHeight - LINE_GAP + PAD_Y;
        gfx.fill(boxX1, boxY1, boxX2, boxY2, BOX_COLOR);

        int y = TOP_Y + PAD_Y;
        for (Line line : lines) {
            gfx.centeredText(font, line.text(), centerX, y, line.colorArgb());
            y += lineHeight;
        }
    }

    /** Whether this villager is a librarian (the only profession with book trades). */
    static boolean isLibrarian(AbstractVillager villager) {
        return villager instanceof Villager && "librarian".equals(professionPath((Villager) villager));
    }

    /** Registry path of a villager's profession, e.g. {@code "librarian"} or {@code "none"}. */
    static String professionPath(Villager villager) {
        try {
            Holder<VillagerProfession> profession = villager.getVillagerData().profession();
            Identifier key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession.value());
            return key == null ? "none" : key.getPath();
        } catch (RuntimeException e) {
            return "none";
        }
    }

    /** "Librarian · Apprentice", "Villager" for the jobless, type name otherwise. */
    public static Component professionTitle(AbstractVillager villager) {
        if (villager instanceof Villager typed) {
            try {
                String path = professionPath(typed);
                if ("none".equals(path)) {
                    return Component.translatable("entity.minecraft.villager");
                }
                int level = typed.getVillagerData().level();
                return Component.translatable("entity.minecraft.villager." + path)
                        .append(Component.literal(" · "))
                        .append(Component.translatable("merchant.level." + Math.min(5, Math.max(1, level))));
            } catch (RuntimeException e) {
                return villager.getType().getDescription();
            }
        }
        return villager.getType().getDescription();
    }

    private static Component fitWidth(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        int room = maxWidth - font.width("…");
        FormattedText cut = font.substrByWidth(text, Math.max(0, room));
        return Component.literal(cut.getString() + "…");
    }

    private record Line(Component text, int colorArgb) {
    }

    private static boolean readFlag(java.util.function.BooleanSupplier reader, boolean fallback) {
        try {
            return reader.getAsBoolean();
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static int readInt(java.util.function.IntSupplier reader, int fallback) {
        try {
            return reader.getAsInt();
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
