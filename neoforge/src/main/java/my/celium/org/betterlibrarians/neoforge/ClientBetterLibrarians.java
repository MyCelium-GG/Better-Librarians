package my.celium.org.betterlibrarians.neoforge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import my.celium.org.betterlibrarians.VillagerDataStore;
import my.celium.org.betterlibrarians.client.LookTitleCard;
import my.celium.org.betterlibrarians.client.NameplateSync;
import my.celium.org.betterlibrarians.client.RenameActions;
import my.celium.org.betterlibrarians.client.VillagerCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientBetterLibrarians {
    private static final String COMMAND_ROOT = "vname";
    private static final String ARGUMENT_NAME = "name";
    private static final String LITERAL_CLEAR = "clear";

    private ClientBetterLibrarians() {
    }

    public static void register() {
        VillagerDataStore.load();
        NeoForge.EVENT_BUS.addListener(ClientBetterLibrarians::onRenderForeground);
        NeoForge.EVENT_BUS.addListener(ClientBetterLibrarians::onRenderGui);
        NeoForge.EVENT_BUS.addListener(ClientBetterLibrarians::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientBetterLibrarians::onRegisterCommands);
    }

    private static void onRenderForeground(ScreenEvent.Render.Foreground event) {
        if (event.getScreen() instanceof MerchantScreen merchant) {
            VillagerCatalog.recordScreen(merchant);
        }
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        LookTitleCard.render(event.getGuiGraphics());
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        NameplateSync.tickClient(Minecraft.getInstance());
    }

    private static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(buildCommand());
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