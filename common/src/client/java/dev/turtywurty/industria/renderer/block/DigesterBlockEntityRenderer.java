package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
import dev.turtywurty.industria.model.DigesterModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.client.SlurryRenderHandler;
import dev.turtywurty.slurryapi.client.SlurryRenderHandlerRegistry;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DigesterBlockEntityRenderer extends IndustriaBlockEntityRenderer<DigesterBlockEntity, DigesterBlockEntityRenderer.DigesterRenderState> {
    private static final float WINDOW_LEFT = -10f / 16f;
    private static final float WINDOW_RIGHT = 10f / 16f;
    private static final float TANK_BOTTOM = 1.0f;
    private static final float TANK_TOP = -4f / 16f;
    private static final float FLUID_RADIUS = 30f / 16f - 0.001f;
    private static final float FLUID_FRONT = -FLUID_RADIUS;
    private static final float FLUID_CORNER_CUT = 19f / 16f;
    private final DigesterModel model;
    private final DigesterModel glassModel;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public DigesterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new DigesterModel(context.bakeLayer(DigesterModel.LAYER_LOCATION));
        this.glassModel = new DigesterModel(context.bakeLayer(DigesterModel.LAYER_LOCATION), true);
    }

    @Override
    protected void onRender(DigesterRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, state,
                matrices, this.model.renderType(DigesterModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
        renderSlurry(state, matrices, queue, light, overlay);
        queue.submitModel(this.glassModel, state,
                matrices, this.glassModel.renderType(DigesterModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }

    private void renderSlurry(DigesterRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || state.amount <= 0 || state.capacity <= 0)
            return;

        if (state.slurry == null || state.slurry.isBlank())
            return;
        SlurryRenderHandler handler = SlurryRenderHandlerRegistry.get(state.slurry.value());
        if (handler == null)
            return;
        TextureAtlasSprite sprite = handler.getSprite(level, state.blockPos);
        int color = 0xFF000000 | (handler.getColor(level, state.blockPos) & 0xFFFFFF);

        float fill = Math.clamp((float) state.amount / state.capacity, 0f, 1f);
        float surfaceY = TANK_BOTTOM + (TANK_TOP - TANK_BOTTOM) * fill;
        this.fluidRenderer.drawTiledXYQuadOnly(sprite, color, queue, matrices, light, overlay,
                WINDOW_LEFT, -TANK_BOTTOM, -FLUID_FRONT, WINDOW_RIGHT, -surfaceY, -FLUID_FRONT, true);

        this.fluidRenderer.renderOctagonalTopFaceOnly(sprite, color, queue, matrices, light, overlay,
                surfaceY, FLUID_RADIUS, FLUID_CORNER_CUT);
    }

    @Override
    public DigesterRenderState createRenderState() {
        return new DigesterRenderState();
    }

    @Override
    public void extractRenderState(DigesterBlockEntity blockEntity, DigesterRenderState state, float tickProgress,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        InputSlurryStorage tank = blockEntity.getInputSlurryStorage();
        state.slurry = tank.getResource();
        state.amount = tank.getAmount();
        state.capacity = tank.capacity(0, tank.getResource());
        int duration = blockEntity.getMaxProgress();
        float progress = blockEntity.getProgress() + (blockEntity.isRunning() ? tickProgress : 0f);
        state.bladeRotation = duration > 0
                ? Math.clamp(progress / duration, 0f, 1f) * (float) (Math.PI * 8)
                : 0f;
    }

    public static class DigesterRenderState extends IndustriaBlockEntityRenderState {
        public float bladeRotation;
        private ResourceVariant<Slurry> slurry;
        private long amount;
        private long capacity;

        public DigesterRenderState() {
            super(0);
        }
    }
}
