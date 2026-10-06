package my.celium.org.betterlibrarians.fabric;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint: server-safe bootstrap only. Client rendering hooks live
 * in {@link BetterLibrariansFabricClient} so dedicated servers never load
 * client classes.
 */
public final class BetterLibrariansFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterLibrariansMod.init();
    }
}
