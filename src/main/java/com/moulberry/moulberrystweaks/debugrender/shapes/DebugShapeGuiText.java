package com.moulberry.moulberrystweaks.debugrender.shapes;

import com.moulberry.moulberrystweaks.debugrender.GuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public record DebugShapeGuiText(List<Component> components, byte location) implements DebugShape {

    public static final byte LOCATION_TOP_LEFT = 0;
    public static final byte LOCATION_TOP_RIGHT = 1;
    public static final byte LOCATION_BOTTOM_LEFT = 2;
    public static final byte LOCATION_BOTTOM_RIGHT = 3;
    public static final byte LOCATION_F3_LEFT = 4;
    public static final byte LOCATION_F3_RIGHT = 5;

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugShapeGuiText> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list()),
        DebugShapeGuiText::components,
        ByteBufCodecs.BYTE,
        DebugShapeGuiText::location,
        DebugShapeGuiText::new
    );

    @Override
    public Vec3 center() {
        return Vec3.ZERO;
    }

    @Override
    public RenderMethod renderMethod() {
        if (this.location == LOCATION_F3_LEFT) {
            return RenderMethod.F3_TEXT_LEFT;
        } else if (this.location == LOCATION_F3_RIGHT) {
            return RenderMethod.F3_TEXT_RIGHT;
        } else {
            return RenderMethod.GUI_IMMEDIATE;
        }
    }

    @Override
    public void renderF3Text(List<String> list, int flags) {
        for (Component component : this.components) {
            list.addAll(Arrays.asList(component.getString().split("\n")));
        }
    }

    @Override
    public void renderGuiImmediate(GuiGraphics guiGraphics, GuiRenderContext context, int flags) {
        Font font = Minecraft.getInstance().font;
        int screenWidth = guiGraphics.guiWidth();
        int maxWidth = screenWidth*5/4;

        if (this.location != LOCATION_TOP_LEFT && this.location != LOCATION_TOP_RIGHT && this.location != LOCATION_BOTTOM_LEFT && this.location != LOCATION_BOTTOM_RIGHT) {
            return;
        }
        if (this.location == LOCATION_BOTTOM_LEFT && Minecraft.getInstance().screen instanceof ChatScreen) {
            return;
        }

        for (Component component : this.components) {
            for (FormattedCharSequence line : font.split(component, maxWidth)) {
                int x;
                int y;
                switch (this.location) {
                    case LOCATION_TOP_LEFT ->  {
                        x = 2;
                        y = 2 + context.topLeftLines * font.lineHeight;
                        context.topLeftLines += 1;
                    }
                    case LOCATION_TOP_RIGHT -> {
                        x = guiGraphics.guiWidth() - 2 - font.width(line);
                        y = 2 + context.topRightLines * font.lineHeight;
                        context.topRightLines += 1;
                    }
                    case LOCATION_BOTTOM_LEFT ->  {
                        x = 2;
                        y = guiGraphics.guiHeight() - (2 + context.bottomLeftLines * font.lineHeight) - font.lineHeight;
                        context.bottomLeftLines += 1;
                    }
                    case LOCATION_BOTTOM_RIGHT -> {
                        x = guiGraphics.guiWidth() - 2 - font.width(line);
                        y = guiGraphics.guiHeight() - (2 + context.bottomRightLines * font.lineHeight) - font.lineHeight;
                        context.bottomRightLines += 1;
                    }
                    default -> {
                        return;
                    }
                }
                guiGraphics.drawString(font, line, x, y, -1);
            }
        }
    }

}
