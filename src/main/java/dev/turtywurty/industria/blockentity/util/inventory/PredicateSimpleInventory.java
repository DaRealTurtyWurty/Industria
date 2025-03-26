package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class PredicateSimpleInventory extends SyncingSimpleInventory {
    private final BiPredicate<ItemStack, Integer> predicate;

    public PredicateSimpleInventory(UpdatableBlockEntity blockEntity, int size, BiPredicate<ItemStack, Integer> predicate) {
        super(blockEntity, size);
        this.predicate = predicate;
    }

    public PredicateSimpleInventory(UpdatableBlockEntity blockEntity, BiPredicate<ItemStack, Integer> predicate, ItemStack... stacks) {
        super(blockEntity, stacks);
        this.predicate = predicate;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return this.predicate.test(stack, slot);
    }

    public BiPredicate<ItemStack, Integer> getPredicate() {
        return this.predicate;
    }

    public static BiPredicate<ItemStack, Integer> createEmptyFluidPredicate(Supplier<FluidVariant> fluidVariantSupplier) {
        return (stack, integer) -> {
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsInsertion())
                return false;

            try (Transaction transaction = Transaction.openOuter()) {
                return storage.insert(fluidVariantSupplier.get(), FluidConstants.BUCKET, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createFluidPredicate(Supplier<FluidStack> fluidStackSupplier) {
        return (stack, index) -> {
            Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
            if (storage == null || !storage.supportsExtraction())
                return false;

            try (Transaction transaction = Transaction.openOuter()) {
                return storage.extract(fluidStackSupplier.get().variant(), FluidConstants.BUCKET, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createEmptySlurryPredicate(Supplier<SlurryVariant> slurryVariantSupplier) {
        return (stack, integer) -> {
            Storage<SlurryVariant> storage = ContainerItemContext.withConstant(stack).find(SlurryStorage.ITEM);
            if (storage == null || !storage.supportsInsertion())
                return false;

            try (Transaction transaction = Transaction.openOuter()) {
                return storage.insert(slurryVariantSupplier.get(), FluidConstants.BUCKET, transaction) > 0;
            }
        };
    }

    public static BiPredicate<ItemStack, Integer> createSlurryPredicate(Supplier<SlurryStack> slurryStackSupplier) {
        return (stack, index) -> {
            Storage<SlurryVariant> storage = ContainerItemContext.withConstant(stack).find(SlurryStorage.ITEM);
            if (storage == null || !storage.supportsExtraction())
                return false;

            try (Transaction transaction = Transaction.openOuter()) {
                return storage.extract(slurryStackSupplier.get().variant(), FluidConstants.BUCKET, transaction) > 0;
            }
        };
    }
}
