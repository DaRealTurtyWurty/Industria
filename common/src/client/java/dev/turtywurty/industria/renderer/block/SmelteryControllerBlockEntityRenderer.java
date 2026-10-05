package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.SmelteryControllerBlockEntity;
import dev.turtywurty.industria.blockentity.util.SmelteryChamberRenderData;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.turtymultiloader.transfer.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SmelteryControllerBlockEntityRenderer implements BlockEntityRenderer<SmelteryControllerBlockEntity, SmelteryControllerBlockEntityRenderer.SmelteryRenderState> {
    private final InWorldFluidRenderingComponent fluidRenderingComponent = new InWorldFluidRenderingComponent();

    public SmelteryControllerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SmelteryControllerBlockEntityRenderer.SmelteryRenderState createRenderState() {
        return new SmelteryControllerBlockEntityRenderer.SmelteryRenderState();
    }

    @Override
    public void extractRenderState(SmelteryControllerBlockEntity blockEntity, SmelteryControllerBlockEntityRenderer.SmelteryRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.chambers = blockEntity.getRenderSnapshot();
    }

    @Override
    public void submit(SmelteryControllerBlockEntityRenderer.SmelteryRenderState smelteryRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        double maxDistancedSquared = (double) getViewDistance() * (double) getViewDistance();
        for (SmelteryChamberRenderData chamber : smelteryRenderState.chambers) {
            if (chamber.capacity() <= 0 || chamber.fluid().isEmpty() || (!chamber.molten() && chamber.solidBlockState().isAir()))
                continue;

            AABB bounds = chamber.bounds();
            //noinspection ConstantValue - IntelliJ is just blatantly wrong here, cameraRenderState.cullFrustum is not final
            if (bounds.distanceToSqr(cameraRenderState.pos) >= maxDistancedSquared
                    || (cameraRenderState.cullFrustum != null && !cameraRenderState.cullFrustum.isVisible(bounds)))
                continue;

            double fill = Math.clamp((double) chamber.fluid().amount() / chamber.capacity(), 0.0, 1.0);
            double filledHeight = bounds.getYsize() * fill;

            FluidRenderUtils.GuiFluidRenderData renderData;
            if (chamber.molten()) {
                renderData = FluidRenderUtils.getRenderData(
                        chamber.fluid().variant(),
                        level,
                        BlockPos.containing(bounds.getCenter())
                );
            } else {
                renderData = new FluidRenderUtils.GuiFluidRenderData(
                        Minecraft.getInstance().getModelManager()
                                .getBlockStateModelSet()
                                .getParticleMaterial(chamber.solidBlockState())
                                .sprite(),
                        0xFFFFFFFF
                );
            }
            if (renderData == null || renderData.stillSprite() == null)
                continue;

            float x1 = (float) (bounds.minX - smelteryRenderState.blockPos.getX() + 0.001f);
            float y1 = (float) (bounds.minY - smelteryRenderState.blockPos.getY());
            float z1 = (float) (bounds.minZ - smelteryRenderState.blockPos.getZ() + 0.001f);
            float x2 = (float) (bounds.maxX - smelteryRenderState.blockPos.getX() - 0.001f);
            float y2 = (float) (bounds.minY - smelteryRenderState.blockPos.getY() + Math.min(filledHeight, bounds.getYsize() - 0.001f));
            float z2 = (float) (bounds.maxZ - smelteryRenderState.blockPos.getZ() - 0.001f);

            int light = LevelRenderer.getLightCoords(level, BlockPos.containing(bounds.getCenter()));
            if (chamber.molten()) {
                light = (light & 0xF00000) | (Math.max((light >> 4) & 15, FluidVariantAttributes.getLuminance(chamber.fluid().variant())) << 4);
            }
            this.fluidRenderingComponent.renderTexturedCuboid(renderData.stillSprite(), renderData.tintColor(),
                    submitNodeCollector, poseStack,
                    light, OverlayTexture.NO_OVERLAY,
                    x1, y1, z1,
                    x2, y2, z2);
        }
    }

    @Override
    public boolean shouldRender(SmelteryControllerBlockEntity blockEntity, Vec3 cameraPosition) {
        double maxDistanceSquared = (double) getViewDistance() * (double) getViewDistance();
        for (SmelteryChamberRenderData renderData : blockEntity.getRenderSnapshot()) {
            if (renderData.bounds().distanceToSqr(cameraPosition) <= maxDistanceSquared)
                return true;
        }

        return false;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class SmelteryRenderState extends BlockEntityRenderState {
        public List<SmelteryChamberRenderData> chambers = List.of();
    }
}
