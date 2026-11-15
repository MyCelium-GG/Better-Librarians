package my.celium.org.betterlibrarians.fabric;

import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.restock.RestockServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Only loaded when Mycel is installed: config, the restock packet and its server tick. */
final class FabricSetup {
    private FabricSetup() {
    }

    static void start() {
        BetterLibrariansMod.init();
        ServerTickEvents.END_SERVER_TICK.register(RestockServer::tick);
    }
}
