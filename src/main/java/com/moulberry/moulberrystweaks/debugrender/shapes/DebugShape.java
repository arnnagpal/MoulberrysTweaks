package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.debugrender.GuiRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.SkipPacketDecoderException;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public sealed interface DebugShape permits DebugShapeBox, DebugShapeEllipsoid, DebugShapeLineStrip, DebugShapeQuad, DebugShapeText, DebugShapeGuiText {

    int FLAG_SHOW_THROUGH_WALLS = 1;
    int FLAG_FULL_OPACITY_BEHIND_WALLS = 2;
    int FLAG_WIREFRAME = 4;
    int FLAG_NO_SHADE = 8;
    int FLAG_SHOW_AXIS = 16;

    float[] WHITE = new float[]{1f, 1f, 1f, 1f};
    float[] QUARTER_OPACITY = new float[]{1f, 1f, 1f, 0.25f};

    enum RenderMethod {
        F3_TEXT_LEFT,
        F3_TEXT_RIGHT,
        GUI_IMMEDIATE,
        WORLD_IMMEDIATE,
        WORLD_CACHED
    }

    record RenderJob(MeshData meshData, RenderType renderType, float[] colour) {
    }

    RenderMethod renderMethod();
    default void renderF3Text(List<String> list, int flags) {}
    default void renderGuiImmediate(GuiGraphics guiGraphics, GuiRenderContext context, int flags) {}
    default void renderWorldImmediate(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera, int flags) {}
    default void renderWorldCached(Consumer<RenderJob> render, int flags) {}
    Vec3 center();

    StreamCodec<RegistryFriendlyByteBuf, DebugShape> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DebugShape decode(RegistryFriendlyByteBuf friendlyByteBuf) {
            String shape = friendlyByteBuf.readUtf();
            return switch (shape) {
                case "box" -> DebugShapeBox.STREAM_CODEC.decode(friendlyByteBuf);
                case "ellipsoid" -> DebugShapeEllipsoid.STREAM_CODEC.decode(friendlyByteBuf);
                case "line_strip" -> DebugShapeLineStrip.STREAM_CODEC.decode(friendlyByteBuf);
                case "quad" -> DebugShapeQuad.STREAM_CODEC.decode(friendlyByteBuf);
                case "text" -> DebugShapeText.STREAM_CODEC.decode(friendlyByteBuf);
                case "gui_text" -> DebugShapeGuiText.STREAM_CODEC.decode(friendlyByteBuf);
                default -> throw new SkipPacketDecoderException("Unknown debug shape: " + shape);
            };
        }

        @Override
        public void encode(RegistryFriendlyByteBuf friendlyByteBuf, DebugShape debugShape) {
            switch (debugShape) {
                case DebugShapeBox box -> {
                    friendlyByteBuf.writeUtf("box");
                    DebugShapeBox.STREAM_CODEC.encode(friendlyByteBuf, box);
                }
                case DebugShapeEllipsoid ellipsoid -> {
                    friendlyByteBuf.writeUtf("ellipsoid");
                    DebugShapeEllipsoid.STREAM_CODEC.encode(friendlyByteBuf, ellipsoid);
                }
                case DebugShapeLineStrip lineStrip -> {
                    friendlyByteBuf.writeUtf("line_strip");
                    DebugShapeLineStrip.STREAM_CODEC.encode(friendlyByteBuf, lineStrip);
                }
                case DebugShapeQuad quad -> {
                    friendlyByteBuf.writeUtf("quad");
                    DebugShapeQuad.STREAM_CODEC.encode(friendlyByteBuf, quad);
                }
                case DebugShapeText text -> {
                    friendlyByteBuf.writeUtf("text");
                    DebugShapeText.STREAM_CODEC.encode(friendlyByteBuf, text);
                }
                case DebugShapeGuiText guiText -> {
                    friendlyByteBuf.writeUtf("gui_text");
                    DebugShapeGuiText.STREAM_CODEC.encode(friendlyByteBuf, guiText);
                }
            }
        }
    };

}
