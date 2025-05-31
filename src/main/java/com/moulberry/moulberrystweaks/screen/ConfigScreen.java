package com.moulberry.moulberrystweaks.screen;

import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends OptionsSubScreen {

    public ConfigScreen(Screen previous) {
        super(previous, Minecraft.getInstance().options, Component.literal("Moulberry's Tweaks Options"));
    }

    protected void addOptions() {
        if (this.list != null) {
            this.list.addSmall(MoulberrysTweaks.config.createOptionInstances());
        }
    }

    public void removed() {
        MoulberrysTweaks.config.saveToDefaultFolder();
    }

}
