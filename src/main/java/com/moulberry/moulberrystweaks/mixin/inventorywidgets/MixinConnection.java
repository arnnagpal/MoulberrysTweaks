package com.moulberry.moulberrystweaks.mixin.inventorywidgets;

import com.moulberry.moulberrystweaks.widget.ActiveWidgets;
import com.moulberry.moulberrystweaks.widget.FloatingTextWidget;
import com.moulberry.moulberrystweaks.widget.PacketViewerWidget;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {

    @Inject(method = "doSendPacket", at = @At("HEAD"))
    public void doSendPacket(Packet<?> packet, @Nullable PacketSendListener sendListener, boolean flush, CallbackInfo ci) {
        if (ActiveWidgets.logPackets && Minecraft.getInstance().player != null && (Object)this == Minecraft.getInstance().player.connection.getConnection()) {
            for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
                if (widget instanceof PacketViewerWidget packetViewerWidget) {
                    if (packet instanceof BundlePacket<?> bundlePacket) {
                        for (Packet<?> subPacket : bundlePacket.subPackets()) {
                            packetViewerWidget.addPacket(true, subPacket);
                        }
                    } else {
                        packetViewerWidget.addPacket(true, packet);
                    }
                    return;
                }
            }
            ActiveWidgets.logPackets = false;
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    public void readPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (ActiveWidgets.logPackets && Minecraft.getInstance().player != null && (Object)this == Minecraft.getInstance().player.connection.getConnection()) {
            for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
                if (widget instanceof PacketViewerWidget packetViewerWidget) {
                    if (packet instanceof BundlePacket<?> bundlePacket) {
                        for (Packet<?> subPacket : bundlePacket.subPackets()) {
                            packetViewerWidget.addPacket(false, subPacket);
                        }
                    } else {
                        packetViewerWidget.addPacket(false, packet);
                    }
                    return;
                }
            }
            ActiveWidgets.logPackets = false;
        }
    }

}
