package dev.turtywurty.industria.init;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.BiomeColors;

public class ColorProviderInit {
    public static void init() {
        BlockColorRegistry.register((_, world, pos, _) -> {
            if (world != null && pos != null)
                return BiomeColors.getAverageFoliageColor(world, pos) + 0x00220A;

            return 0x00BB0A;
        }, WoodSetInit.RUBBER.leaves);
    }
}
