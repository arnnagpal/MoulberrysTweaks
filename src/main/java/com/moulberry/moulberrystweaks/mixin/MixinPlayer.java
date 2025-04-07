package com.moulberry.moulberrystweaks.mixin;

import com.moulberry.moulberrystweaks.ext.LocalPlayerExt;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MixinPlayer {

    @Inject(method = "resetAttackStrengthTicker", at = @At("HEAD"))
    public void resetAttackStrengthTicker(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$resetVisualAttackStrengthScale();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;moveCloak()V"))
    public void tick(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$incrementVisualAttackStrengthScale();
        }
    }

}
