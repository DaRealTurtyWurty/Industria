package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.list.TagList;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaFluidTagProvider extends FabricTagProvider.FluidTagProvider {
    public IndustriaFluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(TagList.Fluids.CRUDE_OIL)
                .add(FluidInit.CRUDE_OIL.still())
                .add(FluidInit.CRUDE_OIL.flowing());

        valueLookupBuilder(TagList.Fluids.DIRTY_SODIUM_ALUMINATE)
                .add(FluidInit.DIRTY_SODIUM_ALUMINATE.still())
                .add(FluidInit.DIRTY_SODIUM_ALUMINATE.flowing());

        valueLookupBuilder(TagList.Fluids.SODIUM_ALUMINATE)
                .add(FluidInit.SODIUM_ALUMINATE.still())
                .add(FluidInit.SODIUM_ALUMINATE.flowing());
    }
}
