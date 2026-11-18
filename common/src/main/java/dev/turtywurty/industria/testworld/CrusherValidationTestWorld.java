package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.blockentity.util.StorageOperations;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import dev.turtywurty.industria.recipe.CrusherRecipe;
import net.minecraft.world.SimpleContainer;

/**
 * Opt-in dedicated-server regression checks using the probabilistic coal ore recipe.
 */
public final class CrusherValidationTestWorld {
    private CrusherValidationTestWorld() {
    }

    public static void start(TestWorldContext context) {
        TestWorldScheduler.schedule(context, 2, CrusherValidationTestWorld::run);
    }

    private static void run(TestWorldContext context) {
        try {
            validate(context, true);
            validate(context.at(2, 0, 0), false);
            LogUtils.getLogger().info(
                "[CrusherValidation] PASS single roll, full outputs, combined capacity, pause/resume and reload with/without secondary");
        } catch (Throwable failure) {
            LogUtils.getLogger().error("[CrusherValidation] FAIL", failure);
            throw new IllegalStateException("Crusher regression checks failed", failure);
        } finally {
            context.server().halt(false);
        }
    }

    private static void validate(TestWorldContext context, boolean secondary) {
        context.setBlock(0, 0, 0, ModBlocks.CRUSHER);
        CrusherBlockEntity crusher = context.requireBlockEntity(0, 0, 0, CrusherBlockEntity.class);
        SimpleContainer input = crusher.getWrappedContainerStorage().getInventory(CrusherBlockEntity.INPUT_SLOT);
        SimpleContainer output = crusher.getWrappedContainerStorage().getInventory(CrusherBlockEntity.OUTPUT_SLOT);
        input.setItem(0, new ItemStack(Items.COAL_ORE));
        StorageOperations.setEnergy(crusher.getEnergy(), 10_000);
        CrusherRecipe recipe = context.overworld().recipeAccess()
            .getRecipeFor(ModRecipeTypes.CRUSHER.get(), crusher.getInventory(), context.overworld())
            .orElseThrow().value();
        check(recipe.outputA().item() == Items.COAL && recipe.outputB().item() == Items.COAL,
            "Expected the coal recipe with matching primary/secondary items");

        // Pick deterministic seeds for both outcomes, then retain the RNG state after exactly one roll.
        long seed = 0;
        var expectedRandom = RandomSource.create(seed);
        while (recipe.assemble(expectedRandom).getSecond().isEmpty() == secondary) {
            check(++seed < 10_000, "Could not find a seed for the requested secondary outcome");
            expectedRandom.setSeed(seed);
        }

        long expectedNextRandom = expectedRandom.nextLong();
        context.overworld().getRandom().setSeed(seed);

        output.setItem(0, new ItemStack(Items.STONE, 64));
        output.setItem(1, new ItemStack(Items.STONE, 64));
        crusher.onTick(); // Select the recipe and roll, even when output storage is full.
        check(crusher.getMaxProgress() == recipe.processTime(), "Operation did not start");
        CompoundTag started = save(context, crusher);
        for (int tick = 0; tick < 20; tick++) {
            crusher.onTick();
        }

        check(crusher.getProgress() == 0 && crusher.getEnergy().getAmount() == 10_000,
            "Full output storage consumed work or energy");
        check(started.get("PendingOutputs").equals(save(context, crusher).get("PendingOutputs")),
            "Full output storage changed the pending roll");

        output.clearContent();
        for (int tick = 0; tick < 4; tick++) {
            crusher.onTick();
        }

        check(crusher.getProgress() == 4, "Operation failed to resume");
        output.setItem(0, new ItemStack(Items.COAL, 63));
        output.setItem(1, new ItemStack(Items.STONE, 64));
        // Each output fits separately, but two coal outputs cannot share one remaining space.
        if (!secondary) {
            output.setItem(0, new ItemStack(Items.COAL, 64));
        }

        long pausedEnergy = crusher.getEnergy().getAmount();
        for (int tick = 0; tick < 20; tick++) {
            crusher.onTick();
        }

        check(crusher.getProgress() == 4 && crusher.getEnergy().getAmount() == pausedEnergy,
            "Blocked operation lost progress or consumed energy");
        check(input.getItem(0).getCount() == 1 && output.getItem(0).getCount() == (secondary ? 63 : 64),
            "Fit check changed inventory");

        CompoundTag paused = save(context, crusher);
        var reloaded = new CrusherBlockEntity(context.origin(), crusher.getBlockState());
        reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
            context.overworld().registryAccess(), paused));
        context.overworld().setBlockEntity(reloaded);
        check(reloaded.getProgress() == 4 && reloaded.getMaxProgress() == recipe.processTime(),
            "Reload lost operation progress");
        check(paused.get("PendingOutputs").equals(save(context, reloaded).get("PendingOutputs")),
            "Reload lost pending outputs (including an empty secondary)");
        reloaded.onTick();
        check(reloaded.getProgress() == 4 && reloaded.getEnergy().getAmount() == pausedEnergy,
            "Reloaded operation did not stay paused");

        output = reloaded.getWrappedContainerStorage().getInventory(CrusherBlockEntity.OUTPUT_SLOT);
        output.setItem(0, new ItemStack(Items.COAL, secondary ? 62 : 63));
        int remaining = reloaded.getMaxProgress() - reloaded.getProgress();
        for (int tick = 0; tick < remaining; tick++) {
            reloaded.onTick();
        }

        check(reloaded.getProgress() == reloaded.getMaxProgress(), "Operation failed to finish processing");

        // Also block the completion tick and verify input/output insertion is all-or-nothing.
        output.setItem(0, new ItemStack(Items.COAL, 64));
        long completionEnergy = reloaded.getEnergy().getAmount();
        reloaded.onTick();
        check(reloaded.getProgress() == recipe.processTime()
            && reloaded.getEnergy().getAmount() == completionEnergy
            && reloaded.getWrappedContainerStorage().getInventory(CrusherBlockEntity.INPUT_SLOT)
                .getItem(0).getCount() == 1,
            "Blocked completion consumed input, energy or progress");
        output.setItem(0, new ItemStack(Items.COAL, secondary ? 62 : 63));
        reloaded.onTick();
        check(reloaded.getProgress() == 0 && reloaded.getMaxProgress() == 0, "Completed operation did not reset");
        check(reloaded.getWrappedContainerStorage().getInventory(CrusherBlockEntity.INPUT_SLOT).isEmpty(),
            "Completion did not consume input");
        check(output.getItem(0).is(Items.COAL) && output.getItem(0).getCount() == 64
            && output.getItem(1).is(Items.STONE) && output.getItem(1).getCount() == 64,
            "Completion lost or rerolled an output");
        check(reloaded.getEnergy().getAmount() == pausedEnergy - (remaining + 1) * CrusherBlockEntity.getEnergyCost(),
            "Incorrect processing energy cost");
        check(!save(context, reloaded).contains("PendingOutputs"), "Completed operation retained pending outputs");
        check(context.overworld().getRandom().nextLong() == expectedNextRandom,
            "Processing, fit checks or reload rolled outputs more than once");
    }

    private static CompoundTag save(TestWorldContext context, CrusherBlockEntity crusher) {
        return crusher.saveWithoutMetadata(context.overworld().registryAccess());
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
