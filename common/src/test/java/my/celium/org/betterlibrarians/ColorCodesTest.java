package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Headless tests for the color-code parser (no registries, no world). */
class ColorCodesTest {
    @Test
    void plainTextPassesThrough() {
        assertEquals("Bob", ColorCodes.parse("Bob").getString());
        assertEquals("", ColorCodes.parse("").getString());
        assertEquals("", ColorCodes.parse(null).getString());
    }

    @Test
    void legacyColorsAreStrippedFromPlainText() {
        assertEquals("Bob", ColorCodes.parse("&aBob").getString());
        assertEquals("Red alert", ColorCodes.parse("&cRed &falert").getString());
    }

    @Test
    void formatsAndReset() {
        assertEquals("Bold", ColorCodes.parse("&lBold").getString());
        assertEquals("AB", ColorCodes.parse("&aA&fB").getString());
        assertEquals("AB", ColorCodes.parse("&aA&rB").getString());
    }

    @Test
    void hexColors() {
        assertEquals("Neon", ColorCodes.parse("&#ff55aaNeon").getString());
        assertEquals("Up", ColorCodes.parse("&#FF55AAUp").getString());
        assertEquals("Braced", ColorCodes.parse("{#ff55aa}Braced").getString());
        assertEquals("Vanilla", ColorCodes.parse("&x&f&f&5&5&a&aVanilla").getString());
    }

    @Test
    void escapesAndUnknownCodesStayLiteral() {
        assertEquals("A&B", ColorCodes.parse("A&&B").getString());
        assertEquals("&xHi", ColorCodes.parse("&xHi").getString());
        assertEquals("End&", ColorCodes.parse("End&").getString());
        assertEquals("&#zzTop", ColorCodes.parse("&#zzTop").getString());
    }

    @Test
    void styledSpansSplit() {
        // One sibling per style run: plain + green + plain.
        assertEquals(3, ColorCodes.parse("A&cB&fC").getSiblings().size());
        assertEquals("ABC", ColorCodes.parse("A&cB&fC").getString());
    }

    @Test
    void hexSpanCarriesRgb() {
        var siblings = ColorCodes.parse("&#ff55aaHi").getSiblings();
        var colored = siblings.stream().filter(s -> !s.getString().isEmpty()).toList();
        assertEquals(1, colored.size());
        assertEquals(0xFF55AA, colored.get(0).getStyle().getColor().getValue());
    }

    @Test
    void clampTrimsAndLimits() {
        assertEquals("Bob", ColorCodes.clamp("  Bob  ", 48));
        assertEquals("abc", ColorCodes.clamp("abcdef", 3));
        assertEquals("", ColorCodes.clamp(null, 48));
    }
}
