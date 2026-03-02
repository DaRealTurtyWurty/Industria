package dev.turtywurty.industria.renderer.conveyor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.init.ConveyorSpecialRendererInit;
import dev.turtywurty.industria.model.conveyor.ConveyorFlapsModel;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public abstract class AbstractFlapConveyorSpecialRenderer implements ConveyorSpecialRendererInit.ConveyorSpecialRenderer {
    private static final RenderType RENDER_TYPE = RenderTypes.entitySolid(ConveyorFlapsModel.TEXTURE_LOCATION);
    private static final float FLAP_MODEL_Y_OFFSET = -0.875f;
    private static final float[] FLAP_X_OFFSETS = {-4.5f, -1.5f, 1.5f, 4.5f};
    private static final float FLAP_TRIGGER_Z = -0.5f;
    private static final float FLAP_TRIGGER_Z_RANGE = 1.75f;
    private static final float FLAP_TRIGGER_X_RANGE = 1.75f;
    private static final float FLAP_KICK_VELOCITY = 14.0f;
    private static final float FLAP_MAX_BEND = (float) Math.toRadians(65.0);
    private static final float FLAP_MAX_BACKSWING = (float) Math.toRadians(22.0);
    private static final float FLAP_SPRING_STRENGTH = 45.0f;
    private static final float FLAP_DAMPING = 7.5f;
    private static final float FLAP_SETTLE_ANGLE_EPSILON = (float) Math.toRadians(0.35);
    private static final float FLAP_SETTLE_VELOCITY_EPSILON = 0.08f;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final Map<ResourceKey<Level>, Long2ObjectMap<FlapSimulation>> simsByDim = new Object2ObjectOpenHashMap<>();
    private ConveyorFlapsModel flapsModel;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet entityModels = this.minecraft.getEntityModels();
        this.flapsModel = new ConveyorFlapsModel(entityModels.bakeLayer(ConveyorFlapsModel.LAYER_LOCATION));
    }

    @Override
    public void render(ConveyorSpecialRendererInit.RenderContext context) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return;

        LevelRenderContext levelRenderContext = context.levelRenderContext();
        SubmitNodeCollector nodeCollector = levelRenderContext.submitNodeCollector();
        float partialTick = context.partialTick();
        BlockPos conveyorPos = context.conveyorPos();
        int lightCoords = context.lightCoords();
        BlockState conveyorState = context.conveyorState();
        Map<ConveyorItem, Pair<List<Vector3d>, Float>> itemRenderData = context.itemRenderData().get();

        PoseStack poseStack = levelRenderContext.poseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-getFacing(conveyorState).toYRot()));
        poseStack.translate(0, FLAP_MODEL_Y_OFFSET, getFlapModelZOffset());

        var state = new ConveyorFlapsModel.RenderState();
        FlapSimulation simulation = getFlapSim(level.dimension(), conveyorPos);
        for (Map.Entry<ConveyorItem, Pair<List<Vector3d>, Float>> entry : itemRenderData.entrySet()) {
            Pair<List<Vector3d>, Float> renderData = entry.getValue();
            List<Vector3d> anchors = renderData.getLeft();
            float progress = renderData.getRight() / (float) ConveyorStorage.MAX_PROGRESS;

            Vector3d anchor = ConveyorNetworkLevelRenderer.interpolateAnchorPosition(anchors, progress);
            Vector3d localAnchor = toFlapModelLocal(anchor, conveyorState);
            simulation.applyImpulseAtLocal(localAnchor);
        }
        simulation.writeAnglesToModel(state, partialTick);

        nodeCollector.submitModel(this.flapsModel, state, poseStack, RENDER_TYPE, lightCoords,
                OverlayTexture.NO_OVERLAY, 0, null);

        poseStack.popPose();
    }

    public void onDimensionUnload(ResourceKey<Level> dimension) {
        this.simsByDim.remove(dimension);
    }

    public void onConveyorRemoved(ResourceKey<Level> dimension, BlockPos pos) {
        Long2ObjectMap<FlapSimulation> simsByPos = this.simsByDim.get(dimension);
        if (simsByPos != null)
            simsByPos.remove(pos.asLong());
    }

    public void onTick(ClientLevel level) {
        Long2ObjectMap<FlapSimulation> simsByPos = this.simsByDim.get(level.dimension());
        if (simsByPos == null)
            return;

        for (FlapSimulation sim : simsByPos.values()) {
            sim.step(1 / 20f);
        }
    }

    protected abstract Direction getFacing(BlockState state);

    protected float getFlapModelZOffset() {
        return 0f;
    }

    private FlapSimulation getFlapSim(ResourceKey<Level> dimension, BlockPos pos) {
        Long2ObjectMap<FlapSimulation> simsByPos = this.simsByDim.computeIfAbsent(dimension, _ -> new Long2ObjectOpenHashMap<>());
        return simsByPos.computeIfAbsent(pos.asLong(), _ -> new FlapSimulation());
    }

    private Vector3d toFlapModelLocal(Vector3d anchor, BlockState state) {
        Vector3d local = switch (getFacing(state)) {
            case EAST -> new Vector3d(-anchor.z, anchor.y, anchor.x);
            case WEST -> new Vector3d(anchor.z, anchor.y, -anchor.x);
            case NORTH -> new Vector3d(-anchor.x, anchor.y, -anchor.z);
            default -> new Vector3d(anchor);
        };

        local.y -= FLAP_MODEL_Y_OFFSET;
        local.z -= getFlapModelZOffset();
        return local.mul(16.0);
    }

    protected static class FlapSimulation {
        private static final float FIXED_DT = 1 / 60f;

        private final float[] flapCooldowns = new float[4];
        private final float[] previousBend = new float[4];
        private final float[] currentBend = new float[4];
        private final float[] velocities = new float[4];
        private float accumulator = 0f;

        protected FlapSimulation() {
            System.arraycopy(this.currentBend, 0, this.previousBend, 0, this.currentBend.length);
        }

        public void step(float deltaSeconds) {
            for (int flapIndex = 0; flapIndex < this.flapCooldowns.length; flapIndex++) {
                this.flapCooldowns[flapIndex] = Math.max(0f, this.flapCooldowns[flapIndex] - deltaSeconds);
            }

            this.accumulator += deltaSeconds;
            while (this.accumulator >= FIXED_DT) {
                System.arraycopy(this.currentBend, 0, this.previousBend, 0, this.currentBend.length);
                for (int flapIndex = 0; flapIndex < this.currentBend.length; flapIndex++) {
                    float acceleration = -FLAP_SPRING_STRENGTH * this.currentBend[flapIndex]
                            - FLAP_DAMPING * this.velocities[flapIndex];
                    this.velocities[flapIndex] += acceleration * FIXED_DT;
                    this.currentBend[flapIndex] += this.velocities[flapIndex] * FIXED_DT;

                    this.currentBend[flapIndex] = Mth.clamp(this.currentBend[flapIndex], -FLAP_MAX_BACKSWING, FLAP_MAX_BEND);

                    if (Math.abs(this.currentBend[flapIndex]) <= FLAP_SETTLE_ANGLE_EPSILON
                            && Math.abs(this.velocities[flapIndex]) <= FLAP_SETTLE_VELOCITY_EPSILON) {
                        this.currentBend[flapIndex] = 0f;
                        this.velocities[flapIndex] = 0f;
                    }
                }
                this.accumulator -= FIXED_DT;
            }
        }

        public void writeAnglesToModel(ConveyorFlapsModel.RenderState state, float partialTick) {
            float alpha = Mth.clamp(partialTick + (this.accumulator / FIXED_DT), 0.0F, 1.0F);
            applyAngles(state, 0, Mth.lerp(alpha, this.previousBend[0], this.currentBend[0]));
            applyAngles(state, 1, Mth.lerp(alpha, this.previousBend[1], this.currentBend[1]));
            applyAngles(state, 2, Mth.lerp(alpha, this.previousBend[2], this.currentBend[2]));
            applyAngles(state, 3, Mth.lerp(alpha, this.previousBend[3], this.currentBend[3]));
        }

        public void applyImpulseAtLocal(Vector3d local) {
            if (Math.abs(local.z - FLAP_TRIGGER_Z) > FLAP_TRIGGER_Z_RANGE)
                return;

            for (int i = 0; i < FLAP_X_OFFSETS.length; i++) {
                float dist = Math.abs((float) local.x - FLAP_X_OFFSETS[i]);
                if (dist > FLAP_TRIGGER_X_RANGE || this.flapCooldowns[i] > 0f)
                    continue;

                this.flapCooldowns[i] = 0.075f;
                this.velocities[i] = Math.max(this.velocities[i], FLAP_KICK_VELOCITY);
            }
        }

        private static void applyAngles(ConveyorFlapsModel.RenderState state, int flapIndex, float bend) {
            state.applyForIndex(flapIndex,
                    -bend * 0.25f,
                    -bend * 0.30f,
                    -bend * 0.45f);
        }
    }
}
