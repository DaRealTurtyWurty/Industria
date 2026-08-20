package dev.turtywurty.industria.renderer.conveyor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.impl.LadderConveyorBlock;
import dev.turtywurty.industria.init.ConveyorSpecialRendererInit;
import dev.turtywurty.industria.model.conveyor.LadderConveyorPlatformModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;

public class LadderConveyorSpecialRenderer implements ConveyorSpecialRendererInit.ConveyorSpecialRenderer {
    public static final LadderConveyorSpecialRenderer INSTANCE = new LadderConveyorSpecialRenderer();
    private static final RenderType RENDER_TYPE = RenderTypes.entitySolid(LadderConveyorPlatformModel.TEXTURE_LOCATION);
    private static final int PLATFORMS_PER_STORAGE_SLOT = 2;
    private static final int MIN_PLATFORM_COUNT = 8;
    private static final int MAX_PLATFORM_COUNT = 96;
    private static final float MIN_Y = -0.56F;
    private static final float MAX_Y = 0.56F;
    private static final float FRONT_Z = 0;
    private static final float BACK_Z = -0.75F;
    private static final float PLATFORM_Y_OFFSET = -0.0625F;
    private static final float BOUNDS_EPSILON = 0.0001F;
    private static final float MODEL_PIVOT_Y = 1.0F / 16.0F;
    private static final float MODEL_PIVOT_Z = 0;

    private LadderConveyorPlatformModel platformModel;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        this.platformModel = new LadderConveyorPlatformModel(modelSet.bakeLayer(LadderConveyorPlatformModel.LAYER_LOCATION));
    }

    @Override
    public void render(ConveyorSpecialRendererInit.RenderContext context) {
        if (this.platformModel == null)
            return;

        SubmitNodeCollector nodeCollector = context.levelRenderContext().submitNodeCollector();
        PoseStack poseStack = context.levelRenderContext().poseStack();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        BlockPos conveyorPos = context.conveyorPos();
        Direction facing = context.conveyorState().getValue(LadderConveyorBlock.FACING);
        boolean upward = context.conveyorState().getValue(LadderConveyorBlock.UPWARD);
        StackBounds stackBounds = getStackBounds(level, conveyorPos, facing, upward);
        int stackStorageSize = getStackStorageSize(context, level, stackBounds, conveyorPos, facing, upward);
        int platformCount = Math.clamp((long) stackStorageSize * PLATFORMS_PER_STORAGE_SLOT, MIN_PLATFORM_COUNT, MAX_PLATFORM_COUNT);
        int conveyorSpeed = context.conveyorState().getBlock() instanceof LadderConveyorBlock ladderConveyor
                ? ladderConveyor.getSpeed(level, conveyorPos, context.conveyorState())
                : 0;
        float progressPerTick = conveyorSpeed / (float) ConveyorStorage.MAX_PROGRESS;
        float loopPerimeter = stackBounds.loopPerimeter(FRONT_Z - BACK_Z);
        float phaseAdvancePerTick = loopPerimeter > 0.0F ? progressPerTick / loopPerimeter : 0.0F;
        float time = context.gameTime() + context.partialTick();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        for (int platformIndex = 0; platformIndex < platformCount; platformIndex++) {
            float phaseOffset = (float) platformIndex / (float) platformCount;
            float phase = wrap((time * phaseAdvancePerTick) + phaseOffset);
            if (!upward) {
                phase = 1.0F - phase;
                if (phase >= 1.0F) {
                    phase = 0.0F;
                }
            }

            PlatformPose platformPose = samplePlatformPose(phase, stackBounds, conveyorPos.getY());
            if (platformPose == null)
                continue;

            if (!platformPose.visible())
                continue;

            poseStack.pushPose();
            poseStack.translate(platformPose.x, platformPose.y + PLATFORM_Y_OFFSET, platformPose.z);
            poseStack.translate(0.0F, MODEL_PIVOT_Y, MODEL_PIVOT_Z);
            poseStack.mulPose(Axis.XP.rotationDegrees(platformPose.yawDegrees));
            poseStack.translate(0.0F, -MODEL_PIVOT_Y, -MODEL_PIVOT_Z);
            nodeCollector.submitModel(this.platformModel, null, poseStack, RENDER_TYPE, context.lightCoords(),
                    OverlayTexture.NO_OVERLAY, 0, null);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static StackBounds getStackBounds(ClientLevel level, BlockPos centerPos, Direction facing, boolean upward) {
        int bottomY = centerPos.getY();
        int topY = centerPos.getY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        while (true) {
            int nextY = bottomY - 1;
            cursor.set(centerPos.getX(), nextY, centerPos.getZ());
            if (!isMatchingLadder(level.getBlockState(cursor), facing, upward))
                break;
            bottomY = nextY;
        }

        while (true) {
            int nextY = topY + 1;
            cursor.set(centerPos.getX(), nextY, centerPos.getZ());
            if (!isMatchingLadder(level.getBlockState(cursor), facing, upward))
                break;
            topY = nextY;
        }

        return new StackBounds(bottomY, topY);
    }

    private static boolean isMatchingLadder(BlockState state, Direction facing, boolean upward) {
        return state.getBlock() instanceof LadderConveyorBlock
                && state.getValue(LadderConveyorBlock.FACING) == facing
                && state.getValue(LadderConveyorBlock.UPWARD) == upward;
    }

    private static int getStackStorageSize(ConveyorSpecialRendererInit.RenderContext context, ClientLevel level, StackBounds stackBounds,
                                           BlockPos centerPos, Direction facing, boolean upward) {
        int total = 0;
        for (int y = stackBounds.bottomY(); y <= stackBounds.topY(); y++) {
            BlockPos pos = new BlockPos(centerPos.getX(), y, centerPos.getZ());
            BlockState state = level.getBlockState(pos);
            if (!isMatchingLadder(state, facing, upward))
                continue;

            ConveyorStorage storage = context.networkStorage().getStorages().get(pos);
            if (storage != null && storage.getItemContainer() != null) {
                total += storage.getItemContainer().getContainerSize();
            }
        }

        if (total > 0)
            return total;

        return context.conveyorStorage().getItemContainer() != null
                ? context.conveyorStorage().getItemContainer().getContainerSize()
                : 1;
    }

    private static PlatformPose samplePlatformPose(float phase, StackBounds stackBounds, int currentY) {
        float height = stackBounds.verticalHeight();
        float depth = FRONT_Z - BACK_Z;
        float perimeter = (height * 2.0F) + (depth * 2.0F);
        float distance = phase * perimeter;
        float worldY;
        float z;
        float yaw;

        if (distance < height) {
            worldY = stackBounds.bottomWorldY() + distance;
            z = FRONT_Z;
            yaw = 180.0F;
            return toLocalPose(currentY, worldY, z, yaw, true, false, false);
        } else {
            distance -= height;
            if (distance < depth) {
                float t = distance / depth;
                worldY = stackBounds.topWorldY();
                z = lerp(FRONT_Z, BACK_Z, t);
                yaw = lerp(180.0F, 0.0F, t);
                return toLocalPose(currentY, worldY, z, yaw, true, false, true);
            } else {
                distance -= depth;
                if (distance < height) {
                    worldY = stackBounds.topWorldY() - distance;
                    z = BACK_Z;
                    yaw = 0.0F;
                    return toLocalPose(currentY, worldY, z, yaw, false, false, false);
                } else {
                    float t = (distance - height) / depth;
                    worldY = stackBounds.bottomWorldY();
                    z = lerp(BACK_Z, FRONT_Z, t);
                    yaw = lerp(0.0F, -180.0F, t);
                    return toLocalPose(currentY, worldY, z, yaw, true, true, false);
                }
            }
        }
    }

    private static float wrap(float value) {
        return value - (float) Math.floor(value);
    }

    private static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private static PlatformPose toLocalPose(int currentY, float worldY, float z, float yaw, boolean visible,
                                            boolean includeLowerBoundary, boolean includeUpperBoundary) {
        float lower = currentY + MIN_Y;
        float upper = currentY + MAX_Y;
        boolean belowLower = includeLowerBoundary ? worldY < lower - BOUNDS_EPSILON : worldY <= lower + BOUNDS_EPSILON;
        boolean aboveUpper = includeUpperBoundary ? worldY > upper + BOUNDS_EPSILON : worldY >= upper - BOUNDS_EPSILON;
        if (belowLower || aboveUpper)
            return null;

        float localY = worldY - currentY;
        return new PlatformPose(0.0F, localY, z, yaw, visible);
    }

    private record PlatformPose(float x, float y, float z, float yawDegrees, boolean visible) {
    }

    private record StackBounds(int bottomY, int topY) {
        private float bottomWorldY() {
            return this.bottomY + MIN_Y;
        }

        private float topWorldY() {
            return this.topY + MAX_Y;
        }

        private float verticalHeight() {
            return this.topWorldY() - this.bottomWorldY();
        }

        private float loopPerimeter(float depth) {
            return (this.verticalHeight() * 2.0F) + (depth * 2.0F);
        }

    }
}
