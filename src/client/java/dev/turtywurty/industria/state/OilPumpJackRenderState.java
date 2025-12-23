package dev.turtywurty.industria.state;

public class OilPumpJackRenderState extends IndustriaBlockEntityRenderState {
    public float clientRotation = 0f;
    public boolean isRunning = false;
    public boolean reverseCounterWeights = false;

    public OilPumpJackRenderState() {
        super(0);
    }
}
