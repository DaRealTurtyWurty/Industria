package dev.turtywurty.industria.blockentity.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public record FluidStack(FluidVariant fluid, long amount) {
}
