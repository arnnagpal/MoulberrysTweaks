package com.moulberry.moulberrystweaks.packet;

import com.moulberry.moulberrystweaks.DebugMovementData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugMovementDataPacket(DebugMovementData debugMovementData) implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = ResourceLocation.fromNamespaceAndPath("moulberrystweaks", "debug_movement_data");
    public static final Type<DebugMovementDataPacket> TYPE = new Type<>(PACKET_ID);

    public static final StreamCodec<FriendlyByteBuf, DebugMovementDataPacket> STREAM_CODEC = new DebugMovementDataPacketStreamCodec();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class DebugMovementDataPacketStreamCodec implements StreamCodec<FriendlyByteBuf, DebugMovementDataPacket> {
        @Override
        public DebugMovementDataPacket decode(FriendlyByteBuf friendlyByteBuf) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void encode(FriendlyByteBuf friendlyByteBuf, DebugMovementDataPacket debugMovementDataPacket) {
            DebugMovementData data = debugMovementDataPacket.debugMovementData();
            friendlyByteBuf.writeVec3(data.baseTickVelocity);
            friendlyByteBuf.writeVec3(data.localPlayerAiStepVelocity);
            friendlyByteBuf.writeVec3(data.livingAiStepVelocity);
            friendlyByteBuf.writeVec3(data.travelVelocity);
            friendlyByteBuf.writeVec3(data.moveRelativeVelocity);
            friendlyByteBuf.writeVec3(data.moveVelocity);
            friendlyByteBuf.writeVec3(data.afterTravelVelocity);
            friendlyByteBuf.writeVec3(data.moveInput);
            friendlyByteBuf.writeFloat(data.moveRelativeSpeed);
            friendlyByteBuf.writeBoolean(data.isInWater);
            friendlyByteBuf.writeBoolean(data.isInLava);
            friendlyByteBuf.writeBoolean(data.isSwimming);
            friendlyByteBuf.writeBoolean(data.isSprinting);
            friendlyByteBuf.writeBoolean(data.hasSprintSpeedModifier);
        }
    }

}
