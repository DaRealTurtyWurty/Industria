package dev.turtywurty.industria.mechanical;

public interface MechanicalSource {
    double getTorqueAtRPM(double shaftRPM);

    default void onShaftUpdate(double shaftRPM, double rotationDelta) {
    }
}
