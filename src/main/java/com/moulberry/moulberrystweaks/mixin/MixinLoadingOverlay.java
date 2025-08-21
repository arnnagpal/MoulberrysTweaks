package com.moulberry.moulberrystweaks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    @Shadow
    private long fadeOutStart;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (MoulberrysTweaks.config.loadingOverlay.fast && this.fadeOutStart != -1L) {
            this.minecraft.setOverlay(null);
        }
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorTexture(Lcom/mojang/blaze3d/textures/GpuTexture;I)V", remap = false))
    public boolean renderShouldClear(CommandEncoder instance, GpuTexture texture, int argb) {
        return !MoulberrysTweaks.config.loadingOverlay.transparent || Minecraft.getInstance().player == null;
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIIIIII)V"))
    public boolean renderShouldBlit(GuiGraphics instance, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n, int o, int p, int q) {
        return !MoulberrysTweaks.config.loadingOverlay.transparent || Minecraft.getInstance().player == null;
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;fadeIn:Z"))
    public boolean renderShouldFadeIn(LoadingOverlay instance, Operation<Boolean> original) {
        if (MoulberrysTweaks.config.loadingOverlay.transparent && Minecraft.getInstance().player != null) {
            return false;
        }
        return original.call(instance);
    }

}
