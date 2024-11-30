package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.list.TagList;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public IndustriaBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(TagList.Blocks.BATTERY_BLOCKS)
                .add((Block) BlockInit.BATTERIES);
    }
}
