package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RotaryKilnControllerRenderState extends IndustriaBlockEntityRenderState {
    public final List<BlockPos> kilnSegments = new ArrayList<>();
    public final List<RotaryKilnControllerBlockEntity.InputRecipeEntry> recipes = new ArrayList<>();

    public RotaryKilnControllerRenderState() {
        super(15);
    }
}
