package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.storage.SlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import dev.turtywurty.turtymultiloader.transfer.unit.Units;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.material.Fluid;

import java.math.RoundingMode;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class PredicateSimpleInventory extends SyncingSimpleInventory {
    private static final long BUCKET_AMOUNT = Units.FLUID_BUCKET.convert(
            1, Units.FLUID_DROPLET, RoundingMode.UNNECESSARY);

    private final BiPredicate<ItemStack, Integer> predicate;

    private static MutableItemContext mutableCopy(ItemStack stack) {
        return MutableItemContext.ofContainerSlot(new SimpleContainer(stack.copy()), 0);
    }

    public PredicateSimpleInventory(UpdateableBlockEntityLike blockEntity, int size, BiPredicate<ItemStack, Integer> predicate) {
        super(blockEntity, size);
        this.predicate = predicate;
    }

    public PredicateSimpleInventory(UpdateableBlockEntityLike blockEntity, BiPredicate<ItemStack, Integer> predicate, ItemStack... stacks) {
        super(blockEntity, stacks);
        this.predicate = predicate;
    }

    public static BiPredicate<ItemStack, Integer> createEmptyFluidPredicate(
            Supplier<ResourceVariant<Fluid>> fluidVariantSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    mutableCopy(stack).find(StorageKeys.FLUID);
            if (storage == null || !storage.supportsInsertion())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.insert(fluidVariantSupplier.get(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createFluidPredicate(Supplier<FluidStack> fluidStackSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    mutableCopy(stack).find(StorageKeys.FLUID);
            if (storage == null || !storage.supportsExtraction())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.extract(fluidStackSupplier.get().variant(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createEmptySlurryPredicate(
            Supplier<ResourceVariant<Slurry>> slurryVariantSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Slurry>> storage =
                    mutableCopy(stack).find(SlurryStorage.KEY);
            if (storage == null || !storage.supportsInsertion())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.insert(slurryVariantSupplier.get(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createSlurryPredicate(Supplier<SlurryStack> slurryStackSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Slurry>> storage =
                    mutableCopy(stack).find(SlurryStorage.KEY);
            if (storage == null || !storage.supportsExtraction())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.extract(slurryStackSupplier.get().variant(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createEmptyGasPredicate(
            Supplier<ResourceVariant<Gas>> gasVariantSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Gas>> storage =
                    mutableCopy(stack).find(GasStorage.KEY);
            if (storage == null || !storage.supportsInsertion())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.insert(gasVariantSupplier.get(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createGasPredicate(Supplier<GasStack> gasStackSupplier) {
        return (stack, _) -> {
            ResourceStorage<ResourceVariant<Gas>> storage =
                    mutableCopy(stack).find(GasStorage.KEY);
            if (storage == null || !storage.supportsExtraction())
                return false;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.extract(gasStackSupplier.get().variant(), BUCKET_AMOUNT, transaction) > 0;
            }
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.predicate.test(stack, slot);
    }

    public BiPredicate<ItemStack, Integer> getPredicate() {
        return this.predicate;
    }
}
