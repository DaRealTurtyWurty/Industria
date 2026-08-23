package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.turtymultiloader.datagen.tag.TagGenerationContext;
import net.minecraft.world.level.material.Fluid;

public final class IndustriaFluidTags {
    public static void generate(TagGenerationContext<Fluid> tags) {
        tags.tag(TagList.Fluids.CRUDE_OIL)
                .add(ModFluids.CRUDE_OIL.still().key())
                .add(ModFluids.CRUDE_OIL.flowing().key());

        tags.tag(TagList.Fluids.DIRTY_SODIUM_ALUMINATE)
                .add(ModFluids.DIRTY_SODIUM_ALUMINATE.still().key())
                .add(ModFluids.DIRTY_SODIUM_ALUMINATE.flowing().key());

        tags.tag(TagList.Fluids.SODIUM_ALUMINATE)
                .add(ModFluids.SODIUM_ALUMINATE.still().key())
                .add(ModFluids.SODIUM_ALUMINATE.flowing().key());

        tags.tag(TagList.Fluids.MOLTEN_ALUMINIUM)
                .add(ModFluids.MOLTEN_ALUMINIUM.still().key())
                .add(ModFluids.MOLTEN_ALUMINIUM.flowing().key());

        tags.tag(TagList.Fluids.MOLTEN_CRYOLITE)
                .add(ModFluids.MOLTEN_CRYOLITE.still().key())
                .add(ModFluids.MOLTEN_CRYOLITE.flowing().key());

        tags.tag(TagList.Fluids.LATEX)
                .add(ModFluids.LATEX.still().key())
                .add(ModFluids.LATEX.flowing().key());

        tags.tag(TagList.Fluids.METHANOL)
                .add(ModFluids.METHANOL.still().key())
                .add(ModFluids.METHANOL.flowing().key());

        tags.tag(TagList.Fluids.FORMIC_ACID)
                .add(ModFluids.FORMIC_ACID.still().key())
                .add(ModFluids.FORMIC_ACID.flowing().key());

        tags.tag(TagList.Fluids.DILUTED_FORMIC_ACID)
                .add(ModFluids.DILUTED_FORMIC_ACID.still().key())
                .add(ModFluids.DILUTED_FORMIC_ACID.flowing().key());
    }

    private IndustriaFluidTags() {
    }
}
