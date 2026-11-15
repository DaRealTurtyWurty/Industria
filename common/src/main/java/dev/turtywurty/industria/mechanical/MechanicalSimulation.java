package dev.turtywurty.industria.mechanical;

import java.util.List;

public final class MechanicalSimulation {
    public static final double SECONDS_PER_TICK = 1.0 / 20.0;
    public static final double RADIANS_PER_RPM = Math.TAU / 60.0;

    // every connected network has the same inertia for now.
    private static final double INERTIA = 4.0;
    private static final double FRICTION_PER_RPM = 0.02;
    private static final double STOPPED_RPM = 0.0001;
    private static final int MIN_SUBSTEPS = 16;
    private static final int MAX_SUBSTEPS = 256;

    private double rpm;
    private double rotationDelta;
    private MechanicalState state;

    public MechanicalSimulation(double initialRPM) {
        if (!Double.isFinite(initialRPM))
            throw new IllegalArgumentException("Initial RPM must be finite");

        this.rpm = initialRPM;
        this.state = new MechanicalState(initialRPM, 0.0, 0.0, Math.abs(initialRPM) < STOPPED_RPM);
    }

    public MechanicalState getState() {
        return state;
    }

    public double getRotationDelta() {
        return rotationDelta;
    }

    public void tick(List<MechanicalSource> sources, List<MechanicalConsumer> consumers) {
        rotationDelta = 0.0;
        int steps = substeps(sources, consumers);
        double dt = SECONDS_PER_TICK / steps;
        double rpmPerTorque = dt / (INERTIA * RADIANS_PER_RPM);
        double[] demands = new double[consumers.size()];
        double[] torqueIntegrals = new double[consumers.size()];
        double[] work = new double[consumers.size()];

        for (int step = 0; step < steps; step++) {
            double sourceTorque = sourceTorque(sources, rpm);
            double direction = Math.signum(rpm == 0.0 ? sourceTorque : rpm);
            double required = 0.0;
            double flexible = 0.0;
            for (int i = 0; i < consumers.size(); i++) {
                MechanicalConsumer consumer = consumers.get(i);
                demands[i] = Math.max(0.0, consumer.getRequiredTorque(rpm));
                if (consumer.getLoadPolicy() == MechanicalConsumer.LoadPolicy.REQUIRE_FULL_TORQUE) {
                    required += demands[i];
                } else {
                    flexible += demands[i];
                }
            }

            if (direction == 0.0)
                continue;

            double friction = FRICTION_PER_RPM * Math.abs(rpm);
            double budget = Math.max(0.0, direction * sourceTorque - friction + Math.abs(rpm) / rpmPerTorque);
            boolean blocked = required > budget;
            double flexibleScale = flexible > 0.0
                ? Math.min(1.0, Math.max(0.0, budget - required) / flexible)
                : 0.0;
            double load = blocked ? budget : required + flexible * flexibleScale;
            double nextRPM = rpm + (sourceTorque - direction * (load + friction)) * rpmPerTorque;
            double duration = dt;
            if (direction * nextRPM < 0.0) {
                duration = rpm == 0.0 ? 0.0 : dt * rpm / (rpm - nextRPM);
                nextRPM = 0.0;
            }

            double angle = (Math.abs(rpm) + Math.abs(nextRPM)) * 0.5 * RADIANS_PER_RPM * duration;
            if (!blocked) {
                for (int i = 0; i < consumers.size(); i++) {
                    double allocated = demands[i];
                    if (consumers.get(i).getLoadPolicy() == MechanicalConsumer.LoadPolicy.ACCEPT_PARTIAL_TORQUE) {
                        allocated *= flexibleScale;
                    }

                    torqueIntegrals[i] += allocated * duration;
                    work[i] += allocated * angle;
                }
            }

            rotationDelta += (rpm + nextRPM) * 0.5 * RADIANS_PER_RPM * duration;
            rpm = nextRPM;
        }

        if (Math.abs(rpm) < STOPPED_RPM) {
            rpm = 0.0;
        }

        double requested = 0.0;
        for (MechanicalConsumer consumer : consumers) {
            requested += Math.max(0.0, consumer.getRequiredTorque(rpm));
        }
        state = new MechanicalState(rpm, sourceTorque(sources, rpm), requested, rpm == 0.0);

        for (int i = 0; i < consumers.size(); i++) {
            consumers.get(i).onMechanicalUpdate(rpm, torqueIntegrals[i] / SECONDS_PER_TICK, work[i]);
        }
    }

    private int substeps(List<MechanicalSource> sources, List<MechanicalConsumer> consumers) {
        double probe = rpm + (rpm < 0.0 ? -1.0 : 1.0);
        double slope = FRICTION_PER_RPM;
        for (MechanicalSource source : sources) {
            slope += Math.abs(source.getTorqueAtRPM(probe) - source.getTorqueAtRPM(rpm));
        }

        for (MechanicalConsumer consumer : consumers) {
            slope += Math.abs(consumer.getRequiredTorque(probe) - consumer.getRequiredTorque(rpm));
        }

        return Math.clamp(
            (int) Math.ceil(4.0 * slope * SECONDS_PER_TICK / (INERTIA * RADIANS_PER_RPM)),
            MIN_SUBSTEPS, MAX_SUBSTEPS);
    }

    private static double sourceTorque(List<MechanicalSource> sources, double shaftRPM) {
        double torque = 0.0;
        for (MechanicalSource source : sources) {
            torque += source.getTorqueAtRPM(shaftRPM);
        }

        return torque;
    }
}
