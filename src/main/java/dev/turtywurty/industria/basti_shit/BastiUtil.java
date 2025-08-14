package dev.turtywurty.industria.basti_shit;

public class BastiUtil {

    public static float map(float value, float fromStart, float fromEnd, float toStart, float toEnd) {
        return toStart + (value - fromStart) * (toEnd - toStart) / (fromEnd - fromStart);
    }
}
