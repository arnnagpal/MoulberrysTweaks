package com.moulberry.moulberrystweaks.mixin.crashfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    // Wrap method call in try catch, prevents crash when server removes player from team which they aren't part of

    @WrapOperation(method = "handleSetPlayerTeamPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void handleSetPlayerTeamPacket_removePlayerFromTeam(Scoreboard instance, String string, PlayerTeam playerTeam, Operation<Void> original) {
        try {
            original.call(instance, string, playerTeam);
        } catch (Exception ignored) {}
    }

}
