package dev.turtywurty.industria.init;

import dev.turtywurty.industria.registry.ArmPositionRegistry;

public class ArmPositionInit {
    public static void init() {
        ArmPositionRegistry.register(stack -> stack.is(ItemInit.SEISMIC_SCANNER),
                (state, leftArm, rightArm) -> {
                    leftArm.skipDraw = false;
                    rightArm.skipDraw = false;

                    leftArm.xRot = (float) Math.toRadians(-30F);
                    leftArm.yRot = (float) Math.toRadians(20F);

                    rightArm.xRot = (float) Math.toRadians(-30F);
                    rightArm.yRot = (float) Math.toRadians(-20F);
                });
    }
}
