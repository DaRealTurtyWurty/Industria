package dev.turtywurty.industria.init;

import dev.turtywurty.multiblocklib.MultiblockLib;

public final class MultiblockLibIntegrationInit {
    private MultiblockLibIntegrationInit() {
    }

    public static void init() {
        // Register Industria machine controllers so MultiblockLib can form definitions onto them.
        MultiblockLib.registerControllerBlock(BlockInit.OIL_PUMP_JACK);
        MultiblockLib.registerControllerBlock(BlockInit.DRILL);
        MultiblockLib.registerControllerBlock(BlockInit.UPGRADE_STATION);
        MultiblockLib.registerControllerBlock(BlockInit.MIXER);
        MultiblockLib.registerControllerBlock(BlockInit.DIGESTER);
        MultiblockLib.registerControllerBlock(BlockInit.CLARIFIER);
        MultiblockLib.registerControllerBlock(BlockInit.CRYSTALLIZER);
        MultiblockLib.registerControllerBlock(BlockInit.ROTARY_KILN_CONTROLLER);
        MultiblockLib.registerControllerBlock(BlockInit.ELECTROLYZER);
        MultiblockLib.registerControllerBlock(BlockInit.SHAKING_TABLE);
        MultiblockLib.registerControllerBlock(BlockInit.CENTRIFUGAL_CONCENTRATOR);
        MultiblockLib.registerControllerBlock(BlockInit.ARC_FURNACE);
    }
}
