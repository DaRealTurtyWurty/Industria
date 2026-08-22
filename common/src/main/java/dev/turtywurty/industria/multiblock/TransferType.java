package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.multiblocklib.port.PortTransfer;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.storage.SlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.StorageTransfer;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKey;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Describes one loader-neutral resource transfer family used by Industria's pipes and multiblock ports.
 *
 * @param <S> the neutral storage type
 * @param <V> the resource variant stored by {@code S}
 * @param <A> the amount type exposed to existing pipe code
 */
public class TransferType<S extends ResourceStorage<V>, V extends ResourceVariant<?>, A extends Number>
        implements PortTransfer<S> {
    private static final List<TransferType<?, ?, ?>> VALUES = new ArrayList<>();

    public static final TransferType<ResourceStorage<ResourceVariant<Item>>, ResourceVariant<Item>, Long> ITEM =
            standard("item", StorageKeys.ITEM);

    public static final TransferType<ResourceStorage<ResourceVariant<Fluid>>, ResourceVariant<Fluid>, Long> FLUID =
            standard("fluid", StorageKeys.FLUID);

    public static final TransferType<ResourceStorage<ResourceVariant<UnitResource>>, ResourceVariant<UnitResource>, Long> ENERGY =
            standard("energy", StorageKeys.ENERGY);

    public static final TransferType<ResourceStorage<ResourceVariant<Slurry>>, ResourceVariant<Slurry>, Long> SLURRY =
            standard("slurry", SlurryStorage.KEY);

    public static final TransferType<ResourceStorage<ResourceVariant<Gas>>, ResourceVariant<Gas>, Long> GAS =
            standard("gas", GasStorage.KEY);

    public static final Codec<TransferType<?, ?, ?>> CODEC =
            Codec.STRING.xmap(TransferType::getByName, TransferType::getName);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferType<?, ?, ?>> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, TransferType::getName, TransferType::getByName);

    private final String name;
    private final StorageKey<V> lookup;
    private final InsertExtractFunction<S, V, A> insertFunction;
    private final InsertExtractFunction<S, V, A> extractFunction;
    private final Function<S, V> valueGetter;
    private final A maxAmount;
    private final Function<Double, A> amountConverter;
    private final A zeroAmount;
    private final Predicate<V> isBlank;
    private final Predicate<S> supportsInsert;
    private final Predicate<S> supportsExtract;

    public TransferType(
            @NotNull String name,
            @NotNull StorageKey<V> lookup,
            @NotNull InsertExtractFunction<S, V, A> insertFunction,
            @NotNull InsertExtractFunction<S, V, A> extractFunction,
            @NotNull Function<S, V> valueGetter,
            @NotNull A maxAmount,
            @NotNull Function<Double, A> amountConverter,
            @NotNull Predicate<V> isBlank,
            @NotNull Predicate<S> supportsInsert,
            @NotNull Predicate<S> supportsExtract) {
        this.name = Objects.requireNonNull(name, "name");
        this.lookup = Objects.requireNonNull(lookup, "lookup");
        this.insertFunction = Objects.requireNonNull(insertFunction, "insertFunction");
        this.extractFunction = Objects.requireNonNull(extractFunction, "extractFunction");
        this.valueGetter = Objects.requireNonNull(valueGetter, "valueGetter");
        this.maxAmount = Objects.requireNonNull(maxAmount, "maxAmount");
        this.amountConverter = Objects.requireNonNull(amountConverter, "amountConverter");
        this.zeroAmount = amountConverter.apply(0D);
        this.isBlank = Objects.requireNonNull(isBlank, "isBlank");
        this.supportsInsert = Objects.requireNonNull(supportsInsert, "supportsInsert");
        this.supportsExtract = Objects.requireNonNull(supportsExtract, "supportsExtract");
        VALUES.add(this);
    }

    private static <T> TransferType<
            ResourceStorage<ResourceVariant<T>>, ResourceVariant<T>, Long> standard(
            String name,
            StorageKey<ResourceVariant<T>> key) {
        return new TransferType<>(
                name,
                key,
                ResourceStorage::insert,
                ResourceStorage::extract,
                storage -> firstVariant(storage, key),
                Long.MAX_VALUE,
                amount -> (long) Math.ceil(amount),
                ResourceVariant::isBlank,
                ResourceStorage::supportsInsertion,
                ResourceStorage::supportsExtraction
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceVariant<T> emptyVariant(StorageKey<ResourceVariant<T>> key) {
        return (ResourceVariant<T>) key.resourceType().empty();
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceVariant<T> firstVariant(
            ResourceStorage<ResourceVariant<T>> storage,
            StorageKey<ResourceVariant<T>> key) {
        if (!storage.hasStableIndices())
            return emptyVariant(key);
        for (int index = 0; index < storage.size(); index++) {
            if (storage.amount(index) > 0)
                return storage.resource(index);
        }
        return emptyVariant(key);
    }

    public static List<TransferType<?, ?, ?>> getValues() {
        return List.copyOf(VALUES);
    }

    public static TransferType<?, ?, ?> getByName(String name) {
        return VALUES.stream()
                .filter(transferType -> transferType.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TransferType found for name: " + name));
    }

    public StorageKey<V> getLookup() {
        return this.lookup;
    }

    public String getName() {
        return this.name;
    }

    public void registerForMultiblockIo() {
        TransferService.get().registerBlockEntityProvider(
                this.lookup,
                ModBlockEntityTypes.AUTO_MULTIBLOCK_IO,
                (blockEntity, direction) -> blockEntity.getProvider(this, direction)
        );
    }

    public void pushTo(Level level, BlockPos primaryPos, BlockPos secondaryPos, @Nullable Direction side) {
        BlockEntity primaryBlockEntity = level.getBlockEntity(primaryPos);
        BlockState primaryState = primaryBlockEntity != null
                ? primaryBlockEntity.getBlockState()
                : level.getBlockState(primaryPos);
        S primaryStorage = lookup(level, primaryPos, primaryState, primaryBlockEntity, side);
        if (primaryStorage == null || !this.supportsExtract.test(primaryStorage))
            return;

        BlockEntity secondaryBlockEntity = level.getBlockEntity(secondaryPos);
        BlockState secondaryState = secondaryBlockEntity != null
                ? secondaryBlockEntity.getBlockState()
                : level.getBlockState(secondaryPos);
        S secondaryStorage = lookup(level, secondaryPos, secondaryState, secondaryBlockEntity, side);
        if (secondaryStorage == null || !this.supportsInsert.test(secondaryStorage))
            return;

        move(primaryStorage, secondaryStorage);
    }

    @Override
    public @Nullable S find(Level level, BlockPos pos, @Nullable Direction side) {
        return lookup(level, pos, side);
    }

    @Override
    public void move(S source, S target) {
        if (!this.supportsExtract.test(source) || !this.supportsInsert.test(target))
            return;

        V value = this.valueGetter.apply(source);
        if (!this.isBlank.test(value))
            StorageTransfer.move(source, target, value, this.maxAmount.longValue());
    }

    public @Nullable S lookup(Level level, BlockPos pos, @Nullable Direction direction) {
        return lookup(level, pos, null, null, direction);
    }

    @SuppressWarnings("unchecked")
    public @Nullable S lookup(
            Level level,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable BlockEntity blockEntity,
            @Nullable Direction direction) {
        return (S) TransferService.get().findBlock(
                this.lookup, level, pos, state, blockEntity, direction);
    }

    @SuppressWarnings("unchecked")
    public @Nullable S lookup(MutableItemContext context) {
        Objects.requireNonNull(context, "context");
        return (S) TransferService.get().findItem(this.lookup, context);
    }

    public A insert(S storage, V value, A maxAmount, TransferContext transaction) {
        return this.insertFunction.function(storage, value, maxAmount, transaction);
    }

    public A extract(S storage, V value, A maxAmount, TransferContext transaction) {
        return this.extractFunction.function(storage, value, maxAmount, transaction);
    }

    public void transferFraction(S source, S target, double fraction) {
        if (fraction <= 0 || !this.supportsExtract.test(source) || !this.supportsInsert.test(target))
            return;

        V value = this.valueGetter.apply(source);
        if (this.isBlank.test(value))
            return;

        A available;
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            available = extract(source, value, this.maxAmount, transaction);
        }

        double requested = fraction * available.doubleValue();
        if (requested > 0)
            StorageTransfer.move(source, target, value, this.amountConverter.apply(requested).longValue());
    }

    public void transferAll(S source, S target) {
        transferFraction(source, target, 1D);
    }

    public A getAmount(Level level, BlockPos pos) {
        S storage = lookup(level, pos, null);
        if (storage == null)
            return this.zeroAmount;

        V value = this.valueGetter.apply(storage);
        if (this.isBlank.test(value))
            return this.zeroAmount;

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            return extract(storage, value, this.maxAmount, transaction);
        }
    }

    @FunctionalInterface
    public interface InsertExtractFunction<
            S extends ResourceStorage<V>, V extends ResourceVariant<?>, A extends Number> {
        A function(S storage, V value, A maxAmount, TransferContext transaction);
    }
}
