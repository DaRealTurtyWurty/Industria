package dev.turtywurty.industria.item;

import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import net.minecraft.item.Item;

public class SimpleDrillHeadItem extends Item implements DrillHeadable {
    public SimpleDrillHeadItem(Settings settings) {
        super(settings);
    }

    @Override
    public DrillRenderData createRenderData() {
        return new SimpleDrillRenderData();
    }

    public static class SimpleDrillRenderData implements DrillRenderData {
        public float clockwiseRotation, counterClockwiseRotation;
    }
}
