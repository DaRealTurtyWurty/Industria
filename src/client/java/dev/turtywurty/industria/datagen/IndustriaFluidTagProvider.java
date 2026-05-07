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

        valueLookupBuilder(TagList.Fluids.MOLTEN_ALUMINIUM)
                .add(FluidInit.MOLTEN_ALUMINIUM.still())
                .add(FluidInit.MOLTEN_ALUMINIUM.flowing());

        valueLookupBuilder(TagList.Fluids.MOLTEN_CRYOLITE)
                .add(FluidInit.MOLTEN_CRYOLITE.still())
                .add(FluidInit.MOLTEN_CRYOLITE.flowing());

        valueLookupBuilder(TagList.Fluids.LATEX)
                .add(FluidInit.LATEX.still())
                .add(FluidInit.LATEX.flowing());

        valueLookupBuilder(TagList.Fluids.METHANOL)
                .add(FluidInit.METHANOL.still())
                .add(FluidInit.METHANOL.flowing());
    }
}
