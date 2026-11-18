package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.blockentity.util.StorageOperations;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Opt-in dedicated-server checks against the production-line crystallizer.
 */
public final class CrystallizerValidationTestWorld {
    private CrystallizerValidationTestWorld() {
    }

    public static void validate(TestWorldContext context) {
        try {
            CrystallizerBlockEntity crystallizer = context.requireBlockEntity(0, 0, 0, CrystallizerBlockEntity.class);
            check(crystallizer.isFormed(), "Crystallizer failed to form");
            BlockPos port = context.origin().offset(CrystallizerBlockEntity.ENERGY_PORT);
            ResourceStorage<ResourceVariant<UnitResource>> energy = TransferType.ENERGY.lookup(context.overworld(),
                port, Direction.UP);
            check(energy != null && energy.supportsInsertion() && !energy.supportsExtraction(), "Energy input lookup");
            for (Direction side : Direction.values()) {
                if (side != Direction.UP) {
                    check(crystallizer.getEnergyStorageForExternal(port, side) == null, "Wrong energy face: " + side);
                }
            }

            check(crystallizer.getEnergyStorageForExternal(port.east(), Direction.UP) == null,
                "Non-center roof block accepted energy");
            check(
                context.overworld().getBlockState(port.above())
                    .getValue(PipeBlock.DOWN) == PipeBlock.ConnectorType.BLOCK,
                "Top cable is disconnected");

            var water = ResourceVariant.of(ResourceTypes.FLUID, BuiltInRegistries.FLUID.wrapAsHolder(Fluids.WATER));
            var crystal = ResourceVariant.of(ResourceTypes.FLUID,
                BuiltInRegistries.FLUID.wrapAsHolder(ModFluids.SODIUM_ALUMINATE.still().get()));
            StorageOperations.set(crystallizer.getWaterFluidStorage(), water, FluidAmounts.BUCKET * 5);
            StorageOperations.set(crystallizer.getCrystalFluidStorage(), crystal, FluidAmounts.BUCKET);
            crystallizer.getCatalystInventory().clearContent();
            StorageOperations.setEnergy(crystallizer.getEnergyStorage(), CrystallizerBlockEntity.ENERGY_PER_TICK - 1);
            crystallizer.onTick();
            check(crystallizer.getMaxProgress() > 0, "No crystallizer recipe found");
            for (int i = 0; i < 5; i++) {
                crystallizer.onTick();
            }

            check(crystallizer.getProgress() == 0 && !crystallizer.isRunning()
                && crystallizer.getWaterFluidStorage().getAmount() == FluidAmounts.BUCKET * 5
                && crystallizer.getCrystalFluidStorage().getAmount() == FluidAmounts.BUCKET,
                "Processed or consumed fluids with insufficient energy");

            StorageOperations.setEnergy(crystallizer.getEnergyStorage(), 0);
            try (var transaction = TransferTransaction.openRoot()) {
                check(energy.insert(SimpleEnergyStorage.ENERGY, 300, transaction) == 300, "Energy insertion rejected");
                transaction.commit();
            }

            for (int i = 0; i < 3; i++) {
                crystallizer.onTick();
            }

            check(crystallizer.getProgress() == 3 && crystallizer.getEnergyStorage().getAmount() == 0,
                "Incorrect processing energy cost");
            long waterRemaining = crystallizer.getWaterFluidStorage().getAmount();
            long crystalRemaining = crystallizer.getCrystalFluidStorage().getAmount();
            crystallizer.onTick();
            check(crystallizer.getProgress() == 3 && !crystallizer.isRunning()
                && crystallizer.getWaterFluidStorage().getAmount() == waterRemaining
                && crystallizer.getCrystalFluidStorage().getAmount() == crystalRemaining,
                "Did not pause without power");
            try (var transaction = TransferTransaction.openRoot()) {
                check(crystallizer.getWaterFluidStorage().insert(water, 1, transaction) == 0,
                    "Paused recipe accepts fluid refills");
            }

            StorageOperations.setEnergy(crystallizer.getEnergyStorage(), 100_000);
            CompoundTag saved = crystallizer.saveWithoutMetadata(context.overworld().registryAccess());
            StorageOperations.setEnergy(crystallizer.getEnergyStorage(), 0);
            crystallizer.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                context.overworld().registryAccess(), saved));
            check(crystallizer.getProgress() == 3 && crystallizer.getEnergyStorage().getAmount() == 100_000,
                "Energy or progress lost on reload");
            int remaining = crystallizer.getMaxProgress() - crystallizer.getProgress();
            for (int i = 0; i < remaining + 3; i++) {
                crystallizer.onTick();
            }

            check(
                crystallizer.getEnergyStorage().getAmount() == 100_000
                    - remaining * CrystallizerBlockEntity.ENERGY_PER_TICK,
                "Incorrect total batch energy cost");
            check(crystallizer.getOutputInventory().getItem(0).is(ModItems.ALUMINIUM_HYDROXIDE.get()),
                "Output missing");
            check(crystallizer.getWaterFluidStorage().getAmount() == 0
                && crystallizer.getCrystalFluidStorage().getAmount() == 0, "Batch inputs not consumed");

            // A catalyst advances four work units per powered tick, at the same per-tick energy cost.
            crystallizer.getOutputInventory().clearContent();
            crystallizer.getByproductInventory().clearContent();
            StorageOperations.set(crystallizer.getWaterFluidStorage(), water, FluidAmounts.BUCKET * 5);
            StorageOperations.set(crystallizer.getCrystalFluidStorage(), crystal, FluidAmounts.BUCKET);
            crystallizer.getCatalystInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_HYDROXIDE.get()));
            StorageOperations.setEnergy(crystallizer.getEnergyStorage(), 100);
            crystallizer.onTick();
            crystallizer.onTick();
            check(crystallizer.getProgress() == 4 && crystallizer.getEnergyStorage().getAmount() == 0,
                "Catalyst energy cost or progress incorrect");
            crystallizer.onTick();
            check(crystallizer.getProgress() == 4, "Catalyst bypasses power requirement");
            LogUtils.getLogger().info(
                "[CrystallizerValidation] PASS top-center port, cable connection, power pause/resume, energy cost, persistence, outputs and catalyst");
        } catch (Exception exception) {
            LogUtils.getLogger().error("[CrystallizerValidation] FAIL", exception);
        } finally {
            context.server().halt(false);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
