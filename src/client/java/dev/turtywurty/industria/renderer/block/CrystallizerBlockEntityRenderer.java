package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.model.CrystallizerModel;
import dev.turtywurty.industria.state.CrystallizerRenderState;
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

import java.util.Random;

public class CrystallizerBlockEntityRenderer extends IndustriaBlockEntityRenderer<CrystallizerBlockEntity, CrystallizerRenderState> {
    private static final Vec3[] OUTPUT_ITEM_POSITIONS = new Vec3[64];

    static {
        var random = new Random(438489438L);

        float minX = -18f / 16f + 0.125f;
        float minY = -0.25f;
        float minZ = -18f / 16f + 0.125f;
        float maxX = 18f / 16f - 0.125f;
        float maxY = 46f / 16f - 0.625f;
        float maxZ = 18f / 16f - 0.125f;

        for (int i = 0; i < OUTPUT_ITEM_POSITIONS.length; i++) {
            int attempts = 0;
            while (true) {
                Vec3 generatedPos = generatePosition(random, minX, -minY, minZ, maxX, -maxY, maxZ);
                if (!intersects(generatedPos, i, 0.35) || attempts++ > 100) {
                    OUTPUT_ITEM_POSITIONS[i] = generatedPos;
                    break;
                }
            }
        }
    }

    private static boolean intersects(Vec3 pos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            if (pos.distanceTo(OUTPUT_ITEM_POSITIONS[i]) < radius)
                return true;
        }

        return false;
    }

    private static Vec3 generatePosition(Random random, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new Vec3(
                minX + random.nextFloat() * (maxX - minX),
                minY + random.nextFloat() * (maxY - minY),
                minZ + random.nextFloat() * (maxZ - minZ)
        );
    }

    private final CrystallizerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public CrystallizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new CrystallizerModel(context.bakeLayer(CrystallizerModel.LAYER_LOCATION));
    }

    @Override
    public CrystallizerRenderState createRenderState() {
        return new CrystallizerRenderState();
    }

    @Override
    public void extractRenderState(CrystallizerBlockEntity blockEntity, CrystallizerRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.nextOutputItemStack = blockEntity.getNextOutputItemStack();
        state.crystalFluidStorage = blockEntity.getCrystalFluidStorage();
        state.waterFluidStorage = blockEntity.getWaterFluidStorage();
        state.updateItemRenderState(0, this, blockEntity, state.nextOutputItemStack);
    }

    @Override
    protected void onRender(CrystallizerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(CrystallizerModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        renderNextOutputItem(state, matrices, queue, light, overlay);
        renderFluids(state, matrices, queue, light, overlay);
    }

    private void renderNextOutputItem(CrystallizerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ItemStack itemStack = state.nextOutputItemStack;
        if (itemStack.isEmpty())
            return;

        int count = Math.min(itemStack.getCount(), OUTPUT_ITEM_POSITIONS.length);
        for (int i = 0; i < count; i++) {
            int index = (OUTPUT_ITEM_POSITIONS.length / count) * i;

            Vec3 position = OUTPUT_ITEM_POSITIONS[index];
            float progress = (float) state.progress / state.maxProgress;
            float scale = 0.05f + 0.35f * progress;

            matrices.pushPose();
            matrices.translate(position);
            matrices.scale(scale, scale, scale);
            matrices.mulPose(Axis.YP.rotationDegrees((float) ((i + 1) * 360) / count));
            matrices.mulPose(Axis.XP.rotationDegrees(180));
            state.renderItemRenderState(0, matrices, queue);
            matrices.mulPose(Axis.YP.rotationDegrees(90));
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }
    }

    private void renderFluids(CrystallizerRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        Level world = Minecraft.getInstance().level;
        this.fluidRenderer.render(state.crystalFluidStorage,
                queue, matrices,
                light, overlay,
                world, state.blockPos,
                -18f / 16f, -0.5001f, -18f / 16f,
                18f / 16f, 46f, 18f / 16f - 0.001f, 0x40000000, ColorMode.SUBTRACTION);

        this.fluidRenderer.render(state.waterFluidStorage,
                queue, matrices,
                light, overlay,
                world, state.blockPos,
                -18f / 16f, -0.5f, -18f / 16f,
                18f / 16f, 46f, 18f / 16f);
    }
}
