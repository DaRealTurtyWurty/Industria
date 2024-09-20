package dev.turtywurty.industria.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.IndustriaClient;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DoublePositionSource;
import dev.turtywurty.industria.util.IndustriaFluidRenderer;
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
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import java.util.List;
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

                renderThirdPersonHologram(stack, matrices, vertexConsumers, light);
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

                    renderFirstPersonHologram(stack, matrices, vertexConsumers, light);
                }
            }

            matrices.pop();
        }
    }

    private static void renderThirdPersonHologram(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if(stack.contains(ComponentTypeInit.FLUID_POCKETS)) {
            List<WorldFluidPocketsState.FluidPocket> fluidPockets = stack.get(ComponentTypeInit.FLUID_POCKETS);
            if(fluidPockets == null || fluidPockets.isEmpty())
                return;

            WorldFluidPocketsState.FluidPocket fluidPocket = fluidPockets.getFirst();
            if(fluidPocket != null && !fluidPocket.isEmpty()) {
                FluidState fluidState = fluidPocket.fluidState();
                List<BlockPos> positions = fluidPocket.fluidPositions();

                int minX = fluidPocket.minX();
                int minY = fluidPocket.minY();
                int minZ = fluidPocket.minZ();
                int maxX = fluidPocket.maxX();
                int maxY = fluidPocket.maxY();
                int maxZ = fluidPocket.maxZ();

                int width = maxX - minX;
                int height = maxY - minY;
                int depth = maxZ - minZ;

                matrices.push();
                matrices.translate(width / 32f, height / 32f, depth / 32f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.translate(-0.125f, (height / 16f) + 0.125f, -0.125f);

                var buffer = new EndableVertexConsumer((VertexConsumerProvider.Immediate) vertexConsumers);
                buffer.end();

                RenderSystem.setShaderColor(1.0F, 2.0F, 5.0F, 0.25F);
                for (BlockPos position : positions) {
                    int relativeX = position.getX() - minX;
                    int relativeY = position.getY() - minY;
                    int relativeZ = -(position.getZ() - minZ);

                    matrices.push();
                    matrices.translate(relativeX / 16f, relativeY / 16f, relativeZ / 16f);
                    matrices.scale(0.0625F, 0.0625F, 0.0625F);

                    IndustriaFluidRenderer.renderFluidBox(fluidState,
                            0.0F, 0.0F, 0.0F,
                            1.0F, 1.0F, 1.0F,
                            buffer, matrices, light, true,
                            1.0f, 1.0f, 1.0f, 1.0f, IndustriaFluidRenderer.ColorMode.MULTIPLICATION);

                    matrices.pop();
                }

                buffer.end();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                matrices.pop();
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderFirstPersonHologram(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if(stack.contains(ComponentTypeInit.FLUID_POCKETS)) {
            List<WorldFluidPocketsState.FluidPocket> fluidPockets = stack.get(ComponentTypeInit.FLUID_POCKETS);
            if(fluidPockets == null || fluidPockets.isEmpty())
                return;

            WorldFluidPocketsState.FluidPocket fluidPocket = fluidPockets.getFirst();
            if (fluidPocket != null && !fluidPocket.isEmpty()) {
                FluidState fluidState = fluidPocket.fluidState();
                List<BlockPos> positions = fluidPocket.fluidPositions();

                int minX = fluidPocket.minX();
                int minY = fluidPocket.minY();
                int minZ = fluidPocket.minZ();
                int maxX = fluidPocket.maxX();
                int maxY = fluidPocket.maxY();
                int maxZ = fluidPocket.maxZ();

                int width = maxX - minX;
                int height = maxY - minY;
                int depth = maxZ - minZ;

                matrices.push();
                matrices.translate(width / 32f, height / 32f, depth / 32f);
                matrices.translate(-1.125f, height / 16f + 0.125f, 0f);

                var buffer = new EndableVertexConsumer((VertexConsumerProvider.Immediate) vertexConsumers);
                buffer.end();

                RenderSystem.setShaderColor(1.0F, 2.0F, 5.0F, 0.25F);
                for (BlockPos position : positions) {
                    int relativeX = position.getX() - minX;
                    int relativeY = position.getY() - minY;
                    int relativeZ = -(position.getZ() - minZ);

                    matrices.push();
                    matrices.translate(relativeX / 32f, relativeY / 32f, relativeZ / 32f);
                    matrices.scale(0.03125F, 0.03125F, 0.03125F);

                    IndustriaFluidRenderer.renderFluidBox(fluidState,
                            0.0F, 0.0F, 0.0F,
                            1.0F, 1.0F, 1.0F,
                            buffer, matrices, light, true,
                            1.0f, 1.0f, 1.0f, 1.0f, IndustriaFluidRenderer.ColorMode.MULTIPLICATION);

                    matrices.pop();
                }

                buffer.end();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                matrices.pop();
            }
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

    public static class EndableVertexConsumer implements VertexConsumerProvider {
        private final VertexConsumerProvider.Immediate source;

        public EndableVertexConsumer(VertexConsumerProvider.Immediate source) {
            this.source = source;
        }

        @Override
        public VertexConsumer getBuffer(RenderLayer layer) {
            return this.source.getBuffer(layer);
        }

        public void end() {
            this.source.draw();
        }
    }
}
