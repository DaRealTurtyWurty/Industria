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
        getOrCreateTagBuilder(TagList.Fluids.CRUDE_OIL)
                .add(FluidInit.CRUDE_OIL)
                .add(FluidInit.CRUDE_OIL_FLOWING);
    }
}
