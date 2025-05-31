package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugRenderRemovePacket(ResourceLocation resourceLocation) implements CustomPacketPayload {
    public static final Type<DebugRenderRemovePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("debugrender", "remove"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugRenderRemovePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        DebugRenderRemovePacket::resourceLocation,
        DebugRenderRemovePacket::new
    );

    public void handle(ClientPlayNetworking.Context context) {
        DebugRenderManager.remove(this.resourceLocation);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
