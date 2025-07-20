package com.moulberry.moulberrystweaks.mixin.preventserverclosingpausescreen;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketHandler extends ClientCommonPacketListenerImpl  {

    protected MixinClientPacketHandler(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Unique
    private static final Set<Class<?>> preventServerFromClosing = Set.of(
        PauseScreen.class,
        OptionsScreen.class,
        PackSelectionScreen.class,
        SoundOptionsScreen.class,
        VideoSettingsScreen.class,
        ControlsScreen.class,
        LanguageSelectScreen.class,
        AccessibilityOptionsScreen.class
    );

    @Inject(method = "handleContainerClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), cancellable = true)
    public void handleContainerClose(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (MoulberrysTweaks.config.gameplay.preventServerClosingPauseScreen) {
            Screen currentScreen = this.minecraft.screen;
            if (currentScreen != null && preventServerFromClosing.contains(currentScreen.getClass())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    public void handleOpenScreenHead(ClientboundOpenScreenPacket packet, CallbackInfo ci, @Share("oldScreenRef") LocalRef<Screen> oldScreenRef) {
        oldScreenRef.set(this.minecraft.screen);
    }

    @Inject(method = "handleOpenScreen", at = @At("RETURN"))
    public void handleOpenScreenReturn(ClientboundOpenScreenPacket packet, CallbackInfo ci, @Share("oldScreenRef") LocalRef<Screen> oldScreenRef) {
        if (MoulberrysTweaks.config.gameplay.preventServerClosingPauseScreen) {
            Screen oldScreen = oldScreenRef.get();
            Screen currentScreen = this.minecraft.screen;
            if (oldScreen == currentScreen) {
                return;
            }
            if (oldScreen != null && preventServerFromClosing.contains(oldScreen.getClass())) {
                if (currentScreen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.shouldCloseOnEsc()) {
                    containerScreen.onClose();
                    currentScreen = this.minecraft.screen;
                    if (currentScreen == null) {
                        Minecraft.getInstance().setScreen(oldScreen);
                    }
                }
            }
        }
    }

}
