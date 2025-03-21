package dev.turtywurty.industria.init;

import dev.turtywurty.industria.registry.ArmPositionRegistry;

public class ArmPositionInit {
    public static void init() {
        ArmPositionRegistry.register(stack -> stack.isOf(ItemInit.SEISMIC_SCANNER),
                (state, leftArm, rightArm) -> {
                    leftArm.hidden = false;
                    rightArm.hidden = false;

                    leftArm.pitch = (float) Math.toRadians(-30F);
                    leftArm.yaw = (float) Math.toRadians(20F);

                    rightArm.pitch = (float) Math.toRadians(-30F);
                    rightArm.yaw = (float) Math.toRadians(-20F);
                });
    }
}
