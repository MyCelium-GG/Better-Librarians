package my.celium.org.betterlibrarians.neoforge;

import my.celium.org.client.ClientHooks;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge entrypoint: server-safe bootstrap; client hooks (config screen +
 * trade overlay) are isolated in {@link ClientBetterLibrarians} so dedicated
 * servers never load client classes.
 */
@Mod(BetterLibrariansMod.ID)
public final class BetterLibrariansNeoForge {
    public BetterLibrariansNeoForge(IEventBus modBus,
            @SuppressWarnings("unused") ModContainer container) {
        BetterLibrariansMod.init();
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent,
                            BetterLibrariansMod.ID));
            ClientBetterLibrarians.register(modBus);
        }
    }
}
