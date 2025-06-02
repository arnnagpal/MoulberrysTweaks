package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.moulberry.moulberrystweaks.debugrender.CustomRenderTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public record DebugShapeQuad(Vec3 one, Vec3 two, Vec3 three, Vec3 four, int argb) implements DebugShape {

    public static final StreamCodec<FriendlyByteBuf, DebugShapeQuad> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        DebugShapeQuad::one,
        Vec3.STREAM_CODEC,
        DebugShapeQuad::two,
        Vec3.STREAM_CODEC,
        DebugShapeQuad::three,
        Vec3.STREAM_CODEC,
        DebugShapeQuad::four,
        ByteBufCodecs.INT,
        DebugShapeQuad::argb,
        DebugShapeQuad::new
    );

    @Override
    public Vec3 center() {
        return this.one;
    }

    @Override
    public RenderMethod renderMethod() {
        return RenderMethod.WORLD_CACHED;
    }

    @Override
    public void renderWorldCached(Consumer<RenderJob> render, int flags) {
        boolean showThroughWalls = (flags & FLAG_SHOW_THROUGH_WALLS) != 0;
        boolean wireframe = (flags & FLAG_WIREFRAME) != 0;

        float alpha = ((this.argb >> 24) & 0xFF)/255f;
        if (alpha > 0.01f) {
            float red = ((this.argb >> 16) & 0xFF)/255f;
            float green = ((this.argb >> 8) & 0xFF)/255f;
            float blue = (this.argb & 0xFF)/255f;

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            Vec3 center = this.center();
            Vec3 a = this.one.subtract(center);
            Vec3 b = this.two.subtract(center);
            Vec3 c = this.three.subtract(center);
            Vec3 d = this.four.subtract(center);

            bufferBuilder.addVertex((float)a.x, (float)a.y, (float)a.z).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex((float)b.x, (float)b.y, (float)b.z).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex((float)d.x, (float)d.y, (float)d.z).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex((float)c.x, (float)c.y, (float)c.z).setColor(red, green, blue, alpha);


            try (MeshData meshData = bufferBuilder.build()) {
                if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(true, wireframe, false), WHITE));
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(false, wireframe, false), QUARTER_OPACITY));
                } else {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(!showThroughWalls, wireframe, false), WHITE));
                }
            }
        }
    }
}
