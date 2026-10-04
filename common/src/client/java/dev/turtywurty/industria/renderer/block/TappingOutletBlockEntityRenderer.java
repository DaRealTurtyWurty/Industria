package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.TappingOutletBlockEntity;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TappingOutletBlockEntityRenderer extends IndustriaBlockEntityRenderer<TappingOutletBlockEntity, TappingOutletBlockEntityRenderer.TappingOutletRenderState> {
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public TappingOutletBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    protected void onRender(TappingOutletRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || state.fluidVariant.isBlank())
            return;

        this.fluidRenderer.renderStream(state.fluidVariant,
                queue, matrices, light, overlay, level, state.blockPos,
                -2f / 16f, -38f / 16f, 0f,
                2f / 16f, -16f / 16f, 2f / 16f,
                false);

        this.fluidRenderer.renderStream(state.fluidVariant,
                queue, matrices, light, overlay, level, state.blockPos,
                -2f / 16f, -16f / 16f, -8f / 16f,
                2f / 16f, -14f / 16f, 2f / 16f,
                true);
    }

    @Override
    public TappingOutletRenderState createRenderState() {
        return new TappingOutletRenderState();
    }

    @Override
    public void extractRenderState(TappingOutletBlockEntity blockEntity, TappingOutletRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.fluidVariant = blockEntity.getPouringFluid();
    }

    public static class TappingOutletRenderState extends IndustriaBlockEntityRenderState {
        public ResourceVariant<Fluid> fluidVariant = ResourceTypes.FLUID.empty();

        public TappingOutletRenderState() {
            super(0);
        }
    }
}
