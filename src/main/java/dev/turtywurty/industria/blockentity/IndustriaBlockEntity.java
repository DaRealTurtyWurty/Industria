package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class IndustriaBlockEntity extends UpdatableBlockEntity {
    public static final Codec<ResourceKey<Recipe<?>>> RECIPE_CODEC = ResourceKey.codec(Registries.RECIPE);
    protected final IndustriaBlock blockRef;

    public IndustriaBlockEntity(IndustriaBlock blockRef, BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.blockRef = blockRef;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (level == null || pos == null || oldState == null)
            return;

        BlockState newState = level.getBlockState(pos);
        boolean isSameBlock = newState.is(oldState.getBlock());
        if (isSameBlock)
            return;

        if (blockRef.multiblockType != null) {
            removeMultiblockOnBreak();
        }

        if (blockRef.dropContentsOnBreak) {
            dropContentsOnBreak();
        }
    }

    protected void removeMultiblockOnBreak() {
        blockRef.multiblockType.onMultiblockBreak(level, worldPosition);
    }

    protected void dropContentsOnBreak() {
        if (this instanceof BlockEntityContentsDropper blockEntityWithInventory) { // TODO: Replace with component access maybe?
            blockEntityWithInventory.dropContents(level, worldPosition);
        }
    }
}
