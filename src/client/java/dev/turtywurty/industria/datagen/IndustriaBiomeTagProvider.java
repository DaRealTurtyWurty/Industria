package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.init.worldgen.BiomeInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;

import java.util.concurrent.CompletableFuture;

public class IndustriaBiomeTagProvider extends FabricTagProvider<Biome> {
    public IndustriaBiomeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        getTagBuilder(TagList.Biomes.FLOATING_ORB_BIOMES)
                .add(BiomeInit.LUMEN_DEPTHS.getValue())
                .add(BiomeInit.REACTOR_BASIN.getValue())
                .add(BiomeInit.LUMINOUS_GROVE.getValue());
    }
}
