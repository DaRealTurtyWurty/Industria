package dev.turtywurty.industria.renderer;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.IndustriaClient;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.DoublePositionSource;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class IndustriaDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, IdentifiableResourceReloadListener {
    public static final IndustriaDynamicItemRenderer INSTANCE = new IndustriaDynamicItemRenderer();

    private final WindTurbineBlockEntity windTurbine = new WindTurbineBlockEntity(BlockPos.ORIGIN, BlockInit.WIND_TURBINE.getDefaultState());
    private final OilPumpJackBlockEntity oilPumpJack = new OilPumpJackBlockEntity(BlockPos.ORIGIN, BlockInit.OIL_PUMP_JACK.getDefaultState());
    private BakedModel seismicScanner;

    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    private final Map<Item, ? extends BlockEntity> blockEntities = Map.of(
            BlockInit.WIND_TURBINE.asItem(), windTurbine,
            BlockInit.OIL_PUMP_JACK.asItem(), oilPumpJack
    );

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (this.blockEntityRenderDispatcher == null) {
            this.blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
            return;
        }

        matrices.push();
        matrices.scale(0.5F, 0.5F, 0.5F);
        if (this.blockEntities.containsKey(stack.getItem())) {
            BlockEntity blockEntity = this.blockEntities.get(stack.getItem());
            if (blockEntity != null) {
                this.blockEntityRenderDispatcher.renderEntity(blockEntity, matrices, vertexConsumers, light, overlay);
            }
        }
        matrices.pop();

        if(this.seismicScanner == null) {
            this.seismicScanner = MinecraftClient.getInstance().getBakedModelManager().getModel(IndustriaClient.SEISMIC_SCANNER);
        }

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        if(stack.isOf(ItemInit.SEISMIC_SCANNER)) {
            matrices.push();
            matrices.translate(0.5, 0.5, 0.5);

            if(mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
                matrices.translate(-0.5, -0.5, -0.5);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-13.0F));
                matrices.translate(-0.35, 0.5, 0.0);

                RenderLayer layer = RenderLayers.getItemLayer(stack, true);
                VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, layer, true, stack.hasGlint());

                itemRenderer.renderBakedItemModel(this.seismicScanner, stack, light, overlay, matrices, consumer);
            } else {
                itemRenderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, this.seismicScanner);

                if(mode.isFirstPerson()) {
                    PlayerEntity player = MinecraftClient.getInstance().player;
                    if (player == null)
                        return;

                    if(player.getRandom().nextInt(30) == 0) {
                        float playerYaw = player.headYaw;
                        float playerPitch = player.prevPitch;
                        float distance = 1.5f;

                        Vec3d eyePos = player.getEyePos();
                        double startX = eyePos.x;
                        double startY = eyePos.y - 0.25f;
                        double startZ = eyePos.z;

                        double targetX = startX - Math.sin(Math.toRadians(playerYaw)) * distance;
                        double targetY = startY - Math.tan(Math.toRadians(playerPitch)) * distance;
                        double targetZ = startZ + Math.cos(Math.toRadians(playerYaw)) * distance;

                        MinecraftClient.getInstance().particleManager.addParticle(
                                new VibrationParticleEffect(new DoublePositionSource(targetX, targetY, targetZ), 20),
                                startX,
                                startY,
                                 startZ,
                                0.0D,
                                0.0D,
                                0.0D);
                    }
                }
            }

            matrices.pop();
        }
    }


    @Override
    public Identifier getFabricId() {
        return Industria.id("dynamic_item_renderer");
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            this.seismicScanner = null;
        }, applyExecutor);
    }
}
