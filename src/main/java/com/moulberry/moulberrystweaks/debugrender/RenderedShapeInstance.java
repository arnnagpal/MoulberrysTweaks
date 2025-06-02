package com.moulberry.moulberrystweaks.debugrender;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.debugrender.shapes.DebugShape;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class RenderedShapeInstance {

    public record RenderEntry(GpuBuffer vertexBuffer, MeshData.DrawState drawState, RenderType renderType, float[] colour) {
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

    public void renderWorldCached(Camera camera, float[] modelViewMat, BiConsumer<RenderPass.Draw, RenderType> render) {
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
                        BufferType.VERTICES, BufferUsage.STATIC_WRITE, job.meshData().vertexBuffer());
                    reuseVertexBuffer.lastMeshData = job.meshData();
                    reuseVertexBuffer.vertexBufferForLastMeshData = vertexBuffer;
                }
                this.renderEntries.add(new RenderEntry(vertexBuffer, job.meshData().drawState(), job.renderType(), job.colour()));
            }), this.flags);
        }

        float[] translatedMatrix = Arrays.copyOf(modelViewMat, modelViewMat.length);
        Vec3 cameraPosition = camera.getPosition();
        float translationX = (float)(this.center.x - cameraPosition.x);
        float translationY = (float)(this.center.y - cameraPosition.y);
        float translationZ = (float)(this.center.z - cameraPosition.z);

        translatedMatrix[12] += translatedMatrix[0]*translationX + translatedMatrix[4]*translationY + translatedMatrix[8]*translationZ;
        translatedMatrix[13] += translatedMatrix[1]*translationX + translatedMatrix[5]*translationY + translatedMatrix[9]*translationZ;
        translatedMatrix[14] += translatedMatrix[2]*translationX + translatedMatrix[6]*translationY + translatedMatrix[10]*translationZ;

        for (RenderEntry renderEntry : this.renderEntries) {
            int indexCount = renderEntry.drawState.indexCount();
            float[] colour = renderEntry.colour;

            render.accept(new RenderPass.Draw(
                0, renderEntry.vertexBuffer, null, null, 0, indexCount, uniformUploader -> {
                uniformUploader.upload("ModelViewMat", translatedMatrix);
                uniformUploader.upload("ColorModulator", colour);
            }
            ), renderEntry.renderType);
        }
    }

    public void close() {
        if (this.renderEntries != null) {
            this.renderEntries.forEach(RenderEntry::close);
            this.renderEntries.clear();
        }
    }

}
