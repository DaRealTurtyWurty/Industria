package dev.turtywurty.industria.renderer;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class IndustriaDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final IndustriaDynamicItemRenderer INSTANCE = new IndustriaDynamicItemRenderer();

    private final WindTurbineBlockEntity windTurbine = new WindTurbineBlockEntity(BlockPos.ORIGIN, BlockInit.WIND_TURBINE.getDefaultState());
    private final OilPumpJackBlockEntity oilPumpJack = new OilPumpJackBlockEntity(BlockPos.ORIGIN, BlockInit.OIL_PUMP_JACK.getDefaultState());

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
    }
}
