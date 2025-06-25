package com.moulberry.moulberrystweaks.mixin.autovanish;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.moulberrystweaks.ext.TranslucentAlphaExt;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public class MixinCapeLayer {

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    public void render_renderToBuffer(HumanoidModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, Operation<Void> original, @Local(argsOnly = true) PlayerRenderState playerRenderState) {
        if (playerRenderState instanceof TranslucentAlphaExt ext) {
            int translucentAlpha = ext.moulberrystweaks$getTranslucentAlpha();
            if (translucentAlpha < 0xFF) {
                instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFF | (translucentAlpha << 24));
                return;
            }
        }

        original.call(instance, poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entitySolid(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType render_entitySolid(ResourceLocation location, Operation<RenderType> original, @Local(argsOnly = true) PlayerRenderState playerRenderState) {
        if (playerRenderState instanceof TranslucentAlphaExt ext && ext.moulberrystweaks$getTranslucentAlpha() < 0xFF) {
            return RenderType.armorTranslucent(location);
        } else {
            return original.call(location);
        }
    }

}
