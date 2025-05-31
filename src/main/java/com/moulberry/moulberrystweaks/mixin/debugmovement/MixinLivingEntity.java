package com.moulberry.moulberrystweaks.mixin.debugmovement;

import com.moulberry.moulberrystweaks.ext.LocalPlayerExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity  {

    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void aiStep(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().livingAiStepVelocity = this.getDeltaMovement();
        }
    }

    @Inject(method = "travel", at = @At("HEAD"))
    public void travelHead(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().travelVelocity = this.getDeltaMovement();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    public void travelReturn(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().afterTravelVelocity = this.getDeltaMovement();
        }
    }

}
