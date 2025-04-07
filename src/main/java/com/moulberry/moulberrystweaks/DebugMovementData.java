package com.moulberry.moulberrystweaks;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DebugMovementData {

    public Vec3 baseTickVelocity;
    public Vec3 localPlayerAiStepVelocity;
    public Vec3 livingAiStepVelocity;
    public Vec3 travelVelocity;
    public Vec3 moveRelativeVelocity;
    public Vec3 moveVelocity;
    public Vec3 afterTravelVelocity;
    public Vec3 moveInput;
    public float moveRelativeSpeed;
    public boolean isInWater;
    public boolean isInLava;
    public boolean isSwimming;
    public boolean isSprinting;
    public boolean hasSprintSpeedModifier;

}
