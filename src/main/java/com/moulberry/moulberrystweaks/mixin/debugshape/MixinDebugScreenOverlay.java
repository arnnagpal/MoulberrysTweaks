package com.moulberry.moulberrystweaks.mixin.debugshape;

import com.moulberry.moulberrystweaks.debugrender.DebugRenderManager;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DebugScreenOverlay.class, priority = 1800)
public class MixinDebugScreenOverlay {

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    public void getGameInformation(CallbackInfoReturnable<List<String>> cir) {
        List<String> info = cir.getReturnValue();
        DebugRenderManager.renderF3Text(info, true);
    }

    @Inject(method = "getSystemInformation", at = @At("RETURN"))
    public void getSystemInformation(CallbackInfoReturnable<List<String>> cir) {
        List<String> info = cir.getReturnValue();
        DebugRenderManager.renderF3Text(info, false);
    }

}
