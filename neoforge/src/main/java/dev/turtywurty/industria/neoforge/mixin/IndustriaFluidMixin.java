package dev.turtywurty.industria.neoforge.mixin;

import dev.turtywurty.industria.fluid.IndustriaFluid;
import dev.turtywurty.industria.neoforge.NeoForgeFluidTypes;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IndustriaFluid.class)
public abstract class IndustriaFluidMixin extends FlowingFluid {
    @Override
    public FluidType getFluidType() {
        return NeoForgeFluidTypes.get((IndustriaFluid) (Object) this);
    }
}
