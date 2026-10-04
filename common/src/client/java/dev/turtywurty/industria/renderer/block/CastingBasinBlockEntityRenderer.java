package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CastingBasinBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CastingBasinBlockEntityRenderer extends IndustriaBlockEntityRenderer<CastingBasinBlockEntity, CastingBasinBlockEntityRenderer.CastingBasinRenderState> {
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public CastingBasinBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    protected void onRender(CastingBasinRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        float fill = state.hasOutput ? 1f : Math.clamp((float) state.fluidAmount / state.fluidCapacity, 0f, 1f);
        float surfaceHeight = (2f / 16f) + fill * (14f / 16f);
        if(state.fluidAmount> 0 && state.fluidCapacity > 0 && !state.fluidVariant.isBlank()) {
            this.fluidRenderer.renderTopFaceOnly(
                    state.fluidVariant,
                    queue,
                    matrices,
                    light,
                    overlay,
                    level,
                    state.blockPos,
                    -7f / 16f + 0.001f,
                    surfaceHeight - 1.5f,
                    -7f / 16f + 0.001f,
                    7f / 16f - 0.001f,
                    7f / 16f - 0.001f
            );
        }

        if(state.hasOutput && state.castingBlockState.isAir()) {
            matrices.pushPose();
            matrices.mulPose(Axis.XP.rotationDegrees(180f));
            matrices.translate(0f, -0.875f, 0f);
            matrices.scale(0.95f, 0.95f, 0.95f);
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }

        if(state.coolingFraction > 0 && !state.castingBlockState.isAir()) {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getModelManager().getBlockStateModelSet()
                    .getParticleMaterial(state.castingBlockState).sprite();

            int alpha = Math.round(Math.clamp(state.coolingFraction, 0f, 1f) * 255f);
            int color = (alpha << 24) | 0x00FFFFFF;

            this.fluidRenderer.renderTexturedTopFace(
                    sprite,
                    color,
                    queue,
                    matrices,
                    light,
                    overlay,
                    -7f / 16f + 0.001f,
                    surfaceHeight - 1.5f + 0.001f,
                    -7f / 16f + 0.001f,
                    7f / 16f - 0.001f,
                    7f / 16f - 0.001f
            );
        }
    }

    @Override
    public CastingBasinRenderState createRenderState() {
        return new CastingBasinRenderState();
    }

    @Override
    public void extractRenderState(CastingBasinBlockEntity blockEntity, CastingBasinRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        SyncingFluidStorage fluidStorage = blockEntity.getFluidStorage();
        state.fluidVariant = fluidStorage.getResource();
        state.fluidAmount = fluidStorage.getAmount();
        state.fluidCapacity = fluidStorage.getCapacity();

        state.coolingFraction = blockEntity.getCoolingFraction();
        state.castingBlockState = blockEntity.getCastingBlockState();

        ItemStack stack = blockEntity.getInventory().getItem(0);
        state.hasOutput = !stack.isEmpty();
        if(state.hasOutput && stack.getItem() instanceof BlockItem blockItem) {
            state.coolingFraction = 1f;
            state.castingBlockState = blockItem.getBlock().defaultBlockState();
        }

        state.updateItemRenderState(
                0,
                this,
                blockEntity,
                stack,
                ItemDisplayContext.FIXED
        );
    }

    public static class CastingBasinRenderState extends IndustriaBlockEntityRenderState {
        public ResourceVariant<Fluid> fluidVariant = ResourceTypes.FLUID.empty();
        public long fluidAmount = 0;
        public long fluidCapacity = 0;
        public boolean hasOutput;
        public float coolingFraction;
        public BlockState castingBlockState = Blocks.AIR.defaultBlockState();

        public CastingBasinRenderState() {
            super(1);
        }
    }
}
