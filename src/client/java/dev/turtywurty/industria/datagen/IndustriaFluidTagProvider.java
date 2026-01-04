package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.list.TagList;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class IndustriaFluidTagProvider extends FabricTagsProvider.FluidTagsProvider {
    public IndustriaFluidTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
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
