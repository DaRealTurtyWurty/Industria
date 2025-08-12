package dev.turtywurty.industria.util;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import net.minecraft.item.ItemConvertible;

public interface DrillHeadable extends ItemConvertible {
    float updateDrill(DrillBlockEntity blockEntity, float drillYOffset);

    float updateRetracting(DrillBlockEntity blockEntity, float drillYOffset);

    DrillRenderData createRenderData();
}
