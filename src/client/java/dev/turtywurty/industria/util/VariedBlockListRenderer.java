package dev.turtywurty.industria.util;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VariedBlockListRenderer {
    public static void renderInWorld(VariedBlockList variedBlockList, @Nullable BlockPos pos, World world, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        CameraRenderState cameraRenderState = buildCameraRenderState(camera, tickDelta);
        renderInWorld(variedBlockList, pos, world, matrices, queue, light, overlay, camera, cameraRenderState, tickDelta);
    }

    public static void renderInWorld(
            VariedBlockList variedBlockList,
            @Nullable BlockPos pos,
            World world,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            Camera camera,
            CameraRenderState cameraRenderState,
            float tickDelta
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockRenderer = client.getBlockRenderManager();
        BlockState state = getCurrentState(variedBlockList, world);
        if (state == null)
            return;

        VertexConsumerProvider vertexConsumers = client.gameRenderer.getEntityRenderDispatcher().vertexConsumers;
        if (pos == null) {
            blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay);
        } else {
            blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay, world, pos);
        }

        if (!state.hasBlockEntity() || pos == null)
            return;

        if (!(state.getBlock() instanceof BlockEntityProvider provider))
            return;

        BlockEntity blockEntity = provider.createBlockEntity(pos, state);
        if (blockEntity == null)
            return;

        blockEntity.setWorld(world);
        BlockEntityRenderManager blockEntityRenderManager = client.getBlockEntityRenderDispatcher();
        blockEntityRenderManager.configure(camera);
        ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay = new ModelCommandRenderer.CrumblingOverlayCommand(0, matrices.peek());
        BlockEntityRenderState renderState = blockEntityRenderManager.getRenderState(blockEntity, tickDelta, crumblingOverlay);
        if (renderState != null) {
            blockEntityRenderManager.render(renderState, matrices, queue, cameraRenderState);
        }
    }

    @Nullable
    private static BlockState getCurrentState(VariedBlockList variedBlockList, World world) {
        List<BlockState> states = variedBlockList.allStates(world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK));
        if (states.isEmpty())
            return null;

        long gameTime = world.getTime();
        int index = (int) ((gameTime / 10) % states.size());
        return states.stream().skip(index).findFirst().orElse(null);
    }

    private static CameraRenderState buildCameraRenderState(Camera camera, float tickDelta) {
        CameraRenderState cameraRenderState = new CameraRenderState();
        cameraRenderState.initialized = camera.isReady();
        cameraRenderState.pos = camera.getPos();
        cameraRenderState.blockPos = camera.getBlockPos();
        Vec3d entityPos = cameraRenderState.pos;
        if (camera.getFocusedEntity() != null) {
            entityPos = camera.getFocusedEntity().getLerpedPos(tickDelta);
        }
        cameraRenderState.entityPos = entityPos;
        cameraRenderState.orientation = new org.joml.Quaternionf(camera.getRotation());
        return cameraRenderState;
    }
}
