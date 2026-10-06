package my.celium.org.betterlibrarians.forge;

import my.celium.org.client.ClientHooks;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entrypoint: server-safe bootstrap; client hooks (config screen +
 * trade overlay) are isolated in {@link ClientBetterLibrarians} so dedicated
 * servers never load client classes.
 */
@Mod(BetterLibrariansMod.ID)
public final class BetterLibrariansForge {
    public BetterLibrariansForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        BetterLibrariansMod.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (Minecraft minecraft, Screen parent) -> ClientHooks.createConfigScreen(parent,
                                    BetterLibrariansMod.ID)));
            ClientBetterLibrarians.register();
        }
    }
}
