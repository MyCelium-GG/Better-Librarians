package my.celium.org.betterlibrarians.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.client.LookTitleCard;
import my.celium.org.betterlibrarians.client.NameplateSync;
import my.celium.org.betterlibrarians.client.RenameActions;
import my.celium.org.betterlibrarians.client.VillagerCatalog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.resources.Identifier;

public final class BetterLibrariansFabricClient implements ClientModInitializer {
    private static final String HUD_PATH = "hud";
    private static final String COMMAND_ROOT = "vname";
    private static final String ARGUMENT_NAME = "name";
    private static final String LITERAL_CLEAR = "clear";
    private static final Identifier HUD_ID =
            Identifier.fromNamespaceAndPath(BetterLibrariansMod.ID, HUD_PATH);

    @Override
    public void onInitializeClient() {
        VillagerDataStore.load();
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof MerchantScreen merchant) {
                ScreenEvents.afterExtract(merchant).register(
                        (current, gfx, mouseX, mouseY, tick) -> VillagerCatalog.recordScreen(merchant));
            }
        });
        HudElementRegistry.addLast(HUD_ID, (gfx, tracker) -> LookTitleCard.render(gfx));
        ClientTickEvents.END_CLIENT_TICK.register(client -> NameplateSync.tickClient(client));
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(buildCommand()));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildCommand() {
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal(COMMAND_ROOT)
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument(
                                ARGUMENT_NAME, StringArgumentType.greedyString())
                        .executes(context -> deliver(context.getSource(),
                                RenameActions.setName(StringArgumentType.getString(context, ARGUMENT_NAME)))))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal(LITERAL_CLEAR)
                        .executes(context -> deliver(context.getSource(), RenameActions.clearName())));
    }

    private static int deliver(FabricClientCommandSource source, RenameActions.Result result) {
        if (result.success()) {
            source.sendFeedback(result.message());
        } else {
            source.sendError(result.message());
        }
        return result.code();
    }
}