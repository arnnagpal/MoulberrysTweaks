package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.moulberry.moulberrystweaks.debugrender.CustomRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public record DebugShapeLineStrip(List<Vec3> points, int argb, float lineThickness) implements DebugShape {

    public static final StreamCodec<FriendlyByteBuf, DebugShapeLineStrip> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC.apply(ByteBufCodecs.list()),
        DebugShapeLineStrip::points,
        ByteBufCodecs.INT,
        DebugShapeLineStrip::argb,
        ByteBufCodecs.FLOAT,
        DebugShapeLineStrip::lineThickness,
        DebugShapeLineStrip::new
    );

    @Override
    public Vec3 center() {
        return this.points.isEmpty() ? Vec3.ZERO : this.points.get(0);
    }

    @Override
    public RenderMethod renderMethod() {
        return RenderMethod.WORLD_CACHED;
    }

    @Override
    public void renderWorldCached(Consumer<RenderJob> render, int flags) {
        if (this.points.size() < 2) {
            return;
        }

        Vec3 center = this.center();

        boolean showThroughWalls = (flags & FLAG_SHOW_THROUGH_WALLS) != 0;

        float alpha = ((this.argb >> 24) & 0xFF)/255f;
        if (alpha > 0.01f) {
            float red = ((this.argb >> 16) & 0xFF)/255f;
            float green = ((this.argb >> 8) & 0xFF)/255f;
            float blue = (this.argb & 0xFF)/255f;

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            Vec3 last = this.points.get(1);
            for (Vec3 point : this.points) {
                float normalX = (float)(point.x - last.x);
                float normalY = (float)(point.y - last.y);
                float normalZ = (float)(point.z - last.z);
                last = point;

                float invLength = 1.0f / (float) Math.sqrt(normalX*normalX + normalY*normalY + normalZ*normalZ);
                if (!Float.isFinite(invLength)) {
                    continue;
                }
                normalX *= invLength;
                normalY *= invLength;
                normalZ *= invLength;

                bufferBuilder.addVertex((float)(point.x - center.x), (float)(point.y - center.y), (float)(point.z - center.z))
                              .setColor(red, green, blue, alpha)
                              .setNormal(normalX, normalY, normalZ);
            }

            try (MeshData meshData = bufferBuilder.build()) {
                if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.LINE_STRIP.apply((double) this.lineThickness), WHITE));
                    render.accept(new RenderJob(meshData, CustomRenderTypes.LINE_STRIP_WITHOUT_DEPTH.apply((double) this.lineThickness), QUARTER_OPACITY));
                } else {
                    RenderType renderType = showThroughWalls ? CustomRenderTypes.LINE_STRIP_WITHOUT_DEPTH.apply((double) this.lineThickness) : CustomRenderTypes.LINE_STRIP.apply((double) this.lineThickness);
                    render.accept(new RenderJob(meshData, renderType, WHITE));
                }
            }
        }
    }
}
