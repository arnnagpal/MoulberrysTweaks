package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.AutoVanishPlayers;
import com.moulberry.moulberrystweaks.ServerState;
import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import com.moulberry.moulberrystweaks.debugrender.shapes.DebugShape;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record AutoVanishPlayersSetPacket(ServerState serverState) implements CustomPacketPayload {
    public static final Type<AutoVanishPlayersSetPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("autovanishplayers", "set"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AutoVanishPlayersSetPacket> STREAM_CODEC = StreamCodec.composite(
        ServerState.STREAM_CODEC,
        AutoVanishPlayersSetPacket::serverState,
        AutoVanishPlayersSetPacket::new
    );

    public void handle(ClientPlayNetworking.Context context) {
        AutoVanishPlayers.setServerState(this.serverState);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
