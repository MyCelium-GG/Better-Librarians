package my.celium.org.betterlibrarians;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Parses color/format codes in user-supplied names into styled components.
 *
 * <p>Supported codes (all case-insensitive, {@code &} prefix):
 * <ul>
 * <li>Legacy colors {@code &0}-{@code &9}, {@code &a}-{@code &f} (standard
 * Minecraft palette, opaque).</li>
 * <li>Formats {@code &k} obfuscated, {@code &l} bold, {@code &m}
 * strikethrough, {@code &n} underline, {@code &o} italic, {@code &r} reset to
 * plain white.</li>
 * <li>True RGB {@code &#RRGGBB} (Bukkit style), {@code {#RRGGBB}} (brace style)
 * or {@code &x&R&R&G&G&B&B} (vanilla hex style), e.g. {@code &#ff55aa}.</li>
 * <li>{@code &&} escapes to a literal {@code &}.</li>
 * </ul>
 *
 * <p>Unknown codes are kept literally. Pure logic - no registries, safe for
 * headless tests.
 */
public final class ColorCodes {
    /** Legacy palette, opaque ARGB. */
    private static final Map<Character, Integer> LEGACY = new HashMap<>();

    static {
        LEGACY.put('0', 0xFF000000);
        LEGACY.put('1', 0xFF0000AA);
        LEGACY.put('2', 0xFF00AA00);
        LEGACY.put('3', 0xFF00AAAA);
        LEGACY.put('4', 0xFFAA0000);
        LEGACY.put('5', 0xFFAA00AA);
        LEGACY.put('6', 0xFFFFAA00);
        LEGACY.put('7', 0xFFAAAAAA);
        LEGACY.put('8', 0xFF555555);
        LEGACY.put('9', 0xFF5555FF);
        LEGACY.put('a', 0xFF55FF55);
        LEGACY.put('b', 0xFF55FFFF);
        LEGACY.put('c', 0xFFFF5555);
        LEGACY.put('d', 0xFFFF55FF);
        LEGACY.put('e', 0xFFFFFF55);
        LEGACY.put('f', 0xFFFFFFFF);
    }

    private ColorCodes() {
    }

    /** Parses {@code raw} into a styled component (never {@code null}). */
    public static MutableComponent parse(String raw) {
        MutableComponent root = Component.empty();
        if (raw == null || raw.isEmpty()) {
            return root;
        }
        StringBuilder chunk = new StringBuilder();
        Integer color = null;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        int i = 0;
        int length = raw.length();
        while (i < length) {
            char c = raw.charAt(i);
            if (c == '{' && i + 8 < length && raw.charAt(i + 1) == '#'
                    && raw.charAt(i + 8) == '}') {
                Integer hex = parseHex(raw, i + 2);
                if (hex != null) {
                    root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                            obfuscated));
                    chunk.setLength(0);
                    color = hex;
                    i += 9;
                    continue;
                }
            }
            if (c == '&' && i + 1 < length) {
                char next = raw.charAt(i + 1);
                if (next == '&') {
                    chunk.append('&');
                    i += 2;
                    continue;
                }
                if (next == '#') {
                    Integer hex = parseHex(raw, i + 2);
                    if (hex != null) {
                        root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                                obfuscated));
                        chunk.setLength(0);
                        color = hex;
                        i += 8;
                        continue;
                    }
                    // Not valid hex: keep literally.
                    chunk.append(c);
                    i++;
                    continue;
                }
                if (next == 'x' || next == 'X') {
                    Integer hex = parseVanillaHex(raw, i + 2);
                    if (hex != null) {
                        root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                                obfuscated));
                        chunk.setLength(0);
                        color = hex;
                        i += 14;
                        continue;
                    }
                    chunk.append(c);
                    i++;
                    continue;
                }
                char code = Character.toLowerCase(next);
                if (LEGACY.containsKey(code)) {
                    root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                            obfuscated));
                    chunk.setLength(0);
                    color = LEGACY.get(code);
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    obfuscated = false;
                    i += 2;
                    continue;
                }
                switch (code) {
                    case 'k', 'l', 'm', 'n', 'o' -> {
                        root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                                obfuscated));
                        chunk.setLength(0);
                        if (code == 'k') {
                            obfuscated = true;
                        } else if (code == 'l') {
                            bold = true;
                        } else if (code == 'm') {
                            strikethrough = true;
                        } else if (code == 'n') {
                            underline = true;
                        } else {
                            italic = true;
                        }
                        i += 2;
                        continue;
                    }
                    case 'r' -> {
                        root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough,
                                obfuscated));
                        chunk.setLength(0);
                        color = null;
                        bold = false;
                        italic = false;
                        underline = false;
                        strikethrough = false;
                        obfuscated = false;
                        i += 2;
                        continue;
                    }
                    default -> {
                        // Unknown code: keep literally.
                    }
                }
            }
            chunk.append(c);
            i++;
        }
        root.append(styledChunk(chunk, color, bold, italic, underline, strikethrough, obfuscated));
        return root;
    }

    private static MutableComponent styledChunk(StringBuilder chunk, Integer color, boolean bold,
            boolean italic, boolean underline, boolean strikethrough, boolean obfuscated) {
        if (chunk.length() == 0) {
            return Component.empty();
        }
        Style style = Style.EMPTY;
        if (color != null) {
            TextColor textColor = TextColor.fromRgb(color & 0xFFFFFF);
            if (textColor != null) {
                style = style.withColor(textColor);
            }
        }
        style = style.withBold(bold).withItalic(italic).withUnderlined(underline)
                .withStrikethrough(strikethrough).withObfuscated(obfuscated);
        return Component.literal(chunk.toString()).withStyle(style);
    }

    /** Parses 6 hex digits at {@code offset}, returning opaque ARGB or {@code null}. */
    private static Integer parseHex(String raw, int offset) {
        if (offset + 6 > raw.length()) {
            return null;
        }
        int rgb = 0;
        for (int j = 0; j < 6; j++) {
            int digit = Character.digit(raw.charAt(offset + j), 16);
            if (digit < 0) {
                return null;
            }
            rgb = (rgb << 4) | digit;
        }
        return 0xFF000000 | rgb;
    }

    /** Parses {@code &x&R&R&G&G&B&B} at {@code offset}, opaque ARGB or {@code null}. */
    private static Integer parseVanillaHex(String raw, int offset) {
        StringBuilder hex = new StringBuilder(6);
        int pos = offset;
        for (int j = 0; j < 6; j++) {
            if (pos + 1 >= raw.length() || raw.charAt(pos) != '&') {
                return null;
            }
            int digit = Character.digit(raw.charAt(pos + 1), 16);
            if (digit < 0) {
                return null;
            }
            hex.append(raw.charAt(pos + 1));
            pos += 2;
        }
        return parseHex(hex.toString(), 0);
    }
    /** Maximum length guard for rename input (raw characters). */
    public static String clamp(String raw, int maxLength) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
