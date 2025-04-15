package dev.turtywurty.industria.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;

public class ColorProviderInit {
    public static void init() {
        initBlocks();
        initItems();
    }

    public static void initBlocks() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                return BiomeColors.getFoliageColor(world, pos) + 0x00220A;
            }

            return 0x00BB0A;
        }, WoodSetInit.RUBBER.leaves);
    }

    public static void initItems() {
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> 0x00BB0A,
                WoodSetInit.RUBBER.leaves
        );
    }
}
