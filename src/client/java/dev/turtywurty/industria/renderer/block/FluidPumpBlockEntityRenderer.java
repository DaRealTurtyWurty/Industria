package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.FluidPumpBlockEntity;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FluidPumpBlockEntityRenderer extends IndustriaBlockEntityRenderer<FluidPumpBlockEntity, FluidPumpBlockEntityRenderer.FluidPumpRenderState> {
    private static final float START_HEIGHT = 17f / 16f;
    private static final float START_WIDTH = 6.5f / 16f;
    private static final float DEPTH = 1f / 16f;
    private static final float END_HEIGHT = START_HEIGHT + 10f / 16f;
    private static final float END_WIDTH = START_WIDTH + 3f / 16f;

    private final InWorldFluidRenderingComponent inWorldFluidRenderer = new InWorldFluidRenderingComponent();

    public FluidPumpBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void onRender(FluidPumpRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || state.fluidVariant == null || state.amount <= 0)
            return;

        for (Direction face : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
            float width = END_WIDTH - START_WIDTH;
            float x0 = -width / 2f;
            float x1 = width / 2f;
            float y1 = (float) state.amount / (float) state.capacity * (END_HEIGHT - START_HEIGHT) + START_HEIGHT;

            matrices.pushPose();
            matrices.mulPose(Axis.YP.rotationDegrees(switch (face) {
                case NORTH -> 0f;
                case EAST -> 90f;
                case SOUTH -> 180f;
                case WEST -> 270f;
                default -> throw new IllegalStateException("Unexpected horizontal face: " + face);
            }));
            matrices.translate(0f, 1.5f, 0.5f);

            this.inWorldFluidRenderer.drawTiledXYQuadOnly(
                    state.fluidVariant, queue, matrices,
                    light, overlay,
                    level, state.blockPos,
                    x0, START_HEIGHT, DEPTH - 0.001f,
                    x1, y1, DEPTH - 0.001f
            );
            matrices.popPose();
        }
    }

    @Override
    public FluidPumpRenderState createRenderState() {
        return new FluidPumpRenderState();
    }

    @Override
    public void extractRenderState(FluidPumpBlockEntity blockEntity, FluidPumpRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        SingleFluidStorage fluidTank = blockEntity.getFluidTank();
        state.fluidVariant = fluidTank.getResource();
        state.amount = fluidTank.getAmount();
        state.capacity = fluidTank.getCapacity();
    }

    public static class FluidPumpRenderState extends IndustriaBlockEntityRenderState {
        private FluidVariant fluidVariant;
        private long amount;
        private long capacity;

        public FluidPumpRenderState() {
            super(0);
        }
    }
}
