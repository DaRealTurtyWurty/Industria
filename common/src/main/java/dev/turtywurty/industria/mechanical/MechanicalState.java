package dev.turtywurty.industria.mechanical;

public record MechanicalState(
    double rpm,
    double sourceTorque,
    double requestedTorque,
    boolean stalled
) {
    public static final MechanicalState STOPPED = new MechanicalState(0, 0, 0, true);
}
