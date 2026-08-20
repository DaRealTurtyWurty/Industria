package dev.turtywurty.industria.util;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import net.minecraft.world.level.ItemLike;

public interface DrillHeadable extends ItemLike {
    float updateDrill(DrillBlockEntity blockEntity, float drillYOffset);
    float updateRetracting(DrillBlockEntity blockEntity, float drillYOffset);
}
