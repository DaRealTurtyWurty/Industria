package dev.turtywurty.industria.util;

import dev.turtywurty.turtymultiloader.transfer.unit.Units;

/**
 * Common fluid quantities expressed in multiloaderlib's neutral droplet unit.
 */
public final class FluidAmounts {
    public static final long DROPLET = Units.FLUID_DROPLET.numerator();
    public static final long NUGGET = Units.FLUID_BUCKET.numerator() / 81;
    public static final long INGOT = Units.FLUID_BUCKET.numerator() / 9;
    public static final long BOTTLE = Units.FLUID_BUCKET.numerator() / 3;
    public static final long BUCKET = Units.FLUID_BUCKET.numerator();
    public static final long BLOCK = Units.FLUID_BUCKET.numerator() * 9;

    private FluidAmounts() {
    }
}
