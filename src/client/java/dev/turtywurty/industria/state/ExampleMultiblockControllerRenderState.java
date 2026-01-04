package dev.turtywurty.industria.state;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ExampleMultiblockControllerRenderState extends IndustriaBlockEntityRenderState {
    public final List<BlockPos> positions = new ArrayList<>();

    public ExampleMultiblockControllerRenderState() {
        super(0);
    }
}
