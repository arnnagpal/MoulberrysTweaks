package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class DebugRenderClearPacket implements CustomPacketPayload {
    public static final Type<DebugRenderClearPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("debugrender", "clear"));
    public static DebugRenderClearPacket INSTANCE = new DebugRenderClearPacket();

    private DebugRenderClearPacket() {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugRenderClearPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public void handle(ClientPlayNetworking.Context context) {
        DebugRenderManager.clear();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
