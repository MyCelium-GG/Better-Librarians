package my.celium.org.betterlibrarians.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.BetterLibrariansMod;
import my.celium.org.betterlibrarians.client.LibrarianClient;
import my.celium.org.betterlibrarians.client.LookTitleCard;
import my.celium.org.betterlibrarians.client.RenameActions;
import my.celium.org.betterlibrarians.client.VillagerCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;

public final class ClientBetterLibrarians {
    private static final String HUD_PATH = "hud";
    private static final String COMMAND_ROOT = "vname";
    private static final String ARGUMENT_NAME = "name";
    private static final String LITERAL_CLEAR = "clear";
    private static final Identifier HUD_ID =
            Identifier.fromNamespaceAndPath(BetterLibrariansMod.ID, HUD_PATH);

    private ClientBetterLibrarians() {
    }

    public static void register() {
        VillagerDataStore.load();
        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(LibrarianClient.OPEN_KEY));
        ContainerScreenEvent.Render.Foreground.BUS.addListener(ClientBetterLibrarians::onRenderForeground);
        AddGuiOverlayLayersEvent.BUS.addListener(ClientBetterLibrarians::onAddLayers);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientBetterLibrarians::onClientTick);
        RegisterClientCommandsEvent.BUS.addListener(ClientBetterLibrarians::onRegisterCommands);
    }

    private static void onRenderForeground(ContainerScreenEvent.Render.Foreground event) {
        if (event.getContainerScreen() instanceof MerchantScreen merchant) {
            VillagerCatalog.recordScreen(merchant);
        }
    }

    private static void onAddLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(HUD_ID, (gfx, tracker) -> LookTitleCard.render(gfx));
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        LibrarianClient.tick(Minecraft.getInstance());
    }

    private static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(buildCommand());
        event.getDispatcher().register(Commands.literal(LibrarianClient.COMMAND)
                .executes(context -> LibrarianClient.requestList()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal(COMMAND_ROOT)
                .then(Commands.argument(ARGUMENT_NAME, StringArgumentType.greedyString())
                        .executes(context -> deliver(context.getSource(),
                                RenameActions.setName(StringArgumentType.getString(context, ARGUMENT_NAME)))))
                .then(Commands.literal(LITERAL_CLEAR)
                        .executes(context -> deliver(context.getSource(), RenameActions.clearName())));
    }

    private static int deliver(CommandSourceStack source, RenameActions.Result result) {
        if (result.success()) {
            source.sendSuccess(result::message, false);
        } else {
            source.sendFailure(result.message());
        }
        return result.code();
    }
}