package my.celium.org.betterlibrarians.forge;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.restock.RestockServer;
import my.celium.org.client.ClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Only loaded when Mycel is installed: config, the restock server tick and the client hooks. */
final class ForgeSetup {
    private ForgeSetup() {
    }

    static void start() {
        BetterLibrariansMod.init();
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> RestockServer.tick(event.server()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (Minecraft minecraft, Screen parent) -> ClientHooks.createConfigScreen(parent,
                                    BetterLibrariansMod.ID)));
            ClientBetterLibrarians.register();
        }
    }
}
