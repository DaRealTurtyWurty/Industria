package dev.turtywurty.industria.renderer.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.data.ClientShaftRotations;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModModels;
import dev.turtywurty.industria.mechanical.ShaftRotation;
import dev.turtywurty.turtymultiloader.event.client.LevelRenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MechanicalNetworkLevelRenderer implements IndustriaLevelRenderer {
    @Override
    public void render(LevelRenderContext context) {
        ClientLevel level = context.level();
        if (level == null)
            return;

        Map<ChunkPos, Map<BlockPos, Long>> chunks = ClientShaftRotations.getShaftChunks(level);
        if (chunks.isEmpty())
            return;

        BlockStateModel model = ModModels.SHAFT_MODEL.get(context.client().getModelManager());
        if (model == null)
            return;

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(0L), parts);

        PoseStack poseStack = context.poseStack();
        Vec3 cameraPos = context.gameRenderer().mainCamera().position();
        long gameTick = level.getGameTime();
        float partialTick = context.client().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        Map<Long, Float> angles = new HashMap<>();
        for (Map.Entry<ChunkPos, Map<BlockPos, Long>> chunk : chunks.entrySet()) {
            for (Map.Entry<BlockPos, Long> shaft : chunk.getValue().entrySet()) {
                BlockPos pos = shaft.getKey();
                BlockState state = level.getBlockState(pos);
                if (!state.is(ModBlocks.SHAFT.get()))
                    continue;

                float angle = angles.computeIfAbsent(shaft.getValue(), networkId -> {
                    ShaftRotation rotation = ClientShaftRotations.getRotation(level, networkId);
                    return rotation == null ? 0f : rotation.angleAt(gameTick, partialTick);
                });

                Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
                Vec3 center = Vec3.atCenterOf(pos).subtract(cameraPos);

                poseStack.pushPose();
                poseStack.translate(center.x, center.y, center.z);
                switch (axis) {
                    case X -> poseStack.rotate(Axis.ZP.rotationDegrees(-90f));
                    case Z -> poseStack.rotate(Axis.XP.rotationDegrees(90f));
                }

                poseStack.rotate(Axis.YP.rotation(angle));
                poseStack.translate(-0.5, -0.5, -0.5);

                context.submitNodeCollector().submitBlockModel(
                    poseStack,
                    RenderTypes.solidMovingBlock(),
                    parts,
                    BlockModelRenderState.EMPTY_TINTS,
                    LightCoordsUtil.getLightCoords(level, pos),
                    OverlayTexture.NO_OVERLAY,
                    0);
                poseStack.popPose();
            }
        }
    }
}
