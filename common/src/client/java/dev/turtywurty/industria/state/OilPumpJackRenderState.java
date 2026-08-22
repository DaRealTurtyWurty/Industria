package dev.turtywurty.industria.state;

public class OilPumpJackRenderState extends IndustriaBlockEntityRenderState {
    public float clientRotation = 0f;
    public boolean isRunning = false;
    public boolean reverseCounterWeights = false;
    public float wheelPitch = 0f;
    public float counterWeightsPitch = 0f;
    public float pitmanArmPitch = 0f;
    public float armPitch = 0f;

    public OilPumpJackRenderState() {
        super(0);
    }
}
