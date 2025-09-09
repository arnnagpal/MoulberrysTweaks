package com.moulberry.moulberrystweaks.mixin.inventorywidgets;

import com.moulberry.moulberrystweaks.widget.ActiveWidgets;
import com.moulberry.moulberrystweaks.widget.ComponentViewerWidget;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import com.moulberry.moulberrystweaks.widget.FloatingTextWidget;
import com.moulberry.moulberrystweaks.widget.PacketViewerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Inject(method = "isHovering(IIIIDD)Z", at = @At("HEAD"), cancellable = true)
    public void isHovering(int x, int y, int width, int height, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
            if (widget.isHovering(mouseX, mouseY)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int key, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
            if (widget.keyPressed(key, scancode, modifiers)) {
                cir.setReturnValue(true);
                return;
            }
        }
        if (MoulberrysTweaks.viewComponentsKeyBind != null && MoulberrysTweaks.viewComponentsKeyBind.matches(key, scancode)) {
            for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
                if (widget instanceof ComponentViewerWidget componentViewerWidget) {
                    if (this.hoveredSlot != null) {
                        componentViewerWidget.setItemStack(this.hoveredSlot.getItem().copy());
                    } else {
                        componentViewerWidget.setItemStack(null);
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
            if (this.hoveredSlot != null) {
                ComponentViewerWidget componentViewerWidget = new ComponentViewerWidget();
                componentViewerWidget.setItemStack(this.hoveredSlot.getItem().copy());
                ActiveWidgets.activeWidgets.add(componentViewerWidget);
            }
            cir.setReturnValue(true);
        }
        if (MoulberrysTweaks.viewPacketsKeyBind != null && MoulberrysTweaks.viewPacketsKeyBind.matches(key, scancode)) {
            boolean removed = ActiveWidgets.activeWidgets.removeIf(widget -> widget instanceof PacketViewerWidget);
            if (removed) {
                ActiveWidgets.logPackets = false;
            } else {
                System.out.println("Adding packet viewer");
                ActiveWidgets.activeWidgets.add(new PacketViewerWidget());
                ActiveWidgets.logPackets = true;
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        for (FloatingTextWidget widget : ActiveWidgets.activeWidgets) {
            if (widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void afterRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float f, CallbackInfo ci) {
        // Render in reverse order so that the first widget renders on top
        for (int i = ActiveWidgets.activeWidgets.size()-1; i >= 0; i--) {
            FloatingTextWidget widget = ActiveWidgets.activeWidgets.get(i);
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }
}
