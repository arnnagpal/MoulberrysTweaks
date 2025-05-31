package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugRenderClearNamespacePacket(String namespace) implements CustomPacketPayload {
    public static final Type<DebugRenderClearNamespacePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("debugrender", "clear_namespace"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugRenderClearNamespacePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        DebugRenderClearNamespacePacket::namespace,
        DebugRenderClearNamespacePacket::new
    );

    public void handle(ClientPlayNetworking.Context context) {
        DebugRenderManager.clearNamespace(this.namespace);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
