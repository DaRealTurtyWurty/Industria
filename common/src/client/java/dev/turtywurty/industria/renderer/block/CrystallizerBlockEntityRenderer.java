package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.CrystallizerModel;
import dev.turtywurty.industria.state.CrystallizerRenderState;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import dev.turtywurty.turtymultiloader.transfer.fluid.FluidVariantAttributes;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CrystallizerBlockEntityRenderer
    extends
        IndustriaBlockEntityRenderer<CrystallizerBlockEntity, CrystallizerRenderState> {
    private static final Vec3[] OUTPUT_ITEM_POSITIONS = new Vec3[64];
    private static final float TANK_BOTTOM = 4.9f / 16f;
    private static final float TANK_TOP = -27.1f / 16f;
    private static final float FLUID_RADIUS = 16.3f / 16f - 0.001f;
    private static final float FLUID_CORNER_CUT = FLUID_RADIUS * (2f - (float) Math.sqrt(2));
    private static final float WINDOW_HALF_WIDTH = 6f / 16f;

    static {
        var random = new Random(438489438L);

        for (int i = 0; i < OUTPUT_ITEM_POSITIONS.length; i++) {
            int attempts = 0;
            while (true) {
                Vec3 generatedPos = generatePosition(random);
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

    private static Vec3 generatePosition(Random random) {
        float angle = random.nextFloat() * Mth.TWO_PI;
        float radius = Mth.sqrt(Mth.lerp(random.nextFloat(), 6f * 6f, 12.5f * 12.5f)) / 16f;
        return new Vec3(
            Mth.cos(angle) * radius,
            (24f - Mth.lerp(random.nextFloat(), 24f, 47f)) / 16f,
            Mth.sin(angle) * radius);
    }

    private final CrystallizerModel model;
    private final CrystallizerModel glassModel;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public CrystallizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new CrystallizerModel(context.bakeLayer(CrystallizerModel.LAYER_LOCATION));
        this.glassModel = new CrystallizerModel(context.bakeLayer(CrystallizerModel.LAYER_LOCATION), true);
    }

    @Override
    public CrystallizerRenderState createRenderState() {
        return new CrystallizerRenderState();
    }

    @Override
    public void extractRenderState(
        CrystallizerBlockEntity blockEntity,
        CrystallizerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.nextOutputItemStack = blockEntity.getNextOutputItemStack();
        state.crystalFluidStorage = blockEntity.getCrystalFluidStorage();
        state.waterFluidStorage = blockEntity.getWaterFluidStorage();
        state.updateItemRenderState(0, this, blockEntity, state.nextOutputItemStack);
    }

    @Override
    protected void onRender(
        CrystallizerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ModelSubmitUtils.submitModel(queue, this.model, null,
            matrices, this.model.renderType(CrystallizerModel.TEXTURE_LOCATION),
            light, overlay, 0, state.breakProgress);

        renderNextOutputItem(state, matrices, queue, light, overlay);
        renderFluids(state, matrices, queue, light, overlay);

        ModelSubmitUtils.submitModel(queue, this.glassModel, null,
            matrices, this.glassModel.renderType(CrystallizerModel.TEXTURE_LOCATION),
            light, overlay, 0, state.breakProgress);
    }

    private void renderNextOutputItem(
        CrystallizerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ItemStack itemStack = state.nextOutputItemStack;
        if (itemStack.isEmpty())
            return;

        int count = Math.min(itemStack.getCount(), OUTPUT_ITEM_POSITIONS.length);
        for (int i = 0; i < count; i++) {
            int index = (OUTPUT_ITEM_POSITIONS.length / count) * i;

            Vec3 position = OUTPUT_ITEM_POSITIONS[index];
            float progress = state.maxProgress > 0
                ? Mth.clamp((float) state.progress / state.maxProgress, 0f, 1f)
                : 0f;
            float scale = 0.05f + 0.35f * progress;

            matrices.pushPose();
            matrices.translate(position);
            matrices.scale(scale, scale, scale);
            matrices.rotate(Axis.YP.rotationDegrees((float) ((i + 1) * 360) / count));
            matrices.rotate(Axis.XP.rotationDegrees(180));
            state.renderItemRenderState(0, matrices, queue);
            matrices.rotate(Axis.YP.rotationDegrees(90));
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }
    }

    private void renderFluids(
        CrystallizerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        Level world = Minecraft.getInstance().level;
        if (world == null)
            return;

        renderFluid(state.waterFluidStorage, state, matrices, queue, light, overlay, world,
            0xFFFFFFFF, ColorMode.MULTIPLICATION, 0f);
        renderFluid(state.crystalFluidStorage, state, matrices, queue, light, overlay, world,
            0x40000000, ColorMode.SUBTRACTION, 0.0001f);
    }

    private void renderFluid(
        @Nullable SyncingFluidStorage tank,
        CrystallizerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        Level world,
        int color,
        ColorMode colorMode,
        float surfaceOffset
    ) {
        if (tank == null || tank.getAmount() <= 0 || tank.getCapacity() <= 0)
            return;

        var fluid = tank.getResource();
        FluidRenderUtils.GuiFluidRenderData renderData = FluidRenderUtils.getRenderData(fluid, world, state.blockPos);
        if (renderData == null)
            return;

        float fill = Mth.clamp((float) tank.getAmount() / tank.getCapacity(), 0f, 1f);
        float surfaceY = Mth.lerp(fill, TANK_BOTTOM, TANK_TOP) - surfaceOffset;
        int fluidColor = ColorMode.modifyColor(renderData.tintColor(), color, colorMode);
        int fluidLight = (light & 0xF00000)
            | (Math.max((light >> 4) & 15, FluidVariantAttributes.getLuminance(fluid)) << 4);

        this.fluidRenderer.drawTiledXYQuadOnly(renderData.stillSprite(), fluidColor,
            queue, matrices, fluidLight, overlay,
            -WINDOW_HALF_WIDTH, -TANK_BOTTOM, -FLUID_RADIUS - surfaceOffset,
            WINDOW_HALF_WIDTH, -surfaceY, -FLUID_RADIUS - surfaceOffset);

        this.fluidRenderer.renderOctagonalTopFaceOnly(renderData.stillSprite(), fluidColor,
            queue, matrices, fluidLight, overlay, surfaceY, FLUID_RADIUS, FLUID_CORNER_CUT);
    }
}
