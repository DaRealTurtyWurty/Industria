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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.*;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ArcFurnaceBlockEntity extends IndustriaBlockEntity implements Multiblockable, SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("arc_furnace");

    private static final List<PortRule> PORT_RULES = List.of(
            PortRule.when(p -> p.x() == 3)
                    .on(LocalDirection.LEFT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PortRule.when(p -> p.z() == 3)
                    .on(LocalDirection.BACK)
                    .types(PortType.output(TransferType.ITEM))
                    .build(),

            PortRule.when(p -> p.x() == 0)
                    .on(LocalDirection.RIGHT)
                    .types(PortType.input(TransferType.ENERGY), PortType.input(TransferType.FLUID), PortType.input(TransferType.GAS))
                    .build()
    );

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private Mode mode = Mode.SMELTING;
    private RegistryKey<Recipe<?>> currentRecipeId;

    private int progress, maxProgress;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int size() {
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

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 9), Direction.WEST);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.SOUTH);
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
        if (world == null || world.isClient)
            return;


    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ARC_FURNACE;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = 0; x <= 3; x++) {
            for (int z = 0; z <= 3; z++) {
                for (int y = 0; y <= 4; y++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    BlockPos pos = this.pos.add(x, y, z);
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
        return new ArcFurnaceScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.propertyDelegate);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        view.putString("Mode", this.mode.asString());
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.put("CurrentRecipeId", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "GasTank", this.wrappedGasStorage);
        Multiblockable.write(this, view);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        this.mode = view.read("Mode", Codec.STRING).flatMap(Mode::fromStringOptional).orElse(Mode.SMELTING);
        this.progress = view.getInt("Progress", 0);
        this.maxProgress = view.getInt("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipeId", RECIPE_CODEC).orElse(null);

        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "GasTank", this.wrappedGasStorage);
        Multiblockable.read(this, view);
    }

    @Override
    public List<PortRule> getPortRules() {
        return PORT_RULES;
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
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
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(Direction.WEST);
    }

    public OutputSimpleInventory getOutputInventory() {
        return (OutputSimpleInventory) this.wrappedInventoryStorage.getInventory(Direction.SOUTH);
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
        if (this.world == null || this.world.isClient)
            return;

        this.mode = mode;
        update();
    }

    public enum Mode implements StringIdentifiable {
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
        public String asString() {
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
