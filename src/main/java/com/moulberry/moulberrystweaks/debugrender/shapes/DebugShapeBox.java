package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.moulberry.moulberrystweaks.debugrender.CustomRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public record DebugShapeBox(Vec3 center, Vec3 size, Quaternionf rotation, int faceArgb, int lineArgb, float lineThickness) implements DebugShape {

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugShapeBox> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        DebugShapeBox::center,
        Vec3.STREAM_CODEC,
        DebugShapeBox::size,
        ByteBufCodecs.QUATERNIONF,
        DebugShapeBox::rotation,
        ByteBufCodecs.INT,
        DebugShapeBox::faceArgb,
        ByteBufCodecs.INT,
        DebugShapeBox::lineArgb,
        ByteBufCodecs.FLOAT,
        DebugShapeBox::lineThickness,
        DebugShapeBox::new
    );

    @Override
    public Vec3 center() {
        return this.center;
    }

    @Override
    public RenderMethod renderMethod() {
        return RenderMethod.WORLD_CACHED;
    }

    @Override
    public void renderWorldCached(Consumer<RenderJob> render, int flags) {
        boolean showThroughWalls = (flags & FLAG_SHOW_THROUGH_WALLS) != 0;
        boolean wireframe = (flags & FLAG_WIREFRAME) != 0;

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.rotate(this.rotation);

        float minX = (float)(-this.size.x/2.0);
        float minY = (float)(-this.size.y/2.0);
        float minZ = (float)(-this.size.z/2.0);
        float maxX = (float)(this.size.x/2.0);
        float maxY = (float)(this.size.y/2.0);
        float maxZ = (float)(this.size.z/2.0);

        float alpha = ((this.faceArgb >> 24) & 0xFF)/255f;
        if (alpha > 0.01f) {
            float red = ((this.faceArgb >> 16) & 0xFF)/255f;
            float green = ((this.faceArgb >> 8) & 0xFF)/255f;
            float blue = (this.faceArgb & 0xFF)/255f;

            boolean shade = (flags & FLAG_NO_SHADE) == 0;
            final float XF = shade ? 0.7f : 1.0f;
            final float YPF = 1f;
            final float YNF = shade ? 0.6f : 1.0f;
            final float ZF = shade ? 0.87f : 1.0f;

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            //-X
            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red*XF, green*XF, blue*XF, alpha);

            //+Z
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);

            //+X
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red*XF, green*XF, blue*XF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red*XF, green*XF, blue*XF, alpha);

            //-Z
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(red*ZF, green*ZF, blue*ZF, alpha);

            //-Y
            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(red*YNF, green*YNF, blue*YNF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red*YNF, green*YNF, blue*YNF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(red*YNF, green*YNF, blue*YNF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red*YNF, green*YNF, blue*YNF, alpha);

            //+Y
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red*YPF, green*YPF, blue*YPF, alpha);

            try (MeshData meshData = bufferBuilder.build()) {
                if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(true, wireframe, true), WHITE));
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(false, wireframe, true), QUARTER_OPACITY));
                } else {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(!showThroughWalls, wireframe, true), WHITE));
                }
            }
        }

        alpha = ((this.lineArgb >> 24) & 0xFF)/255f;
        if (alpha > 0.01) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            float red = ((this.lineArgb >> 16) & 0xFF)/255f;
            float green = ((this.lineArgb >> 8) & 0xFF)/255f;
            float blue = (this.lineArgb & 0xFF)/255f;

            boolean showAxis = (flags & FLAG_SHOW_AXIS) != 0;
            float xAxisRed = showAxis ? 0 : red;
            float yAxisGreen = showAxis ? 0 : green;
            float zAxisBlue = showAxis ? 0 : blue;

            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(red, yAxisGreen, zAxisBlue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red, yAxisGreen, zAxisBlue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(xAxisRed, green, zAxisBlue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(xAxisRed, green, zAxisBlue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, minX, minY, minZ).setColor(xAxisRed, yAxisGreen, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(xAxisRed, yAxisGreen, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m00(), -matrix4f.m01(), -matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m00(), -matrix4f.m01(), -matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m10(), -matrix4f.m11(), -matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m10(), -matrix4f.m11(), -matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m20(), -matrix4f.m21(), -matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(-matrix4f.m20(), -matrix4f.m21(), -matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m00(), matrix4f.m01(), matrix4f.m02());
            bufferBuilder.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m10(), matrix4f.m11(), matrix4f.m12());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());
            bufferBuilder.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(matrix4f.m20(), matrix4f.m21(), matrix4f.m22());

            try (MeshData meshData = bufferBuilder.build()) {
                if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.DEBUG_LINE.apply((double) this.lineThickness), WHITE));
                    render.accept(new RenderJob(meshData, CustomRenderTypes.DEBUG_LINE_WITHOUT_DEPTH.apply((double) this.lineThickness), QUARTER_OPACITY));
                } else {
                    var function = showThroughWalls ? CustomRenderTypes.DEBUG_LINE_WITHOUT_DEPTH : CustomRenderTypes.DEBUG_LINE;
                    RenderType renderType = function.apply((double) this.lineThickness);
                    render.accept(new RenderJob(meshData, renderType, WHITE));
                }
            }
        }
    }
}
