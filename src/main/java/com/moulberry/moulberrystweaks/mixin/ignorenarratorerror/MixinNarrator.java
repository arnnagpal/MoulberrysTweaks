package com.moulberry.moulberrystweaks.mixin.ignorenarratorerror;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.text2speech.Narrator;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Narrator.class)
public interface MixinNarrator {

    @WrapWithCondition(method = "getNarrator", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private static boolean getNarratorLogError(Logger instance, String s, Throwable throwable) {
        return !MoulberrysTweaks.config.debugging.ignoreNarratorError;
    }

}
