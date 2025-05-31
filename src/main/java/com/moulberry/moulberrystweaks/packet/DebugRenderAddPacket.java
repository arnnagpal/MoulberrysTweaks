package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.debugrender.shapes.DebugShape;
import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record DebugRenderAddPacket(Optional<ResourceLocation> resourceLocation, DebugShape debugShape, int flags, int lifetime) implements CustomPacketPayload {
    public static final Type<DebugRenderAddPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("debugrender", "add"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugRenderAddPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        DebugRenderAddPacket::resourceLocation,
        DebugShape.STREAM_CODEC,
        DebugRenderAddPacket::debugShape,
        ByteBufCodecs.VAR_INT,
        DebugRenderAddPacket::flags,
        ByteBufCodecs.VAR_INT,
        DebugRenderAddPacket::lifetime,
        DebugRenderAddPacket::new
    );

    public void handle(ClientPlayNetworking.Context context) {
        DebugRenderManager.add(this.resourceLocation, this.debugShape, this.flags, this.lifetime);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
