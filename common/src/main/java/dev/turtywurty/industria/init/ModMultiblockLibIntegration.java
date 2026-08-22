package dev.turtywurty.industria.init;

import dev.turtywurty.multiblocklib.MultiblockLib;

public final class ModMultiblockLibIntegration {
    private ModMultiblockLibIntegration() {
    }

    public static void init() {
        // Register Industria machine controllers so MultiblockLib can form definitions onto them.
        MultiblockLib.registerControllerBlock(ModBlocks.OIL_PUMP_JACK.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.DRILL.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.UPGRADE_STATION.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.MIXER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.DIGESTER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.CLARIFIER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.CRYSTALLIZER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.ROTARY_KILN_CONTROLLER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.ELECTROLYZER.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.SHAKING_TABLE.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.CENTRIFUGAL_CONCENTRATOR.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.ARC_FURNACE.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.AGITATOR.get(), false);
        MultiblockLib.registerControllerBlock(ModBlocks.DISTILLATION_TOWER.get(), false);
    }
}
