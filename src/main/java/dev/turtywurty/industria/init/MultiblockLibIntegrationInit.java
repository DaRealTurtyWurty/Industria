package dev.turtywurty.industria.init;

import dev.turtywurty.multiblocklib.MultiblockLib;

public final class MultiblockLibIntegrationInit {
    private MultiblockLibIntegrationInit() {
    }

    public static void init() {
        // Register Industria machine controllers so MultiblockLib can form definitions onto them.
        MultiblockLib.registerControllerBlock(BlockInit.OIL_PUMP_JACK, false);
        MultiblockLib.registerControllerBlock(BlockInit.DRILL, false);
        MultiblockLib.registerControllerBlock(BlockInit.UPGRADE_STATION, false);
        MultiblockLib.registerControllerBlock(BlockInit.MIXER, false);
        MultiblockLib.registerControllerBlock(BlockInit.DIGESTER, false);
        MultiblockLib.registerControllerBlock(BlockInit.CLARIFIER, false);
        MultiblockLib.registerControllerBlock(BlockInit.CRYSTALLIZER, false);
        MultiblockLib.registerControllerBlock(BlockInit.ROTARY_KILN_CONTROLLER, false);
        MultiblockLib.registerControllerBlock(BlockInit.ELECTROLYZER, false);
        MultiblockLib.registerControllerBlock(BlockInit.SHAKING_TABLE, false);
        MultiblockLib.registerControllerBlock(BlockInit.CENTRIFUGAL_CONCENTRATOR, false);
        MultiblockLib.registerControllerBlock(BlockInit.ARC_FURNACE, false);
    }
}
