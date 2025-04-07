package com.moulberry.moulberrystweaks.ext;

import com.moulberry.moulberrystweaks.DebugMovementData;

public interface LocalPlayerExt {

    float mt$getVisualAttackStrengthScale(float partialTick);
    void mt$resetVisualAttackStrengthScale();
    void mt$incrementVisualAttackStrengthScale();

    DebugMovementData mt$getDebugMovementData();

}
