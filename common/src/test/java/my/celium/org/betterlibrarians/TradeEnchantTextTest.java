package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Headless tests for the pure label helpers (no registries, no world). */
class TradeEnchantTextTest {
    @Test
    void romanNumerals() {
        assertEquals("I", TradeEnchantText.toRoman(1));
        assertEquals("IV", TradeEnchantText.toRoman(4));
        assertEquals("V", TradeEnchantText.toRoman(5));
        assertEquals("IX", TradeEnchantText.toRoman(9));
        assertEquals("X", TradeEnchantText.toRoman(10));
    }

    @Test
    void plainFormatting() {
        assertEquals("Sharpness V", TradeEnchantText.formatPlain("Sharpness", 5, true));
        assertEquals("Sharpness", TradeEnchantText.formatPlain("Sharpness", 5, false));
        assertEquals("Mending I", TradeEnchantText.formatPlain("Mending", 1, true));
        assertEquals("", TradeEnchantText.formatPlain(null, 3, true));
    }

    @Test
    void truncation() {
        assertEquals("Sharpness", TradeEnchantText.truncate("Sharpness", 20));
        assertEquals("Shar\u2026", TradeEnchantText.truncate("Sharpness", 5));
        assertEquals("", TradeEnchantText.truncate("Sharpness", 0));
        assertEquals("", TradeEnchantText.truncate(null, 5));
    }
}
