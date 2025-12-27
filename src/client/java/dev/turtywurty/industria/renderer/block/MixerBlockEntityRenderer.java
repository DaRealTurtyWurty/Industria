package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.model.MixerModel;
import dev.turtywurty.industria.state.MixerRenderState;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MixerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MixerBlockEntity, MixerRenderState> {
    private final MixerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public MixerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new MixerModel(context.getLayerModelPart(MixerModel.LAYER_LOCATION));

        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    public MixerRenderState createRenderState() {
        return new MixerRenderState();
    }

    @Override
    public void updateRenderState(MixerBlockEntity blockEntity, MixerRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.isMixing = blockEntity.isMixing();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputInventory = blockEntity.getInputInventory();
        state.fluidTank = blockEntity.getInputFluidTank();
        state.mixingItemPositions = blockEntity.mixingItemPositions;
        for (int i = 0; i < 6; i++) {
            state.updateItemRenderState(i, this, blockEntity, state.inputInventory.heldStacks.get(i));
        }
    }

    @Override
    protected void onRender(MixerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        float stirringRotation = 0f;
        if(state.isMixing) {
            stirringRotation = (state.progress / (float) state.maxProgress) * (float) Math.PI * 2f * 4f;
        }

        queue.submitModel(this.model,
                new MixerModel.MixerModelRenderState(stirringRotation),
                matrices, this.model.getLayer(MixerModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);

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

        World world = MinecraftClient.getInstance().world;
        for (int index = 0; index < state.inputInventory.heldStacks.size(); index++) {
            ItemStack stack = state.inputInventory.heldStacks.get(index);
            if (!stack.isEmpty()) {
                matrices.push();

                Vec3d position = state.mixingItemPositions.get(index);

                if (state.isMixing) {
                    float angle = (float) (2 * Math.PI * index / state.inputInventory.heldStacks.size()); // Evenly space items around circle
                    float rotationSpeed = 0.1f;
                    float timeAngle = (float) world.getTime() * rotationSpeed;

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
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotation(world.getTime() * 0.25f));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotation(world.getTime() * 0.25f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotation(world.getTime() * 0.25f));
                }

                state.renderItemRenderState(index, matrices, queue);
                matrices.pop();
            }
        }

        // TODO: Temperature-based color
        this.fluidRenderer.render(state.fluidTank,
                queue, matrices,
                light, overlay,
                world, state.pos,
                x1, y1, z1,
                x2, maxHeightPixels, z2,
                0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }
}
