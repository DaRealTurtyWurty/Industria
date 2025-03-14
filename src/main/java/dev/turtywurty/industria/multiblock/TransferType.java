package dev.turtywurty.industria.multiblock;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferType<S, V> {
    private static final List<TransferType<?, ?>> VALUES = new ArrayList<>();

    public static final TransferType<Storage<ItemVariant>, ItemVariant> ITEM =
            new TransferType<>(ItemStorage.SIDED, ItemStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> MixerBlockEntity.findFirstVariant(storage, null)
                            .orElse(ItemVariant.blank()),
                    ItemVariant::isBlank);

    public static final TransferType<Storage<FluidVariant>, FluidVariant> FLUID =
            new TransferType<>(FluidStorage.SIDED, FluidStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> MixerBlockEntity.findFirstVariant(storage, null)
                            .orElse(FluidVariant.blank()),
                    FluidVariant::isBlank);

    public static final TransferType<EnergyStorage, Long> ENERGY =
            new TransferType<>(EnergyStorage.SIDED, EnergyStorage.ITEM,
                    (storage, value, maxAmount, transaction) -> storage.insert(maxAmount, transaction),
                    (storage, value, maxAmount, transaction) -> storage.extract(maxAmount, transaction),
                    EnergyStorage::getAmount,
                    value -> value <= 0);

    public static final TransferType<HeatStorage, Long> HEAT =
            new TransferType<>(HeatStorage.SIDED, HeatStorage.ITEM,
                    (storage, value, maxAmount, transaction) -> storage.insert(maxAmount, transaction),
                    (storage, value, maxAmount, transaction) -> storage.extract(maxAmount, transaction),
                    HeatStorage::getAmount,
                    value -> value <= 0);

    public static final TransferType<Storage<SlurryVariant>, SlurryVariant> SLURRY =
            new TransferType<>(SlurryStorage.SIDED, SlurryStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> MixerBlockEntity.findFirstVariant(storage, null)
                            .orElse(SlurryVariant.blank()),
                    SlurryVariant::isBlank);

    //public static final TransferType<?> PRESSURE = new TransferType<>(null, null);
    //public static final TransferType<?> GAS = new TransferType<>(null, null);

    public static List<TransferType<?, ?>> getValues() {
        return List.copyOf(VALUES);
    }

    private final BlockApiLookup<S, @Nullable Direction> blockLookup;
    private final ItemApiLookup<S, ContainerItemContext> itemLookup;
    private final InsertExtractFunction<S, V> insertFunction;
    private final InsertExtractFunction<S, V> extractFunction;
    private final Function<S, V> valueGetter;
    private final Predicate<V> isBlank;

    public TransferType(@NotNull BlockApiLookup<S, @Nullable Direction> blockLookup,
                        @NotNull ItemApiLookup<S, ContainerItemContext> itemLookup,
                        @NotNull InsertExtractFunction<S, V> insertFunction,
                        @NotNull InsertExtractFunction<S, V> extractFunction,
                        @NotNull Function<S, V> valueGetter,
                        @NotNull Predicate<V> isBlank) {
        Objects.requireNonNull(blockLookup, "blockLookup must not be null");
        Objects.requireNonNull(itemLookup, "itemLookup must not be null");
        Objects.requireNonNull(insertFunction, "insertFunction must not be null");
        Objects.requireNonNull(extractFunction, "extractFunction must not be null");
        Objects.requireNonNull(valueGetter, "valueGetter must not be null");
        Objects.requireNonNull(isBlank, "isBlank must not be null");

        this.blockLookup = blockLookup;
        this.itemLookup = itemLookup;

        this.insertFunction = insertFunction;
        this.extractFunction = extractFunction;

        this.valueGetter = valueGetter;
        this.isBlank = isBlank;

        VALUES.add(this);
    }

    public BlockApiLookup<S, @Nullable Direction> getBlockLookup() {
        return this.blockLookup;
    }

    public ItemApiLookup<S, ContainerItemContext> getItemLookup() {
        return this.itemLookup;
    }

    public void registerForMultiblockIo() {
        this.blockLookup.registerForBlockEntity((blockEntity, direction) -> blockEntity.getProvider(this, direction), BlockEntityTypeInit.MULTIBLOCK_IO);
    }

    public void distribute(World world, BlockPos pos, BlockEntity controller, Direction side) {
        S primaryStorage = lookup(world, pos, controller.getCachedState(), controller, side);
        if (primaryStorage == null)
            return;

        BlockPos secondaryPos = pos.offset(side);
        BlockState secondaryState = world.getBlockState(secondaryPos);
        BlockEntity secondaryBlockEntity = world.getBlockEntity(secondaryPos);
        S secondaryStorage = lookup(world, secondaryPos, secondaryState, secondaryBlockEntity, side.getOpposite());
        if (secondaryStorage == null)
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            V value = valueGetter.apply(primaryStorage);
            if (isBlank.test(value))
                return;

            long maxAmount;
            try(Transaction transaction1 = transaction.openNested()) {
                maxAmount = extract(primaryStorage, value, Long.MAX_VALUE, transaction1);
                transaction1.abort();
            }

            if(maxAmount <= 0)
                return;

            long inserted = insert(secondaryStorage, value, maxAmount, transaction);
            if (inserted > 0) {
                extract(primaryStorage, value, inserted, transaction);
                transaction.commit();
            }
        }
    }

    public S lookup(World world, BlockPos pos, Direction direction) {
        return lookup(world, pos, null, null, direction);
    }

    public S lookup(World world, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, Direction direction) {
        return this.blockLookup.find(world, pos, state, blockEntity, direction);
    }

    public S lookup(ItemStack stack, ContainerItemContext context) {
        return this.itemLookup.find(stack, context);
    }

    public long insert(S storage, V value, long maxAmount, TransactionContext transaction) {
        return this.insertFunction.function(storage, value, maxAmount, transaction);
    }

    public long extract(S storage, V value, long maxAmount, TransactionContext transaction) {
        return this.extractFunction.function(storage, value, maxAmount, transaction);
    }

    @FunctionalInterface
    public interface InsertExtractFunction<S, V> {
        long function(S storage, V value, long maxAmount, TransactionContext transaction);
    }
}