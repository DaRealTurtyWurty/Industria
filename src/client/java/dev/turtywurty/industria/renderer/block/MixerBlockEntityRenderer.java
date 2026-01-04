package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.model.MixerModel;
import dev.turtywurty.industria.state.MixerRenderState;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MixerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MixerBlockEntity, MixerRenderState> {
    private final MixerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public MixerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new MixerModel(context.bakeLayer(MixerModel.LAYER_LOCATION));

        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    public MixerRenderState createRenderState() {
        return new MixerRenderState();
    }

    @Override
    public void extractRenderState(MixerBlockEntity blockEntity, MixerRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.isMixing = blockEntity.isMixing();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputInventory = blockEntity.getInputInventory();
        state.fluidTank = blockEntity.getInputFluidTank();
        state.mixingItemPositions = blockEntity.mixingItemPositions;
        for (int i = 0; i < 6; i++) {
            state.updateItemRenderState(i, this, blockEntity, state.inputInventory.items.get(i));
        }
    }

    @Override
    protected void onRender(MixerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        float stirringRotation = 0f;
        if(state.isMixing) {
            stirringRotation = (state.progress / (float) state.maxProgress) * (float) Math.PI * 2f * 4f;
        }

        queue.submitModel(this.model,
                new MixerModel.MixerModelRenderState(stirringRotation),
                matrices, this.model.renderType(MixerModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        float widthReduction = 2f / 16f;
        float x1 = -1f + widthReduction;
        float y1 = -1.25f - (2f / 16f);
        float z1 = -1f + widthReduction;
        float x2 = 1f - widthReduction;
        float maxHeightPixels = 44f;
        float z2 = 1f - widthReduction;

        float width = x2 - x1;
        float depth = z2 - z1;

        float progress = (float) state.progress / state.maxProgress;

        Level world = Minecraft.getInstance().level;
        for (int index = 0; index < state.inputInventory.items.size(); index++) {
            ItemStack stack = state.inputInventory.items.get(index);
            if (!stack.isEmpty()) {
                matrices.pushPose();

                Vec3 position = state.mixingItemPositions.get(index);

                if (state.isMixing) {
                    float angle = (float) (2 * Math.PI * index / state.inputInventory.items.size()); // Evenly space items around circle
                    float rotationSpeed = 0.1f;
                    float timeAngle = (float) world.getGameTime() * rotationSpeed;

                    float radius = Math.max(width, depth) * 0.5f - 0.375f;

                    double xOffset = radius * Math.sin(angle + timeAngle);
                    double yOffset = Math.sin(angle + index + timeAngle * 0.2f) - 1f;
                    double zOffset = radius * Math.cos(angle + timeAngle);

                    position = position.add(xOffset, yOffset, zOffset);
                }

                matrices.translate(position.x, position.y, position.z);
                matrices.scale(0.5f, 0.5f, 0.5f);

                if (state.isMixing) {
                    matrices.scale(1f - progress, 1f - progress, 1f - progress); // TODO: Make them fade away instead (maybe? :3)
                    matrices.mulPose(Axis.YP.rotation(world.getGameTime() * 0.25f));
                    matrices.mulPose(Axis.XP.rotation(world.getGameTime() * 0.25f));
                    matrices.mulPose(Axis.ZP.rotation(world.getGameTime() * 0.25f));
                }

                state.renderItemRenderState(index, matrices, queue);
                matrices.popPose();
            }
        }

        // TODO: Temperature-based color
        this.fluidRenderer.render(state.fluidTank,
                queue, matrices,
                light, overlay,
                world, state.blockPos,
                x1, y1, z1,
                x2, maxHeightPixels, z2,
                0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }
}
