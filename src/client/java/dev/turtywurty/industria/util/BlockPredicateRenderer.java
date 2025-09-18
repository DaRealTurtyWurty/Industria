package dev.turtywurty.industria.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockPredicateRenderer {
    public static void renderInWorld(BlockPredicate predicate, @Nullable BlockPos pos, World world, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float tickDelta) {
        BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
        RegistryEntryList<Block> entryList = predicate.blocks().orElse(RegistryEntryList.empty());
        if(entryList.size() <= 0)
            return;

        long gameTime = world.getTime();
        int index = (int) ((gameTime / 10) % entryList.size());

        RegistryEntry<Block> entry = entryList.stream().skip(index).findFirst().orElse(null);
        if(entry == null)
            return;

        BlockState blockState = entry.value().getDefaultState();
        if(pos == null) {
            blockRenderer.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, overlay);
        } else {
            blockRenderer.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, overlay, world, pos);
        }
    }
}
