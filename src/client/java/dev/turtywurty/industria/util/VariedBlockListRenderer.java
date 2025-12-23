package dev.turtywurty.industria.util;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VariedBlockListRenderer {
    public static void renderInWorld(VariedBlockList variedBlockList, @Nullable BlockPos pos, World world, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
        List<BlockState> states = variedBlockList.allStates(world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK));
        if (states.isEmpty())
            return;

        long gameTime = world.getTime();
        int index = (int) ((gameTime / 10) % states.size());

        BlockState state = states.stream().skip(index).findFirst().orElse(null);
        if (state == null)
            return;

        VertexConsumerProvider vertexConsumers = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher().vertexConsumers;
        if (pos == null) {
            blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay);
        } else {
            blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay, world, pos);
        }
    }
}
