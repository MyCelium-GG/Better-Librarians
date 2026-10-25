package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.Mycel;
import my.celium.org.betterlibrarians.client.LookTitleCard;
import my.celium.org.testing.MycelTesting;

/** The mod boots on Mycel with a tiny, well-defaulted config. */
class BetterLibrariansModTest {
    @BeforeAll
    static void initOnce(@TempDir Path tempDir) {
        MycelTesting.useConfigDir(tempDir);
        BetterLibrariansMod.init();
    }

    @Test
    void registersWithMycel() {
        assertTrue(Mycel.getRegisteredMods().contains(BetterLibrariansMod.META));
        assertEquals("betterlibrarians", BetterLibrariansMod.META.id());
    }

    @Test
    void configDefaults() {
        assertTrue(BetterLibrariansMod.enabled.get());
        assertTrue(BetterLibrariansMod.showLevel.get());
        assertTrue(BetterLibrariansMod.highlightMaxLevel.get());
        assertEquals(6, BetterLibrariansMod.maxBooks.get());
        assertTrue(BetterLibrariansMod.showUncataloguedHint.get());
    }

    @Test
    void enableToggleFlowsThrough() {
        assertTrue(BetterLibrariansMod.isModEnabled());
        Mycel.setEnabled(BetterLibrariansMod.ID, false);
        try {
            assertTrue(!BetterLibrariansMod.isModEnabled());
        } finally {
            Mycel.setEnabled(BetterLibrariansMod.ID, true);
        }
        assertTrue(BetterLibrariansMod.isModEnabled());
    }

    @Test
    void titleCardToleratesNulls() {
        // Loader HUD hooks must never crash the frame.
        LookTitleCard.render(null);
    }
}
