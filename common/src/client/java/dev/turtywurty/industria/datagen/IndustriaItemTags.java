package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.turtymultiloader.datagen.convention.ConventionTags;
import dev.turtywurty.turtymultiloader.datagen.tag.TagGenerationContext;
import net.minecraft.world.item.Item;

public final class IndustriaItemTags {
    public static void generate(TagGenerationContext<Item> tags) {
        tags.tag(ConventionTags.item("ingots"))
                .add(ModItems.STEEL_INGOT.get().builtInRegistryHolder().key());

        tags.tag(TagList.Items.ELECTROLYSIS_RODS)
                .add(ModItems.CARBON_ROD.get().builtInRegistryHolder().key());
    }

    private IndustriaItemTags() {
    }
}
