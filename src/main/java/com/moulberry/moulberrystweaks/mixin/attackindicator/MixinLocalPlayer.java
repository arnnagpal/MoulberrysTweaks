package com.moulberry.moulberrystweaks.mixin.attackindicator;

import com.mojang.authlib.GameProfile;
import com.moulberry.moulberrystweaks.DebugMovementData;
import com.moulberry.moulberrystweaks.ext.LocalPlayerExt;
import com.moulberry.moulberrystweaks.packet.DebugMovementDataPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends Player implements LocalPlayerExt {

    @Shadow
    @Final
    public ClientPacketListener connection;
    @Unique
    private int visualAttackStrengthTicker = 0;

    public MixinLocalPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "swing", at = @At("HEAD"))
    public void swing(InteractionHand interactionHand, CallbackInfo ci) {
        this.visualAttackStrengthTicker = 0;
    }

    @Override
    public float mt$getVisualAttackStrengthScale(float partialTick) {
        return Mth.clamp(((float)this.visualAttackStrengthTicker + partialTick) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }

    @Override
    public void mt$resetVisualAttackStrengthScale() {
        this.visualAttackStrengthTicker = 0;
    }

    @Override
    public void mt$incrementVisualAttackStrengthScale() {
        this.visualAttackStrengthTicker += 1;
    }

    @Unique
    private final DebugMovementData debugMovementData = new DebugMovementData();

    @Override
    public DebugMovementData mt$getDebugMovementData() {
        return this.debugMovementData;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void aiStep(CallbackInfo ci) {
        this.debugMovementData.localPlayerAiStepVelocity = this.getDeltaMovement();
        this.debugMovementData.isInWater = this.isInWater();
        this.debugMovementData.isInLava = this.isInLava();
        this.debugMovementData.isSwimming = this.isSwimming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V"))
    public void beforeSendPosition(CallbackInfo ci) {
        if (this.connection.serverBrand().equalsIgnoreCase("graphite")) {
            ClientPlayNetworking.send(new DebugMovementDataPacket(this.debugMovementData));
        }
    }

}
