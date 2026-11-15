package my.celium.org.betterlibrarians.forge;

import my.celium.org.betterlibrarians.MissingMycel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entrypoint. Without Mycel the game still starts: Better Librarians stays off and says so in
 * chat. Everything that needs Mycel lives in {@link ForgeSetup}; client hooks in
 * {@link ClientBetterLibrarians}, so dedicated servers never load client classes.
 */
@Mod("betterlibrarians")
public final class BetterLibrariansForge {
    public BetterLibrariansForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        if (!ModList.isLoaded("mycel")) {
            MissingMycel.logOnce();
            PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    MissingMycel.tell(player);
                }
            });
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientMissingMycel.register();
            }
            return;
        }
        ForgeSetup.start();
    }
}
