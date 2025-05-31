package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public record DebugShapeText(Vec3 position, Component component, boolean shadow, int backgroundColor) implements DebugShape {

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugShapeText> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        DebugShapeText::position,
        ComponentSerialization.TRUSTED_STREAM_CODEC,
        DebugShapeText::component,
        ByteBufCodecs.BOOL,
        DebugShapeText::shadow,
        ByteBufCodecs.INT,
        DebugShapeText::backgroundColor,
        DebugShapeText::new
    );

    @Override
    public Vec3 center() {
        return this.position;
    }

    @Override
    public RenderMethod renderMethod() {
        return RenderMethod.IMMEDIATE;
    }

    @Override
    public void renderImmediate(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera, int flags) {
        boolean showThroughWalls = (flags & FLAG_SHOW_THROUGH_WALLS) != 0;

        Vec3 cameraPosition = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(this.position.x - cameraPosition.x, this.position.y - cameraPosition.y, this.position.z - cameraPosition.z);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        Quaternionf quaternionf = new Quaternionf().rotationYXZ((float) -Math.toRadians(camera.getYRot() - 180), (float) Math.toRadians(camera.getXRot()), 0.0F);
        poseStack.mulPose(quaternionf);

        Font font = Minecraft.getInstance().font;
        int width = font.width(this.component);

        if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
            poseStack.translate(0, 0, -0.05);
            font.drawInBatch(this.component, -width/2f, -font.lineHeight/2f, 0x40FFFFFF, false, poseStack.last().pose(),
                multiBufferSource, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            poseStack.translate(0, 0, 0.05);
            font.drawInBatch(this.component, -width/2f, -font.lineHeight/2f, -1, this.shadow, poseStack.last().pose(),
                multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, this.backgroundColor, 0xF000F0);
        } else {
            Font.DisplayMode displayMode = showThroughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET;
            font.drawInBatch(this.component, -width/2f, -font.lineHeight/2f, -1, this.shadow, poseStack.last().pose(),
                multiBufferSource, displayMode, this.backgroundColor, 0xF000F0);
        }

        poseStack.popPose();
    }

}
