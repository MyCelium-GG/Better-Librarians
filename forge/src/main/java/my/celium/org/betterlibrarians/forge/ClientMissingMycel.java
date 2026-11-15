package my.celium.org.betterlibrarians.forge;

import my.celium.org.betterlibrarians.MissingMycel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

/** Without Mycel: one chat line when joining a server (singleplayer worlds get it from the server side). */
final class ClientMissingMycel {
    private ClientMissingMycel() {
    }

    static void register() {
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.isLocalServer() && event.getPlayer() != null) {
                event.getPlayer().sendSystemMessage(MissingMycel.message());
            }
        });
    }
}
