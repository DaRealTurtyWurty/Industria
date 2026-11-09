package dev.turtywurty.industria.neoforge.connected.model;

import dev.turtywurty.industria.connected.model.BakedVerticalConnectedModels;
import dev.turtywurty.industria.connected.model.VerticalConnectedModelSettings;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class NeoForgeVerticalConnectedModel extends DelegateBlockStateModel {
    private final BakedVerticalConnectedModels models;
    private final Set<Block> additionalConnections;

    public NeoForgeVerticalConnectedModel(BakedVerticalConnectedModels models, VerticalConnectedModelSettings settings) {
        super(models.normal());
        this.models = models;
        this.additionalConnections = settings.resolveBlocks();
    }

    private BlockStateModel select(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return models.select(level, pos, state, additionalConnections);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        BlockStateModel selected = select(level, pos, state);
        selected.collectParts(level, pos, state, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return select(level, pos, state).createGeometryKey(level, pos, state, random);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return select(level, pos, state).particleMaterial(level, pos, state);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return select(level, pos, state).materialFlags(level, pos, state);
    }
}
