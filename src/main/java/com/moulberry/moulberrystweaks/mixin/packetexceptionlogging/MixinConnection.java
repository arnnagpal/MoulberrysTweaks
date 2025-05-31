package com.moulberry.moulberrystweaks.mixin.packetexceptionlogging;

import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {

    @Shadow
    @Final
    private PacketFlow receiving;
    @Unique
    private static long lastStackTrace = 0;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        if (this.receiving == PacketFlow.CLIENTBOUND && MoulberrysTweaks.config.logPacketExceptions) {
            throwable = ignoreOrUnwrapException(throwable);
            if (throwable == null) {
                return;
            }

            long time = System.currentTimeMillis();
            long delta = time - lastStackTrace;

            if (delta < 0 || delta > 1000) {
                lastStackTrace = time;
                MoulberrysTweaks.LOGGER.error("Exception in connection. Logging for debug purposes", throwable);
            }
        }
    }

    @Unique
    @Nullable
    private static Throwable ignoreOrUnwrapException(Throwable e) {
        if (e instanceof SkipPacketException) {
            return null;
        }
        if (e instanceof io.netty.channel.ChannelException || e instanceof io.netty.channel.ConnectTimeoutException || e instanceof io.netty.channel.ChannelPipelineException) {
            return ignoreOrUnwrapException(e.getCause());
        }
        return e;
    }

}
