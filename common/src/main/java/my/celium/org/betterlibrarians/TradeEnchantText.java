package my.celium.org.betterlibrarians;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Reads the enchantments off a trade result and formats them for the overlay.
 *
 * <p>Vanilla stores librarian results as {@code STORED_ENCHANTMENTS} on an
 * enchanted book; enchanted tools/armor use {@code ENCHANTMENTS}. Both are
 * handled so the optional "label everything" config works.
 */
public final class TradeEnchantText {
    private TradeEnchantText() {
    }

    /** Whether this result stack carries any enchantment worth labelling. */
    public static boolean isEnchantedBookResult(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() != Items.ENCHANTED_BOOK) {
            return false;
        }
        return !storedEnchantments(stack).isEmpty();
    }

    /** Stored (book) enchantments first, falling back to live enchantments. */
    public static ItemEnchantments storedEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemEnchantments.EMPTY;
        }
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null && !stored.isEmpty()) {
            return stored;
        }
        ItemEnchantments live = stack.get(DataComponents.ENCHANTMENTS);
        return live == null ? ItemEnchantments.EMPTY : live;
    }

    /**
     * Full label for a trade result, e.g. {@code "Sharpness V"}.
     * Returns {@code null} when the stack has nothing to label.
     */
    public static Component label(ItemStack stack, boolean showLevel) {
        ItemEnchantments enchants = storedEnchantments(stack);
        if (enchants.isEmpty()) {
            return null;
        }
        List<Component> parts = new ArrayList<>(enchants.size());
        for (var entry : enchants.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();
            parts.add(showLevel ? Enchantment.getFullname(holder, level) : holder.value().description().copy());
        }
        MutableComponent out = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                out.append(Component.literal(", "));
            }
            out.append(parts.get(i));
        }
        return out;
    }

    /** Whether every enchantment on the stack is at its vanilla max level. */
    public static boolean isMaxLevel(ItemStack stack) {
        ItemEnchantments enchants = storedEnchantments(stack);
        if (enchants.isEmpty()) {
            return false;
        }
        for (var entry : enchants.entrySet()) {
            if (entry.getIntValue() < entry.getKey().value().getMaxLevel()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Headless formatter used by unit tests: {@code ("Sharpness", 5, true)}
     * gives {@code "Sharpness V"}.
     */
    public static String formatPlain(String baseName, int level, boolean showLevel) {
        if (baseName == null) {
            return "";
        }
        if (!showLevel || level <= 0) {
            return baseName;
        }
        return baseName + " " + toRoman(level);
    }

    /** Truncates with an ellipsis; headless-testable. */
    public static String truncate(String text, int maxChars) {
        if (text == null || maxChars <= 0) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, Math.max(0, maxChars - 1)) + "\u2026";
    }

    /** Standard roman numerals for enchantment levels. */
    public static String toRoman(int level) {
        if (level <= 0) {
            return String.valueOf(level);
        }
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        if (level >= 4000) {
            return String.valueOf(level);
        }
        return thousands[level / 1000]
                + hundreds[(level % 1000) / 100]
                + tens[(level % 100) / 10]
                + ones[level % 10];
    }
}
