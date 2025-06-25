package com.moulberry.moulberrystweaks.mixin.autovanish;

import com.moulberry.moulberrystweaks.ext.TranslucentAlphaExt;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class MixinPlayerRenderState implements TranslucentAlphaExt {

    @Unique
    private int translucentAlpha = 0xFF;

    @Override
    public int moulberrystweaks$getTranslucentAlpha() {
        return this.translucentAlpha;
    }

    @Override
    public void moulberrystweaks$setTranslucentAlpha(int alpha) {
        this.translucentAlpha = alpha;
    }
}
