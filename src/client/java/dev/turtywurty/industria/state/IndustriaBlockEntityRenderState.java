package dev.turtywurty.industria.state;

import dev.turtywurty.industria.renderer.block.IndustriaBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;

import java.util.ArrayList;
import java.util.List;

public class IndustriaBlockEntityRenderState extends BlockEntityRenderState {
    public final List<ItemRenderState> itemRenderStates = new ArrayList<>();
    public float tickProgress;

    public IndustriaBlockEntityRenderState(int itemRenderStateCount) {
        super();
        for (int i = 0; i < itemRenderStateCount; i++) {
            addItemRenderState();
        }
    }

    public void addItemRenderState() {
        this.itemRenderStates.add(new ItemRenderState());
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack, ItemDisplayContext displayContext, HeldItemContext heldItemContext, int seed) {
        renderer.getItemModelManager().clearAndUpdate(this.itemRenderStates.get(index), stack, displayContext, blockEntity.getWorld(), heldItemContext, seed);
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack, ItemDisplayContext displayContext) {
        updateItemRenderState(index, renderer, blockEntity, stack, displayContext, null, 0);
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack) {
        updateItemRenderState(index, renderer, blockEntity, stack, ItemDisplayContext.NONE);
    }

    public void renderItemRenderState(int index, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        this.itemRenderStates.get(index).render(matrices, queue, this.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
    }
}
