package dev.turtywurty.industria.renderer.conveyor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.conveyor.block.impl.entity.ContainmentConveyorBlockEntity;
import dev.turtywurty.industria.init.ConveyorSpecialRendererInit;
import dev.turtywurty.industria.init.ItemInit;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ContainmentConveyorSpecialRenderer implements ConveyorSpecialRendererInit.ConveyorSpecialRenderer {
    public static final ContainmentConveyorSpecialRenderer INSTANCE = new ContainmentConveyorSpecialRenderer();
    private static final double ENTITY_BASE_Y_OFFSET = 2.0D / 16.0D;
    private static final double JAR_Y_OFFSET = 3.0D / 16.0D;
    private static final AABB JAR_BOUNDING_BOX = new AABB(3.0 / 16.0, 0, 3.0 / 16.0, 13.0 / 16.0, 10.0 / 16.0, 13.0 / 16.0);

    @Override
    public void render(ConveyorSpecialRendererInit.RenderContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        BlockPos pos = context.conveyorPos();
        ContainmentConveyorBlockEntity blockEntity = (ContainmentConveyorBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        LevelRenderContext renderContext = context.levelRenderContext();
        SubmitNodeCollector nodeCollector = renderContext.submitNodeCollector();
        PoseStack poseStack = renderContext.poseStack();
        int lightCoords = context.lightCoords();
        RandomSource random = level.getRandom();
        LivingEntity entity = blockEntity.getContainingEntity();
        if (entity == null)
            return;

        BlockState conveyorState = context.conveyorState();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 relativePosition = Vec3.atLowerCornerOf(pos).subtract(camera.position());

        poseStack.pushPose();
        poseStack.translate(relativePosition.x, relativePosition.y, relativePosition.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-conveyorState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()));
        poseStack.translate(-0.5D, ENTITY_BASE_Y_OFFSET, 0.5D);

        renderEntity(context, entity, poseStack, blockEntity, nodeCollector, renderContext);
        renderMobJar(nodeCollector, poseStack, lightCoords, random);

        Gizmos.cuboid(JAR_BOUNDING_BOX.move(0D, JAR_Y_OFFSET, 0D).move(pos), GizmoStyle.stroke(0xFF0000FF));
        Gizmos.cuboid(entity.getBoundingBox().move(entity.position().reverse()).move(0.5D, (2.0/16.0D), 0.5D).move(pos), GizmoStyle.stroke(0xFFFF0000));

        AABB entityBoundingBox = entity.getBoundingBox().move(entity.position().reverse());
        float renderScale = computeRenderScale(blockEntity, entityBoundingBox);
        Gizmos.cuboid(createRenderedEntityBounds(entityBoundingBox, renderScale, pos, conveyorState), GizmoStyle.stroke(0xFFFFFF00));

        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    private void renderEntity(ConveyorSpecialRendererInit.RenderContext context, LivingEntity entity, PoseStack poseStack, ContainmentConveyorBlockEntity blockEntity, SubmitNodeCollector nodeCollector, LevelRenderContext renderContext) {
        EntityPoseSnapshot poseSnapshot = EntityPoseSnapshot.capture(entity);
        try {
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            CameraRenderState cameraRenderState = renderContext.levelState().cameraRenderState;
            poseSnapshot.applyStablePose(entity);

            poseStack.pushPose();

            AABB entityBoundingBox = entity.getBoundingBox().move(entity.position().reverse());
            float renderScale = computeRenderScale(blockEntity, entityBoundingBox);
            poseStack.translate(0.0D, computeEntityYOffset(blockEntity, entityBoundingBox), 0.0D);
            poseStack.scale(renderScale, renderScale, renderScale);

            EntityRenderState renderState = ((EntityRenderer<LivingEntity, EntityRenderState>) entityRenderDispatcher.getRenderer(entity))
                    .createRenderState(entity, context.partialTick());
            entityRenderDispatcher.submit(renderState, cameraRenderState, 0.0, 0.0, 0.0, poseStack, nodeCollector);
            poseStack.popPose();
        } catch (Exception ignored) {
        } finally {
            poseSnapshot.restore(entity);
        }
    }

    private static float computeFittedScale(AABB entityBoundingBox) {
        double entityWidth = Math.max(entityBoundingBox.getXsize(), entityBoundingBox.getZsize());
        double entityHeight = entityBoundingBox.getYsize();
        if (entityWidth <= 1.0E-6D || entityHeight <= 1.0E-6D)
            return 1.0F;

        double widthScale = JAR_BOUNDING_BOX.getXsize() / entityWidth;
        double heightScale = JAR_BOUNDING_BOX.getYsize() / entityHeight;
        return (float) Math.min(1.0D, Math.min(widthScale, heightScale));
    }

    private static float computeRenderScale(ContainmentConveyorBlockEntity blockEntity, AABB entityBoundingBox) {
        float shrinkProgress = computeShrinkProgress(blockEntity);
        float fittedScale = computeFittedScale(entityBoundingBox);
        return Mth.lerp(shrinkProgress, 1.0F, fittedScale);
    }

    private static float computeShrinkProgress(ContainmentConveyorBlockEntity blockEntity) {
        float shrinkTicks = Math.min(blockEntity.getProgress(), 60.0F);
        return Mth.clamp(shrinkTicks / 60.0F, 0.0F, 1.0F);
    }

    private static double computeEntityYOffset(ContainmentConveyorBlockEntity blockEntity, AABB entityBoundingBox) {
        float shrinkProgress = computeShrinkProgress(blockEntity);
        float fittedScale = computeFittedScale(entityBoundingBox);
        double targetYOffset = JAR_Y_OFFSET - ENTITY_BASE_Y_OFFSET - (entityBoundingBox.minY * fittedScale);
        return Mth.lerp(shrinkProgress, 0.0D, targetYOffset);
    }

    private static AABB createRenderedEntityBounds(AABB entityBoundingBox, float renderScale, BlockPos pos, BlockState conveyorState) {
        Matrix4f transform = new Matrix4f()
                .translate(pos.getX(), pos.getY(), pos.getZ())
                .rotateY((float) Math.toRadians(-conveyorState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()))
                .translate(-0.5F, (float) (ENTITY_BASE_Y_OFFSET + JAR_Y_OFFSET - ENTITY_BASE_Y_OFFSET - (entityBoundingBox.minY * computeFittedScale(entityBoundingBox))), 0.5F)
                .scale(renderScale);

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        double[] xs = {entityBoundingBox.minX, entityBoundingBox.maxX};
        double[] ys = {entityBoundingBox.minY, entityBoundingBox.maxY};
        double[] zs = {entityBoundingBox.minZ, entityBoundingBox.maxZ};

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    Vector3f transformed = transform.transformPosition((float) x, (float) y, (float) z, new Vector3f());
                    minX = Math.min(minX, transformed.x());
                    minY = Math.min(minY, transformed.y());
                    minZ = Math.min(minZ, transformed.z());
                    maxX = Math.max(maxX, transformed.x());
                    maxY = Math.max(maxY, transformed.y());
                    maxZ = Math.max(maxZ, transformed.z());
                }
            }
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void renderMobJar(SubmitNodeCollector nodeCollector, PoseStack poseStack, int lightCoords, RandomSource randomSource) {
        Minecraft minecraft = Minecraft.getInstance();
        var modelResolver = minecraft.getItemModelResolver();
        var itemState = new ItemStackRenderState();

        ItemStack jarStack = ItemInit.EMPTY_MOB_JAR.getDefaultInstance();
        modelResolver.updateForTopItem(itemState, jarStack, ItemDisplayContext.FIXED, minecraft.level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0, (3.0D / 16.0D) + (6.0 / 16.0), 0);
        poseStack.scale(2F, 2F, 2F);

        itemState.submit(poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private record EntityPoseSnapshot(float yRot, float yRotO, float xRot, float xRotO,
                                      float yBodyRot, float yBodyRotO, float yHeadRot, float yHeadRotO) {
        private static EntityPoseSnapshot capture(LivingEntity entity) {
            return new EntityPoseSnapshot(entity.getYRot(), entity.yRotO, entity.getXRot(), entity.xRotO,
                    entity.yBodyRot, entity.yBodyRotO, entity.yHeadRot, entity.yHeadRotO);
        }

        private void applyStablePose(LivingEntity entity) {
            entity.setYRot(0.0F);
            entity.yRotO = 0.0F;
            entity.setXRot(0.0F);
            entity.xRotO = 0.0F;
            entity.yBodyRot = 0.0F;
            entity.yBodyRotO = 0.0F;
            entity.yHeadRot = 0.0F;
            entity.yHeadRotO = 0.0F;
        }

        private void restore(LivingEntity entity) {
            entity.setYRot(this.yRot);
            entity.yRotO = this.yRotO;
            entity.setXRot(this.xRot);
            entity.xRotO = this.xRotO;
            entity.yBodyRot = this.yBodyRot;
            entity.yBodyRotO = this.yBodyRotO;
            entity.yHeadRot = this.yHeadRot;
            entity.yHeadRotO = this.yHeadRotO;
        }
    }
}
