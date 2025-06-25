package com.moulberry.moulberrystweaks.mixin.autovanish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.ext.TranslucentAlphaExt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer {

    @Shadow
    @Final
    private EquipmentLayerRenderer equipmentRenderer;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    public void renderHead(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HumanoidRenderState humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (humanoidRenderState instanceof TranslucentAlphaExt ext1 && this.equipmentRenderer instanceof TranslucentAlphaExt ext2) {
            ext2.moulberrystweaks$setTranslucentAlpha(ext1.moulberrystweaks$getTranslucentAlpha());
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("RETURN"))
    public void renderReturn(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HumanoidRenderState humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (this.equipmentRenderer instanceof TranslucentAlphaExt ext2) {
            ext2.moulberrystweaks$setTranslucentAlpha(0xFF);
        }
    }

}
