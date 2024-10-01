package dev.turtywurty.industria.util;

import net.minecraft.item.ItemConvertible;

public interface DrillHeadable extends ItemConvertible {
    DrillRenderData createRenderData();
}
