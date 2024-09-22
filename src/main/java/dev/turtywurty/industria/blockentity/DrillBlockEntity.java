package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrillBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, Multiblockable {
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.DRILL, pos, state);
    }

    @Override
    public void tick() {
        if(this.world == null || this.world.isClient)
            return;

        // Do stuff
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if(nbt.contains("MultiblockPositions", NbtElement.LIST_TYPE)) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MultiblockPositions", NbtElement.INT_ARRAY_TYPE));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
    }

    @Override
    public MultiblockType type() {
        return MultiblockType.DRILL;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        return List.of();
    }
}
