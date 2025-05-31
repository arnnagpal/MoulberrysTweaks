package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public record DebugShapeEllipsoid(Vec3 center, Vec3 size, Quaternionf rotation, int argb, int detail) implements DebugShape {

    public static final LoadingCache<Integer, float[]> SPHERE_SURFACE = CacheBuilder.newBuilder()
         .expireAfterAccess(Duration.ofMinutes(1))
         .build(new CacheLoader<>() {
             @Override
             public float[] load(Integer sizeInt) {
                 int size = sizeInt;
                 float[] data = new float[3 * (size+1) * (size+1)];
                 int index = 0;
                 for (int i = 0; i <= size; i++) {
                     for (int j = 0; j <= size; j++) {
                         float ip = (float) i / size * 2f - 1f;
                         float jp = (float) j / size * 2f - 1f;

                         float lengthSq = 1*1 + ip*ip + jp*jp;
                         float length = (float) Math.sqrt(lengthSq);
                         float invLength = 1 / length;

                         data[index++] = ip * invLength;
                         data[index++] = jp * invLength;
                         data[index++] = invLength;
                     }
                 }
                 return data;
             }
         });

    public static final StreamCodec<FriendlyByteBuf, DebugShapeEllipsoid> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        DebugShapeEllipsoid::center,
        Vec3.STREAM_CODEC,
        DebugShapeEllipsoid::size,
        ByteBufCodecs.QUATERNIONF,
        DebugShapeEllipsoid::rotation,
        ByteBufCodecs.INT,
        DebugShapeEllipsoid::argb,
        ByteBufCodecs.VAR_INT,
        DebugShapeEllipsoid::detail,
        DebugShapeEllipsoid::new
    );

    @Override
    public Vec3 center() {
        return this.center;
    }

    @Override
    public RenderMethod renderMethod() {
        return RenderMethod.CACHED;
    }

    @Override
    public void render(Consumer<RenderJob> render, int flags) {
        boolean showThroughWalls = (flags & FLAG_SHOW_THROUGH_WALLS) != 0;
        boolean wireframe = (flags & FLAG_WIREFRAME) != 0;

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.rotate(this.rotation);

        float extX = (float)this.size.x/2.0f;
        float extY = (float)this.size.y/2.0f;
        float extZ = (float)this.size.z/2.0f;

        float alpha = ((this.argb >> 24) & 0xFF)/255f;
        if (alpha > 0.01f) {
            float red = ((this.argb >> 16) & 0xFF)/255f;
            float green = ((this.argb >> 8) & 0xFF)/255f;
            float blue = (this.argb & 0xFF)/255f;

            boolean shade = (flags & FLAG_NO_SHADE) == 0;

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int detail = Math.max(0, this.detail)*2+1;

            float[] surface;
            try {
                surface = SPHERE_SURFACE.get(detail);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            //-X
            for (int i = 0; i < detail; i++) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                for (int j = 0; j < detail; j++) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*c1*c1 + (0.8f+b1*0.2f)*b1*b1 + 0.87f*a1*a1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*c2*c2 + (0.8f+b2*0.2f)*b2*b2 + 0.87f*a2*a2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*c3*c3 + (0.8f+b3*0.2f)*b3*b3 + 0.87f*a3*a3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*c4*c4 + (0.8f+b4*0.2f)*b4*b4 + 0.87f*a4*a4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, -extX*c1, extY*b1, extZ*a1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                    bufferBuilder.addVertex(matrix4f, -extX*c2, extY*b2, extZ*a2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, -extX*c3, extY*b3, extZ*a3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                    bufferBuilder.addVertex(matrix4f, -extX*c4, extY*b4, extZ*a4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                }
                if (i != detail-1) {
                    float a5 = surface[base+(detail-1)*jN+jN + (detail+1)*3];
                    float b5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+1];
                    float c5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+2];
                    bufferBuilder.addVertex(matrix4f, -extX*c5, extY*b5, extZ*a5).setColor(0f, 0f, 0f, 0f);
                }
            }

            //+Z
            for (int i = detail-1; i >= 0; i--) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                for (int j = 0; j < detail; j++) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*b1*b1 + (0.8f+a1*0.2f)*a1*a1 + 0.87f*c1*c1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*b2*b2 + (0.8f+a2*0.2f)*a2*a2 + 0.87f*c2*c2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*b3*b3 + (0.8f+a3*0.2f)*a3*a3 + 0.87f*c3*c3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*b4*b4 + (0.8f+a4*0.2f)*a4*a4 + 0.87f*c4*c4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, extX*b2, extY*a2, extZ*c2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b1, extY*a1, extZ*c1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b4, extY*a4, extZ*c4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b3, extY*a3, extZ*c3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                }
                if (i != 0) {
                    float a5 = surface[base+(detail-1)*jN+jN];
                    float b5 = surface[base+(detail-1)*jN+jN + 1];
                    float c5 = surface[base+(detail-1)*jN+jN + 2];
                    bufferBuilder.addVertex(matrix4f, extX*b5, extY*a5, extZ*c5).setColor(0f, 0f, 0f, 0f);
                }
            }

            //+X
            for (int i = detail-1; i >= 0; i--) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                for (int j = 0; j < detail; j++) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*c1*c1 + (0.8f+b1*0.2f)*b1*b1 + 0.87f*a1*a1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*c2*c2 + (0.8f+b2*0.2f)*b2*b2 + 0.87f*a2*a2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*c3*c3 + (0.8f+b3*0.2f)*b3*b3 + 0.87f*a3*a3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*c4*c4 + (0.8f+b4*0.2f)*b4*b4 + 0.87f*a4*a4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, extX*c2, extY*b2, extZ*a2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*c1, extY*b1, extZ*a1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*c4, extY*b4, extZ*a4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*c3, extY*b3, extZ*a3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                }
                if (i != 0) {
                    float a5 = surface[base+(detail-1)*jN+jN];
                    float b5 = surface[base+(detail-1)*jN+jN + 1];
                    float c5 = surface[base+(detail-1)*jN+jN + 2];
                    bufferBuilder.addVertex(matrix4f, extX*c5, extY*b5, extZ*a5).setColor(0f, 0f, 0f, 0f);
                }
            }

            //-Z
            for (int i = detail-1; i >= 0; i--) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                for (int j = detail-1; j >= 0; j--) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*b1*b1 + (0.8f+a1*0.2f)*a1*a1 + 0.87f*c1*c1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*b2*b2 + (0.8f+a2*0.2f)*a2*a2 + 0.87f*c2*c2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*b3*b3 + (0.8f+a3*0.2f)*a3*a3 + 0.87f*c3*c3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*b4*b4 + (0.8f+a4*0.2f)*a4*a4 + 0.87f*c4*c4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, extX*b4, extY*a4, -extZ*c4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b3, extY*a3, -extZ*c3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b2, extY*a2, -extZ*c2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b1, extY*a1, -extZ*c1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                }
                if (i != 0) {
                    float a5 = surface[base];
                    float b5 = surface[base + 1];
                    float c5 = surface[base + 2];
                    bufferBuilder.addVertex(matrix4f, extX*b5, extY*a5, -extZ*c5).setColor(0f, 0f, 0f, 0f);
                }
            }

            //-Y
            for (int i = 0; i < detail; i++) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                for (int j = 0; j < detail; j++) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*a1*a1 + (0.8f-c1*0.2f)*c1*c1 + 0.87f*b1*b1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*a2*a2 + (0.8f-c2*0.2f)*c2*c2 + 0.87f*b2*b2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*a3*a3 + (0.8f-c3*0.2f)*c3*c3 + 0.87f*b3*b3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*a4*a4 + (0.8f-c4*0.2f)*c4*c4 + 0.87f*b4*b4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, extX*a1, -extY*c1, extZ*b1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*a2, -extY*c2, extZ*b2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*a3, -extY*c3, extZ*b3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*a4, -extY*c4, extZ*b4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                }
                float a5 = surface[base+(detail-1)*jN+jN + (detail+1)*3];
                float b5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+1];
                float c5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+2];
                bufferBuilder.addVertex(matrix4f, extX*a5, -extY*c5, extZ*b5).setColor(0f, 0f, 0f, 0f);
            }

            //+Y
            for (int i = 0; i < detail; i++) {
                int jN;
                int base;
                if (i % 2 == 0) {
                    jN = 3;
                    base = i*(detail+1)*3;
                } else {
                    jN = -3;
                    base = (i*(detail+1) + detail)*3;
                }

                if (i == 0) {
                    float a0 = surface[base];
                    float b0 = surface[base+1];
                    float c0 = surface[base+2];
                    bufferBuilder.addVertex(matrix4f, extX*b0, extY*c0, extZ*a0).setColor(0f, 0f, 0f, 0f);
                }
                for (int j = 0; j < detail; j++) {
                    int start = base+j*jN;

                    float a1 = surface[start];
                    float b1 = surface[start+1];
                    float c1 = surface[start+2];
                    float a2 = surface[start + (detail+1)*3];
                    float b2 = surface[start + (detail+1)*3+1];
                    float c2 = surface[start + (detail+1)*3+2];
                    float a3 = surface[start+jN];
                    float b3 = surface[start+jN + 1];
                    float c3 = surface[start+jN + 2];
                    float a4 = surface[start+jN + (detail+1)*3];
                    float b4 = surface[start+jN + (detail+1)*3+1];
                    float c4 = surface[start+jN + (detail+1)*3+2];

                    float shadeComp1 = shade ? 0.7f*b1*b1 + (0.8f+c1*0.2f)*c1*c1 + 0.87f*a1*a1 : 1.0f;
                    float shadeComp2 = shade ? 0.7f*b2*b2 + (0.8f+c2*0.2f)*c2*c2 + 0.87f*a2*a2 : 1.0f;
                    float shadeComp3 = shade ? 0.7f*b3*b3 + (0.8f+c3*0.2f)*c3*c3 + 0.87f*a3*a3 : 1.0f;
                    float shadeComp4 = shade ? 0.7f*b4*b4 + (0.8f+c4*0.2f)*c4*c4 + 0.87f*a4*a4 : 1.0f;

                    bufferBuilder.addVertex(matrix4f, extX*b1, extY*c1, extZ*a1).setColor(red*shadeComp1, green*shadeComp1, blue*shadeComp1, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b2, extY*c2, extZ*a2).setColor(red*shadeComp2, green*shadeComp2, blue*shadeComp2, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b3, extY*c3, extZ*a3).setColor(red*shadeComp3, green*shadeComp3, blue*shadeComp3, alpha);
                    bufferBuilder.addVertex(matrix4f, extX*b4, extY*c4, extZ*a4).setColor(red*shadeComp4, green*shadeComp4, blue*shadeComp4, alpha);
                }
                if (i != detail-1) {
                    float a5 = surface[base+(detail-1)*jN+jN + (detail+1)*3];
                    float b5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+1];
                    float c5 = surface[base+(detail-1)*jN+jN + (detail+1)*3+2];
                    bufferBuilder.addVertex(matrix4f, extX*b5, extY*c5, extZ*a5).setColor(0f, 0f, 0f, 0f);
                }
            }


            try (MeshData meshData = bufferBuilder.build()) {
                if (showThroughWalls && (flags & FLAG_FULL_OPACITY_BEHIND_WALLS) == 0) {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(true, wireframe, true), WHITE));
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(false, wireframe, true), QUARTER_OPACITY));
                } else {
                    render.accept(new RenderJob(meshData, CustomRenderTypes.debugTriangleStrip(!showThroughWalls, wireframe, true), WHITE));
                }
            }
        }
    }
}
