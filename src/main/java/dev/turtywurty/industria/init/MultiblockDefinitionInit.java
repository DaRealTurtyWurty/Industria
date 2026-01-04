package dev.turtywurty.industria.init;

import com.mojang.math.Quadrant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.List;

public class MultiblockDefinitionInit {
    public static void bootstrap(BootstrapContext<MultiblockDefinition> context) {
    }

    public static final MultiblockDefinition EXAMPLE = register("example",
            new MultiblockDefinition.Builder()
                    .size(4, 2, 3)
                    .addPatternRow(List.of("GGGG", "GBGG", "GGGG"))
                    .addPatternRow(List.of("CGGG", "GPGG", "OGGG"))
                    .anchor(0, 0, 0)
                    .addPaletteEntry('G', BlockPredicate.matchesBlocks(Blocks.GLASS))
                    .addPaletteEntry('B', BlockPredicate.matchesBlocks(Blocks.BLUE_STAINED_GLASS))
                    .addPaletteEntry('P', BlockPredicate.matchesBlocks(Blocks.PINK_STAINED_GLASS))
                    .addPaletteEntry('O', BlockPredicate.matchesBlocks(Blocks.ORANGE_STAINED_GLASS))
                    .addPaletteEntry('C', BlockPredicate.matchesBlocks(BlockInit.EXAMPLE_MULTIBLOCK_CONTROLLER))
                    .addRotation(Quadrant.R90, Quadrant.R180, Quadrant.R270)
    );

    public static MultiblockDefinition register(String name, MultiblockDefinition.Builder definition) {
        Identifier id = Industria.id(name);
        return Registry.register(IndustriaRegistries.MULTIBLOCK_DEFINITIONS, id, definition.build(id));
    }

    public static void init() {
    }
}
