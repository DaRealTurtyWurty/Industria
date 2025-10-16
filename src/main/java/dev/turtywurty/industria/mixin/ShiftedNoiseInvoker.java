package dev.turtywurty.industria.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DensityFunctionTypes.ShiftedNoise.class)
public interface ShiftedNoiseInvoker {
    @Invoker("<init>")
    static DensityFunctionTypes.ShiftedNoise createShiftedNoise(DensityFunction shiftX,
                                                                DensityFunction shiftY,
                                                                DensityFunction shiftZ,
                                                                double xzScale, double yScale,
                                                                DensityFunction.Noise noise) {
        throw new AssertionError();
    }
}
