package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;

import java.util.List;

public class MultiblockDefinitionInit {
    public static void bootstrap(Registerable<MultiblockDefinition> context) {
    }

    public static final MultiblockDefinition EXAMPLE = register("example",
            new MultiblockDefinition.Builder()
                    .size(4, 2, 3)
                    .addPatternRow(List.of("GGGG", "GBGG", "GGGG"))
                    .addPatternRow(List.of("CGGG", "GPGG", "OGGG"))
                    .anchor(0, 0, 0)
                    .addPaletteEntry('G', BlockPredicate.matchingBlocks(Blocks.GLASS))
                    .addPaletteEntry('B', BlockPredicate.matchingBlocks(Blocks.BLUE_STAINED_GLASS))
                    .addPaletteEntry('P', BlockPredicate.matchingBlocks(Blocks.PINK_STAINED_GLASS))
                    .addPaletteEntry('O', BlockPredicate.matchingBlocks(Blocks.ORANGE_STAINED_GLASS))
                    .addPaletteEntry('C', BlockPredicate.matchingBlocks(BlockInit.EXAMPLE_MULTIBLOCK_CONTROLLER))
                    .addRotation(AxisRotation.R90, AxisRotation.R180, AxisRotation.R270)
    );

    public static MultiblockDefinition register(String name, MultiblockDefinition.Builder definition) {
        Identifier id = Industria.id(name);
        return Registry.register(IndustriaRegistries.MULTIBLOCK_DEFINITIONS, id, definition.build(id));
    }

    public static void init() {
    }
}
