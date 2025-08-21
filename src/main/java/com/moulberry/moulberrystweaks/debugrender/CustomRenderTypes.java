package com.moulberry.moulberrystweaks.debugrender;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.Function;

public class CustomRenderTypes {

    public record RenderParams(boolean depth, boolean wireframe, boolean cull) {}

    public static RenderType debugTriangleStrip(boolean depth, boolean wireframe, boolean cull) {
        return DEBUG_TRIANGLE_STRIP.apply(new RenderParams(depth, wireframe, cull && !wireframe));
        // debugrender:add -> sphere, cube, line_strip
        // debugrender:remove
        // debugrender:clear
        // debugrender:clear_namespace
    }

    private static final Function<RenderParams, RenderType> DEBUG_TRIANGLE_STRIP = Util.memoize(params -> {
        String name = "debug_triangle_strip";
        if (!params.depth) {
            name += "_nodepth";
        }
        if (!params.cull) {
            name += "_nocull";
        }
        if (params.wireframe) {
            name += "_wireframe";
        }
        var pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                          .withLocation(ResourceLocation.fromNamespaceAndPath("moulberrystweaks", "pipeline/"+name))
                          .withDepthTestFunction(params.depth ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST)
                          .withPolygonMode(params.wireframe ? PolygonMode.WIREFRAME : PolygonMode.FILL)
                          .withCull(params.cull)
                          .withDepthWrite(!params.wireframe)
                          .withDepthBias(-1, -1)
                          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                          .build()
        );

        return RenderType.create(
            "moulberrystweaks/"+name,
            1536,
            pipeline,
            RenderType.CompositeState.builder().createCompositeState(false)
        );
    });

    public static final Function<Double, RenderType.CompositeRenderType> DEBUG_LINE = Util.memoize(width -> RenderType.create(
        "moulberrystweaks/debug_line",
        1536,
        RenderPipelines.LINES,
        RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width))).createCompositeState(false)
    ));


    private static final RenderPipeline PIPELINE_LINES_WITHOUT_DEPTH = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                      .withLocation(ResourceLocation.fromNamespaceAndPath("moulberrystweaks", "pipeline/lines_without_depth"))
                      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                      .build()
    );

    public static final Function<Double, RenderType.CompositeRenderType> DEBUG_LINE_WITHOUT_DEPTH = Util.memoize(width -> RenderType.create(
        "moulberrystweaks/debug_line_without_depth",
        1536,
        PIPELINE_LINES_WITHOUT_DEPTH,
        RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width))).createCompositeState(false)
    ));

    public static final Function<Double, RenderType.CompositeRenderType> LINE_STRIP = Util.memoize(width -> RenderType.create(
        "moulberrystweaks/line_strip",
        1536,
        RenderPipelines.LINE_STRIP,
        RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width))).setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING).createCompositeState(false)
    ));

    private static final RenderPipeline PIPELINE_LINE_STRIP_WITHOUT_DEPTH = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                      .withLocation(ResourceLocation.fromNamespaceAndPath("moulberrystweaks", "pipeline/line_strip_without_depth"))
                      .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINE_STRIP)
                      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                      .build()
    );

    public static final Function<Double, RenderType.CompositeRenderType> LINE_STRIP_WITHOUT_DEPTH = Util.memoize(width -> RenderType.create(
        "moulberrystweaks/line_strip_without_depth",
        1536,
        PIPELINE_LINE_STRIP_WITHOUT_DEPTH,
        RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width))).setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING).createCompositeState(false)
    ));

    private static final RenderPipeline PIPELINE_DEBUG_LINE_STRIP_WITHOUT_DEPTH = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                      .withLocation(ResourceLocation.fromNamespaceAndPath("moulberrystweaks", "pipeline/debug_line_strip_without_depth"))
                      .withLocation("pipeline/debug_line_strip")
                      .withVertexShader("core/position_color")
                      .withFragmentShader("core/position_color")
                      .withCull(false)
                      .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                      .build()
    );

    public static final Function<Double, RenderType.CompositeRenderType> DEBUG_LINE_STRIP_WITHOUT_DEPTH = Util.memoize(width -> RenderType.create(
        "moulberrystweaks/debug_line_strip_without_depth",
        1536,
        PIPELINE_DEBUG_LINE_STRIP_WITHOUT_DEPTH,
        RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width))).setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING).createCompositeState(false)
    ));


}
