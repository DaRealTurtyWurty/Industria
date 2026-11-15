package dev.turtywurty.industria.mechanical;

public interface MechanicalConsumer {
    double getRequiredTorque(double rpm);

    default LoadPolicy getLoadPolicy() {
        return LoadPolicy.REQUIRE_FULL_TORQUE;
    }

    void onMechanicalUpdate(double rpm, double allocatedTorque, double work);

    enum LoadPolicy {
        REQUIRE_FULL_TORQUE,
        ACCEPT_PARTIAL_TORQUE
    }
}
