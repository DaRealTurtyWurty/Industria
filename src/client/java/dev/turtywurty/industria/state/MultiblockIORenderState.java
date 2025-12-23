package dev.turtywurty.industria.state;

import dev.turtywurty.industria.multiblock.old.Port;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class MultiblockIORenderState extends IndustriaBlockEntityRenderState {
    public final Map<Direction, Map<Direction, Port>> ports = new HashMap<>();

    public MultiblockIORenderState() {
        super(0);
    }
}
