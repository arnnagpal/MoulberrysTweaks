package com.moulberry.moulberrystweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TransferableSelectionList.PackEntry.class)
public class MixinTransferableSelectionListPackEntry {

    @WrapOperation(method = "handlePackSelection", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z"))
    public boolean handlePackSelection_isCompatible(PackCompatibility instance, Operation<Boolean> original) {
        if (MoulberrysTweaks.config.resourcePack.disableWarnings) {
            return true;
        }
        return original.call(instance);
    }

}
