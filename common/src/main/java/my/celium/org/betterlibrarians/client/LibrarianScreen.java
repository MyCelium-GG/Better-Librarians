package my.celium.org.betterlibrarians.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;

import my.celium.org.betterlibrarians.ColorCodes;
import my.celium.org.betterlibrarians.LibrarianSearch;
import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.VillagerDataStore.StoredBook;
import my.celium.org.betterlibrarians.VillagerDataStore.StoredVillager;
import my.celium.org.client.MycelScreens;
import my.celium.org.client.MycelTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Every librarian you have traded with in this world. Search a book to see who
 * sells it, cheapest first; pick one to read all their books and make them
 * glow. Client only.
 */
public final class LibrarianScreen extends Screen {
    private static final int TITLE_BAR = 20;
    private static final int PAD = 8;
    private static final int LIST_W = 150;
    private static final int ROW_H = 24;
    private static final int BOOK_H = 18;
    private static final ItemStack BOOK = new ItemStack(Items.ENCHANTED_BOOK);
    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    private final List<LibrarianSearch.Hit> rows = new ArrayList<>();
    private EditBox search;
    private FlatButton findButton;
    private FlatButton forgetButton;
    private String selectedId;
    private int scroll;
    private String notice = "";

    LibrarianScreen(UUID looked) {
        super(Component.literal("Better Librarians"));
        selectedId = looked == null ? null : looked.toString();
    }

    private int panelW() {
        return Math.min(width - 12, 346);
    }

    private int panelH() {
        return Math.min(height - 12, 224);
    }

    private int px() {
        return (width - panelW()) / 2;
    }

    private int py() {
        return (height - panelH()) / 2;
    }

    private int listX() {
        return px() + PAD;
    }

    private int listY() {
        return py() + TITLE_BAR + PAD + 18;
    }

    private int listH() {
        return py() + panelH() - PAD - listY();
    }

    private int rightX() {
        return listX() + LIST_W + PAD;
    }

    private int rightW() {
        return px() + panelW() - PAD - rightX();
    }

    private int visibleRows() {
        return listH() / ROW_H;
    }

    private int maxScroll() {
        return Math.max(0, rows.size() - visibleRows());
    }

    @Override
    protected void init() {
        search = new EditBox(font, listX() + 4, py() + TITLE_BAR + PAD + 3, LIST_W - 8, 9, Component.literal("Search books"));
        search.setBordered(false);
        search.setMaxLength(48);
        search.setHint(MycelTheme.colored("Search a book, e.g. mending", MycelTheme.FAINT));
        search.setResponder(text -> {
            scroll = 0;
            refresh();
        });
        addRenderableWidget(search);
        int bottom = py() + panelH() - PAD - FlatButton.HEIGHT;
        int buttonW = (rightW() - 8) / 3;
        findButton = new FlatButton(rightX(), bottom, buttonW, Component.literal("Find"), true, this::findSelected);
        forgetButton = new FlatButton(rightX() + buttonW + 4, bottom, buttonW, Component.literal("Forget"), false, this::forgetSelected);
        addRenderableWidget(findButton);
        addRenderableWidget(forgetButton);
        addRenderableWidget(new FlatButton(rightX() + (buttonW + 4) * 2, bottom, buttonW, Component.literal("Done"), false, this::onClose));
        refresh();
    }

    private void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        rows.clear();
        String query = search == null ? "" : search.getValue();
        rows.addAll(LibrarianSearch.search(VillagerDataStore.entries(), LibrarianPlaces.currentWorld(minecraft), query));
        if (query.isBlank() && minecraft.player != null) {
            rows.sort(Comparator.comparingDouble(hit -> LibrarianPlaces.distance(minecraft.player, hit.villager())));
        }
        if (selected() == null) {
            selectedId = rows.isEmpty() ? null : rows.get(0).id();
        }
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        updateButtons();
    }

    private LibrarianSearch.Hit selected() {
        for (LibrarianSearch.Hit hit : rows) {
            if (hit.id().equals(selectedId)) {
                return hit;
            }
        }
        return null;
    }

    private void updateButtons() {
        boolean any = selected() != null;
        if (findButton != null) {
            findButton.active = any;
            forgetButton.active = any;
        }
    }

    private void findSelected() {
        LibrarianSearch.Hit hit = selected();
        if (hit == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (LibrarianClient.find(minecraft, UUID.fromString(hit.id()))) {
            onClose();
        } else {
            StoredVillager villager = hit.villager();
            notice = villager.dimension == null ? "Too far away to glow. Trade with them again to save where they are."
                    : "Too far away to glow. Last seen at " + villager.x + " " + villager.y + " " + villager.z + ".";
        }
    }

    private void forgetSelected() {
        LibrarianSearch.Hit hit = selected();
        if (hit != null) {
            VillagerDataStore.forgetBooks(UUID.fromString(hit.id()));
            selectedId = null;
            notice = "";
            refresh();
        }
    }

    private String displayName(StoredVillager villager) {
        return villager.name == null || villager.name.isBlank() ? "Librarian" : ColorCodes.parse(villager.name).getString();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mx, int my, float a) {
        int x0 = px();
        int y0 = py();
        int x1 = x0 + panelW();
        int y1 = y0 + panelH();
        MycelTheme.panel(gfx, x0, y0, x1, y1);
        gfx.fill(x0 + 2, y0 + 2, x1 - 2, y0 + TITLE_BAR, MycelTheme.ACCENT_DARK);
        gfx.fill(x0 + 2, y0 + TITLE_BAR, x1 - 2, y0 + TITLE_BAR + 1, MycelTheme.HEADER_EDGE);
        gfx.text(font, "Better Librarians", x0 + PAD, y0 + 7, MycelTheme.HEADING);
        String count = rows.size() + (rows.size() == 1 ? " librarian" : " librarians");
        gfx.text(font, count, x1 - PAD - font.width(count), y0 + 7, MycelTheme.HEADING);

        int fieldY = y0 + TITLE_BAR + PAD;
        gfx.fill(listX(), fieldY, listX() + LIST_W, fieldY + 14, search.isFocused() ? MycelTheme.ACCENT : MycelTheme.BORDER);
        gfx.fill(listX() + 1, fieldY + 1, listX() + LIST_W - 1, fieldY + 13, MycelTheme.SURFACE);
        drawList(gfx, mx, my);
        drawSelected(gfx);
        super.extractRenderState(gfx, mx, my, a);
    }

    private void drawList(GuiGraphicsExtractor gfx, int mx, int my) {
        int x = listX();
        int y0 = listY();
        int h = listH();
        gfx.fill(x, y0, x + LIST_W, y0 + h, MycelTheme.BORDER);
        gfx.fill(x + 1, y0 + 1, x + LIST_W - 1, y0 + h - 1, MycelTheme.SURFACE);
        if (rows.isEmpty()) {
            String[] lines = search.getValue().isBlank()
                    ? new String[] {"No librarians yet.", "Trade with one once", "and they appear here."}
                    : new String[] {"Nobody you traded with", "sells that."};
            for (int i = 0; i < lines.length; i++) {
                gfx.text(font, lines[i], x + 6, y0 + 6 + i * 11, MycelTheme.DIM);
            }
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int end = Math.min(rows.size(), scroll + visibleRows());
        for (int i = scroll; i < end; i++) {
            LibrarianSearch.Hit hit = rows.get(i);
            int y = y0 + (i - scroll) * ROW_H;
            boolean isSelected = hit.id().equals(selectedId);
            boolean hover = mx >= x && mx < x + LIST_W && my >= y && my < y + ROW_H;
            if (isSelected || hover) {
                gfx.fill(x + 1, y + 1, x + LIST_W - 1, y + ROW_H - 1, isSelected ? MycelTheme.BORDER : MycelTheme.NOTE);
            }
            if (isSelected) {
                gfx.fill(x + 1, y + 1, x + 3, y + ROW_H - 1, MycelTheme.ACCENT);
            }
            String place = minecraft.player == null ? "" : LibrarianPlaces.describe(minecraft.player, hit.villager());
            int placeW = font.width(place);
            gfx.text(font, MycelTheme.fit(font, displayName(hit.villager()), LIST_W - 14 - placeW - 4), x + 6, y + 3, MycelTheme.TEXT);
            gfx.text(font, place, x + LIST_W - 6 - placeW, y + 3, MycelTheme.FAINT);
            String detail;
            int detailColor;
            if (hit.book() != null) {
                detail = hit.book().text;
                detailColor = hit.book().maxLevel ? MycelTheme.WARN : MycelTheme.TEXT;
            } else {
                int books = hit.villager().books.size();
                detail = books + (books == 1 ? " book" : " books");
                detailColor = MycelTheme.DIM;
            }
            int costRoom = hit.book() != null && hit.book().cost > 0 ? font.width(Integer.toString(hit.book().cost)) + 4 : 0;
            gfx.text(font, MycelTheme.fit(font, detail, LIST_W - 14 - costRoom), x + 6, y + 13, detailColor);
            if (costRoom > 0) {
                String cost = Integer.toString(hit.book().cost);
                gfx.text(font, cost, x + LIST_W - 6 - font.width(cost), y + 13, MycelTheme.GOOD);
            }
        }
        if (maxScroll() > 0) {
            int trackH = h - 2;
            int thumb = Math.max(10, trackH * visibleRows() / rows.size());
            int y = y0 + 1 + (trackH - thumb) * scroll / maxScroll();
            gfx.fill(x + LIST_W - 3, y, x + LIST_W - 1, y + thumb, MycelTheme.DIM);
        }
    }

    private void drawSelected(GuiGraphicsExtractor gfx) {
        int x = rightX();
        int y = py() + TITLE_BAR + PAD;
        LibrarianSearch.Hit hit = selected();
        if (hit == null) {
            gfx.text(font, "Your librarians show up here", x, y + 3, MycelTheme.TEXT);
            gfx.text(font, "after you trade with them.", x, y + 14, MycelTheme.TEXT);
            return;
        }
        StoredVillager villager = hit.villager();
        gfx.text(font, MycelTheme.fit(font, displayName(villager), rightW()), x, y + 1, MycelTheme.HEADING);
        Minecraft minecraft = Minecraft.getInstance();
        String place = minecraft.player == null ? "" : LibrarianPlaces.describe(minecraft.player, villager);
        gfx.text(font, place.isEmpty() ? "Trade again to save where they are" : place, x, y + 12, MycelTheme.FAINT);
        int top = y + 26;
        int bottom = py() + panelH() - PAD - FlatButton.HEIGHT - (notice.isEmpty() ? 4 : 28);
        String query = search.getValue().trim().toLowerCase(Locale.ROOT);
        int shown = Math.max(0, (bottom - top) / BOOK_H);
        List<StoredBook> books = villager.books;
        for (int i = 0; i < Math.min(shown, books.size()); i++) {
            StoredBook book = books.get(i);
            int rowY = top + i * BOOK_H;
            boolean match = !query.isEmpty() && book.text.toLowerCase(Locale.ROOT).contains(query);
            gfx.fill(x, rowY, x + rightW(), rowY + BOOK_H - 2, match ? MycelTheme.NOTE : MycelTheme.SURFACE);
            gfx.item(BOOK, x + 1, rowY);
            int costRoom = book.cost > 0 ? font.width(Integer.toString(book.cost)) + 22 : 4;
            gfx.text(font, MycelTheme.fit(font, book.text, rightW() - 20 - costRoom), x + 19, rowY + 4,
                    book.maxLevel ? MycelTheme.WARN : MycelTheme.TEXT);
            if (book.cost > 0) {
                String cost = Integer.toString(book.cost);
                gfx.text(font, cost, x + rightW() - 19 - font.width(cost), rowY + 4, MycelTheme.GOOD);
                gfx.item(EMERALD, x + rightW() - 17, rowY);
            }
        }
        if (books.size() > shown && shown > 0) {
            gfx.text(font, "…and " + (books.size() - shown) + " more", x, top + shown * BOOK_H, MycelTheme.DIM);
        }
        if (!notice.isEmpty()) {
            int noticeY = py() + panelH() - PAD - FlatButton.HEIGHT - 24;
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(notice), rightW());
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                gfx.text(font, lines.get(i), x, noticeY + i * 10, MycelTheme.WARN);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        setFocused(null);
        double mx = event.x();
        double my = event.y();
        if (mx >= listX() && mx < listX() + LIST_W && my >= listY() && my < listY() + listH()) {
            int index = scroll + (int) ((my - listY()) / ROW_H);
            if (index >= 0 && index < rows.size()) {
                selectedId = rows.get(index).id();
                notice = "";
                updateButtons();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (x >= listX() && x < listX() + LIST_W) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll + (scrollY > 0 ? -1 : scrollY < 0 ? 1 : 0)));
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        MycelScreens.open(null);
    }
}
