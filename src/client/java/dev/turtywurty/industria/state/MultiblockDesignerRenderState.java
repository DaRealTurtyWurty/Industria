package dev.turtywurty.industria.state;

import dev.turtywurty.industria.multiblock.PieceData;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class MultiblockDesignerRenderState extends IndustriaBlockEntityRenderState {
    public final Map<BlockPos, PieceData> pieces = new HashMap<>();

    public MultiblockDesignerRenderState() {
        super(0);
    }
}
