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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.locks.ReentrantLock;

public class DebugRenderManager {

    public static final ReentrantLock lock = new ReentrantLock();

    private static boolean allHidden = false;
    public static final LinkedHashSet<String> availableNamespaces = new LinkedHashSet<>();
    public static final LinkedHashSet<String> hiddenNamespaces = new LinkedHashSet<>();

    private static final Map<ResourceLocation, RenderedShapeInstance> shapeInstancesByResourceLocation = new LinkedHashMap<>();
    private static final List<RenderedShapeInstance> shapeInstances = new ArrayList<>();

    public static volatile boolean updateSortedShapeInstances = false;
    private static final List<RenderedShapeInstance> pendingCloseOnRenderThread = new ArrayList<>();
    private static final EnumMap<DebugShape.RenderMethod, List<RenderedShapeInstance>> sortedShapeInstancesByRenderMethod = new EnumMap<>(DebugShape.RenderMethod.class);
    private static Vec3 lastSortedPosition = Vec3.ZERO;

    public static void add(Optional<ResourceLocation> resourceLocationOptional, DebugShape debugShape, int flags, int lifetime) {
        lock.lock();
        try {
            RenderedShapeInstance renderedShapeInstance = new RenderedShapeInstance(resourceLocationOptional.orElse(null), debugShape, flags, lifetime);

            if (resourceLocationOptional.isPresent()) {
                ResourceLocation resourceLocation = resourceLocationOptional.get();

                availableNamespaces.add(resourceLocation.getNamespace());

                RenderedShapeInstance old = shapeInstancesByResourceLocation.get(resourceLocation);
                if (old != null) {
                    shapeInstances.remove(old);
                    old.close();
                }

                shapeInstancesByResourceLocation.put(resourceLocation, renderedShapeInstance);
            }
            shapeInstances.add(renderedShapeInstance);
            updateSortedShapeInstances = true;
        } finally {
            lock.unlock();
        }
    }

    public static void remove(ResourceLocation resourceLocation) {
        lock.lock();
        try {
            RenderedShapeInstance instance = shapeInstancesByResourceLocation.remove(resourceLocation);
            if (instance != null) {
                shapeInstances.remove(instance);
                updateSortedShapeInstances = true;
                instance.close();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void clear() {
        lock.lock();
        try {
            if (RenderSystem.isOnRenderThread()) {
                shapeInstances.forEach(RenderedShapeInstance::close);
            } else {
                pendingCloseOnRenderThread.addAll(shapeInstances);
            }
            shapeInstances.clear();
            updateSortedShapeInstances = true;
            shapeInstancesByResourceLocation.clear();
            availableNamespaces.clear();
        } finally {
            lock.unlock();
        }

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
        lock.lock();
        try {
            boolean isOnRenderThread = RenderSystem.isOnRenderThread();
            shapeInstancesByResourceLocation.entrySet().removeIf(entry -> {
                if (entry.getKey().getNamespace().equals(namespace)) {
                    RenderedShapeInstance instance = entry.getValue();
                    shapeInstances.remove(instance);
                    updateSortedShapeInstances = true;
                    if (isOnRenderThread) {
                        instance.close();
                    } else {
                        pendingCloseOnRenderThread.add(instance);
                    }
                    return true;
                } else {
                    return false;
                }
            });
            availableNamespaces.remove(namespace);
        } finally {
            lock.unlock();
        }
    }

    public static boolean hasShapesToRender() {
        return !shapeInstances.isEmpty() && !allHidden;
    }

    public static void renderF3Text(List<String> list, boolean left) {
        if (!RenderSystem.isOnRenderThread()) {
            return;
        }

        if (!pendingCloseOnRenderThread.isEmpty()) {
            pendingCloseOnRenderThread.forEach(RenderedShapeInstance::close);
            pendingCloseOnRenderThread.clear();
        }

        updateRenderLists(null);

        List<RenderedShapeInstance> f3Text = sortedShapeInstancesByRenderMethod.get(left ? DebugShape.RenderMethod.F3_TEXT_LEFT : DebugShape.RenderMethod.F3_TEXT_RIGHT);
        if (f3Text != null && !f3Text.isEmpty()) {
            list.add("");
            for (RenderedShapeInstance instance : f3Text) {
                instance.renderF3Text(list);
            }
        }
    }

    public static void renderGui(GuiGraphics guiGraphics) {
        RenderSystem.assertOnRenderThread();

        if (!pendingCloseOnRenderThread.isEmpty()) {
            pendingCloseOnRenderThread.forEach(RenderedShapeInstance::close);
            pendingCloseOnRenderThread.clear();
        }

        // Disable rendering gui elements while the debug screen is active
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            return;
        }

        updateRenderLists(null);

        List<RenderedShapeInstance> guiImmediate = sortedShapeInstancesByRenderMethod.get(DebugShape.RenderMethod.GUI_IMMEDIATE);
        if (guiImmediate != null && !guiImmediate.isEmpty()) {
            GuiRenderContext context = new GuiRenderContext();

            for (RenderedShapeInstance instance : guiImmediate) {
                instance.renderGuiImmediate(guiGraphics, context);
            }
        }
    }

    public static void renderWorld(PoseStack poseStack, Camera camera) {
        RenderSystem.assertOnRenderThread();

        if (!pendingCloseOnRenderThread.isEmpty()) {
            pendingCloseOnRenderThread.forEach(RenderedShapeInstance::close);
            pendingCloseOnRenderThread.clear();
        }

        updateRenderLists(camera);

        List<RenderedShapeInstance> worldImmediate = sortedShapeInstancesByRenderMethod.get(DebugShape.RenderMethod.WORLD_IMMEDIATE);
        if (worldImmediate != null && !worldImmediate.isEmpty()) {
            MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

            for (RenderedShapeInstance instance : worldImmediate) {
                instance.renderWorldImmediate(poseStack, multiBufferSource, camera);
            }
        }

        List<RenderedShapeInstance> worldCached = sortedShapeInstancesByRenderMethod.get(DebugShape.RenderMethod.WORLD_CACHED);
        if (worldCached != null && !worldCached.isEmpty()) {
            FogParameters oldFog = RenderSystem.getShaderFog();
            RenderSystem.setShaderFog(FogParameters.NO_FOG);

            float[] modelViewMat = poseStack.last().pose().get(new float[16]);

            LinkedHashMap<RenderType, List<RenderPass.Draw>> drawsForRenderType = new LinkedHashMap<>();

            for (RenderedShapeInstance instance : worldCached) {
                if (instance.resourceLocation != null && hiddenNamespaces.contains(instance.resourceLocation.getNamespace())) {
                    continue;
                }

                instance.renderWorldCached(camera, modelViewMat, (draw, renderType) -> {
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
    }

    private static void updateRenderLists(@Nullable Camera camera) {
        boolean resortDueToMovement = camera != null && camera.getPosition().distanceToSqr(lastSortedPosition) > 0.25*0.25;
        boolean updateDueToAddOrRemove = updateSortedShapeInstances;
        updateSortedShapeInstances = false;

        if (updateDueToAddOrRemove || resortDueToMovement) {
            lock.lock();
            try {
                if (updateDueToAddOrRemove) {
                    sortedShapeInstancesByRenderMethod.clear();
                    for (RenderedShapeInstance instance : shapeInstances) {
                        sortedShapeInstancesByRenderMethod.computeIfAbsent(instance.renderMethod, k -> new ArrayList<>()).add(instance);
                    }
                }
                if (camera != null) {
                    lastSortedPosition = camera.getPosition();
                }
                Comparator<RenderedShapeInstance> comparator = Comparator.<RenderedShapeInstance>comparingDouble(instance -> -lastSortedPosition.distanceToSqr(instance.center))
                    .thenComparing(instance -> Objects.toString(instance.resourceLocation));
                for (List<RenderedShapeInstance> list : sortedShapeInstancesByRenderMethod.values()) {
                    list.sort(comparator);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public static void tick() {
        lock.lock();
        try {
            if (RenderSystem.isOnRenderThread() && !pendingCloseOnRenderThread.isEmpty()) {
                pendingCloseOnRenderThread.forEach(RenderedShapeInstance::close);
                pendingCloseOnRenderThread.clear();
            }

            shapeInstances.removeIf(instance -> {
                if (instance.lifetime > 0) {
                    instance.lifetime -= 1;
                    if (instance.lifetime == 0) {
                        shapeInstancesByResourceLocation.values().remove(instance);
                        updateSortedShapeInstances = true;
                        instance.close();
                        return true;
                    }
                }
                return false;
            });
        } finally {
            lock.unlock();
        }
    }


}
