package dev.turtywurty.industria.blockentity;

import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.SyncingGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.heat.SyncingHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ElectrolyzerRecipe;
import dev.turtywurty.industria.recipe.input.ElectrolyzerRecipeInput;
import dev.turtywurty.industria.screenhandler.ElectrolyzerScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class ElectrolyzerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("electrolyzer");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> wrappedHeatStorage = new WrappedHeatStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private int progress, maxProgress;
    private int electrolyteConversionProgress, maxElectrolyteConversionProgress;
    private RegistryKey<Recipe<?>> currentRecipeId;
    private FluidStack leftoverOutputFluid = FluidStack.EMPTY;
    private GasStack leftoverOutputGas = GasStack.EMPTY;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> electrolyteConversionProgress;
                case 3 -> maxElectrolyteConversionProgress;
                default -> throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + size());
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> electrolyteConversionProgress = value;
                case 3 -> maxElectrolyteConversionProgress = value;
                default -> throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + size());
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTROLYZER, BlockEntityTypeInit.ELECTROLYZER, pos, state);

        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.SOUTH);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1, (stack, integer) -> stack.isIn(TagList.Items.ELECTROLYSIS_RODS)), Direction.WEST);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.EAST);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.DOWN);
        this.wrappedGasStorage.addStorage(new OutputGasStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 100_000, 0), Direction.UP);
        this.wrappedHeatStorage.addStorage(new SyncingHeatStorage(this, 1000, 1000, 0), Direction.DOWN);
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = getInputInventory();
        SyncingSimpleInventory electrolyteInventory = getElectrolyteInventory();
        PredicateSimpleInventory anodeInventory = getAnodeInventory();
        SyncingSimpleInventory cathodeInventory = getCathodeInventory();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        InputFluidStorage electrolyteFluidStorage = getElectrolyteFluidStorage();
        OutputFluidStorage outputFluidStorage = getOutputFluidStorage();
        OutputGasStorage outputGasStorage = getOutputGasStorage();
        SyncingHeatStorage heatStorage = getHeatStorage();
        return List.of(inputInventory, electrolyteInventory, anodeInventory, cathodeInventory, energyStorage, electrolyteFluidStorage, outputFluidStorage, outputGasStorage, heatStorage);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        processOutputs();
        if(hasLeftover())
            return;

        SyncingSimpleInventory inputInventory = getInputInventory();
        PredicateSimpleInventory anodeInventory = getAnodeInventory();
        SyncingSimpleInventory cathodeInventory = getCathodeInventory();
        SyncingSimpleInventory electrolyteInventory = getElectrolyteInventory();
        InputFluidStorage electrolyteFluidStorage = getElectrolyteFluidStorage();
        var recipeInput = new ElectrolyzerRecipeInput(
                inputInventory,
                anodeInventory,
                cathodeInventory,
                electrolyteInventory,
                electrolyteFluidStorage
        );
        if(this.currentRecipeId == null) {
            Optional<RecipeEntry<ElectrolyzerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if(recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<ElectrolyzerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        ElectrolyzerRecipe recipe = recipeEntry.get().value();
        if(recipe.electrolyteItem().testForRecipe(electrolyteInventory.getStack(0))) {
            FluidStack fluidStack = recipe.electrolyteFluid();
            if(electrolyteFluidStorage.canInsert(fluidStack)) {
                if(this.electrolyteConversionProgress >= this.maxElectrolyteConversionProgress) {
                    electrolyteFluidStorage.variant = fluidStack.variant();
                    electrolyteFluidStorage.amount += fluidStack.amount();

                    this.electrolyteConversionProgress = 0;
                    this.maxElectrolyteConversionProgress = 0;
                } else {
                    this.electrolyteConversionProgress++;
                    this.maxElectrolyteConversionProgress = 100;
                }
            } else {
                this.electrolyteConversionProgress = 0;
                this.maxElectrolyteConversionProgress = 0;
            }
        }

        if(!recipe.electrolyteFluid().testForRecipe(electrolyteFluidStorage))
            return;

        if(this.progress >= this.maxProgress) {
            if(recipe.outputFluid().amount() > 0) {
                this.leftoverOutputFluid = recipe.outputFluid();
            }

            if(recipe.outputGas().amount() > 0) {
                this.leftoverOutputGas = recipe.outputGas();
            }

            inputInventory.getStack(0).decrement(recipe.input().stackData().count());
            anodeInventory.getStack(0).damage(1, (ServerWorld) this.world, null, item -> {});

            this.progress = 0;
            update();
        } else {
            SyncingEnergyStorage energyStorage = getEnergyStorage();
            if(energyStorage.amount >= recipe.energyCost()) {
                energyStorage.amount -= recipe.energyCost();
                this.progress++;
                update();
            }
        }
    }

    private void processOutputs() {
        SyncingFluidStorage outputFluidStorage = getOutputFluidStorage();
        if(outputFluidStorage.canInsert(this.leftoverOutputFluid)) {
            long inserted = Math.min(outputFluidStorage.getCapacity() - outputFluidStorage.amount, this.leftoverOutputFluid.amount());
            outputFluidStorage.variant = this.leftoverOutputFluid.variant();
            outputFluidStorage.amount += inserted;

            this.leftoverOutputFluid = this.leftoverOutputFluid.withAmount(this.leftoverOutputFluid.amount() - inserted);
            update();
        }

        SyncingGasStorage outputGasStorage = getOutputGasStorage();
        if(outputGasStorage.canInsert(this.leftoverOutputGas)) {
            long inserted = Math.min(outputGasStorage.getCapacity() - outputGasStorage.amount, this.leftoverOutputGas.amount());
            outputGasStorage.variant = this.leftoverOutputGas.variant();
            outputGasStorage.amount += inserted;

            this.leftoverOutputGas = this.leftoverOutputGas.withAmount(this.leftoverOutputGas.amount() - inserted);
            update();
        }
    }

    private boolean hasLeftover() {
        return !this.leftoverOutputFluid.isEmpty() || !this.leftoverOutputGas.isEmpty();
    }

    private Optional<RecipeEntry<ElectrolyzerRecipe>> getCurrentRecipe(ElectrolyzerRecipeInput recipeInput) {
        if(this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(ElectrolyzerRecipe.Type.INSTANCE, recipeInput, serverWorld);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("FluidStorage", this.wrappedFluidStorage.writeNbt(registries));
        nbt.put("EnergyStorage", this.wrappedEnergyStorage.writeNbt(registries));
        nbt.put("GasStorage", this.wrappedGasStorage.writeNbt(registries));
        nbt.put("HeatStorage", this.wrappedHeatStorage.writeNbt(registries));

        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> nbt.put("CurrentRecipe", nbtElement));
        }

        if (!this.leftoverOutputFluid.isEmpty()) {
            nbt.put("LeftoverOutputFluid", FluidStack.CODEC.codec()
                    .encodeStart(NbtOps.INSTANCE, this.leftoverOutputFluid)
                    .getOrThrow());
        }

        if (!this.leftoverOutputGas.isEmpty()) {
            nbt.put("LeftoverOutputGas", GasStack.CODEC.codec()
                    .encodeStart(NbtOps.INSTANCE, this.leftoverOutputGas)
                    .getOrThrow());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("Inventory"))
            this.wrappedInventoryStorage.readNbt(nbt.getListOrEmpty("Inventory"), registries);

        if (nbt.contains("FluidStorage"))
            this.wrappedFluidStorage.readNbt(nbt.getListOrEmpty("FluidStorage"), registries);

        if (nbt.contains("EnergyStorage"))
            this.wrappedEnergyStorage.readNbt(nbt.getListOrEmpty("EnergyStorage"), registries);

        if (nbt.contains("GasStorage"))
            this.wrappedGasStorage.readNbt(nbt.getListOrEmpty("GasStorage"), registries);

        if (nbt.contains("HeatStorage"))
            this.wrappedHeatStorage.readNbt(nbt.getListOrEmpty("HeatStorage"), registries);

        this.progress = nbt.getInt("Progress", 0);
        this.maxProgress = nbt.getInt("MaxProgress", 0);

        if (nbt.contains("CurrentRecipe")) {
            this.currentRecipeId = nbt.get("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE))
                    .orElse(null);
        }

        if (nbt.contains("LeftoverOutputFluid")) {
            this.leftoverOutputFluid = nbt.get("LeftoverOutputFluid", FluidStack.CODEC.codec())
                    .orElse(FluidStack.EMPTY);
        } else {
            this.leftoverOutputFluid = FluidStack.EMPTY;
        }

        if (nbt.contains("LeftoverOutputGas")) {
            this.leftoverOutputGas = nbt.get("LeftoverOutputGas", GasStack.CODEC.codec())
                    .orElse(GasStack.EMPTY);
        } else {
            this.leftoverOutputGas = GasStack.EMPTY;
        }
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
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
        return new ElectrolyzerScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.propertyDelegate);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public SyncingSimpleInventory getElectrolyteInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
    }

    public PredicateSimpleInventory getAnodeInventory() {
        return (PredicateSimpleInventory) this.wrappedInventoryStorage.getInventory(2);
    }

    public SyncingSimpleInventory getCathodeInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(3);
    }

    public InputFluidStorage getElectrolyteFluidStorage() {
        return (InputFluidStorage) this.wrappedFluidStorage.getStorage(0);
    }

    public OutputFluidStorage getOutputFluidStorage() {
        return (OutputFluidStorage) this.wrappedFluidStorage.getStorage(1);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(0);
    }

    public OutputGasStorage getOutputGasStorage() {
        return (OutputGasStorage) this.wrappedGasStorage.getStorage(0);
    }

    public SyncingHeatStorage getHeatStorage() {
        return (SyncingHeatStorage) this.wrappedHeatStorage.getStorage(0);
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleGasStorage getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    public SimpleHeatStorage getHeatProvider(Direction side) {
        return this.wrappedHeatStorage.getStorage(side);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ELECTROLYZER;
    }

    // x y z
    // 3 2 2
    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if(this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 0; z++) {
                for (int y = 0; y <= 1; y++) {
                    if(x == 0 && z == 0 && y == 0)
                        continue;

                    BlockPos pos = this.pos.add(x, y, z);
                    if(this.world.getBlockState(pos).isReplaceable()) {
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
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        Direction left = getCachedState().get(Properties.HORIZONTAL_FACING).rotateYCounterclockwise();
        Direction right = left.getOpposite();

        if (offsetFromPrimary.getX() * left.getOffsetX() + offsetFromPrimary.getZ() * left.getOffsetZ() > 0 && direction == left) {
            List<TransferType<?, ?, ?>> types = transferTypes.computeIfAbsent(direction, k -> new ArrayList<>());
            types.add(TransferType.ITEM);
            types.add(TransferType.FLUID);
            types.add(TransferType.ENERGY);
            types.add(TransferType.HEAT);
        }

        if (offsetFromPrimary.getX() * right.getOffsetX() + offsetFromPrimary.getZ() * right.getOffsetZ() > 0 && direction == right) {
            List<TransferType<?, ?, ?>> types = transferTypes.computeIfAbsent(direction, k -> new ArrayList<>());
            types.add(TransferType.FLUID);
            types.add(TransferType.GAS);
        }

        if(offsetFromPrimary.getY() == 1 && direction == Direction.UP) {
            transferTypes.computeIfAbsent(Direction.UP, k -> new ArrayList<>()).add(TransferType.ITEM);
        }

        return Multiblockable.toIOPortMap(transferTypes);
    }
}
