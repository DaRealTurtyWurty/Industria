package dev.turtywurty.industria.datagen.provider;

import dev.turtywurty.industria.init.IndustriaRegistries;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record MultiblockDefinitionProvider(Path path, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture,
                                           String modId) implements DataProvider {
    public MultiblockDefinitionProvider(DataOutput path, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, String modId) {
        this(path.resolvePath(DataOutput.OutputType.DATA_PACK).resolve("multiblock_definitions"), registriesFuture, modId);
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registriesFuture.thenCompose(registries -> {
            RegistryWrapper.Impl<MultiblockDefinition> multiblockDefinitions = registries.getOrThrow(IndustriaRegistries.MULTIBLOCK_DEFINITION_KEY);
            List<RegistryEntry.Reference<MultiblockDefinition>> referenceList = multiblockDefinitions.streamKeys()
                    .filter(registryKey -> registryKey.getValue().getNamespace().equals(this.modId))
                    .map(multiblockDefinitions::getOrThrow)
                    .toList();

            return CompletableFuture.allOf(referenceList.stream()
                    .map(entry -> {
                        Identifier id = entry.getKey().orElseThrow().getValue();
                        MultiblockDefinition definition = entry.value();
                        Path filePath = resolvePath(id);

                        return DataProvider.writeCodecToPath(writer, registries, MultiblockDefinition.CODEC.codec(), definition, filePath);
                    })
                    .toArray(CompletableFuture[]::new));
        });
    }

    private Path resolvePath(Identifier id) {
        return this.path.resolve(id.getNamespace()).resolve(id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Multiblock Definitions";
    }
}
