package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.heat.InputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RotaryKilnControllerBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("rotary_kiln");

    private final Set<BlockPos> kilnSegments = new HashSet<>();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> wrappedHeatStorage = new WrappedHeatStorage<>();

    public RotaryKilnControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.ROTARY_KILN_CONTROLLER, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
        this.wrappedHeatStorage.addStorage(new InputHeatStorage(this, 2000, 2000));
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of();
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;


    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ROTARY_KILN_CONTROLLER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if(this.world == null || this.world.isClient)
            return List.of();

        // 5x5x1 structure
        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        int widthRange = 2;  // -2 to 2 = 5 blocks
        int heightRange = 4; // 0 to 4 = 5 blocks
        int depthRange = 0;  // 0 only = 1 block

        if (facing == null)
            throw new NullPointerException("Unexpected facing direction: null");

        // Define axis-aligned directions based on facing
        Direction right, forward;
        switch (facing) {
            case NORTH -> {
                right = Direction.EAST;
                forward = Direction.NORTH;
            }
            case SOUTH -> {
                right = Direction.WEST;
                forward = Direction.SOUTH;
            }
            case EAST -> {
                right = Direction.NORTH;
                forward = Direction.EAST;
            }
            case WEST -> {
                right = Direction.SOUTH;
                forward = Direction.WEST;
            }
            default -> throw new IllegalStateException("Unexpected facing direction: " + facing);
        }

        // The main loop structure is the same for all directions
        for (int w = -widthRange; w <= widthRange; w++) {
            for (int h = 0; h <= heightRange; h++) {
                for (int d = 0; d <= depthRange; d++) {
                    BlockPos pos = this.pos
                            .offset(right, w)
                            .offset(Direction.UP, h)
                            .offset(forward, d);

                    if (this.world.getBlockState(pos).isReplaceable()) {
                        positions.add(pos);
                    } else {
                        invalidPositions.add(pos);
                    }
                }
            }
        }

        return invalidPositions.isEmpty() ? positions : List.of();
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RotaryKilnScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("Heat", this.wrappedHeatStorage.writeNbt(registries));
        nbt.put("MachinePositions", Multiblockable.writeMultiblockToNbt(this));

        var kilnSegments = new NbtList();
        for (BlockPos pos : this.kilnSegments) {
            kilnSegments.add(NbtHelper.fromBlockPos(pos));
        }

        nbt.put("KilnSegments", kilnSegments);
    }

    public void setCreatedByReplacement() {
        if (world == null || world.isClient)
            return;

        Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);

        int segmentIndex = 1;
        while (segmentIndex <= 16) {
            BlockPos offsetPos = pos.offset(facing);
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.isOf(BlockInit.ROTARY_KILN)) {
                world.setBlockState(offsetPos, offsetState.with(RotaryKilnBlock.SEGMENT_INDEX, segmentIndex++));
                // TODO: Maybe add as kiln segments?
            } else {
                break;
            }
        }
    }

    public void addKilnSegment(BlockPos pos) {
        this.kilnSegments.add(pos);
        update();
    }
}
