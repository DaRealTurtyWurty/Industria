package dev.turtywurty.industria.state;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.renderer.block.IndustriaBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class IndustriaBlockEntityRenderState extends BlockEntityRenderState {
    public final List<ItemStackRenderState> itemRenderStates = new ArrayList<>();
    public float tickProgress;

    public IndustriaBlockEntityRenderState(int itemRenderStateCount) {
        super();
        for (int i = 0; i < itemRenderStateCount; i++) {
            addItemRenderState();
        }
    }

    public void addItemRenderState() {
        this.itemRenderStates.add(new ItemStackRenderState());
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack, ItemDisplayContext displayContext, ItemOwner heldItemContext, int seed) {
        renderer.getItemModelManager().updateForTopItem(this.itemRenderStates.get(index), stack, displayContext, blockEntity.getLevel(), heldItemContext, seed);
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack, ItemDisplayContext displayContext) {
        updateItemRenderState(index, renderer, blockEntity, stack, displayContext, null, 0);
    }

    public void updateItemRenderState(int index, IndustriaBlockEntityRenderer<?, ?> renderer, BlockEntity blockEntity, ItemStack stack) {
        updateItemRenderState(index, renderer, blockEntity, stack, ItemDisplayContext.NONE);
    }

    public void renderItemRenderState(int index, PoseStack matrices, SubmitNodeCollector queue) {
        this.itemRenderStates.get(index).submit(matrices, queue, this.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
