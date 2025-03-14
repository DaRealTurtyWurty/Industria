package dev.turtywurty.industria.renderer.item;

import com.mojang.datafixers.util.Either;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.IndustriaClient;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.util.DrillHeadable;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class IndustriaDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, IdentifiableResourceReloadListener {
    public static final IndustriaDynamicItemRenderer INSTANCE = new IndustriaDynamicItemRenderer();

    private final WindTurbineBlockEntity windTurbine = new WindTurbineBlockEntity(BlockPos.ORIGIN, BlockInit.WIND_TURBINE.getDefaultState());
    private final OilPumpJackBlockEntity oilPumpJack = new OilPumpJackBlockEntity(BlockPos.ORIGIN, BlockInit.OIL_PUMP_JACK.getDefaultState());
    private final DrillBlockEntity drill = new DrillBlockEntity(BlockPos.ORIGIN, BlockInit.DRILL.getDefaultState());
    private final MotorBlockEntity motor = new MotorBlockEntity(BlockPos.ORIGIN, BlockInit.MOTOR.getDefaultState());
    private final UpgradeStationBlockEntity upgradeStation = new UpgradeStationBlockEntity(BlockPos.ORIGIN, BlockInit.UPGRADE_STATION.getDefaultState());
    private final MixerBlockEntity mixer = new MixerBlockEntity(BlockPos.ORIGIN, BlockInit.MIXER.getDefaultState());
    private final DigesterBlockEntity digester = new DigesterBlockEntity(BlockPos.ORIGIN, BlockInit.DIGESTER.getDefaultState());
    private final ClarifierBlockEntity clarifier = new ClarifierBlockEntity(BlockPos.ORIGIN, BlockInit.CLARIFIER.getDefaultState());

    private BakedModel seismicScanner;
    private final Map<DrillHeadable, Model> drillHeadModels = new HashMap<>();
    private final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    private final Map<Item, ? extends BlockEntity> blockEntities = Map.of(
            BlockInit.WIND_TURBINE.asItem(), windTurbine,
            BlockInit.OIL_PUMP_JACK.asItem(), oilPumpJack,
            BlockInit.DRILL.asItem(), drill,
            BlockInit.MOTOR.asItem(), motor,
            BlockInit.UPGRADE_STATION.asItem(), upgradeStation,
            BlockInit.MIXER.asItem(), mixer,
            BlockInit.DIGESTER.asItem(), digester,
            BlockInit.CLARIFIER.asItem(), clarifier
    );

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (this.blockEntityRenderDispatcher == null) {
            this.blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
            return;
        }

        if (this.blockEntities.containsKey(stack.getItem())) {
            BlockEntity blockEntity = this.blockEntities.get(stack.getItem());
            if (blockEntity != null) {
                matrices.push();
                matrices.scale(0.5F, 0.5F, 0.5F);
                this.blockEntityRenderDispatcher.renderEntity(blockEntity, matrices, vertexConsumers, light, overlay);
                matrices.pop();
            }

            return;
        }

        if (this.seismicScanner == null) {
            this.seismicScanner = MinecraftClient.getInstance().getBakedModelManager().getModel(IndustriaClient.SEISMIC_SCANNER);
        }

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        EntityModelLoader entityModelLoader = MinecraftClient.getInstance().getEntityModelLoader();
        if (stack.isOf(ItemInit.SEISMIC_SCANNER)) {
            matrices.push();
            SeismicScannerRendering.renderSeismicScanner(stack, itemRenderer, this.seismicScanner, mode, matrices, vertexConsumers, light, overlay);
            matrices.pop();
        }

        if(stack.getItem() instanceof DrillHeadable drillHeadable) {
            DrillHeadRegistry.DrillHeadClientData clientData = DrillHeadRegistry.getClientData(drillHeadable);
            if(clientData != null && clientData.renderDynamicItem()) {
                Model model = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> clientData.modelResolver().apply(Either.right(entityModelLoader)));
                Identifier textureLocation = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> clientData.textureLocation());
                matrices.push();
                matrices.translate(0.5f, 0.75f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.scale(0.5F, 0.5F, 0.5F);
                model.render(matrices, vertexConsumers.getBuffer(model.getLayer(textureLocation)), light, overlay);
                matrices.pop();
            }
        }
    }

    @Override
    public Identifier getFabricId() {
        return Industria.id("dynamic_item_renderer");
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            this.seismicScanner = null;
            this.drillHeadModels.clear();
            this.drillHeadTextures.clear();
        }, applyExecutor);
    }

}
