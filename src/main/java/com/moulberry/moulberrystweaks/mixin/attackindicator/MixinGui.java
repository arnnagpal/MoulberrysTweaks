package com.moulberry.moulberrystweaks.mixin.attackindicator;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import com.moulberry.moulberrystweaks.ext.LocalPlayerExt;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class MixinGui {

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    public float renderCrosshair_getAttackStrengthScale(LocalPlayer instance, float partialTick, Operation<Float> original) {
        if (instance instanceof LocalPlayerExt localPlayerExt && MoulberrysTweaks.config.correctAttackIndicator) {
            return localPlayerExt.mt$getVisualAttackStrengthScale(partialTick);
        } else {
            return original.call(instance, partialTick);
        }
    }


}
