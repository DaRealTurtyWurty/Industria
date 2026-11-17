package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.blockentity.CastingTableBlockEntity;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.metal.MetalDefinition;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.molding.MoldPiece;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.Direction;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.List;
import java.util.UUID;

/** Dedicated-server checks for casting an item, then pouring gold around the item left on the table. */
public final class CastingValidationTestWorld {
    private CastingValidationTestWorld() {
    }

    public static void start(TestWorldContext context) {
        TestWorldScheduler.schedule(context, 2, CastingValidationTestWorld::run);
    }

    private static void run(TestWorldContext context) {
        try {
            List<Example> examples = List.of(
                new Example(MoldPatterns.INGOT, Items.IRON_INGOT, ModMetals.IRON, FluidAmounts.INGOT),
                new Example(MoldPatterns.ROD, ModItems.IRON_ROD.get(), ModMetals.IRON, FluidAmounts.INGOT / 2),
                new Example(MoldPatterns.PLATE, ModItems.ALUMINIUM_PLATE.get(), ModMetals.ALUMINIUM, FluidAmounts.INGOT),
                new Example(MoldPatterns.GEAR, ModItems.BRONZE_GEAR.get(), ModMetals.BRONZE, FluidAmounts.INGOT * 3 / 2),
                new Example(MoldPatterns.BALL_BEARING, ModItems.BRONZE_BALL_BEARING.get(), ModMetals.BRONZE,
                    FluidAmounts.INGOT / 8));
            for (int i = 0; i < examples.size(); i++) {
                validate(context.at(i * 2, 0, 0), examples.get(i));
            }
            validateCancellation(context.at(12, 0, 0), examples.getFirst());
            LogUtils.getLogger().info(
                "[CastingValidation] PASS all five gold mold patterns, cast templates, output insertion rejection, fluid limits, reload, cancellation and reusable molds");
        } catch (Throwable failure) {
            LogUtils.getLogger().error("[CastingValidation] FAIL", failure);
            throw new IllegalStateException("Casting regression checks failed", failure);
        } finally {
            context.server().halt(false);
        }
    }

    private static void validate(TestWorldContext context, Example example) {
        CastingTableBlockEntity table = castTemplate(context, example);
        var gold = ResourceTypes.FLUID.of(ModFluids.MOLTEN_GOLD.still().holder());
        var metal = ResourceTypes.FLUID.of(example.metal().moltenFluid().still().holder());
        check(table.isAcceptingFluid(), "Table rejected the item left by casting: " + example.pattern().id());
        check(pour(table, metal, FluidAmounts.BUCKET) == 0, "Non-gold fluid accepted for making a gold mold");

        long required = FluidAmounts.INGOT * 2;
        if (example.pattern() == MoldPatterns.INGOT) {
            check(pour(table, gold, required - 1) == required - 1, "Partial pour failed");
            for (int tick = 0; tick < 110; tick++) {
                table.onTick();
            }
            check(table.getCoolingFraction() == 0 && table.getOutputInventory().getItem(0).is(example.item())
                && table.getFluidStorage().getAmount() == required - 1, "Incomplete pour consumed the template/fluid");
            check(pour(table, gold, FluidAmounts.BUCKET) == 1, "Final droplet was not capped to the recipe");
        } else {
            check(pour(table, gold, FluidAmounts.BUCKET) == required, "Gold pour exceeded or missed two ingots");
        }
        check(pour(table, gold, 1) == 0, "Accepted excess gold");
        for (int tick = 0; tick < 7; tick++) {
            table.onTick();
        }
        check(Math.abs(table.getCoolingFraction() - 0.07f) < 0.0001f, "Incorrect cooling progress");
        check(table.getOutputInventory().getItem(0).is(example.item()), "Template consumed before completion");
        var pending = table.getCastingItemStack().get(ModDataComponentTypes.MOLD.get());
        check(pending != null && pending.moldId().equals(example.pattern().id()) && pending.piece() == MoldPiece.WHOLE,
            "Pending gold mold lost its pattern");

        var saved = table.saveWithoutMetadata(context.overworld().registryAccess());
        var reloaded = new CastingTableBlockEntity(context.origin(), table.getBlockState());
        reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
            context.overworld().registryAccess(), saved));
        context.overworld().setBlockEntity(reloaded);
        table = reloaded;
        check(Math.abs(table.getCoolingFraction() - 0.07f) < 0.0001f
            && table.getFluidStorage().getAmount() == required, "Reload lost progress or gold");
        for (int tick = 7; tick < 100; tick++) {
            table.onTick();
        }
        ItemStack mold = table.getOutputInventory().getItem(0).copy();
        var moldData = mold.get(ModDataComponentTypes.MOLD.get());
        check(mold.is(ModItems.GOLD_MOLD.get()) && mold.getCount() == 1 && moldData != null
            && moldData.moldId().equals(example.pattern().id()) && moldData.piece() == MoldPiece.WHOLE,
            "Gold mold missing/wrong pattern after cooling");
        check(table.getFluidStorage().getAmount() == 0 && table.getInputInventory().isEmpty()
            && !table.isAcceptingFluid(), "Completed mold did not consume exactly the gold/template");

        // The new mold must itself work, including the whole gold ingot mold for a normally split pattern.
        table.getOutputInventory().clearContent();
        table.getInputInventory().setItem(0, mold);
        check(table.getAssembledMold().map(MoldPattern::id).orElseThrow().equals(example.pattern().id()),
            "New gold mold is unusable");
        check(pour(table, gold, FluidAmounts.BUCKET) == 0, "Gold mold accepted molten gold");
        check(pour(table, metal, FluidAmounts.BUCKET) == example.amount(), "Reusable mold accepts incorrect fluid amount");
        for (int tick = 0; tick < 100; tick++) {
            table.onTick();
        }
        check(table.getOutputInventory().getItem(0).is(example.item()), "New gold mold failed to cast its shape");
        check(table.getInputInventory().getItem(0).is(ModItems.GOLD_MOLD.get())
            && table.getInputInventory().getItem(0).getDamageValue() == 1, "Gold mold consumed or not worn once");
        check(pour(table, gold, required) == 0, "Occupied normal casting table accepted another pour");
    }

    private static CastingTableBlockEntity castTemplate(TestWorldContext context, Example example) {
        context.setBlock(0, 0, 0, ModBlocks.CASTING_TABLE);
        var table = context.requireBlockEntity(0, 0, 0, CastingTableBlockEntity.class);
        var player = context.createFakePlayer(0, 0, 2, new GameProfile(UUID.randomUUID(), "CastTest"));
        var menu = table.createMenu(1, player.getInventory(), player);
        check(!menu.getSlot(2).mayPlace(new ItemStack(example.item())), "Player can place an item in the output slot");
        try (var transaction = TransferTransaction.openRoot()) {
            check(table.getInventoryProvider(Direction.DOWN).insert(ResourceVariant.ofItem(new ItemStack(example.item())),
                1, transaction) == 0, "Automation can insert into the output slot");
        }
        menu.removed(player);

        List<ItemStack> molds = ModItems.SAND_MOLD.get().createStacks(example.pattern());
        for (int i = 0; i < molds.size(); i++) {
            table.getInputInventory().setItem(i, molds.get(i));
        }
        check(pour(table, ResourceTypes.FLUID.of(example.metal().moltenFluid().still().holder()),
            FluidAmounts.BUCKET) == example.amount(), "Initial sand mold fill failed");
        for (int tick = 0; tick < 100; tick++) {
            table.onTick();
        }
        check(table.getOutputInventory().getItem(0).is(example.item()) && table.getInputInventory().isEmpty(),
            "Casting did not leave its item on the table/consume sand molds");
        return table;
    }

    private static void validateCancellation(TestWorldContext context, Example example) {
        var table = castTemplate(context, example);
        var gold = ResourceTypes.FLUID.of(ModFluids.MOLTEN_GOLD.still().holder());
        long required = FluidAmounts.INGOT * 2;
        check(pour(table, gold, required) == required, "Cancellation fixture did not fill");
        table.onTick();
        table.getOutputInventory().removeItem(0, 1);
        table.onTick();
        check(table.getCoolingFraction() == 0 && table.getCastingItemStack().isEmpty()
            && table.getFluidStorage().getAmount() == required, "Removing the template lost gold or created a free mold");
        check(pour(table, gold, 1) == 0, "Empty table accepted fluid after template removal");
    }

    private static long pour(CastingTableBlockEntity table, ResourceVariant<Fluid> fluid, long amount) {
        try (var transaction = TransferTransaction.openRoot()) {
            long inserted = table.getFluidProvider(Direction.UP).insert(fluid, amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }

    private record Example(MoldPattern pattern, Item item, MetalDefinition metal, long amount) {
    }
}
