package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.gas.InputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ArcFurnaceBlockEntity extends IndustriaBlockEntity implements AutoMultiblockable, SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("arc_furnace");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.x() == 3)
                    .on(LocalDirection.LEFT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.z() == 3)
                    .on(LocalDirection.BACK)
                    .types(PortType.output(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.x() == 0)
                    .on(LocalDirection.RIGHT)
                    .types(PortType.input(TransferType.ENERGY), PortType.input(TransferType.FLUID), PortType.input(TransferType.GAS))
                    .build()
    );

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private Mode mode = Mode.SMELTING;
    private ResourceKey<Recipe<?>> currentRecipeId;

    private int progress, maxProgress;

    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int getCount() {
            return 3; // mode, progress, maxProgress
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mode.ordinal();
                case 1 -> progress;
                case 2 -> maxProgress;
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> mode = Mode.values()[value];
                case 1 -> progress = value;
                case 2 -> maxProgress = value;
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
        }
    };

    public ArcFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ARC_FURNACE, BlockEntityTypeInit.ARC_FURNACE, pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 9), Direction.WEST);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.SOUTH);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000_000, 1_000_000, 0), Direction.EAST);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 10), Direction.EAST);
        this.wrappedGasStorage.addStorage(new InputGasStorage(this, FluidConstants.BUCKET * 5), Direction.EAST);
    }

    @Override
    public Block getBlock() {
        return BlockInit.ARC_FURNACE;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncableStorage inputInventory = getInputInventory();
        SyncableStorage outputInventory = getOutputInventory();
        SyncableStorage energyStorage = getEnergyStorage();
        SyncableStorage fluidStorage = getFluidStorage();
        SyncableStorage gasStorage = getGasStorage();

        return List.of(inputInventory, outputInventory, energyStorage, fluidStorage, gasStorage);
    }

    @Override
    public void onTick() {
        if (level == null || level.isClientSide())
            return;


    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ARC_FURNACE;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = 0; x <= 3; x++) {
            for (int z = 0; z <= 3; z++) {
                for (int y = 0; y <= 4; y++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    if (this.level.getBlockState(pos).canBeReplaced()) {
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
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ArcFurnaceScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        view.putString("Mode", this.mode.getSerializedName());
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipeId", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "GasTank", this.wrappedGasStorage);
        Multiblockable.write(this, view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        this.mode = view.read("Mode", Codec.STRING).flatMap(Mode::fromStringOptional).orElse(Mode.SMELTING);
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipeId", RECIPE_CODEC).orElse(null);

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "GasTank", this.wrappedGasStorage);
        Multiblockable.read(this, view);
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingEnergyStorage getEnergyProvider(Direction side) {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleGasStorage getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(Direction.WEST);
    }

    public OutputSimpleInventory getOutputInventory() {
        return (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(Direction.SOUTH);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return getEnergyProvider(Direction.EAST);
    }

    public InputFluidStorage getFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.EAST);
    }

    public InputGasStorage getGasStorage() {
        return (InputGasStorage) getGasProvider(Direction.EAST);
    }

    public void setMode(Mode mode) {
        if (this.level == null || this.level.isClientSide())
            return;

        this.mode = mode;
        update();
    }

    public enum Mode implements StringRepresentable {
        SMELTING,
        ALLOYING,
        RECYCLING;

        private final String name;

        Mode() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return name;
        }

        public static Mode fromString(String name) {
            for (Mode mode : values()) {
                if (mode.name.equals(name))
                    return mode;
            }

            throw new IllegalArgumentException("No enum constant " + Mode.class.getCanonicalName() + "." + name);
        }

        public static Optional<Mode> fromStringOptional(String name) {
            try {
                return Optional.of(fromString(name));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
    }
}
