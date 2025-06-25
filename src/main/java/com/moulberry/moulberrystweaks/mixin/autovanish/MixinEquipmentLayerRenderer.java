package com.moulberry.moulberrystweaks.mixin.autovanish;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.moulberrystweaks.ext.TranslucentAlphaExt;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class MixinEquipmentLayerRenderer implements TranslucentAlphaExt {

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

    @WrapOperation(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;getColorForLayer(Lnet/minecraft/client/resources/model/EquipmentClientInfo$Layer;I)I"))
    public int renderLayers_getColorForLayer(EquipmentClientInfo.Layer layer, int color, Operation<Integer> original) {
        int argb = original.call(layer, color);
        if (this.translucentAlpha < 0xFF) {
            int oldAlpha = (argb >> 24) & 0xFF;
            int newAlpha = Math.min(oldAlpha, this.translucentAlpha);
            argb &= 0xFFFFFF;
            argb |= newAlpha << 24;
        }
        return argb;
    }

    @WrapOperation(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType renderLayers_armorCutoutNoCull(ResourceLocation location, Operation<RenderType> original) {
        if (this.translucentAlpha < 0xFF) {
            return RenderType.armorTranslucent(location);
        } else {
            return original.call(location);
        }
    }

}
