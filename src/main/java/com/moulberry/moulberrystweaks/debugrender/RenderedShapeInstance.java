package com.moulberry.moulberrystweaks.debugrender;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.debugrender.shapes.DebugShape;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class RenderedShapeInstance {

    public record RenderEntry(GpuBuffer vertexBuffer, MeshData.DrawState drawState, RenderType renderType, Vector4f colour) {
        public void close() {
            this.vertexBuffer.close();
        }
    }

    public final @Nullable ResourceLocation resourceLocation;
    private final DebugShape debugShape;
    private List<RenderEntry> renderEntries = null;
    public final int flags;
    public int lifetime;
    public final Vec3 center;
    public final DebugShape.RenderMethod renderMethod;

    public RenderedShapeInstance(@Nullable ResourceLocation resourceLocation, DebugShape debugShape, int flags, int lifetime) {
        this.resourceLocation = resourceLocation;
        this.lifetime = lifetime;
        this.flags = flags;
        this.debugShape = debugShape;
        this.center = debugShape.center();
        this.renderMethod = debugShape.renderMethod();
    }

    public void renderF3Text(List<String> list) {
        this.debugShape.renderF3Text(list, this.flags);
    }

    public void renderGuiImmediate(GuiGraphics guiGraphics, GuiRenderContext context) {
        this.debugShape.renderGuiImmediate(guiGraphics, context, this.flags);
    }

    public void renderWorldImmediate(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera) {
        this.debugShape.renderWorldImmediate(poseStack, multiBufferSource, camera, this.flags);
    }

    public void renderWorldCached(Camera camera, Matrix4f modelViewMat, List<DynamicUniforms.Transform> transforms, DebugRenderManager.DrawConsumer render) {
        if (this.renderEntries == null) {
            this.renderEntries = new ArrayList<>();
            class ReuseVertexBuffer {
                MeshData lastMeshData = null;
                GpuBuffer vertexBufferForLastMeshData = null;
            }
            ReuseVertexBuffer reuseVertexBuffer = new ReuseVertexBuffer();
            this.debugShape.renderWorldCached((job -> {
                if (job.meshData() == null || job.meshData().drawState().vertexCount() <= 0) {
                    return;
                }
                GpuBuffer vertexBuffer;
                if (job.meshData() == reuseVertexBuffer.lastMeshData) {
                    vertexBuffer = reuseVertexBuffer.vertexBufferForLastMeshData;
                } else {
                    vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Shape vertex buffer",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, job.meshData().vertexBuffer());
                    reuseVertexBuffer.lastMeshData = job.meshData();
                    reuseVertexBuffer.vertexBufferForLastMeshData = vertexBuffer;
                }
                this.renderEntries.add(new RenderEntry(vertexBuffer, job.meshData().drawState(), job.renderType(), job.colour()));
            }), this.flags);
        }

        Matrix4f translatedMatrix = new Matrix4f(modelViewMat);
        Vec3 cameraPosition = camera.getPosition();
        float translationX = (float)(this.center.x - cameraPosition.x);
        float translationY = (float)(this.center.y - cameraPosition.y);
        float translationZ = (float)(this.center.z - cameraPosition.z);
        translatedMatrix.translate(translationX, translationY, translationZ);

        for (RenderEntry renderEntry : this.renderEntries) {
            int indexCount = renderEntry.drawState.indexCount();
            Vector4f colour = renderEntry.colour;

            transforms.add(new DynamicUniforms.Transform(
                translatedMatrix,
                colour,
                RenderSystem.getModelOffset(),
                RenderSystem.getTextureMatrix(),
                RenderSystem.getShaderLineWidth()
            ));
            int index = transforms.size() - 1;
            render.addDraw(new RenderPass.Draw<>(0, renderEntry.vertexBuffer, null, null, 0, indexCount, (slices, uniformUploader) -> {
                uniformUploader.upload("DynamicTransforms", slices[index]);
            }), renderEntry.renderType);
        }
    }

    public void close() {
        if (this.renderEntries != null) {
            this.renderEntries.forEach(RenderEntry::close);
            this.renderEntries.clear();
        }
    }

}
