package dev.turtywurty.industria.multiblock;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.util.TransferUtils;
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

public class TransferType<S, V, A extends Number> {
    private static final List<TransferType<?, ?, ?>> VALUES = new ArrayList<>();

    public static final TransferType<Storage<ItemVariant>, ItemVariant, Long> ITEM =
            new TransferType<>("item", ItemStorage.SIDED, ItemStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> TransferUtils.findFirstVariant(storage, null)
                            .orElse(ItemVariant.blank()),
                    Long.MAX_VALUE,
                    aDouble -> (long) Math.ceil(aDouble),
                    ItemVariant::isBlank);

    public static final TransferType<Storage<FluidVariant>, FluidVariant, Long> FLUID =
            new TransferType<>("fluid", FluidStorage.SIDED, FluidStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> TransferUtils.findFirstVariant(storage, null)
                            .orElse(FluidVariant.blank()),
                    Long.MAX_VALUE,
                    aDouble -> (long) Math.ceil(aDouble),
                    FluidVariant::isBlank);

    public static final TransferType<EnergyStorage, Long, Long> ENERGY =
            new TransferType<>("energy", EnergyStorage.SIDED, EnergyStorage.ITEM,
                    (storage, value, maxAmount, transaction) -> storage.insert(maxAmount, transaction),
                    (storage, value, maxAmount, transaction) -> storage.extract(maxAmount, transaction),
                    EnergyStorage::getAmount,
                    Long.MAX_VALUE,
                    aDouble -> (long) Math.ceil(aDouble),
                    value -> value <= 0);

    public static final TransferType<HeatStorage, Double, Double> HEAT =
            new TransferType<>("heat", HeatStorage.SIDED, HeatStorage.ITEM,
                    (storage, value, maxAmount, transaction) -> storage.insert(value, transaction),
                    (storage, value, maxAmount, transaction) -> storage.extract(value, transaction),
                    HeatStorage::getAmount,
                    Double.MAX_VALUE,
                    Function.identity(),
                    value -> value <= 0);

    public static final TransferType<Storage<SlurryVariant>, SlurryVariant, Long> SLURRY =
            new TransferType<>("slurry", SlurryStorage.SIDED, SlurryStorage.ITEM, Storage::insert, Storage::extract,
                    storage -> TransferUtils.findFirstVariant(storage, null)
                            .orElse(SlurryVariant.blank()),
                    Long.MAX_VALUE,
                    aDouble -> (long) Math.ceil(aDouble),
                    SlurryVariant::isBlank);

    //public static final TransferType<?> PRESSURE = new TransferType<>(null, null);
    //public static final TransferType<?> GAS = new TransferType<>(null, null);

    public static List<TransferType<?, ?, ?>> getValues() {
        return List.copyOf(VALUES);
    }

    private final String name;
    private final BlockApiLookup<S, @Nullable Direction> blockLookup;
    private final ItemApiLookup<S, ContainerItemContext> itemLookup;
    private final InsertExtractFunction<S, V, A> insertFunction;
    private final InsertExtractFunction<S, V, A> extractFunction;
    private final Function<S, V> valueGetter;
    private final A maxAmount;
    private final Function<Double, A> amountConverter;
    private final A zeroAmount;
    private final Predicate<V> isBlank;

    public TransferType(@NotNull String name,
                        @NotNull BlockApiLookup<S, @Nullable Direction> blockLookup,
                        @NotNull ItemApiLookup<S, ContainerItemContext> itemLookup,
                        @NotNull InsertExtractFunction<S, V, A> insertFunction,
                        @NotNull InsertExtractFunction<S, V, A> extractFunction,
                        @NotNull Function<S, V> valueGetter,
                        @NotNull A maxAmount,
                        @NotNull Function<Double, A> amountConverter,
                        @NotNull Predicate<V> isBlank) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(blockLookup, "blockLookup must not be null");
        Objects.requireNonNull(itemLookup, "itemLookup must not be null");
        Objects.requireNonNull(insertFunction, "insertFunction must not be null");
        Objects.requireNonNull(extractFunction, "extractFunction must not be null");
        Objects.requireNonNull(valueGetter, "valueGetter must not be null");
        Objects.requireNonNull(maxAmount, "maxAmount must not be null");
        Objects.requireNonNull(amountConverter, "amountConverter must not be null");
        Objects.requireNonNull(isBlank, "isBlank must not be null");

        this.name = name;

        this.blockLookup = blockLookup;
        this.itemLookup = itemLookup;

        this.insertFunction = insertFunction;
        this.extractFunction = extractFunction;

        this.valueGetter = valueGetter;
        this.maxAmount = maxAmount;
        this.amountConverter = amountConverter;
        this.zeroAmount = amountConverter.apply(0D);
        this.isBlank = isBlank;

        VALUES.add(this);
    }

    public BlockApiLookup<S, @Nullable Direction> getBlockLookup() {
        return this.blockLookup;
    }

    public ItemApiLookup<S, ContainerItemContext> getItemLookup() {
        return this.itemLookup;
    }

    public String getName() {
        return this.name;
    }

    public void registerForMultiblockIo() {
        this.blockLookup.registerForBlockEntity((blockEntity, direction) -> blockEntity.getProvider(this, direction), BlockEntityTypeInit.MULTIBLOCK_IO);
    }

    public void pushTo(World world, BlockPos primaryPos, BlockPos secondaryPos, Direction side) {
        BlockEntity primaryBlockEntity = world.getBlockEntity(primaryPos);
        BlockState primaryState = primaryBlockEntity != null ? primaryBlockEntity.getCachedState() : world.getBlockState(primaryPos);
        S primaryStorage = lookup(world, primaryPos, primaryState, primaryBlockEntity, side);
        if (primaryStorage == null)
            return;

        BlockState secondaryState = world.getBlockState(secondaryPos);
        BlockEntity secondaryBlockEntity = world.getBlockEntity(secondaryPos);
        S secondaryStorage = lookup(world, secondaryPos, secondaryState, secondaryBlockEntity, side);
        if (secondaryStorage == null)
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            V value = valueGetter.apply(primaryStorage);
            if (isBlank.test(value))
                return;

            A maxAmount;
            try (Transaction transaction1 = transaction.openNested()) {
                maxAmount = extract(primaryStorage, value, this.maxAmount, transaction1);
                transaction1.abort();
            }

            if (maxAmount.doubleValue() <= 0)
                return;

            A inserted = insert(secondaryStorage, value, maxAmount, transaction);
            if (inserted.doubleValue() > 0) {
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

    public A insert(S storage, V value, A maxAmount, TransactionContext transaction) {
        return this.insertFunction.function(storage, value, maxAmount, transaction);
    }

    public A extract(S storage, V value, A maxAmount, TransactionContext transaction) {
        return this.extractFunction.function(storage, value, maxAmount, transaction);
    }

    public void transferFraction(S storage, S storage1, double fraction) {
        V value = valueGetter.apply(storage);
        if (isBlank.test(value))
            return;

        A inStorage;
        try (Transaction transaction = Transaction.openOuter()) {
            inStorage = extract(storage, value, this.maxAmount, transaction);
        }

        double amount = (fraction * inStorage.doubleValue());
        if (amount <= 0)
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            A extracted = extract(storage, value, this.amountConverter.apply(amount), transaction);
            if (extracted.doubleValue() > 0) {
                insert(storage1, value, extracted, transaction);
                transaction.commit();
            }
        }
    }

    public void transferAll(S storage, S storage1) {
        transferFraction(storage, storage1, 1);
    }

    public A getAmount(World world, BlockPos pos) {
        try(Transaction transaction = Transaction.openOuter()) {
            S storage = lookup(world, pos, null);
            V value = this.valueGetter.apply(storage);
            if(this.isBlank.test(value))
                return this.zeroAmount;

            return extract(storage, value, this.maxAmount, transaction);
        } catch (RuntimeException e) {
            return this.zeroAmount;
        }
    }

    @FunctionalInterface
    public interface InsertExtractFunction<S, V, A extends Number> {
        A function(S storage, V value, A maxAmount, TransactionContext transaction);
    }
}