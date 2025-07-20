package com.moulberry.moulberrystweaks;

import com.moulberry.lattice.Lattice;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class MoulberrysTweaksModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return oldScreen -> Lattice.createConfigScreen(MoulberrysTweaks.configElements, MoulberrysTweaks.config::saveToDefaultFolder, oldScreen);
    }

}
