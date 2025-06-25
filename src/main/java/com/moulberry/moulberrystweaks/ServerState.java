package com.moulberry.moulberrystweaks;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum ServerState {
    CLIENT_OR_DEFAULT,
    FORCE_ON,
    FORCE_OFF,
    ON,
    OFF;

    public static final IntFunction<ServerState> BY_ID = ByIdMap.continuous(ServerState::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ServerState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ServerState::ordinal);

}
