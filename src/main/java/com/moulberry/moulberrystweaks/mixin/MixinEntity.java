package com.moulberry.moulberrystweaks.mixin;

import com.moulberry.moulberrystweaks.DebugMovementData;
import com.moulberry.moulberrystweaks.ext.LocalPlayerExt;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract boolean isSprinting();

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().baseTickVelocity = this.getDeltaMovement();
        }
    }

    @Inject(method = "moveRelative", at = @At("HEAD"))
    public void moveRelative(float speed, Vec3 inputs, CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().moveRelativeVelocity = this.getDeltaMovement();
            localPlayerExt.mt$getDebugMovementData().moveRelativeSpeed = speed;
            localPlayerExt.mt$getDebugMovementData().isSprinting = this.isSprinting();
            localPlayerExt.mt$getDebugMovementData().moveInput = inputs;

            localPlayerExt.mt$getDebugMovementData().hasSprintSpeedModifier = false;
            if ((Object) this instanceof LivingEntity livingEntity) {
                for (AttributeModifier modifier : livingEntity.getAttribute(Attributes.MOVEMENT_SPEED).getModifiers()) {
                    if (modifier.is(ResourceLocation.withDefaultNamespace("sprinting"))) {
                        localPlayerExt.mt$getDebugMovementData().hasSprintSpeedModifier = true;
                    }
                }
            }
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    public void move(CallbackInfo ci) {
        if (this instanceof LocalPlayerExt localPlayerExt) {
            localPlayerExt.mt$getDebugMovementData().moveVelocity = this.getDeltaMovement();
        }
    }

}
