package dev.turtywurty.industria;

import dev.turtywurty.turtymultiloader.datagen.DataGeneration;
import dev.turtywurty.turtymultiloader.fabric.datagen.FabricDataGeneration;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class IndustriaDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGeneration.run(fabricDataGenerator, IndustriaDataGeneration.SPEC);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        DataGeneration.addRegistryBootstraps(IndustriaDataGeneration.SPEC, registryBuilder);
    }
}
