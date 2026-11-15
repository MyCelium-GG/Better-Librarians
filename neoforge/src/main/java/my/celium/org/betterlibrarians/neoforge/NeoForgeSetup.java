package my.celium.org.betterlibrarians.neoforge;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.restock.RestockServer;
import my.celium.org.client.ClientHooks;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Only loaded when Mycel is installed: config, the restock server tick and the client hooks. */
final class NeoForgeSetup {
    private NeoForgeSetup() {
    }

    static void start(IEventBus modBus) {
        BetterLibrariansMod.init();
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> RestockServer.tick(event.getServer()));
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent,
                            BetterLibrariansMod.ID));
            ClientBetterLibrarians.register(modBus);
        }
    }
}
