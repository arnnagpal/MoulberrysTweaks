package com.moulberry.moulberrystweaks.widget;

import com.google.common.hash.HashCode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.formatting.FormattedSnbtPrinter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.locale.Language;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.HashOps;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FloatingTextWidget {

    private static final int PADDING = 10;

    private final String name;
    protected Font font;
    private List<FormattedText> lines = null;
    private int windowX = Integer.MIN_VALUE;
    private int windowY = Integer.MIN_VALUE;
    private int windowWidth = 320;
    private int windowHeight = 240;
    private boolean automaticWidth = true;
    private int moveGrabOffsetX = Integer.MIN_VALUE;
    private int moveGrabOffsetY = Integer.MIN_VALUE;
    private int resizeGrabOffsetX = Integer.MIN_VALUE;
    private int resizeGrabOffsetY = Integer.MIN_VALUE;
    private double maxScrollOffset = 0.0;
    private double scrollOffset = 0.0;
    private boolean wasHovering = false;

    public FloatingTextWidget(String name) {
        this.name = name;
        this.font = Minecraft.getInstance().font;
    }

    public void setLines(List<FormattedText> lines) {
        if (lines == null) {
            this.close();
        } else {
            this.lines = lines;

            if (this.automaticWidth) {
                int maxWidth = 32;
                for (FormattedText line : this.lines) {
                    maxWidth = Math.max(maxWidth, this.font.width(line));
                }
                this.windowWidth = Math.max(32, maxWidth + PADDING*2);
            }

            this.updateScrollOffset(true);
        }
    }

    public void addLines(List<FormattedText> lines) {
        if (lines.isEmpty()) {
            return;
        }
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.addAll(lines);

        if (this.automaticWidth) {
            int maxWidth = 32;
            for (FormattedText line : lines) {
                maxWidth = Math.max(maxWidth, this.font.width(line));
            }
            this.windowWidth = Math.max(32, maxWidth + PADDING*2);
        }

        this.updateScrollOffset(false);
        this.scrollToBottom();
    }

    @Nullable
    public List<FormattedText> getLines() {
        return this.lines;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!isHovering(mouseX, mouseY)) {
            return false;
        }

        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.moveGrabOffsetX = this.windowX - (int) mouseX;
            this.moveGrabOffsetY = this.windowY - (int) mouseY;
            this.resizeGrabOffsetX = Integer.MIN_VALUE;
            this.resizeGrabOffsetY = Integer.MIN_VALUE;
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.resizeGrabOffsetX = this.windowX + this.windowWidth - (int) mouseX;
            this.resizeGrabOffsetY = this.windowY + this.windowHeight - (int) mouseY;
            this.moveGrabOffsetX = Integer.MIN_VALUE;
            this.moveGrabOffsetY = Integer.MIN_VALUE;
        }

        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.moveGrabOffsetX != Integer.MIN_VALUE || this.moveGrabOffsetY != Integer.MIN_VALUE) {
                this.moveGrabOffsetX = Integer.MIN_VALUE;
                this.moveGrabOffsetY = Integer.MIN_VALUE;
                return true;
            }
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (this.resizeGrabOffsetX != Integer.MIN_VALUE || this.resizeGrabOffsetY != Integer.MIN_VALUE) {
                this.resizeGrabOffsetX = Integer.MIN_VALUE;
                this.resizeGrabOffsetY = Integer.MIN_VALUE;
                return true;

            }
        }
        return false;
    }

    public boolean isOpen() {
        return this.lines != null && this.windowX != Integer.MIN_VALUE && this.windowY != Integer.MIN_VALUE;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return this.isOpen() && mouseX >= this.windowX && mouseY >= this.windowY && mouseX <= this.windowX+this.windowWidth && mouseY <= this.windowY+this.windowHeight;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!this.wasHovering) {
            return false;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= this.windowX && mouseY >= this.windowY && mouseX <= this.windowX+this.windowWidth && mouseY <= this.windowY+this.windowHeight) {
            this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.scrollOffset - scrollY * 16));
            return true;
        }
        return false;
    }

    public void close() {
        this.windowX = Integer.MIN_VALUE;
        this.windowY = Integer.MIN_VALUE;
        this.lines = null;
        this.windowWidth = 320;
        this.windowHeight = 240;
        this.wasHovering = false;
        this.updateScrollOffset(true);

        this.moveGrabOffsetX = Integer.MIN_VALUE;
        this.moveGrabOffsetY = Integer.MIN_VALUE;
        this.resizeGrabOffsetX = Integer.MIN_VALUE;
        this.resizeGrabOffsetY = Integer.MIN_VALUE;
    }

    public void scrollToBottom() {
        this.scrollOffset = this.maxScrollOffset;
    }

    private void updateScrollOffset(boolean reset) {
        if (this.lines == null) {
            this.maxScrollOffset = this.scrollOffset = 0.0;
            return;
        }

        boolean wasMaxScrolled = this.scrollOffset >= this.maxScrollOffset;
        this.maxScrollOffset = Math.max(0, this.lines.size() * this.font.lineHeight - (this.windowHeight - PADDING*2));
        if (reset) {
            this.scrollOffset = 0.0;
        } else if (wasMaxScrolled) {
            this.scrollOffset = this.maxScrollOffset;
        } else {
            this.scrollOffset = Math.min(this.maxScrollOffset, this.scrollOffset);
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.wasHovering = this.isHovering(mouseX, mouseY);
        if (this.lines != null) {
            if (this.windowX == Integer.MIN_VALUE) {
                this.windowX = mouseX;
            }
            if (this.windowY == Integer.MIN_VALUE) {
                this.windowY = mouseY;
            }
            if (this.moveGrabOffsetX != Integer.MIN_VALUE && this.moveGrabOffsetY != Integer.MIN_VALUE) {
                this.windowX = mouseX + this.moveGrabOffsetX;
                this.windowY = mouseY + this.moveGrabOffsetY;
            }
            if (this.resizeGrabOffsetX != Integer.MIN_VALUE && this.resizeGrabOffsetY != Integer.MIN_VALUE) {
                this.windowWidth = Math.max(32, mouseX + this.resizeGrabOffsetX - this.windowX);
                this.windowHeight = Math.max(24, mouseY + this.resizeGrabOffsetY - this.windowY);
                this.automaticWidth = false;
                this.updateScrollOffset(false);
            }

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 1000);

            guiGraphics.enableScissor(this.windowX + 6, this.windowY + 6, this.windowX+this.windowWidth - 6, this.windowY+this.windowHeight - 6);

            int y = this.windowY + PADDING - (int) this.scrollOffset;
            int maxX = 0;

            int minLineIndex = (int) Math.floor((this.scrollOffset - PADDING) / 9);
            if (minLineIndex < 0) minLineIndex = 0;

            int maxLineIndex = minLineIndex + this.windowHeight/9;
            y += this.font.lineHeight * minLineIndex;

            if (maxLineIndex > this.lines.size()-1) maxLineIndex = this.lines.size()-1;

            Style hoveredStyle = null;

            boolean showTooltip = this.wasHovering && this.moveGrabOffsetX == Integer.MIN_VALUE && this.resizeGrabOffsetX == Integer.MIN_VALUE;

            for (int i = minLineIndex; i <= maxLineIndex; i++) {
                FormattedText line = this.lines.get(i);

                if (showTooltip && mouseY >= y && mouseY < y + this.font.lineHeight) {
                    hoveredStyle = this.font.getSplitter().componentStyleAtWidth(line, mouseX - (this.windowX + PADDING));
                }

                int lineWidth = guiGraphics.drawString(this.font, Language.getInstance().getVisualOrder(line), this.windowX + PADDING, y, -1);
                y += this.font.lineHeight;
                maxX = Math.max(maxX, lineWidth);
            }

            guiGraphics.disableScissor();

            guiGraphics.blitSprite(RenderType::guiTextured, ResourceLocation.fromNamespaceAndPath("minecraft", "popup/background"),
                this.windowX, this.windowY, this.windowWidth, this.windowHeight);

            guiGraphics.drawString(this.font, this.name, this.windowX+10, this.windowY-this.font.lineHeight, -1);

            if (hoveredStyle != null) {
                guiGraphics.renderComponentHoverEffect(this.font, hoveredStyle, mouseX, mouseY);
            }

            poseStack.popPose();
        }
    }

}
