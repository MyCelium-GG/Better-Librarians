package my.celium.org.betterlibrarians.client;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3x2fStack;

import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.client.MycelScreens;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    static final int BOX_COLOR = 0xE6171717;

    static final int TOP_Y = 14;
    static final int PAD_X = 8;
    static final int PAD_Y = 5;
    static final int LINE_H = 12;
    static final int ICON = 10;
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
        if (MycelScreens.current() != null) {
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
            lines.add(new Line(professionTitle(villager), COLOR_TITLE, false, 0));
        } else {
            lines.add(new Line(fitWidth(font,
                    my.celium.org.betterlibrarians.ColorCodes.parse(rename), MAX_LINE_WIDTH),
                    COLOR_NAME_FALLBACK, false, 0));
            lines.add(new Line(professionTitle(villager), COLOR_SUBTITLE, false, 0));
        }
        if (stored.isEmpty()) {
            if (!showHint) {
                return;
            }
            lines.add(new Line(Component.literal("Trade once to catalogue books"), COLOR_HINT, false, 0));
        } else {
            int shown = Math.min(stored.size(), Math.max(1, maxBooks));
            for (int i = 0; i < shown; i++) {
                VillagerDataStore.StoredBook book = stored.get(i);
                int color = book.maxLevel && highlightMax ? COLOR_BOOK_MAX : COLOR_BOOK;
                lines.add(new Line(fitWidth(font, Component.literal(book.text), MAX_LINE_WIDTH),
                        color, true, book.cost));
            }
            if (stored.size() > shown) {
                lines.add(new Line(Component.literal("…and " + (stored.size() - shown) + " more"),
                        COLOR_MORE, false, 0));
            }
        }

        int width = 0;
        boolean anyCost = false;
        for (Line line : lines) {
            int lineWidth = font.width(line.text()) + (line.book() ? ICON + 3 : 0);
            width = Math.max(width, lineWidth);
            anyCost |= line.cost() > 0;
        }
        int costWidth = anyCost ? font.width("64") + ICON + 8 : 0;
        int centerX = gfx.guiWidth() / 2;
        int cardW = width + costWidth + PAD_X * 2;
        int x0 = centerX - cardW / 2;
        int x1 = x0 + cardW;
        int y1 = TOP_Y + PAD_Y * 2 + lines.size() * LINE_H;
        gfx.fill(x0 + 2, TOP_Y + 2, x1 + 2, y1 + 2, MycelTheme.SHADOW);
        gfx.fill(x0, TOP_Y, x1, y1, MycelTheme.BORDER);
        gfx.fill(x0 + 1, TOP_Y + 1, x1 - 1, y1 - 1, BOX_COLOR);

        ItemStack bookIcon = new ItemStack(Items.ENCHANTED_BOOK);
        ItemStack emeraldIcon = new ItemStack(Items.EMERALD);
        int y = TOP_Y + PAD_Y;
        for (Line line : lines) {
            int textY = y + (LINE_H - font.lineHeight) / 2 + 1;
            if (line.book()) {
                icon(gfx, bookIcon, x0 + PAD_X, y + 1);
                gfx.text(font, line.text(), x0 + PAD_X + ICON + 3, textY, line.colorArgb());
                if (line.cost() > 0) {
                    String cost = Integer.toString(line.cost());
                    gfx.text(font, cost, x1 - PAD_X - ICON - 2 - font.width(cost), textY, MycelTheme.GOOD);
                    icon(gfx, emeraldIcon, x1 - PAD_X - ICON, y + 1);
                }
            } else {
                gfx.centeredText(font, line.text(), centerX, textY, line.colorArgb());
            }
            y += LINE_H;
        }
    }

    /** An item drawn at {@link #ICON} pixels. */
    private static void icon(GuiGraphicsExtractor gfx, ItemStack stack, int x, int y) {
        Matrix3x2fStack pose = gfx.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(ICON / 16f, ICON / 16f);
        gfx.item(stack, 0, 0);
        pose.popMatrix();
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

    private record Line(Component text, int colorArgb, boolean book, int cost) {
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
