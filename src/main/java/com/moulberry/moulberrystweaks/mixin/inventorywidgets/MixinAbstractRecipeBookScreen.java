package com.moulberry.moulberrystweaks.mixin.inventorywidgets;

import com.moulberry.moulberrystweaks.widget.ActiveWidgets;
import com.moulberry.moulberrystweaks.widget.FloatingTextWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public class MixinAbstractRecipeBookScreen {
    @Inject(method = "render", at = @At("RETURN"))
    public void afterRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float f, CallbackInfo ci) {
        // Render in reverse order so that the first widget renders on top
        for (int i = ActiveWidgets.activeWidgets.size()-1; i >= 0; i--) {
            FloatingTextWidget widget = ActiveWidgets.activeWidgets.get(i);
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }
}
