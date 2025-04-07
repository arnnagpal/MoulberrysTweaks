package com.moulberry.moulberrystweaks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.moulberry.moulberrystweaks.packet.DebugMovementDataPacket;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MoulberrysTweaks implements ModInitializer {
	public static final String MOD_ID = "moulberrystweaks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean isAutoVanishPlayersEnabled = false;

    /*
        List of tweaks
        1. Server-correct attack strength ticker
        2. autovanishplayers command
        3. Prevents disconnect when server removes player from team
        4. Automatically reloads resourcepacks when resourcepack folder is modified
        5. Faster loading overlay
        6. Unpaused loading overlay
        7. Generate font width command
     */

	@Override
	public void onInitialize() {
		LOGGER.info("Initialized Moulberry's Tweaks");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var command = ClientCommandManager.literal("autovanishplayers");
            command.then(ClientCommandManager.literal("on").executes(cmd -> {
                isAutoVanishPlayersEnabled = true;
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is now ON"));
                return 0;
            }));
            command.then(ClientCommandManager.literal("off").executes(cmd -> {
                isAutoVanishPlayersEnabled = false;
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is now OFF"));
                return 0;
            }));
            command.executes(cmd -> {
                String onOff = isAutoVanishPlayersEnabled ? "ON" : "OFF";
                cmd.getSource().sendFeedback(Component.literal("AutoVanishPlayers is " + onOff));
                return 0;
            });
            dispatcher.register(command);

            command = ClientCommandManager.literal("generatefontwidthtable")
                                          .then(ClientCommandManager.argument("font", ResourceLocationArgument.id())
                                              .executes(MoulberrysTweaks::writeFontWidths));
            dispatcher.register(command);
        });

        PayloadTypeRegistry.playC2S().register(DebugMovementDataPacket.TYPE, DebugMovementDataPacket.STREAM_CODEC);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!(Minecraft.getInstance().getOverlay() instanceof LoadingOverlay)) {
                PackFolderWatcher.tick();
            }
        });
	}

    private static int writeFontWidths(CommandContext<FabricClientCommandSource> cmd) {
        ResourceLocation fontName = cmd.getArgument("font", ResourceLocation.class);

        Font font = Minecraft.getInstance().font;
        FontSet fontSet = font.fonts.apply(fontName);
        if (fontSet.name().equals(FontManager.MISSING_FONT)) {
            cmd.getSource().sendFeedback(Component.literal("Font does not exist"));
            return 0;
        }

        JsonArray array = new JsonArray();

        int lastWidth = -1;
        int runLength = 0;
        for (int c = Character.MIN_CODE_POINT; c <= Character.MAX_CODE_POINT; c++) {
            int width = Mth.ceil(fontSet.getGlyphInfo(c, false).getAdvance());

            if (lastWidth == -1) {
                lastWidth = width;
            } else if (lastWidth != width) {
                array.add(runLength);
                array.add(lastWidth);

                lastWidth = width;
                runLength = 1;
            } else {
                runLength += 1;
            }
        }

        array.add(runLength);
        array.add(lastWidth);

        Path path = FabricLoader.getInstance().getGameDir().resolve("widths.json");
        try {
            Files.writeString(path, new Gson().toJson(array));
            cmd.getSource().sendFeedback(Component.literal("Wrote widths.json in .minecraft folder"));
        } catch (IOException ignored) {
            cmd.getSource().sendFeedback(Component.literal("Failed to write widths.json"));
        }

        return 0;
    }
}
