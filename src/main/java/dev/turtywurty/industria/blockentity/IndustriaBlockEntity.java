package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;

public abstract class IndustriaBlockEntity extends UpdatableBlockEntity {
    public static final Codec<RegistryKey<Recipe<?>>> RECIPE_CODEC = RegistryKey.createCodec(RegistryKeys.RECIPE);
    protected final IndustriaBlock blockRef;

    public IndustriaBlockEntity(IndustriaBlock blockRef, BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.blockRef = blockRef;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (world == null || pos == null || oldState == null)
            return;

        BlockState newState = world.getBlockState(pos);
        boolean isSameBlock = newState.isOf(oldState.getBlock());
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
        blockRef.multiblockType.onMultiblockBreak(world, pos);
    }

    protected void dropContentsOnBreak() {
        if (this instanceof BlockEntityContentsDropper blockEntityWithInventory) { // TODO: Replace with component access maybe?
            blockEntityWithInventory.dropContents(world, pos);
        }
    }
}
