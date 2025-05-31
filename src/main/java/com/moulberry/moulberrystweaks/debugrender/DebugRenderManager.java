package com.moulberry.moulberrystweaks.debugrender;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.moulberry.moulberrystweaks.debugrender.shapes.DebugShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class DebugRenderManager {

    public static final LinkedHashSet<String> availableNamespaces = new LinkedHashSet<>();
    public static final LinkedHashSet<String> hiddenNamespaces = new LinkedHashSet<>();
    private static final Map<ResourceLocation, RenderedShapeInstance> shapeInstancesByResourceLocation = new LinkedHashMap<>();
    private static final List<RenderedShapeInstance> shapeInstances = new ArrayList<>();
    private static List<RenderedShapeInstance> sortedShapeInstances = null;
    private static Vec3 lastSortedPosition = Vec3.ZERO;
    private static boolean allHidden = false;

    public static void add(Optional<ResourceLocation> resourceLocationOptional, DebugShape debugShape, int flags, int lifetime) {
        RenderedShapeInstance renderedShapeInstance = new RenderedShapeInstance(resourceLocationOptional.orElse(null), debugShape, flags, lifetime);

        if (resourceLocationOptional.isPresent()) {
            ResourceLocation resourceLocation = resourceLocationOptional.get();

            availableNamespaces.add(resourceLocation.getNamespace());

            RenderedShapeInstance old = shapeInstancesByResourceLocation.get(resourceLocation);
            if (old != null) {
                old.close();
            }

            shapeInstancesByResourceLocation.put(resourceLocation, renderedShapeInstance);
        }
        shapeInstances.add(renderedShapeInstance);
        sortedShapeInstances = null;
    }

    public static void remove(ResourceLocation resourceLocation) {
        RenderedShapeInstance instance = shapeInstancesByResourceLocation.remove(resourceLocation);
        if (instance != null) {
            shapeInstances.remove(instance);
            sortedShapeInstances = null;
            instance.close();
        }
    }

    public static void clear() {
        shapeInstances.forEach(RenderedShapeInstance::close);
        shapeInstances.clear();
        sortedShapeInstances.clear();
        shapeInstancesByResourceLocation.clear();
        availableNamespaces.clear();
    }

    public static boolean isAllHidden() {
        return allHidden;
    }

    public static boolean showAll() {
        if (allHidden) {
            allHidden = false;
            hiddenNamespaces.clear();
            return true;
        } else if (hiddenNamespaces.isEmpty()) {
            return false;
        } else {
            hiddenNamespaces.clear();
            return true;
        }
    }

    public static boolean hideAll() {
        if (allHidden) {
            return false;
        } else {
            allHidden = true;
            hiddenNamespaces.clear();
            return true;
        }
    }

    public static boolean hideNamespace(String namespace) {
        if (allHidden) {
            return false;
        } else {
            return hiddenNamespaces.add(namespace);
        }
    }

    public static boolean showNamespace(String namespace) {
        boolean wasAllHidden = false;
        if (allHidden) {
            for (ResourceLocation resourceLocation : shapeInstancesByResourceLocation.keySet()) {
                hiddenNamespaces.add(resourceLocation.getNamespace());
            }
            allHidden = false;
            wasAllHidden = true;
        }
        if (hiddenNamespaces.remove(namespace)) {
            return true;
        } else {
            if (wasAllHidden) {
                hiddenNamespaces.clear();
                allHidden = true;
            }
            return false;
        }
    }

    public static void clearNamespace(String namespace) {
        shapeInstancesByResourceLocation.entrySet().removeIf(entry -> {
            if (entry.getKey().getNamespace().equals(namespace)) {
                RenderedShapeInstance instance = entry.getValue();
                shapeInstances.remove(instance);
                sortedShapeInstances = null;
                entry.getValue().close();
                return true;
            } else {
                return false;
            }
        });
        availableNamespaces.remove(namespace);
    }

    public static boolean hasShapesToRender() {
        return !shapeInstances.isEmpty() && !allHidden;
    }

    public static void render(PoseStack poseStack, Camera camera) {
        if (sortedShapeInstances == null) {
            lastSortedPosition = camera.getPosition();
            sortedShapeInstances = new ArrayList<>(shapeInstances);
            sortedShapeInstances.sort(Comparator.comparingDouble(instance -> -lastSortedPosition.distanceToSqr(instance.center)));
        } else if (camera.getPosition().distanceToSqr(lastSortedPosition) > 0.25*0.25) {
            lastSortedPosition = camera.getPosition();
            sortedShapeInstances.sort(Comparator.comparingDouble(instance -> -lastSortedPosition.distanceToSqr(instance.center)));
        }

        FogParameters oldFog = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(FogParameters.NO_FOG);

        float[] modelViewMat = poseStack.last().pose().get(new float[16]);

        LinkedHashMap<RenderType, List<RenderPass.Draw>> drawsForRenderType = new LinkedHashMap<>();

        MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (RenderedShapeInstance instance : sortedShapeInstances) {
            if (instance.resourceLocation != null && hiddenNamespaces.contains(instance.resourceLocation.getNamespace())) {
                continue;
            }

            instance.render(poseStack, multiBufferSource, camera, modelViewMat, (draw, renderType) -> {
                drawsForRenderType.computeIfAbsent(renderType, k -> new ArrayList<>()).add(draw);
            });
        }

        for (Map.Entry<RenderType, List<RenderPass.Draw>> entry : drawsForRenderType.entrySet()) {
            RenderType renderType = entry.getKey();
            List<RenderPass.Draw> draws = entry.getValue();

            int maxIndex = 0;
            for (RenderPass.Draw draw : draws) {
                if (draw.indexBuffer() == null) {
                    maxIndex = Math.max(maxIndex, draw.indexCount());
                }
            }

            RenderPipeline renderPipeline = renderType.getRenderPipeline();
            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
            GpuTexture colour = renderTarget.getColorTexture();
            GpuTexture depth = renderTarget.getDepthTexture();

            RenderSystem.AutoStorageIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(renderType.mode());
            GpuBuffer sharedIndexBuffer = maxIndex == 0 ? null : sequentialBuffer.getBuffer(maxIndex);
            VertexFormat.IndexType sharedIndexType = maxIndex == 0 ? null : sequentialBuffer.type();

            renderType.setupRenderState();

            try (RenderPass renderPass = RenderSystem.getDevice()
                                                     .createCommandEncoder()
                                                     .createRenderPass(colour, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                renderPass.setPipeline(renderPipeline);
                renderPass.drawMultipleIndexed(draws, sharedIndexBuffer, sharedIndexType);
            }

            renderType.clearRenderState();
        }

        RenderSystem.setShaderFog(oldFog);
    }

    public static void tick() {
        shapeInstances.removeIf(instance -> {
            if (instance.lifetime > 0) {
                instance.lifetime -= 1;
                if (instance.lifetime == 0) {
                    shapeInstancesByResourceLocation.values().remove(instance);
                    sortedShapeInstances = null;
                    instance.close();
                    return true;
                }
            }
            return false;
        });
    }


}
