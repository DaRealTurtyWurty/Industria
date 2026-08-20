package dev.turtywurty.industria.state;

public class WindTurbineRenderState extends IndustriaBlockEntityRenderState {
    public float propellerRotation = 0f;
    public long energyOutput = 0L;

    public WindTurbineRenderState() {
        super(0);
    }
}
