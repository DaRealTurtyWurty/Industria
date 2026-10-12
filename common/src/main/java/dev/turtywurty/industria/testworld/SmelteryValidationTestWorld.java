package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.blockentity.CastingBasinBlockEntity;
import dev.turtywurty.industria.blockentity.ChargingHatchBlockEntity;
import dev.turtywurty.industria.blockentity.FireboxBlockEntity;
import dev.turtywurty.industria.blockentity.SmelteryControllerBlockEntity;
import dev.turtywurty.industria.blockentity.TappingOutletBlockEntity;
import dev.turtywurty.industria.blockentity.util.StorageOperations;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.menu.ChargingHatchMenu;
import dev.turtywurty.industria.menu.FireboxMenu;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.ChargingHatchOpenPayload;
import dev.turtywurty.industria.recipe.SmelteryRecipe;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.FuelUtils;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ContainerStorage;

/** Opt-in server tests that mutate real structures and inspect their serialized controller state. */
public final class SmelteryValidationTestWorld {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long BLOCK = FluidAmounts.BLOCK;

    private SmelteryValidationTestWorld() {
    }

    public static void start(TestWorldContext root) {
        var suite = new Suite(root);

        suite.scenario("glass side walls and vertical corners accepted", 4)
            .step(60, "replace side wall and corner with glass", s -> {
                s.expect(0, s.expected(1, 3, 0));
                s.seed(BLOCK);
                s.context.setBlock(3, 3, 0, ModBlocks.SMELTERY_GLASS);
                s.context.setBlock(0, 4, 0, ModBlocks.SMELTERY_GLASS);
            })
            .step(40, "glass sides retain chamber and fluid", s -> s.expect(0, s.expected(1, 3, BLOCK)));

        suite.scenario("glass shared wall accepted", 8, 4)
            .step(60, "replace shared wall with glass", s -> {
                s.expect(0, s.expected(1, 3, 0), s.expected(5, 7, 0));
                s.seed(BLOCK, 2 * BLOCK);
                s.context.setBlock(4, 3, 2, ModBlocks.SMELTERY_GLASS);
            })
            .step(40, "both chambers retain independent fluid", s -> s.expect(0,
                s.expected(1, 3, BLOCK), s.expected(5, 7, 2 * BLOCK)));

        invalidStructure(suite, "glass roof rejected", s -> s.context.setBlock(2, 5, 1, ModBlocks.SMELTERY_GLASS));
        invalidStructure(suite, "glass floor rejected", s -> s.context.setBlock(2, 2, 1, ModBlocks.SMELTERY_GLASS));
        invalidStructure(suite, "glass foundation bottom rejected",
            s -> s.context.setBlock(2, 0, 2, ModBlocks.SMELTERY_GLASS));
        invalidStructure(suite, "glass foundation bottom corner rejected",
            s -> s.context.setBlock(0, 0, 0, ModBlocks.SMELTERY_GLASS));
        invalidStructure(suite, "glass fuel perimeter rejected",
            s -> s.context.setBlock(2, 1, 0, ModBlocks.SMELTERY_GLASS));
        invalidStructure(suite, "glass fuel perimeter corner rejected",
            s -> s.context.setBlock(0, 1, 0, ModBlocks.SMELTERY_GLASS));

        suite.scenario("controller embedded rebuild preserves fluid", 4)
            .step(60, "embedded formation and shared-wall resize", s -> {
                s.expect(0, s.expected(1, 3, 0));
                s.seed(BLOCK);
                var controller = s.controller();
                s.build(8, 4);
                check(s.controller() == controller, "Rebuild replaced the embedded controller");
            })
            .step(40, "both chambers use the wall controller", s -> {
                s.expect(0, s.expected(1, 3, BLOCK), s.expected(5, 7, 0));
                s.build(4);
            })
            .step(40, "resize retains original chamber fluid", s -> s.expect(0, s.expected(1, 3, BLOCK)));

        suite.scenario("controller exterior attachment rejected", 4)
            .step(60, "move controller outside an otherwise valid shell", s -> {
                s.expect(0, s.expected(1, 3, 0));
                s.context.setBlock(0, 3, 1, ModBlocks.FIREBRICK);
                s.context.setBlock(0, 2, -1, ModBlocks.SMELTERY_CONTROLLER.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
            })
            .step(40, "outside controller cannot form chambers", s -> {
                var controller = s.context.requireBlockEntity(0, 2, -1, SmelteryControllerBlockEntity.class);
                check(!controller.isStructureReady() && controller.getChamberInfos().isEmpty(),
                    "An exterior controller formed the smeltery");
                s.context.removeBlock(0, 2, -1);
                s.build(4);
            })
            .step(40, "embedded replacement forms again", s -> s.expect(0, s.expected(1, 3, 0)));

        suite.scenario("shared walls and common fuel", 8, 4)
            .step(60, "initial formation", s -> {
                s.expect(0, s.expected(1, 3, 0), s.expected(5, 7, 0));
                s.seed(5 * BLOCK, 7 * BLOCK);
            })
            .step(40, "stable independent tanks", s -> s.expect(0,
                s.expected(1, 3, 5 * BLOCK), s.expected(5, 7, 7 * BLOCK)));

        suite.scenario("isolated fuel then reconnection", 8, 4)
            .step(60, "disconnect fuel", s -> {
                s.expect(0, s.expected(1, 3, 0), s.expected(5, 7, 0));
                s.seed(BLOCK, 2 * BLOCK);
                s.context.fill(4, 1, 1, 4, 1, 3, ModBlocks.FIREBRICK.get());
            })
            .step(40, "unheated chamber rejected", s -> {
                s.expect(2 * BLOCK, s.expected(1, 3, BLOCK));
                s.context.fillAir(4, 1, 1, 4, 1, 3);
            })
            .step(40, "reconnected chamber restored", s -> s.expect(0,
                s.expected(1, 3, BLOCK), s.expected(5, 7, 2 * BLOCK)));

        suite.scenario("resize growth", 4)
            .step(60, "grow", s -> {
                s.seed(10 * BLOCK);
                s.build(8);
            })
            .step(40, "larger tank preserves fluid", s -> s.expect(0, s.expected(1, 7, 10 * BLOCK)));

        suite.scenario("shrink excess and expand", 4)
            .step(60, "shrink", s -> {
                s.seed(15 * BLOCK);
                s.build(3);
            })
            .step(40, "overflow retained", s -> {
                s.expect(3 * BLOCK, s.expected(1, 2, 12 * BLOCK));
                s.roundTrip();
                s.build(4);
            })
            .step(40, "overflow restored on expansion", s -> s.expect(0, s.expected(1, 3, 15 * BLOCK)));

        suite.scenario("split fills one then next", 8)
            .step(60, "split", s -> {
                s.seed(25 * BLOCK);
                s.build(8, 4);
            })
            .step(40, "split allocation", s -> s.expect(0,
                s.expected(1, 3, 18 * BLOCK), s.expected(5, 7, 7 * BLOCK)))
            .step(40, "split stable", s -> s.expect(0,
                s.expected(1, 3, 18 * BLOCK), s.expected(5, 7, 7 * BLOCK)));

        suite.scenario("split overflow", 8)
            .step(60, "split full tank", s -> {
                s.seed(40 * BLOCK);
                s.build(8, 4);
            })
            .step(40, "both full and excess retained", s -> s.expect(4 * BLOCK,
                s.expected(1, 3, 18 * BLOCK), s.expected(5, 7, 18 * BLOCK)))
            .step(40, "overflow stable", s -> s.expect(4 * BLOCK,
                s.expected(1, 3, 18 * BLOCK), s.expected(5, 7, 18 * BLOCK)));

        suite.scenario("active merge discards losing fluid", 8, 4)
            .step(60, "merge", s -> {
                s.seed(5 * BLOCK, 7 * BLOCK);
                s.build(8);
            })
            .step(40, "one original survives", s -> s.expect(0, s.expected(1, 7, 5 * BLOCK)))
            .step(40, "discard remains discarded", s -> s.expect(0, s.expected(1, 7, 5 * BLOCK)));

        suite.scenario("merge different active fluids", 8, 4)
            .step(60, "merge water and lava", s -> {
                s.seed(new Fluid[]{Fluids.WATER, Fluids.LAVA}, 5 * BLOCK, 7 * BLOCK);
                s.build(8);
            })
            .step(40, "surviving water only", s -> s.expect(0, s.expected(1, 7, 5 * BLOCK)));

        suite.scenario("merge after both sources become reserves", 8, 4)
            .step(60, "retire both chambers", s -> {
                s.seed(5 * BLOCK, 7 * BLOCK);
                s.context.removeBlock(2, 6, 2);
                s.context.removeBlock(6, 6, 2);
            })
            .step(40, "both fluids retained before merge", s -> {
                s.expect(12 * BLOCK);
                s.build(8);
            })
            .step(40, "reserve merge must keep just one original", s -> s.expect(0, s.expected(1, 7, 5 * BLOCK)));

        suite.scenario("partition move with separate resizes", 8, 4)
            .step(60, "move partition", s -> {
                s.seed(15 * BLOCK, BLOCK);
                s.build(8, 3);
            })
            .step(40, "independent fluids and shrink overflow", s -> s.expect(3 * BLOCK,
                s.expected(1, 2, 12 * BLOCK), s.expected(4, 7, BLOCK)));

        suite.scenario("simultaneous split and merge", 8, 4)
            .step(60, "move partition across original interior", s -> {
                s.seed(15 * BLOCK, BLOCK);
                s.build(8, 2);
            })
            .step(40, "new geometry and survivor allocation", s -> s.expect(0,
                s.expected(1, 1, 6 * BLOCK), s.expected(3, 7, 9 * BLOCK)))
            .step(40, "allocation stays stable", s -> s.expect(0,
                s.expected(1, 1, 6 * BLOCK), s.expected(3, 7, 9 * BLOCK)));

        suite.scenario("unloaded structure chunk preserves prior state", 52)
            .step(60, "unload remote structure", s -> {
                s.seed(4 * BLOCK);
                int controllerChunk = s.context.origin().getX() >> 4;
                for (int chunk : s.forcedChunks) {
                    if (chunk != controllerChunk) {
                        s.context.overworld().setChunkForced(chunk, s.context.origin().getZ() >> 4, false);
                    }
                }
            })
            .step(380, "unknown terrain is not retired", s -> {
                check(!s.context.overworld().isLoaded(s.context.pos(52, 3, 2)), "Remote chunk did not actually unload");
                s.expect(0, s.expected(1, 51, 4 * BLOCK));
                var hatch = s.context.requireBlockEntity(26, 3, 0, ChargingHatchBlockEntity.class);
                check(hatch.getChamber() == null, "Unloaded structure still resolves an operational hatch");
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 2));
            })
            .step(40, "unloaded structure does not start new fuel", s -> {
                check(s.firebox().getBurnTime() == 0 && s.fuelCount(Items.COAL) == 2,
                    "Unloaded structure consumed fuel");
                s.expect(0, s.expected(1, 51, 4 * BLOCK));
            });

        invalidStructure(suite, "second hatch in one chamber", s -> s.context.setBlock(4, 3, 2,
            ModBlocks.CHARGING_HATCH.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)));
        invalidStructure(suite, "second connected controller", s -> s.context.setBlock(4, 2, -1,
            ModBlocks.SMELTERY_CONTROLLER.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)));
        invalidStructure(suite, "second firebox", s -> s.context.setBlock(4, 1, 2,
            ModBlocks.FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)));
        invalidStructure(suite, "missing firebox", s -> s.context.removeBlock(0, 1, 2));
        invalidStructure(suite, "firebox faces into structure", s -> s.context.setBlock(0, 1, 2,
            ModBlocks.FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)));
        invalidStructure(suite, "missing hatch", s -> s.context.setBlock(2, 3, 0, ModBlocks.FIREBRICK));
        invalidStructure(suite, "hatch faces into chamber", s -> s.context.setBlock(2, 3, 0,
            ModBlocks.CHARGING_HATCH.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)));
        invalidStructure(suite, "chimney inline with roof", s -> {
            s.context.removeBlock(2, 6, 2);
            s.context.setBlock(2, 5, 2, ModBlocks.CHIMNEY);
        });
        invalidStructure(suite, "chimney above roof perimeter", s -> {
            s.context.removeBlock(2, 6, 2);
            s.context.setBlock(0, 6, 2, ModBlocks.CHIMNEY);
        });
        invalidStructure(suite, "chimney two blocks above roof", s -> {
            s.context.setBlock(2, 6, 2, ModBlocks.FIREBRICK);
            s.context.setBlock(2, 7, 2, ModBlocks.CHIMNEY);
        });
        invalidStructure(suite, "outlet inline with wall", s -> {
            s.context.removeBlock(2, 3, -1);
            s.context.setBlock(4, 3, 2, ModBlocks.TAPPING_OUTLET);
        });
        invalidStructure(suite, "outlet outside wall corner", s -> {
            s.context.removeBlock(2, 3, -1);
            s.context.setBlock(0, 3, -1, ModBlocks.TAPPING_OUTLET);
        });
        invalidStructure(suite, "outlet at floor height", s -> {
            s.context.removeBlock(2, 3, -1);
            s.context.setBlock(2, 2, -1, ModBlocks.TAPPING_OUTLET);
        });
        invalidStructure(suite, "outlet above roof", s -> {
            s.context.removeBlock(2, 3, -1);
            s.context.setBlock(2, 6, 0, ModBlocks.TAPPING_OUTLET);
        });
        invalidStructure(suite, "fuel perimeter breach", s -> s.context.removeBlock(4, 1, 2));
        invalidStructure(suite, "missing bottom foundation brick", s -> s.context.removeBlock(2, 0, 2));
        invalidStructure(suite, "missing chamber floor brick", s -> s.context.removeBlock(2, 2, 2));
        invalidStructure(suite, "obstructed fuel layer", s -> s.context.setBlock(2, 1, 2, ModBlocks.FIREBRICK));
        invalidStructure(suite, "chamber wall breach", s -> s.context.removeBlock(4, 3, 2));
        invalidStructure(suite, "chamber roof breach", s -> s.context.removeBlock(1, 5, 1));
        invalidStructure(suite, "noncuboid chamber interior", s -> s.context.setBlock(1, 3, 1, ModBlocks.FIREBRICK));

        suite.scenario("hatch in shared wall invalidates both chambers", 8, 4)
            .step(60, "replace shared brick with hatch", s -> {
                s.seed(BLOCK, 2 * BLOCK);
                s.context.setBlock(4, 3, 2, ModBlocks.CHARGING_HATCH.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
            })
            .step(40, "both chambers rejected without losing fluid", s -> {
                s.expect(3 * BLOCK);
                s.build(8, 4);
            })
            .step(40, "shared wall repair restores independent tanks", s -> s.expect(0,
                s.expected(1, 3, BLOCK), s.expected(5, 7, 2 * BLOCK)));

        suite.scenario("alternate valid chimney and outlet positions", 4)
            .step(60, "move attachments", s -> {
                s.seed(BLOCK);
                s.context.removeBlock(2, 6, 2);
                s.context.setBlock(1, 6, 1, ModBlocks.CHIMNEY);
                s.context.removeBlock(2, 3, -1);
                s.context.setBlock(-1, 4, 2, ModBlocks.TAPPING_OUTLET);
            })
            .step(40, "different roof position and west wall remain valid", s -> s.expect(0,
                s.expected(1, 3, BLOCK)));

        suite.scenario("hatch link follows chamber invalidation and repair", 4)
            .step(60, "linked hatch survives serialization", s -> {
                s.expectHatchLink(true);
                var hatch = s.hatch();
                var data = hatch.saveWithoutMetadata(s.context.overworld().registryAccess());
                hatch.setControllerPos(null);
                hatch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                s.expectHatchLink(true);
                s.seed(BLOCK);
                s.context.removeBlock(2, 6, 2);
            })
            .step(40, "chamber removal clears link and rejects stale saved position", s -> {
                s.expect(BLOCK);
                s.expectHatchLink(false);
                s.hatch().setControllerPos(s.controller().getBlockPos());
                check(s.hatch().getChamber() == null, "Stale position resolves an inactive chamber");
                s.hatch().setControllerPos(null);
                s.context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "repair relinks same hatch and restores fluid", s -> {
                s.expect(0, s.expected(1, 3, BLOCK));
                s.expectHatchLink(true);
            });

        suite.scenario("deactivation clears empty chamber hatch link", 4)
            .step(60, "remove firebox from empty chamber", s -> {
                s.expectHatchLink(true);
                s.context.removeBlock(0, 1, 2);
            })
            .step(40, "empty chamber hatch unlinked", s -> {
                s.expect(0);
                s.expectHatchLink(false);
                s.context.setBlock(0, 1, 2, ModBlocks.FIREBOX.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
            })
            .step(40, "firebox repair restores link", s -> {
                s.expect(0, s.expected(1, 3, 0));
                s.expectHatchLink(true);
            });

        suite.scenario("hatch rejects incorrect controller position", 4)
            .step(60, "point hatch at firebrick instead of controller", s -> {
                s.expectHatchLink(true);
                s.hatch().setControllerPos(s.context.pos(0, 2, 0));
                check(s.hatch().getChamber() == null, "Non-controller position resolves a chamber");
            })
            .step(40, "validation repairs hatch ownership", s -> s.expectHatchLink(true));

        suite.scenario("hatch inventory serialization preserves every slot", 4)
            .step(60, "save clear and reload inventory", s -> {
                var hatch = s.hatch();
                check(hatch.getInventory().getContainerSize() == 18, "Expected 18 hatch slots");
                hatch.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 17));
                hatch.getInventory().setItem(4, new ItemStack(Items.GOLD_INGOT, 3));
                hatch.getInventory().setItem(8, new ItemStack(Items.DIAMOND, 2));
                var data = hatch.saveWithoutMetadata(s.context.overworld().registryAccess());
                hatch.getInventory().clearContent();
                hatch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                check(hatch.getInventory().getItem(0).is(Items.IRON_INGOT)
                    && hatch.getInventory().getItem(0).getCount() == 1, "First slot changed on reload");
                check(hatch.getInventory().getItem(4).is(Items.GOLD_INGOT)
                    && hatch.getInventory().getItem(4).getCount() == 1, "Middle slot changed on reload");
                check(hatch.getInventory().getItem(8).is(Items.DIAMOND)
                    && hatch.getInventory().getItem(8).getCount() == 1, "Last slot changed on reload");
                check(s.itemCount(Items.IRON_INGOT) + s.itemCount(Items.GOLD_INGOT)
                    + s.itemCount(Items.DIAMOND) == 3, "Reload duplicated or lost items");
                s.expectHatchLink(true);
            });

        suite.scenario("hatch inventory survives invalidation and repair", 4)
            .step(60, "invalidate chamber containing input items", s -> {
                s.hatch().getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 24));
                s.context.removeBlock(2, 6, 2);
            })
            .step(40, "items stay in unlinked hatch", s -> {
                s.expectHatchLink(false);
                check(s.itemCount(Items.IRON_INGOT) == 1, "Invalidation changed input items");
                s.context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "repair preserves input inventory", s -> {
                s.expectHatchLink(true);
                check(s.itemCount(Items.IRON_INGOT) == 1, "Repair changed input items");
            });

        suite.scenario("hatch automation supports committed and rolled back transfers", 4)
            .step(60, "look up registered storage on every side", s -> {
                var variant = ResourceTypes.ITEM.of(Items.IRON_INGOT.builtInRegistryHolder());
                for (Direction direction : Direction.values()) {
                    var storage = TransferService.get().findBlock(StorageKeys.ITEM,
                        s.context.overworld(), s.context.pos(2, 3, 0), direction);
                    check(storage != null, "No registered item storage on " + direction);
                    try (var transaction = TransferTransaction.openRoot()) {
                        check(storage.insert(variant, 1, transaction) == 1, "Insertion failed on " + direction);
                        transaction.commit();
                    }
                }
                check(s.itemCount(Items.IRON_INGOT) == 6, "Committed transfers lost items");
                var storage = TransferService.get().findBlock(StorageKeys.ITEM,
                    s.context.overworld(), s.context.pos(2, 3, 0), Direction.NORTH);
                try (var transaction = TransferTransaction.openRoot()) {
                    check(storage.insert(variant, 2, transaction) == 2, "Rollback insertion failed");
                }
                check(s.itemCount(Items.IRON_INGOT) == 6, "Uncommitted transfer changed inventory");
                try (var transaction = TransferTransaction.openRoot()) {
                    check(storage.extract(variant, 2, transaction) == 2, "Extraction failed");
                    transaction.commit();
                }
                check(s.itemCount(Items.IRON_INGOT) == 4, "Committed extraction changed wrong amount");
            });

        suite.scenario("breaking hatch drops its inventory exactly once", 4)
            .step(60, "break occupied hatch", s -> {
                s.hatch().getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 17));
                s.hatch().getInventory().setItem(4, new ItemStack(Items.GOLD_INGOT, 3));
                s.hatch().getInventory().setItem(8, new ItemStack(Items.DIAMOND, 2));
                s.context.removeBlock(2, 3, 0);
                s.expectDrops(1, 1, 1);
                s.context.removeBlock(2, 3, 0);
                s.expectDrops(1, 1, 1);
            });

        suite.scenario("hatch menu maps slots and shift click without losing items", 4)
            .step(60, "create server menu and transfer items both ways", s -> {
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "HatchMenuTest"));
                var hatch = s.hatch();
                var created = hatch.createMenu(1, player.getInventory(), player);
                check(created instanceof ChargingHatchMenu, "Hatch did not create its registered menu");
                var menu = (ChargingHatchMenu) created;
                check(menu.slots.size() == 54, "Expected 18 hatch and 36 player slots");
                for (int index = 0; index < 9; index++) {
                    var slot = menu.getSlot(index);
                    check(slot.container == hatch.getInventory(), "Server slot uses the wrong inventory");
                    check(slot.x == 62 + (index % 3) * 18 && slot.y == 17 + (index / 3) * 18,
                        "Slot does not align with dispenser background");
                    hatch.getInventory().setItem(index, new ItemStack(Items.IRON_INGOT, index + 1));
                    check(slot.getItem().getCount() == 1 && slot.getMaxStackSize() == 1,
                        "Hatch slot must hold one item");
                }
                hatch.getInventory().clearContent();
                player.getInventory().clearContent();
                player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 10));
                check(menu.quickMoveStack(player, 45).getCount() == 10, "Player-to-hatch shift click failed");
                check(s.itemCount(Items.IRON_INGOT) == 1 && player.getInventory().getItem(0).getCount() == 9,
                    "First shift click did not transfer exactly one item");
                for (int index = 0; index < 8; index++) {
                    check(!menu.quickMoveStack(player, 45).isEmpty(), "Later one-item slot transfer failed");
                }
                check(s.itemCount(Items.IRON_INGOT) == 9 && player.getInventory().getItem(0).getCount() == 1,
                    "Player-to-hatch transfer duplicated or lost items");
                for (int index = 0; index < 9; index++) {
                    check(menu.quickMoveStack(player, index).getCount() == 1, "Hatch-to-player shift click failed");
                }
                int playerAmount = 0;
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    var stack = player.getInventory().getItem(slot);
                    if (stack.is(Items.IRON_INGOT)) {
                        playerAmount += stack.getCount();
                    }
                }
                check(s.itemCount(Items.IRON_INGOT) == 0 && playerAmount == 10,
                    "Hatch-to-player transfer duplicated or lost items");
                check(menu.stillValid(player), "Nearby player cannot use hatch menu");
                player.setPos(s.context.origin().getX() + 100, s.context.origin().getY() + 3,
                    s.context.origin().getZ());
                check(!menu.stillValid(player), "Distant player can still use hatch menu");
                player.setPos(s.context.origin().getX() + 2.5, s.context.origin().getY() + 3,
                    s.context.origin().getZ() - 1.5);
                s.context.removeBlock(2, 3, 0);
                check(!menu.stillValid(player), "Menu remains valid after hatch removal");
                menu.removed(player);
            });

        suite.scenario("hatch payload menu uses separate synchronized inventory", 4)
            .step(60, "construct payload-side menu", s -> {
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "HatchClientTest"));
                s.hatch().getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 7));
                var menu = new ChargingHatchMenu(1, player.getInventory(),
                    new ChargingHatchOpenPayload(s.context.pos(2, 3, 0), s.hatch().getInventory().getContainerSize()));
                check(menu.slots.size() == 54, "Payload menu has wrong slot count");
                check(menu.getSlot(0).container != s.hatch().getInventory(),
                    "Payload slots bypass client inventory copy");
                check(menu.getSlot(0).getItem().isEmpty(), "Payload inventory was not initially empty");
                check(menu.getSlot(0).getMaxStackSize() == 1,
                    "Client hatch slot does not preserve the one-item limit");
                menu.getSlot(0).set(new ItemStack(Items.GOLD_INGOT, 3));
                check(s.itemCount(Items.IRON_INGOT) == 1 && s.itemCount(Items.GOLD_INGOT) == 0,
                    "Payload menu changed backing block entity inventory");
                menu.removed(player);
            });

        suite.scenario("resizable hatch distributes items without loss", 4)
            .step(60, "fill all allocated slots and return excess", s -> {
                var inventory = s.hatch().getInventory();
                check(inventory.getContainerSize() == 18 && s.hatch().getProcessingSlotCount() == 18,
                    "Volume did not allocate 18 processing slots");
                var supplied = new ItemStack(Items.IRON_INGOT, 20);
                var remainder = inventory.addItem(supplied);
                check(supplied.getCount() == 20, "addItem modified its supplied stack");
                check(remainder.getCount() == 2 && s.itemCount(Items.IRON_INGOT) == 18,
                    "addItem lost or duplicated items");
                for (int slot = 0; slot < 18; slot++) {
                    check(inventory.getItem(slot).getCount() == 1, "Slot exceeded the one-item limit");
                }
                check(!inventory.canAddItem(new ItemStack(Items.IRON_INGOT)),
                    "Full one-item slots report insertion room");
                check(!inventory.canPlaceItem(-1, supplied) && !inventory.canPlaceItem(18, supplied),
                    "Insertion accepts an invalid slot index");
            });

        suite.scenario("hatch overflow persists and remains extractable", 4)
            .step(60, "shrink, reload and extract overflow", s -> {
                var hatch = s.hatch();
                hatch.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT));
                hatch.getInventory().setItem(17, new ItemStack(Items.GOLD_INGOT));
                hatch.prepareMeltingSlot(0, 40);
                hatch.advanceMeltingProgress(0, 1.5);
                hatch.prepareMeltingSlot(17, 40);
                hatch.advanceMeltingProgress(17, 10);
                hatch.setProcessingSlotCount(6);
                check(hatch.getInventory().getContainerSize() == 18, "Shrink deleted allocated slots");
                check(hatch.getMeltingProgress(17) == 0, "Overflow retains melting progress");
                check(!hatch.getInventory().canPlaceItem(17, new ItemStack(Items.GOLD_INGOT)),
                    "Overflow permits insertion");
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "HatchOverflowTest"));
                var menu = (ChargingHatchMenu) hatch.createMenu(1, player.getInventory(), player);
                check(menu.getProcessingSlotCount() == 6 && !menu.getSlot(17).mayPlace(new ItemStack(Items.GOLD_INGOT))
                    && menu.getSlot(17).mayPickup(player), "Menu mishandles overflow insertion or extraction");
                check(menu.getDisplayedSlotCount() == 18 && menu.getMaxScrollRow() == 3,
                    "Occupied overflow does not extend the displayed range");
                menu.setScrollRow(3);
                check(menu.isSlotVisible(17) && !menu.isSlotVisible(16),
                    "Empty overflow remains visible beside an occupied overflow slot");
                check(Math.abs(menu.getMeltingProgress(0) - 0.0375F) < 0.001F,
                    "Progress property indexing changed");
                var snapshot = hatch.saveWithoutMetadata(s.context.overworld().registryAccess()).copy();
                var restored = new ChargingHatchBlockEntity(hatch.getBlockPos(), hatch.getBlockState());
                restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), snapshot));
                check(restored.getInventory().getContainerSize() == 18 && restored.getProcessingSlotCount() == 6,
                    "Reload lost allocated or processing capacity");
                check(restored.getInventory().getItem(17).is(Items.GOLD_INGOT)
                    && restored.getMeltingProgress(0) == 1.5, "Reload lost overflow or fractional progress");
                var storage = hatch.getInventoryProvider(Direction.NORTH);
                try (var transaction = TransferTransaction.openRoot()) {
                    check(
                        storage.extract(ResourceTypes.ITEM.of(Items.GOLD_INGOT.builtInRegistryHolder()), 1,
                            transaction) == 1,
                        "Automation cannot extract overflow");
                    transaction.commit();
                }
                check(hatch.getInventory().getItem(17).isEmpty(), "Overflow extraction did not remove item");
                check(menu.getDisplayedSlotCount() == 6 && menu.getMaxScrollRow() == 0,
                    "Removing the last overflow item leaves empty trailing rows");
                menu.setScrollRow(menu.getScrollRow());
                check(menu.getScrollRow() == 0 && menu.isSlotVisible(0) && !menu.isSlotVisible(6),
                    "Scroll clamping or empty overflow visibility is incorrect after extraction");
                menu.removed(player);
            });

        suite.scenario("hatch cached automation follows inventory growth", 4)
            .step(60, "grow while storage and menu references exist", s -> {
                var hatch = s.hatch();
                var inventory = hatch.getInventory();
                var storage = hatch.getInventoryProvider(Direction.NORTH);
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "HatchGrowthTest"));
                var menu = (ChargingHatchMenu) hatch.createMenu(1, player.getInventory(), player);
                hatch.getInventory().setItem(17, new ItemStack(Items.GOLD_INGOT));
                hatch.prepareMeltingSlot(17, 40);
                hatch.advanceMeltingProgress(17, 1.5);
                hatch.setProcessingSlotCount(27);
                check(hatch.getInventory() == inventory && inventory.getContainerSize() == 27,
                    "Growth replaced the exposed inventory or kept its old size");
                check(inventory.getItem(17).is(Items.GOLD_INGOT) && hatch.getMeltingProgress(17) == 1.5,
                    "Growth lost existing items or progress");
                check(!menu.stillValid(player), "Menu stays valid with an obsolete slot layout");
                inventory.clearContent();
                try (var transaction = TransferTransaction.openRoot()) {
                    check(
                        storage.insert(ResourceTypes.ITEM.of(Items.IRON_INGOT.builtInRegistryHolder()), 27,
                            transaction) == 27,
                        "Cached storage cannot insert into newly allocated slots");
                    transaction.commit();
                }
                check(s.itemCount(Items.IRON_INGOT) == 27, "Cached transfer lost inserted items");
                menu.removed(player);
            });

        suite.scenario("hatch scrolling preserves slot indices and items", 4)
            .step(60, "scroll copied menu to its last row", s -> {
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "HatchScrollTest"));
                // Deliberately differ from the block entity's 18-slot inventory.
                var menu = new ChargingHatchMenu(1, player.getInventory(),
                    new ChargingHatchOpenPayload(s.hatch().getBlockPos(), 27));
                check(menu.getHatchSlotCount() == 27 && menu.slots.size() == 63,
                    "Payload slot count was replaced by the block entity's size");
                menu.setData(0, 18);
                menu.getSlot(26).set(new ItemStack(Items.DIAMOND));
                var backing = menu.getSlot(26).container;
                var playerSlot = menu.getSlot(27);
                menu.setScrollRow(100);
                check(menu.getScrollRow() == 6 && !menu.isSlotVisible(18) && menu.isSlotVisible(26)
                    && !menu.isSlotVisible(17), "Scroll range or visibility is incorrect");
                for (int index = 0; index < 27; index++) {
                    check(menu.getSlot(index).index == index && menu.getSlot(index).getContainerSlot() == index,
                        "Scrolling changed a menu or container index");
                }
                check(menu.getSlot(26).container == backing && menu.getSlot(26).getItem().is(Items.DIAMOND)
                    && menu.getSlot(26).x == 98 && menu.getSlot(26).y == 53,
                    "Scrolling changed items or placed the final slot incorrectly");
                check(menu.getSlot(27) == playerSlot, "Scrolling replaced player inventory slots");
                menu.setScrollRow(-10);
                check(menu.getScrollRow() == 0 && menu.isSlotVisible(0) && !menu.isSlotVisible(9),
                    "Scrolling upwards did not clamp at the first row");
                menu.removed(player);
            });

        suite.scenario("hatch capacity follows physical chamber resizing", 4)
            .step(60, "raise the chamber roof", s -> {
                s.context.fillAir(1, 5, 1, 3, 5, 3);
                s.context.fill(0, 6, 0, 4, 6, 4, ModBlocks.FIREBRICK.get());
                s.context.setBlock(2, 7, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "grown chamber allocates 27 slots", s -> {
                s.expectHatchLink(true);
                check(s.hatch().getProcessingSlotCount() == 27 && s.hatch().getInventory().getContainerSize() == 27,
                    "Physical growth did not update hatch capacity");
                s.hatch().getInventory().setItem(26, new ItemStack(Items.GOLD_INGOT));
                s.context.fillAir(0, 5, 0, 4, 7, 4);
                s.context.fill(0, 4, 0, 4, 4, 4, ModBlocks.FIREBRICK.get());
                s.context.setBlock(2, 5, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "shrunk chamber retains its overflow", s -> {
                s.expectHatchLink(true);
                var hatch = s.hatch();
                check(hatch.getProcessingSlotCount() == 9 && hatch.getInventory().getContainerSize() == 27,
                    "Physical shrink deleted slots or retained excessive processing capacity");
                check(hatch.getInventory().getItem(26).is(Items.GOLD_INGOT)
                    && !hatch.getInventory().canPlaceItem(26, new ItemStack(Items.GOLD_INGOT)),
                    "Physical shrink lost overflow or allows insertion into it");
                var saved = hatch.saveWithoutMetadata(s.context.overworld().registryAccess()).copy();
                var restored = new ChargingHatchBlockEntity(hatch.getBlockPos(), hatch.getBlockState());
                restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), saved));
                check(restored.getInventory().getContainerSize() == 27 && restored.getProcessingSlotCount() == 9
                    && restored.getInventory().getItem(26).is(Items.GOLD_INGOT),
                    "Physical shrink overflow did not survive reload");
            });

        suite.scenario("firebox consumes one coal and counts down once per tick", 4)
            .step(60, "insert coal with pending work", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(1, "first fuel lights immediately", s -> {
                var firebox = s.firebox();
                int duration = FuelUtils.burnDuration(s.context.overworld(), new ItemStack(Items.COAL));
                check(s.fuelCount(Items.COAL) == 2, "Starting fire did not consume exactly one coal");
                check(firebox.getMaxBurnTime() == duration, "Maximum duration differs from fuel value");
                check(firebox.getBurnTime() >= duration - 1 && firebox.getBurnTime() <= duration,
                    "Burn duration started incorrectly");
                check(firebox.getBlockState().getValue(BlockStateProperties.LIT),
                    "Starting coal did not light firebox");
                check(!firebox.tryStartFuel() && s.fuelCount(Items.COAL) == 2, "Burning firebox consumed extra coal");
                s.lastBurnTime = firebox.getBurnTime();
                s.lastGameTime = s.context.overworld().getGameTime();
            })
            .step(40, "one decrement per actual server tick", s -> {
                long elapsed = s.context.overworld().getGameTime() - s.lastGameTime;
                check(s.firebox().getBurnTime() == s.lastBurnTime - elapsed, "Fuel countdown is not once per tick");
                check(s.fuelCount(Items.COAL) == 2, "Ongoing burn consumed another item");
            });

        suite.scenario("valid firebox starts next fuel after expiry", 4)
            .step(60, "start first fuel", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(1, "shorten remaining burn for expiry test", s -> {
                check(s.fuelCount(Items.COAL) == 2, "First fuel was not consumed");
                s.setRemainingBurn(2);
            })
            .step(5, "next item starts exactly once", s -> {
                check(s.fuelCount(Items.COAL) == 1, "Expiry consumed wrong number of fuel items");
                check(s.firebox().isBurning(), "Valid structure did not restart fire after expiry");
                check(s.firebox().getBlockState().getValue(BlockStateProperties.LIT), "Restarted firebox is unlit");
            });

        suite.scenario("invalid structure pauses existing fuel until repair", 4)
            .step(60, "start fuel", s -> s.startWorkingWithFuel(Items.COAL, 2))
            .step(1, "break roof attachment while burning", s -> {
                check(s.fuelCount(Items.COAL) == 1, "First coal did not start");
                s.setRemainingBurn(80);
                s.context.removeBlock(2, 6, 2);
            })
            .step(40, "invalid chamber retains fuel already burning", s -> {
                s.expectHatchLink(false);
                check(!s.firebox().isBurning() && s.remainingUnits() > 0 && s.remainingUnits() < 720,
                    "Invalidation did not pause the existing fire");
                s.lastFuelUnits = s.remainingUnits();
                check(s.fuelCount(Items.COAL) == 1, "Invalidation consumed a new coal");
            })
            .step(60, "paused invalid fire stays off", s -> {
                check(!s.firebox().isBurning() && s.fuelCount(Items.COAL) == 1, "Invalid structure restarted fuel");
                check(s.remainingUnits() == s.lastFuelUnits, "Invalid structure drained saved fuel energy");
                check(!s.firebox().getBlockState().getValue(BlockStateProperties.LIT), "Idle firebox stays lit");
                s.context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "repair permits new fuel", s -> {
                s.expectHatchLink(true);
                check(s.firebox().isBurning() && s.fuelCount(Items.COAL) == 1
                    && s.remainingUnits() < s.lastFuelUnits, "Repair failed to reuse saved fuel");
            });

        suite.scenario("standalone firebox does not start itself", 4)
            .step(60, "remove controller before inserting coal", s -> {
                s.context.removeBlock(0, 3, 1);
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
            })
            .step(40, "uncontrolled firebox retains all fuel", s -> {
                check(s.fuelCount(Items.COAL) == 3 && !s.firebox().isBurning(), "Standalone firebox consumed fuel");
                check(!s.firebox().getBlockState().getValue(BlockStateProperties.LIT), "Standalone firebox lit itself");
            });

        suite.scenario("firebox fuel remainder merges before filling its emptied slot", 4)
            .step(60, "consume lava fuel alongside existing buckets", s -> {
                var inventory = s.firebox().getInventory();
                inventory.setItem(0, new ItemStack(Items.LAVA_BUCKET));
                inventory.setItem(1, new ItemStack(Items.BUCKET, 5));
                inventory.setItem(2, new ItemStack(Items.COAL, 64));
                check(s.firebox().tryStartFuel(), "Lava fuel did not start");
                check(inventory.getItem(0).isEmpty() && inventory.getItem(1).getCount() == 6,
                    "Fuel remainder used an empty slot instead of merging existing buckets");
                check(s.fuelCount(Items.BUCKET) == 6 && s.fuelCount(Items.COAL) == 64,
                    "Remainder placement changed unrelated fuel or bucket totals");
            });

        suite.scenario("firebox full bucket stack uses an empty slot", 4)
            .step(60, "consume lava beside a full remainder stack", s -> {
                var inventory = s.firebox().getInventory();
                int bucketLimit = new ItemStack(Items.BUCKET).getMaxStackSize();
                inventory.setItem(0, new ItemStack(Items.LAVA_BUCKET));
                inventory.setItem(1, new ItemStack(Items.BUCKET, bucketLimit));
                inventory.setItem(2, new ItemStack(Items.COAL, 64));
                check(s.firebox().tryStartFuel(), "Lava fuel did not start");
                check(inventory.getItem(0).is(Items.BUCKET) && inventory.getItem(0).getCount() == 1
                    && inventory.getItem(1).getCount() == bucketLimit,
                    "Full remainder stack overflowed or empty slot lost its bucket");
            });

        suite.scenario("firebox remainder partly merges and drops only excess", 4)
            .step(60, "route a multi-item remainder into a full inventory", s -> {
                var inventory = s.firebox().getInventory();
                int bucketLimit = new ItemStack(Items.BUCKET).getMaxStackSize();
                inventory.setItem(0, new ItemStack(Items.BUCKET, bucketLimit - 1));
                inventory.setItem(1, new ItemStack(Items.COAL, 64));
                inventory.setItem(2, new ItemStack(Items.COAL, 64));
                s.storeFuelRemainderForTest(new ItemStack(Items.BUCKET, 3));
                check(inventory.getItem(0).getCount() == bucketLimit && s.fuelCount(Items.COAL) == 128,
                    "Partial remainder merge exceeded the stack limit or changed fuel");
                int dropped = s.context.overworld().getEntitiesOfClass(ItemEntity.class,
                    new AABB(s.context.pos(0, 1, 2)).inflate(3)).stream()
                    .filter(entity -> entity.getItem().is(Items.BUCKET))
                    .mapToInt(entity -> entity.getItem().getCount()).sum();
                check(dropped == 2, "Full inventory did not drop exactly the leftover remainder");
            });

        suite.scenario("firebox remainder fills multiple slots without exceeding limits", 4)
            .step(60, "place more remainder items than one slot can hold", s -> {
                var inventory = s.firebox().getInventory();
                int bucketLimit = new ItemStack(Items.BUCKET).getMaxStackSize();
                inventory.setItem(0, new ItemStack(Items.COAL, 64));
                s.storeFuelRemainderForTest(new ItemStack(Items.BUCKET, bucketLimit + 2));
                check(inventory.getItem(1).is(Items.BUCKET) && inventory.getItem(1).getCount() == bucketLimit
                    && inventory.getItem(2).is(Items.BUCKET) && inventory.getItem(2).getCount() == 2,
                    "Remainder distribution lost items or exceeded slot limits");
            });

        suite.scenario("lava fuel leaves retrievable empty bucket", 4)
            .step(60, "insert lava bucket with pending work", s -> s.startWorkingWithFuel(Items.LAVA_BUCKET, 1))
            .step(1, "bucket remainder and duration preserved", s -> {
                check(s.fuelCount(Items.LAVA_BUCKET) == 0 && s.fuelCount(Items.BUCKET) == 1,
                    "Lava fuel lost or duplicated its bucket remainder");
                check(s.firebox().getMaxBurnTime() == FuelUtils.burnDuration(s.context.overworld(),
                    new ItemStack(Items.LAVA_BUCKET)), "Wrong lava duration");
                check(s.firebox().getBlockState().getValue(BlockStateProperties.LIT), "Lava did not light immediately");
                var storage = TransferService.get().findBlock(StorageKeys.ITEM,
                    s.context.overworld(), s.context.pos(0, 1, 2), Direction.WEST);
                var bucket = ResourceTypes.ITEM.of(Items.BUCKET.builtInRegistryHolder());
                try (var transaction = TransferTransaction.openRoot()) {
                    check(storage.extract(bucket, 1, transaction) == 1, "Automation cannot retrieve bucket remainder");
                    transaction.commit();
                }
                check(s.fuelCount(Items.BUCKET) == 0, "Extracted remainder remains in inventory");
            });

        suite.scenario("firebox searches later slots and skips nonfuel remainders", 4)
            .step(60, "put fuel behind invalid items", s -> {
                s.addPendingInput();
                s.firebox().getInventory().setItem(0, new ItemStack(Items.BUCKET));
                s.firebox().getInventory().setItem(1, new ItemStack(Items.IRON_INGOT));
                s.firebox().getInventory().setItem(2, new ItemStack(Items.COAL, 2));
            })
            .step(1, "third slot starts fuel without consuming other items", s -> {
                check(s.firebox().isBurning() && s.fuelCount(Items.COAL) == 1, "Later fuel slot was skipped");
                check(s.fuelCount(Items.BUCKET) == 1 && s.fuelCount(Items.IRON_INGOT) == 1,
                    "Nonfuel items were consumed");
            });

        suite.scenario("firebox storage rejects nonfuel on every side", 4)
            .step(60, "registered predicate rejects iron and accepts coal", s -> {
                var iron = ResourceTypes.ITEM.of(Items.IRON_INGOT.builtInRegistryHolder());
                var coal = ResourceTypes.ITEM.of(Items.COAL.builtInRegistryHolder());
                for (Direction direction : Direction.values()) {
                    var storage = TransferService.get().findBlock(StorageKeys.ITEM,
                        s.context.overworld(), s.context.pos(0, 1, 2), direction);
                    check(storage != null, "Missing firebox storage on " + direction);
                    try (var transaction = TransferTransaction.openRoot()) {
                        check(storage.insert(iron, 1, transaction) == 0, "Firebox accepted nonfuel on " + direction);
                        check(storage.insert(coal, 1, transaction) == 1, "Firebox refused fuel on " + direction);
                        transaction.commit();
                    }
                }
                check(s.fuelCount(Items.COAL) == 6 && s.fuelCount(Items.IRON_INGOT) == 0,
                    "Registered fuel storage changed wrong items");
            });

        suite.scenario("firebox save load preserves burning fuel and inventory", 4)
            .step(60, "start coal", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(1, "round trip saved burn state", s -> {
                var firebox = s.firebox();
                int before = firebox.getBurnTime();
                int maxBefore = firebox.getMaxBurnTime();
                var data = firebox.saveWithoutMetadata(s.context.overworld().registryAccess());
                firebox.getInventory().clearContent();
                firebox.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                check(firebox.getBurnTime() == before && firebox.getMaxBurnTime() == maxBefore,
                    "Reload lost burn counters");
                check(s.fuelCount(Items.COAL) == 2, "Reload changed remaining fuel inventory");
                s.lastBurnTime = before;
                s.lastGameTime = s.context.overworld().getGameTime();
            })
            .step(40, "saved fire keeps counting down without duplicate consumption", s -> {
                check(s.firebox().getBurnTime() == s.lastBurnTime
                    - (s.context.overworld().getGameTime() - s.lastGameTime), "Reload changed countdown rate");
                check(s.fuelCount(Items.COAL) == 2, "Reload consumed duplicate fuel");
            });

        for (int width : new int[]{3, 8, 31}) {
            int airCount = (width - 1) * 3;
            suite.scenario("exact fuel scaling for " + airCount + " air blocks", width)
                .step(60, "start fuel at derived foundation rate", s -> {
                    check(s.controller().getFuelSpaceSize() == airCount, "Incorrect fuel air count");
                    s.addPendingInput();
                    s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
                })
                .step(1, "capture exact energy and temperature", s -> {
                    check(s.fuelCount(Items.COAL) == 2, "Expected one starting coal");
                    s.captureEnergyAndTemperature();
                    var data = s.firebox().saveWithoutMetadata(s.context.overworld().registryAccess());
                    s.firebox().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                        s.context.overworld().registryAccess(), data));
                    check(s.remainingUnits() == s.lastFuelUnits, "Reload changed exact energy");
                })
                .step(40, "energy scales while heating rate stays constant", s -> {
                    long elapsed = s.context.overworld().getGameTime() - s.lastGameTime;
                    check(s.remainingUnits() == s.lastFuelUnits - airCount * elapsed,
                        "Incorrect exact energy consumption for foundation size");
                    check(Math.abs(s.controller().getTemperature() - s.lastTemperature - 0.5F * elapsed) < 0.01F,
                        "Heating rate changed with foundation size");
                    check(s.fuelCount(Items.COAL) == 2, "Unnecessary extra coal consumed");
                });
        }

        suite.scenario("shared chambers count common fuel space only once", 8, 4)
            .step(60, "start shared fuel", s -> {
                check(s.controller().getFuelSpaceSize() == 21, "Common fuel space counted per chamber");
                s.addPendingInput();
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
            })
            .step(1, "capture shared energy", Scenario::captureEnergyAndTemperature)
            .step(40, "shared heat consumes 21 units per tick",
                s -> check(
                    s.remainingUnits() == s.lastFuelUnits - 21 * (s.context.overworld().getGameTime() - s.lastGameTime),
                    "Shared chambers multiplied fuel cost"));

        suite.scenario("foundation growth updates existing firebox consumption", 4)
            .step(60, "start coal", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(1, "extend foundation and chamber without replacing firebox", s -> {
                s.context.fill(5, 0, 0, 8, 5, 4, ModBlocks.FIREBRICK.get());
                s.context.fillAir(4, 1, 1, 7, 1, 3);
                s.context.fillAir(4, 3, 1, 7, 4, 3);
            })
            .step(40, "new foundation size applied", s -> {
                check(s.controller().getFuelSpaceSize() == 21, "Resize kept previous fuel space size");
                check(s.fuelCount(Items.COAL) == 2, "Resize consumed extra fuel");
                s.captureEnergyAndTemperature();
            })
            .step(40, "existing fire uses resized rate",
                s -> check(
                    s.remainingUnits() == s.lastFuelUnits - 21 * (s.context.overworld().getGameTime() - s.lastGameTime),
                    "Existing burn did not adopt resized rate"));

        suite.scenario("invalid structure cools while preserving fuel energy", 4)
            .step(60, "start heating", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(80, "break chimney after heating", s -> s.context.removeBlock(2, 6, 2))
            .step(40, "capture temperature after invalidation", s -> {
                check(s.controller().getChamberForHatch(s.context.pos(2, 3, 0)) == null,
                    "Invalid structure still exposes an active chamber");
                check(!s.firebox().isBurning() && s.firebox().hasFuelEnergy(), "Invalid structure did not pause fuel");
                s.captureEnergyAndTemperature();
            })
            .step(40, "cool at expected rate", s -> {
                check(Math.abs(s.controller().getTemperature() - s.lastTemperature + 4.0F) < 0.01F,
                    "Invalid structure continued heating or cooled at incorrect rate");
                check(s.fuelCount(Items.COAL) == 2, "Invalid structure consumed new fuel");
                check(s.remainingUnits() == s.lastFuelUnits, "Cooling invalid structure drained fuel energy");
            });

        suite.scenario("temperature persistence and bounds", 4)
            .step(60, "ambient and reload bounds", s -> {
                check(s.controller().getTemperature() == 20.0F, "Idle temperature fell below ambient");
                var data = s.save();
                data.putFloat("Temperature", 123.5F);
                s.load(data);
                check(s.controller().getTemperature() == 123.5F, "Reload lost temperature");
                data.putFloat("Temperature", -100.0F);
                s.load(data);
                check(s.controller().getTemperature() == 20.0F, "Lower temperature bound failed");
                data.putFloat("Temperature", 4100.0F);
                s.load(data);
                check(s.controller().getTemperature() == 4000.0F, "Upper temperature bound failed");
                s.addPendingInput();
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
            })
            .step(30, "excess stored heat cools without burning fuel", s -> {
                check(s.controller().getTemperature() < 4000.0F && s.controller().getTemperature() > 3900.0F,
                    "Stored heat did not cool naturally");
                check(s.fuelCount(Items.COAL) == 3 && s.remainingUnits() == 0,
                    "Smeltery above recipe target burned fuel");
            });

        suite.scenario("refuel loop repays saved negative energy", 8)
            .step(60, "load fuel debt", s -> {
                s.addPendingInput();
                long unitsPerStick = 9L * FuelUtils.burnDuration(s.context.overworld(), new ItemStack(Items.STICK));
                check(unitsPerStick > 0, "Stick is not fuel");
                var data = s.firebox().saveWithoutMetadata(s.context.overworld().registryAccess());
                data.putLong("RemainingFuelUnits", -2 * unitsPerStick - 1);
                s.firebox().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                s.firebox().getInventory().setItem(0, new ItemStack(Items.STICK, 4));
            })
            .step(1, "consume enough items to repay debt", s -> {
                check(s.fuelCount(Items.STICK) == 1 && s.firebox().isBurning(),
                    "Refuel stopped before repaying fuel debt");
                s.captureEnergyAndTemperature();
            })
            .step(1, "continue exact countdown",
                s -> check(s.remainingUnits() == s.lastFuelUnits - 21, "Refuel discarded negative energy remainder"));

        suite.scenario("melting consumes selected slot only after required time", 4)
            .step(60, "warm chamber and verify loaded recipe", s -> {
                var recipe = s.controller().getMatchingRecipe(new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                check(recipe.isPresent(), "Generated aluminium melting recipe was not loaded");
                check(recipe.get().value().processTime() == 200, "Unexpected test recipe duration");
                s.warmForMelting();
            })
            .step(200, "insert input behind nonmelting item", s -> {
                s.hatch().getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 3));
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 2));
            })
            .step(95, "input remains until accelerated completion", s -> {
                check(s.hatch().getMeltingProgress(4) == 190, "Progress did not respect the speed cap");
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Input consumed early");
                check(s.meltingStorage().getAmount() == 0, "Fluid produced early");
            })
            .step(5, "one batch completes exactly", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Incorrect melting input count");
                check(s.itemCount(Items.IRON_INGOT) == 1, "Melting extracted from wrong slot");
                s.expectMolten(FluidAmounts.INGOT);
                check(s.hatch().getMeltingProgress(4) == 0, "Completed batch kept progress");
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
            })
            .step(200, "second batch completes independently", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Second batch not consumed");
                s.expectMolten(2 * FluidAmounts.INGOT);
            });

        suite.scenario("all 18 hatch slots melt simultaneously including hidden slots", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(200, "fill all slots", s -> {
                for (int slot = 0; slot < 18; slot++) {
                    s.hatch().getInventory().setItem(slot, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                }
            })
            .step(95, "all slots advance independently at capped speed", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 18, "Parallel input consumed early");
                for (int slot = 0; slot < 18; slot++) {
                    check(s.hatch().getMeltingProgress(slot) == 190, "Slot " + slot + " did not progress");
                }
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "ParallelMenuTest"));
                var menu = (ChargingHatchMenu) s.hatch().createMenu(1, player.getInventory(), player);
                for (int slot = 0; slot < 18; slot++) {
                    check(Math.abs(menu.getMeltingProgress(slot) - 0.95F) < 0.001F,
                        "Slot " + slot + " menu progress is wrong");
                }
                menu.removed(player);
                check(s.meltingStorage().getAmount() == 0, "Parallel melting produced fluid early");
            })
            .step(5, "all slots complete together", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Parallel melting left input");
                s.expectMolten(18 * FluidAmounts.INGOT);
                for (int slot = 0; slot < 18; slot++) {
                    check(s.hatch().getMeltingProgress(slot) == 0, "Completed slot kept progress");
                }
            });

        suite.scenario("staggered slot progress survives save load independently", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(200, "start first slot",
                s -> s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get())))
            .step(25, "start later slot",
                s -> s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get())))
            .step(25, "save two different progress values", s -> {
                var data = s.hatch().saveWithoutMetadata(s.context.overworld().registryAccess()).copy();
                s.hatch().resetMeltingProgress();
                s.hatch().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                check(s.hatch().getMeltingProgress(0) == 100 && s.hatch().getMeltingProgress(4) == 50,
                    "Reload combined or lost independent progress");
            })
            .step(50, "first slot finishes alone", s -> {
                check(s.hatch().getInventory().getItem(0).isEmpty(), "First slot did not complete");
                check(s.hatch().getMeltingProgress(4) == 150, "Other slot was reset by completion");
                s.expectMolten(FluidAmounts.INGOT);
            })
            .step(25, "later slot completes", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Later slot did not complete");
                s.expectMolten(2 * FluidAmounts.INGOT);
            });

        suite.scenario("simultaneous completions wait when chamber has room for one", 4)
            .step(60, "seed chamber with one output of space", s -> {
                s.seed(new Fluid[]{ModFluids.MOLTEN_ALUMINIUM.still().get()},
                    s.meltingStorage().getCapacity() - FluidAmounts.INGOT);
                s.warmForMelting();
            })
            .step(200, "start two slots", s -> {
                s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
            })
            .step(200, "only first slot completes", s -> {
                check(s.hatch().getInventory().getItem(0).isEmpty()
                    && !s.hatch().getInventory().getItem(4).isEmpty(),
                    "Capacity contention lost input or chose wrong completion");
                check(s.hatch().getMeltingProgress(4) == s.aluminiumRecipe().processTime()
                    - s.aluminiumRecipe().maxSpeedMultiplier(), "Waiting slot lost progress");
                check(s.meltingStorage().getAmount() == s.meltingStorage().getCapacity(), "Chamber overfilled");
            })
            .step(25, "waiting progress remains intact and free space", s -> {
                check(s.hatch().getMeltingProgress(4) == s.aluminiumRecipe().processTime()
                    - s.aluminiumRecipe().maxSpeedMultiplier(), "Blocked progress changed");
                var storage = s.meltingStorage();
                try (var transaction = TransferTransaction.openRoot()) {
                    check(storage.extract(storage.getResource(), FluidAmounts.INGOT, transaction) == FluidAmounts.INGOT,
                        "Could not free chamber space");
                    transaction.commit();
                }
            })
            .step(5, "waiting slot completes when space returns", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Waiting item did not resume");
                check(s.meltingStorage().getAmount() == s.meltingStorage().getCapacity(), "Resumed output is wrong");
            });

        suite.scenario("recipe speed interpolates and caps while completing exact work", 4)
            .step(60, "validate loaded recipe speed limits", s -> {
                var recipe = s.controller().getMatchingRecipe(new ItemStack(ModItems.ALUMINIUM_INGOT.get()))
                    .orElseThrow().value();
                check(recipe.getSpeedMultiplier(recipe.requiredTemperature() - 1) == 0.0, "Cold recipe has a speed");
                check(recipe.getSpeedMultiplier(recipe.requiredTemperature()) == 1.0,
                    "Minimum temperature is not normal speed");
                check(
                    recipe
                        .getSpeedMultiplier((recipe.requiredTemperature() + recipe.preferredTemperature()) / 2) == 1.5,
                    "Midpoint did not interpolate speed");
                check(
                    recipe.getSpeedMultiplier(recipe.preferredTemperature()) == 2.0
                        && recipe.getSpeedMultiplier(2000) == 2.0,
                    "Recipe exceeded or missed its speed cap");
                float[] temperatures = {recipe.requiredTemperature(),
                    (recipe.requiredTemperature() + recipe.preferredTemperature()) / 2, recipe.preferredTemperature(),
                    2000};
                int[] durations = {recipe.processTime(), (int) Math.ceil(recipe.processTime() / 1.5),
                    recipe.processTime() / 2, recipe.processTime() / 2};
                for (int test = 0; test < temperatures.length; test++) {
                    s.setTemperature(temperatures[test]);
                    s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                    long before = s.meltingStorage().getAmount();
                    for (int tick = 0; tick < durations[test] - 1; tick++) {
                        s.processSlotOnce(0);
                    }
                    check(!s.hatch().getInventory().getItem(0).isEmpty()
                        && s.meltingStorage().getAmount() == before,
                        "Accelerated recipe completed early at " + temperatures[test]);
                    s.processSlotOnce(0);
                    check(s.hatch().getInventory().getItem(0).isEmpty(), "Recipe did not finish exact work");
                    check(s.meltingStorage().getAmount() == before + FluidAmounts.INGOT,
                        "Speed bonus changed output amount");
                }
            });

        suite.scenario("fractional melting progress survives save load and menu normalization", 4)
            .step(60, "round trip fractional work", s -> {
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                s.hatch().prepareMeltingSlot(4, 40);
                s.hatch().advanceMeltingProgress(4, 1.5);
                var data = s.hatch().saveWithoutMetadata(s.context.overworld().registryAccess()).copy();
                s.hatch().resetMeltingProgress();
                s.hatch().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                check(s.hatch().getMeltingProgress(4) == 1.5, "Reload rounded fractional progress");
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "FractionalMenuTest"));
                var menu = (ChargingHatchMenu) s.hatch().createMenu(1, player.getInventory(), player);
                check(Math.abs(menu.getMeltingProgress(4) - 0.037F) < 0.0001F,
                    "Menu did not normalize fractional progress");
                menu.removed(player);
            });

        suite.scenario("heating stops at recipe target and pauses when work is removed", 4)
            .step(60, "prepare chamber just below preferred temperature", s -> {
                s.setTemperature(s.aluminiumRecipe().preferredTemperature() - 0.25F);
                s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
            })
            .step(1, "heating clamps without overshoot",
                s -> check(s.controller().getTemperature() == s.aluminiumRecipe().preferredTemperature(),
                    "Heating overshot preferred temperature"))
            .step(10, "maintain target while melting", s -> {
                check(
                    s.controller().getTemperature() == s.aluminiumRecipe().preferredTemperature()
                        && s.firebox().isBurning(),
                    "Loaded recipe did not maintain preferred temperature");
                s.lastFuelUnits = s.remainingUnits();
                s.hatch().getInventory().clearContent();
            })
            .step(10, "no work cools without fuel", s -> {
                check(s.controller().getTemperature() < s.aluminiumRecipe().preferredTemperature(),
                    "Idle smeltery did not cool");
                check(s.remainingUnits() == s.lastFuelUnits && !s.firebox().isBurning(),
                    "Empty hatch kept consuming fuel");
            });

        suite.scenario("cold chamber preserves melting input", 4)
            .step(60, "insert input at ambient temperature",
                s -> s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 2)))
            .step(80, "cold input remains untouched", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Cold chamber consumed input");
                check(s.hatch().getMeltingProgress(0) == 0 && s.meltingStorage().getAmount() == 0,
                    "Cold chamber progressed or produced fluid");
            });

        for (boolean incompatible : new boolean[]{false, true}) {
            suite.scenario(
                incompatible ? "incompatible chamber fluid blocks melting" : "insufficient fluid space blocks melting",
                4)
                .step(60, "warm and fill chamber", s -> {
                    long capacity = s.meltingStorage().getCapacity();
                    s.seed(new Fluid[]{incompatible ? Fluids.WATER : ModFluids.MOLTEN_ALUMINIUM.still().get()},
                        incompatible ? FluidAmounts.BUCKET : capacity - FluidAmounts.INGOT + 1);
                    s.warmForMelting();
                })
                .step(40, "insert blocked input", s -> {
                    s.lastFluidAmount = s.meltingStorage().getAmount();
                    s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 2));
                })
                .step(80, "blocked input and fluid unchanged", s -> {
                    check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Blocked output consumed input");
                    check(s.meltingStorage().getAmount() == s.lastFluidAmount, "Blocked output changed fluid");
                    check(s.hatch().getMeltingProgress(0) == 0, "Blocked output advanced progress");
                });
        }

        suite.scenario("changing melting slot restarts duration", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(200, "start slot zero", s -> s.hatch().getInventory().setItem(0,
                new ItemStack(ModItems.ALUMINIUM_INGOT.get())))
            .step(50, "move input to another slot", s -> {
                check(s.hatch().getMeltingProgress(0) == 100, "Expected partial progress");
                s.hatch().getInventory().setItem(0, ItemStack.EMPTY);
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
            })
            .step(50, "new slot has its own partial duration", s -> {
                check(s.hatch().getMeltingProgress(4) == 100 && s.hatch().getMeltingProgress(0) == 0,
                    "Moved input did not receive independent progress");
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Moved input melted early");
                check(s.meltingStorage().getAmount() == 0, "Moved input produced early fluid");
            })
            .step(50, "new slot finishes after accelerated duration", s -> s.expectMolten(FluidAmounts.INGOT));

        suite.scenario("melting progress survives hatch save load", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(200, "start input", s -> s.hatch().getInventory().setItem(0,
                new ItemStack(ModItems.ALUMINIUM_INGOT.get())))
            .step(50, "round trip partial progress", s -> {
                var data = s.hatch().saveWithoutMetadata(s.context.overworld().registryAccess()).copy();
                s.hatch().resetMeltingProgress();
                s.hatch().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
                check(s.hatch().getMeltingProgress(0) == 100, "Reload lost partial progress");
            })
            .step(45, "saved duration not shortened", s -> {
                check(s.hatch().getMeltingProgress(0) == 190 && s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1,
                    "Reload changed duration or lost input");
            })
            .step(5, "saved batch completes once", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Reloaded batch did not consume input");
                s.expectMolten(FluidAmounts.INGOT);
            });

        suite.scenario("breaking chamber stops unfinished melting", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(40, "start input", s -> s.hatch().getInventory().setItem(0,
                new ItemStack(ModItems.ALUMINIUM_INGOT.get())))
            .step(5, "break chimney early in batch", s -> s.context.removeBlock(2, 6, 2))
            .step(40, "invalid chamber preserves input and resets work", s -> {
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Broken chamber completed melting");
                check(s.hatch().getMeltingProgress(0) == 0, "Disconnect kept partial progress");
                check(s.controller().getChamberForHatch(s.hatch().getBlockPos()) == null,
                    "Broken chamber remains processable");
            });

        suite.scenario("failed melt insertion rolls back selected input extraction", 4)
            .step(60, "seed almost full molten chamber", s -> {
                s.seed(new Fluid[]{ModFluids.MOLTEN_ALUMINIUM.still().get()},
                    s.meltingStorage().getCapacity() - FluidAmounts.INGOT + 1);
                s.warmForMelting();
            })
            .step(40, "exercise completion rollback directly", s -> {
                s.hatch().getInventory().setItem(4, new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 2));
                long before = s.meltingStorage().getAmount();
                var recipe = s.controller().getMatchingRecipe(s.hatch().getInventory().getItem(4)).orElseThrow()
                    .value();
                try {
                    // Bypass the eligibility check specifically to test the transaction's failure path.
                    var method = SmelteryControllerBlockEntity.class.getDeclaredMethod("completeMelting",
                        ChargingHatchBlockEntity.class, SmelteryControllerBlockEntity.SmelteryChamber.class,
                        int.class, SmelteryRecipe.class);
                    method.setAccessible(true);
                    boolean completed = (boolean) method.invoke(s.controller(), s.hatch(),
                        s.controller().getChamberForHatch(s.hatch().getBlockPos()), 4, recipe);
                    check(!completed, "Incomplete output was committed");
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Could not exercise melt transaction", exception);
                }
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Failed output lost extracted input");
                check(s.meltingStorage().getAmount() == before, "Failed output committed partial fluid");
            });

        suite.scenario("firebox menu maps fuel slots and transfers without losing items", 4)
            .step(60, "exercise server fuel menu", s -> {
                var player = s.context.createFakePlayer(0, 1, 1,
                    new GameProfile(UUID.randomUUID(), "FireboxMenuTest"));
                var created = s.firebox().createMenu(1, player.getInventory(), player);
                check(created instanceof FireboxMenu, "Firebox did not create fuel menu");
                var menu = (FireboxMenu) created;
                check(menu.slots.size() == 39, "Expected three fuel and 36 player slots");
                for (int index = 0; index < 3; index++) {
                    var slot = menu.getSlot(index);
                    check(slot.container == s.firebox().getInventory(), "Fuel slot uses wrong inventory");
                    check(slot.x == 62 + index * 18 && slot.y == 35, "Fuel slot does not align with texture");
                    check(slot.mayPlace(new ItemStack(Items.COAL)), "Fuel slot rejects coal");
                    check(!slot.mayPlace(new ItemStack(Items.IRON_INGOT))
                        && !slot.mayPlace(new ItemStack(Items.BUCKET)), "Fuel slot accepts nonfuel");
                    s.firebox().getInventory().setItem(index, new ItemStack(Items.COAL, index + 1));
                    check(slot.getItem().getCount() == index + 1, "Fuel slot maps to wrong index");
                }
                s.firebox().getInventory().clearContent();
                player.getInventory().clearContent();
                player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 10));
                check(menu.quickMoveStack(player, 30).isEmpty(), "Shift click inserted nonfuel");
                check(player.getInventory().getItem(0).getCount() == 10 && s.fuelCount(Items.IRON_INGOT) == 0,
                    "Rejected transfer changed inventory");
                player.getInventory().setItem(0, new ItemStack(Items.COAL, 10));
                check(menu.quickMoveStack(player, 30).getCount() == 10, "Player-to-firebox shift click failed");
                check(s.fuelCount(Items.COAL) == 10 && player.getInventory().getItem(0).isEmpty(),
                    "Fuel insertion duplicated or lost coal");
                check(menu.quickMoveStack(player, 0).getCount() == 10, "Fuel extraction shift click failed");
                int playerCoal = 0;
                for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                    var stack = player.getInventory().getItem(index);
                    if (stack.is(Items.COAL)) {
                        playerCoal += stack.getCount();
                    }
                }
                check(s.fuelCount(Items.COAL) == 0 && playerCoal == 10,
                    "Fuel extraction duplicated or lost coal");
                s.firebox().getInventory().setItem(2, new ItemStack(Items.BUCKET));
                check(menu.quickMoveStack(player, 2).getCount() == 1 && s.fuelCount(Items.BUCKET) == 0,
                    "Menu traps fuel remainder bucket");
                check(menu.stillValid(player), "Nearby player cannot use firebox menu");
                player.setPos(s.context.origin().getX() + 100, s.context.origin().getY() + 1,
                    s.context.origin().getZ());
                check(!menu.stillValid(player), "Distant player can use firebox menu");
                player.setPos(s.context.origin().getX() + 0.5, s.context.origin().getY() + 1,
                    s.context.origin().getZ() + 1.5);
                s.context.removeBlock(0, 1, 2);
                check(!menu.stillValid(player), "Removed firebox remains usable");
                menu.removed(player);
            });

        suite.scenario("firebox payload menu copies inventory and reads synchronized burn data", 4)
            .step(60, "exercise payload menu data", s -> {
                var player = s.context.createFakePlayer(0, 1, 1,
                    new GameProfile(UUID.randomUUID(), "FireboxPayloadTest"));
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 7));
                var menu = new FireboxMenu(1, player.getInventory(), new BlockPosPayload(s.context.pos(0, 1, 2)));
                check(menu.slots.size() == 39 && menu.getSlot(0).container != s.firebox().getInventory(),
                    "Payload menu has incorrect inventory");
                check(menu.getSlot(0).getItem().isEmpty(), "Payload inventory is not initially empty");
                check(menu.getSlot(0).mayPlace(new ItemStack(Items.COAL))
                    && !menu.getSlot(0).mayPlace(new ItemStack(Items.IRON_INGOT)),
                    "Payload fuel restrictions differ from server");
                check(!menu.isBurning() && menu.getBurnProgress() == 0.0F, "Idle payload data is incorrect");
                menu.setData(0, 500);
                menu.setData(1, 1);
                check(menu.isBurning() && menu.getBurnProgress() == 0.5F, "Payload menu reads incorrect burn indexes");
                menu.setData(1, 0);
                check(!menu.isBurning(), "Payload burn flag did not update");
                menu.getSlot(0).set(new ItemStack(Items.COAL, 2));
                check(s.fuelCount(Items.COAL) == 7, "Payload menu changed real fuel inventory");
                menu.removed(player);
            });

        suite.scenario("firebox server menu exposes live normalized burn fraction", 4)
            .step(60, "create menu and start fuel", s -> {
                var player = s.context.createFakePlayer(0, 1, 1,
                    new GameProfile(UUID.randomUUID(), "FireboxBurnTest"));
                s.fireboxMenu = (FireboxMenu) s.firebox().createMenu(1, player.getInventory(), player);
                check(!s.fireboxMenu.isBurning() && s.fireboxMenu.getBurnProgress() == 0.0F,
                    "Unfueled menu burn values are incorrect");
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL));
                s.addPendingInput();
                check(s.firebox().burnForTick(), "Could not burn coal");
                check(s.fireboxMenu.isBurning() && s.fireboxMenu.getBurnProgress() > 0.99F,
                    "Started fuel is not visible through menu data");
            })
            .step(40, "remaining fraction updates and expiry clears flag", s -> {
                float exact = s.remainingUnits() / (s.firebox().getMaxBurnTime() * 9.0F);
                check(s.fireboxMenu.isBurning() && Math.abs(s.fireboxMenu.getBurnProgress() - exact) < 0.0011F,
                    "Menu fraction differs from remaining fuel");
                check(s.fireboxMenu.getBurnProgress() < 1.0F, "Menu fraction did not decrease");
                s.setRemainingBurn(0);
                check(!s.fireboxMenu.isBurning() && s.fireboxMenu.getBurnProgress() == 0.0F,
                    "Expired fuel remains active through menu data");
            });

        suite.scenario("empty smeltery never starts fuel", 4)
            .step(60, "insert coal without work",
                s -> s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3)))
            .step(80, "idle inventory and temperature unchanged", s -> {
                check(s.fuelCount(Items.COAL) == 3 && s.remainingUnits() == 0, "Idle smeltery consumed fuel");
                check(!s.firebox().isBurning() && !s.firebox().getBlockState().getValue(BlockStateProperties.LIT),
                    "Idle firebox shows burning");
                check(s.controller().getTemperature() == 20.0F, "Idle smeltery heated without work");
                s.hatch().getInventory().setItem(0, new ItemStack(Items.STICK));
            })
            .step(40, "nonmelting input still does not start fuel", s -> {
                check(s.fuelCount(Items.COAL) == 3 && s.remainingUnits() == 0, "Nonmelting input started fuel");
            });

        suite.scenario("removing and returning cold input pauses and resumes stored energy", 4)
            .step(60, "start heating cold input", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(20, "remove unfinished input", s -> {
                check(s.firebox().isBurning() && s.controller().getTemperature() > 20.0F,
                    "Cold work did not request heating");
                s.lastFuelUnits = s.remainingUnits();
                s.lastTemperature = s.controller().getTemperature();
                s.hatch().getInventory().clearContent();
            })
            .step(40, "idle keeps remaining energy and cools", s -> {
                check(s.remainingUnits() == s.lastFuelUnits && s.fuelCount(Items.COAL) == 2,
                    "Idle firebox drained or refilled energy");
                check(!s.firebox().isBurning() && !s.firebox().getBlockState().getValue(BlockStateProperties.LIT),
                    "Idle firebox stayed active");
                check(s.controller().getTemperature() < s.lastTemperature, "Idle chamber failed to cool");
                var data = s.firebox().saveWithoutMetadata(s.context.overworld().registryAccess());
                s.firebox().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), data));
            })
            .step(40, "reload preserves idle energy", s -> {
                check(s.remainingUnits() == s.lastFuelUnits && !s.firebox().isBurning(),
                    "Reload resumed or lost paused energy");
                s.addPendingInput();
            })
            .step(1, "resuming reuses stored energy", s -> {
                check(s.remainingUnits() == s.lastFuelUnits - 9 && s.fuelCount(Items.COAL) == 2,
                    "Resuming consumed new coal or incorrect energy");
                check(s.firebox().isBurning(), "Returning input did not resume burning");
            });

        suite.scenario("last completed batch pauses fuel until another item arrives", 4)
            .step(60, "warm chamber", Scenario::warmForMelting)
            .step(200, "insert one batch and fuel", s -> {
                s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 2));
            })
            .step(200, "capture energy at completion", s -> {
                s.expectMolten(FluidAmounts.INGOT);
                check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 0, "Final input did not finish");
                s.lastFuelUnits = s.remainingUnits();
            })
            .step(200, "completed chamber preserves remaining fuel", s -> {
                check(s.remainingUnits() == s.lastFuelUnits && s.fuelCount(Items.COAL) == 2,
                    "Finished chamber continued consuming fuel");
                check(!s.firebox().isBurning(), "Finished chamber remained active");
                s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
            })
            .step(200, "next batch uses remaining fuel", s -> {
                s.expectMolten(2 * FluidAmounts.INGOT);
                check(s.remainingUnits() == s.lastFuelUnits && s.fuelCount(Items.COAL) == 2,
                    "Next batch burned fuel despite sufficient stored heat");
            });

        for (boolean incompatible : new boolean[]{false, true}) {
            suite
                .scenario(
                    incompatible ? "incompatible output does not request fuel" : "full tank does not request fuel", 4)
                .step(60, "fill chamber and insert work and coal", s -> {
                    s.seed(new Fluid[]{incompatible ? Fluids.WATER : ModFluids.MOLTEN_ALUMINIUM.still().get()},
                        incompatible ? FluidAmounts.BUCKET : s.meltingStorage().getCapacity());
                    s.hatch().getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get()));
                    s.firebox().getInventory().setItem(0, new ItemStack(Items.COAL, 3));
                })
                .step(80, "blocked work does not burn or consume input", s -> {
                    check(s.remainingUnits() == 0 && s.fuelCount(Items.COAL) == 3 && !s.firebox().isBurning(),
                        "Blocked output burned fuel");
                    check(s.itemCount(ModItems.ALUMINIUM_INGOT.get()) == 1, "Blocked output consumed input");
                });
        }

        suite.scenario("duplicate burn requests and firebox ticks do not double consume", 4)
            .step(60, "start pending work", s -> s.startWorkingWithFuel(Items.COAL, 3))
            .step(1, "request already paid tick twice", s -> {
                long before = s.remainingUnits();
                check(s.firebox().burnForTick() && s.firebox().burnForTick(), "Paid tick was not recognized");
                s.firebox().tick();
                check(s.remainingUnits() == before && s.fuelCount(Items.COAL) == 2,
                    "Duplicate request or firebox tick consumed energy twice");
            });

        suite.scenario("outlet registered extraction commits and rolls back", 4)
            .step(60, "seed chamber", s -> s.seed(BLOCK))
            .step(40, "extract through registered providers on every side", s -> {
                var water = ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder());
                for (Direction direction : Direction.values()) {
                    var storage = TransferService.get().findBlock(StorageKeys.FLUID,
                        s.context.overworld(), s.context.pos(2, 3, -1), direction);
                    check(storage != null, "Missing outlet provider on " + direction);
                    check(storage.support(0) == TransferSupport.EXTRACT_ONLY, "Outlet advertised insertion");
                    try (var transaction = TransferTransaction.openRoot()) {
                        check(storage.insert(water, BLOCK, transaction) == 0, "Outlet accepted insertion");
                        long extracted = storage.extract(water, FluidAmounts.BUCKET, transaction);
                        check(extracted == FluidAmounts.BUCKET, "Outlet extraction returned " + extracted);
                    }
                    check(storage.amount(0) == BLOCK, "Rolled-back extraction lost fluid");
                }
                s.outletStorage = s.outlet().getFluidProvider(Direction.DOWN);
                try (var transaction = TransferTransaction.openRoot()) {
                    check(s.outletStorage.extract(water, 1000, transaction) == 1000, "Committed extraction failed");
                    transaction.commit();
                }
                check(s.meltingStorage().getAmount() == BLOCK - 1000, "Extraction did not drain chamber");
            })
            .step(40, "cached provider follows replacement tank", s -> {
                check(s.outletStorage.amount(0) == BLOCK - 1000, "Cached provider lost current tank");
                try (var transaction = TransferTransaction.openRoot()) {
                    check(s.outletStorage.extract(
                        ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                        1000, transaction) == 1000, "Cached provider cannot extract");
                    transaction.commit();
                }
                check(s.meltingStorage().getAmount() == BLOCK - 2000, "Cached provider drained an old tank");
            });

        suite.scenario("three outlets share one tank and transactional extraction", 4)
            .step(60, "attach outlets to the east and west walls", s -> {
                s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                s.context.setBlock(-1, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
            })
            .step(40, "extract through all three without duplicating fluid", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                var west = s.context.requireBlockEntity(-1, 3, 2, TappingOutletBlockEntity.class);
                check(s.outlet().getChamber() != null && east.getChamber() == s.outlet().getChamber()
                    && west.getChamber() == s.outlet().getChamber(), "Outlets do not resolve the same chamber");
                var water = ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder());
                check(StorageOperations.set(s.meltingStorage(), water, 2500), "Cannot seed shared tank");
                var providers = List.of(s.outlet().getFluidProvider(Direction.DOWN),
                    east.getFluidProvider(Direction.DOWN), west.getFluidProvider(Direction.DOWN));
                try (var transaction = TransferTransaction.openRoot()) {
                    check(providers.get(0).extract(water, 1000, transaction) == 1000
                        && providers.get(1).extract(water, 1000, transaction) == 1000
                        && providers.get(2).extract(water, 1000, transaction) == 500,
                        "Shared transaction extracted more or less than the available fluid");
                }
                check(s.meltingStorage().getAmount() == 2500, "Rollback lost shared fluid");
                try (var transaction = TransferTransaction.openRoot()) {
                    check(providers.get(0).extract(water, 1000, transaction) == 1000
                        && providers.get(1).extract(water, 1000, transaction) == 1000
                        && providers.get(2).extract(water, 1000, transaction) == 500,
                        "Committed shared extraction did not respect available fluid");
                    transaction.commit();
                }
                check(providers.stream().allMatch(provider -> provider.amount(0) == 0),
                    "An outlet retained a separate copy of the drained fluid");
            });

        suite.scenario("multiple outlet lists and fluid survive save load", 4)
            .step(60, "attach a second outlet",
                s -> s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)))
            .step(40, "save and reload the list-only format", s -> {
                check(StorageOperations.set(s.meltingStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                    BLOCK), "Cannot seed saved shared tank");
                var snapshot = s.save().copy();
                var savedChamber = (CompoundTag) snapshot.getListOrEmpty("Chambers").getFirst();
                check(
                    savedChamber.getListOrEmpty("outlet_positions").size() == 2 && !savedChamber.contains("outlet_pos"),
                    "Save does not use the list-only outlet format");
                s.load(snapshot);
                check(((CompoundTag) s.save().getListOrEmpty("Chambers").getFirst())
                    .getListOrEmpty("outlet_positions").size() == 2, "Reload discarded outlet positions");
                s.expect(0, s.expected(1, 3, BLOCK));
            })
            .step(40, "both outlets resolve the revalidated saved chamber", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                check(east.getChamber() != null && east.getChamber() == s.outlet().getChamber(),
                    "Reload did not restore both outlet links");
                check(east.getFluidProvider(Direction.DOWN).amount(0) == BLOCK, "Reload changed shared fluid");
            });

        suite.scenario("removing one outlet retains chamber until its last outlet is removed", 4)
            .step(60, "attach a second outlet",
                s -> s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)))
            .step(40, "seed and remove the original outlet", s -> {
                check(StorageOperations.set(s.meltingStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                    BLOCK), "Cannot seed shared tank before removal");
                s.outletStorage = s.outlet().getFluidProvider(Direction.DOWN);
                s.context.removeBlock(2, 3, -1);
            })
            .step(40, "remaining outlet keeps access to the tank", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                check(east.getChamber() != null && east.getFluidProvider(Direction.DOWN).amount(0) == BLOCK,
                    "Removing one outlet invalidated the chamber or lost fluid");
                check(s.outletStorage.support(0) == TransferSupport.NONE, "Removed outlet retained cached access");
                s.context.removeBlock(5, 3, 2);
            })
            .step(40, "removing the last outlet retains the chamber's fluid", s -> s.expect(BLOCK));

        suite.scenario("chamber invalidation clears every outlet link", 4)
            .step(60, "attach a second outlet",
                s -> s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)))
            .step(40, "seed tank and invalidate the chamber", s -> {
                check(StorageOperations.set(s.meltingStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                    BLOCK), "Cannot seed shared tank before invalidation");
                s.context.removeBlock(2, 6, 2);
            })
            .step(40, "all outlets lose their links", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                check(s.outlet().getControllerPos() == null && east.getControllerPos() == null,
                    "Invalidation left one of the outlet links active");
                check(east.getFluidProvider(Direction.DOWN).support(0) == TransferSupport.NONE,
                    "Invalid outlet still exposes fluid");
                s.context.setBlock(2, 6, 2, ModBlocks.CHIMNEY);
            })
            .step(40, "repair relinks every outlet without duplicating fluid", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                check(east.getChamber() != null && east.getChamber() == s.outlet().getChamber(),
                    "Repair did not relink both outlets");
                s.expect(0, s.expected(1, 3, BLOCK));
            });

        suite.scenario("two outlets pour from the same limited tank", 4)
            .step(60, "attach a second outlet and two basins", s -> {
                s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                s.context.setBlock(5, 2, 2, ModBlocks.CASTING_BASIN.get());
            })
            .step(40, "pour both receivers without overdrawing the source", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                var eastBasin = s.context.requireBlockEntity(5, 2, 2, CastingBasinBlockEntity.class);
                check(StorageOperations.set(s.meltingStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                    2500), "Cannot seed shared pouring source");
                s.outlet().startPouring();
                east.startPouring();
                s.outlet().tick();
                east.tick();
                s.outlet().tick();
                east.tick();
                check(s.basin().getFluidStorage().getAmount() == 1500
                    && eastBasin.getFluidStorage().getAmount() == 1000 && s.meltingStorage().getAmount() == 0,
                    "Concurrent pouring lost or duplicated shared fluid");
                check(s.outlet().getPouringFluid().isBlank() && east.getPouringFluid().isBlank(),
                    "Drained shared source left an outlet stream active");
            });

        suite.scenario("pipe extraction and pouring share the remaining fluid", 4)
            .step(60, "attach another outlet and a basin", s -> {
                s.context.setBlock(5, 3, 2, ModBlocks.TAPPING_OUTLET.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
            })
            .step(40, "pour and extract during the same server tick", s -> {
                var east = s.context.requireBlockEntity(5, 3, 2, TappingOutletBlockEntity.class);
                var water = ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder());
                check(StorageOperations.set(s.meltingStorage(), water, 1500), "Cannot seed mixed extraction source");
                s.outlet().startPouring();
                s.outlet().tick();
                try (var transaction = TransferTransaction.openRoot()) {
                    check(east.getFluidProvider(Direction.DOWN).extract(water, 1000, transaction) == 500,
                        "Pipe extraction did not account for fluid already poured");
                    transaction.commit();
                }
                s.outlet().tick();
                check(s.basin().getFluidStorage().getAmount() == 1000 && s.meltingStorage().getAmount() == 0,
                    "Mixed extraction lost or duplicated shared fluid");
            });

        suite.scenario("cached outlet provider disables and recovers with chamber", 4)
            .step(60, "seed chamber", s -> s.seed(BLOCK))
            .step(40, "cache provider and break chimney", s -> {
                s.outletStorage = s.outlet().getFluidProvider(Direction.DOWN);
                s.context.removeBlock(2, 6, 2);
            })
            .step(40, "invalid chamber exposes empty storage", s -> {
                check(s.outlet().getControllerPos() == null, "Broken chamber kept outlet link");
                check(s.outletStorage.resource(0).isBlank() && s.outletStorage.amount(0) == 0,
                    "Broken chamber still exposes fluid");
                check(s.outletStorage.support(0) == TransferSupport.NONE, "Invalid outlet still supports transfers");
                try (var transaction = TransferTransaction.openRoot()) {
                    check(s.outletStorage.extract(
                        ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                        BLOCK, transaction) == 0, "Broken chamber allowed extraction");
                }
                s.context.setBlock(2, 6, 2, ModBlocks.CHIMNEY.get().defaultBlockState());
            })
            .step(40, "cached provider resolves repaired chamber", s -> {
                check(s.outletStorage.amount(0) == BLOCK, "Repair did not restore cached provider access");
                check(s.outletStorage.support(0) == TransferSupport.EXTRACT_ONLY, "Repair did not restore extraction");
            });

        suite.scenario("removed outlet cannot keep cached access", 4)
            .step(60, "seed chamber", s -> s.seed(BLOCK))
            .step(40, "replace outlet block entity", s -> {
                s.outletStorage = s.outlet().getFluidProvider(Direction.DOWN);
                s.context.removeBlock(2, 3, -1);
                s.context.setBlock(2, 3, -1, ModBlocks.TAPPING_OUTLET.get().defaultBlockState());
                check(s.outletStorage.resource(0).isBlank() && s.outletStorage.amount(0) == 0,
                    "Removed outlet retained fluid access");
            })
            .step(40, "replacement links without reactivating removed provider", s -> {
                check(s.outlet().getFluidProvider(Direction.DOWN).amount(0) == BLOCK,
                    "Replacement outlet did not link to chamber");
                check(s.outletStorage.support(0) == TransferSupport.NONE, "Removed provider reactivated");
            });

        suite.scenario("pouring drains only available fluid and stays stopped after empty", 4)
            .step(60, "seed partial nugget and place basin", s -> {
                s.seed(1500);
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
            })
            .step(40, "click and pour exact partial amounts", s -> {
                s.outlet().startPouring();
                s.outlet().tick();
                check(s.basin().getFluidStorage().getAmount() == 1000
                    && s.meltingStorage().getAmount() == 500, "First pour did not transfer one nugget");
                check(s.outlet().getPouringFluid().value() == ModFluids.MOLTEN_ALUMINIUM.still().get(),
                    "Active stream does not expose transferred fluid");
                var streamTag = s.outlet().getUpdateTag(s.context.overworld().registryAccess());
                check(streamTag.contains("PouringFluid"), "Stream variant missing from client update tag");
                s.outlet().tick();
                check(s.basin().getFluidStorage().getAmount() == 1500
                    && s.meltingStorage().getAmount() == 0, "Partial final pour lost fluid");
                check(s.outlet().getPouringFluid().isBlank(), "Empty source left stream active");
                check(StorageOperations.set(s.meltingStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()), 1000),
                    "Cannot refill test chamber");
            })
            .step(5, "refilling chamber does not restart pouring", s -> {
                check(s.basin().getFluidStorage().getAmount() == 1500, "Empty-source stop restarted itself");
                s.outlet().startPouring();
            })
            .step(5, "server ticker pours after another click", s -> {
                check(s.basin().getFluidStorage().getAmount() == 2500
                    && s.meltingStorage().getAmount() == 0, "Server ticker did not pour");
            });

        suite.scenario("pouring fills basin exactly and does not restart after draining", 4)
            .step(60, "seed source and nearly full basin", s -> {
                s.seed(BLOCK);
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                check(StorageOperations.set(s.basin().getFluidStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().get().builtInRegistryHolder()),
                    BLOCK - 500), "Cannot seed basin");
            })
            .step(40, "pour only remaining capacity", s -> {
                s.outlet().startPouring();
                s.outlet().tick();
                check(s.basin().getFluidStorage().getAmount() == BLOCK
                    && s.meltingStorage().getAmount() == BLOCK - 500, "Full-basin pour lost fluid");
                check(s.outlet().getPouringFluid().isBlank(), "Full basin left stream active");
                check(StorageOperations.set(s.basin().getFluidStorage(), ResourceTypes.FLUID.empty(), 0),
                    "Cannot empty basin");
            })
            .step(5, "full receiver stop requires another click", s -> {
                check(s.basin().getFluidStorage().getAmount() == 0, "Drained basin restarted pouring");
            });

        for (String obstruction : List.of("missing", "output item", "incompatible fluid")) {
            suite.scenario("pouring rejects " + obstruction, 4)
                .step(60, "seed source and blocked receiver", s -> {
                    s.seed(BLOCK);
                    if (!obstruction.equals("missing")) {
                        s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                        if (obstruction.equals("output item")) {
                            s.basin().getInventory().setItem(0, new ItemStack(Items.IRON_BLOCK));
                        } else {
                            check(StorageOperations.set(s.basin().getFluidStorage(),
                                ResourceTypes.FLUID.of(Fluids.LAVA.builtInRegistryHolder()), 1000), "Cannot seed lava");
                        }
                    }
                })
                .step(40, "blocked pour leaves source intact", s -> {
                    s.outlet().startPouring();
                    s.outlet().tick();
                    check(s.meltingStorage().getAmount() == BLOCK, "Blocked pour drained chamber");
                    if (obstruction.equals("missing")) {
                        s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                    } else {
                        check(
                            s.basin().getFluidStorage()
                                .getAmount() == (obstruction.equals("incompatible fluid") ? 1000 : 0),
                            "Blocked receiver changed");
                        s.basin().getInventory().setItem(0, ItemStack.EMPTY);
                        check(StorageOperations.set(s.basin().getFluidStorage(), ResourceTypes.FLUID.empty(), 0),
                            "Cannot clear receiver");
                    }
                })
                .step(5, "removing blockage does not restart pouring", s -> {
                    check(s.meltingStorage().getAmount() == BLOCK
                        && s.basin().getFluidStorage().getAmount() == 0, "Blocked pour restarted itself");
                });
        }

        suite.scenario("basin persists fluid and output item", 4)
            .step(60, "save receiver state", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                check(StorageOperations.set(s.basin().getFluidStorage(),
                    ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()), 12345), "Cannot seed receiver");
                s.basin().getInventory().setItem(0, new ItemStack(Items.IRON_BLOCK));
                var saved = s.basin().saveWithoutMetadata(s.context.overworld().registryAccess());
                s.context.removeBlock(2, 2, -1);
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                s.basin().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), saved));
                check(s.basin().getFluidStorage().getAmount() == 12345
                    && s.basin().getFluidStorage().getResource().value() == Fluids.WATER, "Basin lost saved fluid");
                check(s.basin().getInventory().getItem(0).is(Items.IRON_BLOCK), "Basin lost saved output");
                check(!s.basin().isAcceptingFluid(), "Loaded output did not block filling");
            });

        suite.scenario("basin casts aluminium only after full cooling duration", 4)
            .step(60, "fill receiver and cool", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                check(StorageOperations.set(s.basin().getFluidStorage(),
                    ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().holder()), BLOCK), "Cannot fill basin");
                for (int tick = 0; tick < 99; tick++) {
                    s.basin().tick();
                }
                check(s.basin().getInventory().isEmpty() && s.basin().getFluidStorage().getAmount() == BLOCK,
                    "Casting finished early or consumed fluid early");
                s.basin().tick();
                check(s.basin().getInventory().getItem(0).is(ModBlocks.ALUMINIUM_BLOCK.get().asItem())
                    && s.basin().getInventory().getItem(0).getCount() == 1, "Casting produced wrong output");
                check(s.basin().getFluidStorage().getAmount() == 0, "Casting did not consume exact fluid");
                for (int tick = 0; tick < 100; tick++) {
                    s.basin().tick();
                }
                check(s.basin().getInventory().getItem(0).getCount() == 1, "Casting duplicated output");
            });

        suite.scenario("basin cooling resumes after save and resets on insufficient fluid", 4)
            .step(60, "cool halfway and reload", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                var molten = ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().holder());
                check(StorageOperations.set(s.basin().getFluidStorage(), molten, BLOCK), "Cannot fill basin");
                for (int tick = 0; tick < 50; tick++) {
                    s.basin().tick();
                }
                var saved = s.basin().saveWithoutMetadata(s.context.overworld().registryAccess());
                s.context.removeBlock(2, 2, -1);
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                s.basin().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    s.context.overworld().registryAccess(), saved));
                for (int tick = 0; tick < 49; tick++) {
                    s.basin().tick();
                }
                check(s.basin().getInventory().isEmpty(), "Loaded cast completed early");
                s.basin().tick();
                check(s.basin().getInventory().getItem(0).is(ModBlocks.ALUMINIUM_BLOCK.get().asItem()),
                    "Loaded cooling did not resume");
                s.basin().getInventory().setItem(0, ItemStack.EMPTY);
                check(StorageOperations.set(s.basin().getFluidStorage(), molten, BLOCK), "Cannot refill basin");
                for (int tick = 0; tick < 50; tick++) {
                    s.basin().tick();
                }
                check(StorageOperations.set(s.basin().getFluidStorage(), molten, BLOCK - 1), "Cannot drain one unit");
                s.basin().tick();
                check(StorageOperations.set(s.basin().getFluidStorage(), molten, BLOCK), "Cannot top up basin");
                for (int tick = 0; tick < 99; tick++) {
                    s.basin().tick();
                }
                check(s.basin().getInventory().isEmpty(), "Insufficient fluid retained old progress");
                s.basin().tick();
                check(s.basin().getInventory().getItem(0).is(ModBlocks.ALUMINIUM_BLOCK.get().asItem()),
                    "Refilled basin did not cast");
            });

        suite.scenario("basin holds one item and automation only extracts", 4)
            .step(60, "insert internally and extract through registration", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                var item = ResourceVariant.ofItem(new ItemStack(ModBlocks.ALUMINIUM_BLOCK.get()));
                var internal = ContainerStorage.of(s.basin().getInventory(), null);
                try (var transaction = TransferTransaction.openRoot()) {
                    check(internal.insert(0, item, 2, transaction) == 1, "Basin allowed more than one item");
                    transaction.commit();
                }
                for (Direction direction : Direction.values()) {
                    var provider = TransferService.get().findBlock(StorageKeys.ITEM,
                        s.context.overworld(), s.context.pos(2, 2, -1), direction);
                    check(provider != null, "Missing basin item provider");
                    try (var transaction = TransferTransaction.openRoot()) {
                        check(provider.insert(item, 1, transaction) == 0, "Basin accepted external insertion");
                        check(provider.extract(item, 1, transaction) == 1, "Basin did not expose output");
                    }
                    check(s.basin().getInventory().getItem(0).getCount() == 1, "Rollback lost output");
                }
                var provider = s.basin().getInventoryProvider(Direction.DOWN);
                try (var transaction = TransferTransaction.openRoot()) {
                    check(provider.extract(item, 1, transaction) == 1, "Cannot extract output");
                    transaction.commit();
                }
                check(s.basin().getInventory().isEmpty() && s.basin().isAcceptingFluid(),
                    "Extraction did not free basin");
            });

        suite.scenario("basin output collection does not duplicate", 4)
            .step(60, "collect output twice", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                s.basin().getInventory().setItem(0, new ItemStack(ModBlocks.ALUMINIUM_BLOCK.get()));
                var player = s.context.createFakePlayer(2, 3, -2,
                    new GameProfile(UUID.randomUUID(), "BasinCollectTest"));
                s.basin().collectOutput(player);
                s.basin().collectOutput(player);
                int count = 0;
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    var stack = player.getInventory().getItem(slot);
                    if (stack.is(ModBlocks.ALUMINIUM_BLOCK.get().asItem())) {
                        count += stack.getCount();
                    }
                }
                check(count == 1 && s.basin().getInventory().isEmpty(), "Collection duplicated or lost output");
            });

        suite.scenario("empty basin update clears previously displayed output", 4)
            .step(60, "apply empty inventory update over occupied receiver", s -> {
                s.context.setBlock(2, 2, -1, ModBlocks.CASTING_BASIN.get());
                var registryAccess = s.context.overworld().registryAccess();
                var emptyUpdate = s.basin().getUpdateTag(registryAccess);
                s.basin().getInventory().setItem(0, new ItemStack(ModBlocks.ALUMINIUM_BLOCK.get()));
                var occupiedUpdate = s.basin().getUpdateTag(registryAccess);
                s.basin().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    registryAccess, emptyUpdate));
                check(s.basin().getInventory().isEmpty(), "Empty update left previous item visible");
                check(s.basin().getInventory().getContainerSize() == 1, "Clearing update removed inventory slot");
                s.basin().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    registryAccess, occupiedUpdate));
                check(s.basin().getInventory().getItem(0).is(ModBlocks.ALUMINIUM_BLOCK.get().asItem()),
                    "Occupied update did not restore item");
                s.basin().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    registryAccess, emptyUpdate));
                check(s.basin().getInventory().isEmpty(), "Repeated empty update left stale item");
            });

        for (Scenario scenario : suite.scenarios) {
            scenario.next();
        }
    }

    private static void invalidStructure(Suite suite, String name, Consumer<Scenario> mutation) {
        suite.scenario(name, 4)
            .step(60, "break valid structure", s -> {
                s.expect(0, s.expected(1, 3, 0));
                s.seed(BLOCK);
                mutation.accept(s);
            })
            .step(40, "invalid structure rejected and fluid retained", s -> {
                s.expect(BLOCK);
                // Extra controller and raised chimney lie outside build()'s cleared volume.
                s.context.removeBlock(4, 2, -1);
                s.context.removeBlock(2, 2, -1);
                s.context.removeBlock(0, 3, -1);
                s.context.removeBlock(2, 7, 2);
                s.build(4);
            })
            .step(40, "repair restores fluid", s -> s.expect(0, s.expected(1, 3, BLOCK)))
            .step(40, "repair remains stable", s -> s.expect(0, s.expected(1, 3, BLOCK)));
    }

    private static final class Suite {
        private final TestWorldContext root;
        private final List<Scenario> scenarios = new ArrayList<>();
        private int finished;
        private int failures;

        private Suite(TestWorldContext root) {
            this.root = root;
        }

        private Scenario scenario(String name, int width, int... partitions) {
            var scenario = new Scenario(this, name, root.at(66, 32, 66 + scenarios.size() * 64));
            String filter = System.getProperty("industria.test.smeltery.filter", "");
            if (!filter.isEmpty() && !name.contains(filter))
                return scenario;
            scenarios.add(scenario);
            scenario.build(width, partitions);
            return scenario;
        }

        private void finish(Scenario scenario, Exception failure) {
            finished++;
            if (failure == null) {
                LOGGER.info("[SmelterySuite] PASS {}", scenario.name);
            } else {
                failures++;
                LOGGER.error("[SmelterySuite] FAIL " + scenario.name + ": " + failure.getMessage(), failure);
            }
            if (finished == scenarios.size()) {
                // Avoid a mass unload/save while other scenarios still need server ticks.
                // The unloaded-structure scenario explicitly releases its own test chunks.
                for (Scenario completed : scenarios) {
                    for (int chunk : completed.forcedChunks) {
                        root.overworld().setChunkForced(chunk, completed.context.origin().getZ() >> 4, false);
                    }
                }
                LOGGER.info("[SmelterySuite] SUMMARY tested={} passed={} failed={}", finished, finished - failures,
                    failures);
                root.server().halt(false);
            }
        }
    }

    private record Step(int delay, String name, Consumer<Scenario> action) {
    }
    private record Chamber(AABB bounds, long capacity, FluidStack fluid) {
    }
    private record Expected(AABB bounds, long amount) {
    }

    private static final class Scenario {
        private final Suite suite;
        private final String name;
        private final TestWorldContext context;
        private final ArrayDeque<Step> steps = new ArrayDeque<>();
        private final List<Integer> forcedChunks = new ArrayList<>();
        private int lastBurnTime;
        private long lastGameTime;
        private long lastFuelUnits;
        private float lastTemperature;
        private long lastFluidAmount;
        private FireboxMenu fireboxMenu;
        private int structureWidth;
        private ResourceStorage<ResourceVariant<Fluid>> outletStorage;

        private TappingOutletBlockEntity outlet() {
            return (TappingOutletBlockEntity) context.overworld().getBlockEntity(context.pos(2, 3, -1));
        }

        private CastingBasinBlockEntity basin() {
            return (CastingBasinBlockEntity) context.overworld().getBlockEntity(context.pos(2, 2, -1));
        }

        private boolean usesTappableFluid() {
            return name.startsWith("pouring rejects") || Set.of(
                "outlet registered extraction commits and rolls back",
                "three outlets share one tank and transactional extraction",
                "multiple outlet lists and fluid survive save load",
                "removing one outlet retains chamber until its last outlet is removed",
                "chamber invalidation clears every outlet link",
                "two outlets pour from the same limited tank",
                "pipe extraction and pouring share the remaining fluid",
                "cached outlet provider disables and recovers with chamber",
                "removed outlet cannot keep cached access",
                "pouring drains only available fluid and stays stopped after empty",
                "pouring fills basin exactly and does not restart after draining").contains(name);
        }

        private Fluid fixtureFluid() {
            return usesTappableFluid() ? ModFluids.MOLTEN_ALUMINIUM.still().get() : Fluids.WATER;
        }

        private SmelteryRecipe aluminiumRecipe() {
            return controller().getMatchingRecipe(new ItemStack(ModItems.ALUMINIUM_INGOT.get())).orElseThrow().value();
        }

        private Scenario(Suite suite, String name, TestWorldContext context) {
            this.suite = suite;
            this.name = name;
            this.context = context;
        }

        private Scenario step(int delay, String label, Consumer<Scenario> action) {
            steps.add(new Step(delay, label, action));
            return this;
        }

        private void next() {
            if (steps.isEmpty()) {
                suite.finish(this, null);
                return;
            }
            Step step = steps.removeFirst();
            context.afterTicks(step.delay(), _ -> {
                try {
                    LOGGER.info("[SmelterySuite] {}: {}", name, step.name());
                    step.action().accept(this);
                    next();
                } catch (Exception failure) {
                    suite.finish(this, failure);
                }
            });
        }

        private void build(int width, int... partitions) {
            structureWidth = width;
            // Preserve the controller embedded in the west wall through every rebuild.
            fillExceptController(0, 0, 0, Math.max(width, 12), 6, 4, Blocks.AIR);
            context.fillAir(1, 3, -1, Math.max(width, 12), 3, -1);
            fillExceptController(0, 0, 0, width, 5, 4, ModBlocks.FIREBRICK.get());
            context.fillAir(1, 1, 1, width - 1, 1, 3);
            context.fillAir(1, 3, 1, width - 1, 4, 3);
            for (int partition : partitions) {
                context.fill(partition, 3, 1, partition, 4, 3, ModBlocks.FIREBRICK.get());
            }
            int first = 1;
            for (int i = 0; i <= partitions.length; i++) {
                int last = i == partitions.length ? width - 1 : partitions[i] - 1;
                int center = (first + last) / 2;
                context.setBlock(center, 3, 0, ModBlocks.CHARGING_HATCH.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
                context.setBlock(center, 6, 2, ModBlocks.CHIMNEY);
                context.setBlock(center, 3, -1, ModBlocks.TAPPING_OUTLET);
                first = last + 2;
            }
            context.setBlock(0, 1, 2, ModBlocks.FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
            if (!(context.overworld().getBlockEntity(context.pos(0, 3, 1)) instanceof SmelteryControllerBlockEntity)) {
                context.setBlock(0, 3, 1, ModBlocks.SMELTERY_CONTROLLER.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
            }
            if (usesTappableFluid() && controller().getTemperature() < aluminiumRecipe().requiredTemperature()) {
                setTemperature(aluminiumRecipe().preferredTemperature() + 100.0F);
            }
            int startChunk = context.origin().getX() >> 4;
            int endChunk = context.pos(width, 0, 0).getX() >> 4;
            for (int chunk = startChunk; chunk <= endChunk; chunk++) {
                if (!forcedChunks.contains(chunk)) {
                    forcedChunks.add(chunk);
                }
                context.overworld().setChunkForced(chunk, context.origin().getZ() >> 4, true);
            }
        }

        private SmelteryControllerBlockEntity controller() {
            return context.requireBlockEntity(0, 3, 1, SmelteryControllerBlockEntity.class);
        }

        private void fillExceptController(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (x != 0 || y != 3 || z != 1) {
                            context.setBlock(x, y, z, block);
                        }
                    }
                }
            }
        }

        private ChargingHatchBlockEntity hatch() {
            return context.requireBlockEntity(2, 3, 0, ChargingHatchBlockEntity.class);
        }

        private FireboxBlockEntity firebox() {
            return context.requireBlockEntity(0, 1, 2, FireboxBlockEntity.class);
        }

        private void storeFuelRemainderForTest(ItemStack remainder) {
            try {
                var method = FireboxBlockEntity.class.getDeclaredMethod("storeFuelRemainder", ItemStack.class);
                method.setAccessible(true);
                method.invoke(firebox(), remainder);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot exercise firebox remainder placement", failure);
            }
        }

        private int fuelCount(Item item) {
            int count = 0;
            var inventory = firebox().getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                var stack = inventory.getItem(slot);
                if (stack.is(item)) {
                    count += stack.getCount();
                }
            }
            return count;
        }

        private void addPendingInput() {
            for (int x = 1; x < structureWidth; x++) {
                if (context.overworld()
                    .getBlockEntity(context.pos(x, 3, 0)) instanceof ChargingHatchBlockEntity hatch) {
                    hatch.getInventory().setItem(0, new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 64));
                    return;
                }
            }
            throw new IllegalStateException("No hatch found for pending input");
        }

        private void startWorkingWithFuel(Item fuel, int count) {
            addPendingInput();
            firebox().getInventory().setItem(0, new ItemStack(fuel, count));
        }

        private void setRemainingBurn(int ticks) {
            var firebox = firebox();
            var data = firebox.saveWithoutMetadata(context.overworld().registryAccess());
            data.putLong("RemainingFuelUnits", ticks * 9L);
            firebox.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                context.overworld().registryAccess(), data));
            firebox.setChanged();
        }

        private long remainingUnits() {
            return firebox().saveWithoutMetadata(context.overworld().registryAccess())
                .getLongOr("RemainingFuelUnits", 0L);
        }

        private void captureEnergyAndTemperature() {
            lastFuelUnits = remainingUnits();
            lastTemperature = controller().getTemperature();
            lastGameTime = context.overworld().getGameTime();
        }

        private void setTemperature(float temperature) {
            try {
                var field = SmelteryControllerBlockEntity.class.getDeclaredField("temperature");
                field.setAccessible(true);
                field.setFloat(controller(), temperature);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not set test temperature", exception);
            }
        }

        private void processSlotOnce(int slot) {
            try {
                var method = SmelteryControllerBlockEntity.class.getDeclaredMethod("processSlot",
                    ChargingHatchBlockEntity.class, SmelteryControllerBlockEntity.SmelteryChamber.class, int.class);
                method.setAccessible(true);
                method.invoke(controller(), hatch(), controller().getChamberForHatch(hatch().getBlockPos()), slot);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not process test slot", exception);
            }
        }

        private void warmForMelting() {
            var data = save();
            data.putFloat("Temperature", aluminiumRecipe().preferredTemperature() + 100.0F);
            load(data);
        }

        private SyncingFluidStorage meltingStorage() {
            var chamber = controller().getChamberForHatch(hatch().getBlockPos());
            check(chamber != null, "Expected active melting chamber");
            return chamber.getFluidStorage();
        }

        private void expectMolten(long amount) {
            var storage = meltingStorage();
            check(storage.getResource().value() == ModFluids.MOLTEN_ALUMINIUM.still().get(),
                "Wrong melting output fluid");
            check(storage.getAmount() == amount,
                "Incorrect molten amount: " + storage.getAmount() + " expected " + amount);
        }

        private int itemCount(Item item) {
            int amount = 0;
            var inventory = hatch().getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                var stack = inventory.getItem(slot);
                if (stack.is(item)) {
                    amount += stack.getCount();
                }
            }
            return amount;
        }

        private void expectDrops(int iron, int gold, int diamond) {
            var bounds = new AABB(context.pos(2, 3, 0)).inflate(4);
            var drops = context.overworld().getEntitiesOfClass(ItemEntity.class, bounds);
            int actualIron = 0, actualGold = 0, actualDiamond = 0;
            for (var drop : drops) {
                var stack = drop.getItem();
                if (stack.is(Items.IRON_INGOT)) {
                    actualIron += stack.getCount();
                } else if (stack.is(Items.GOLD_INGOT)) {
                    actualGold += stack.getCount();
                } else if (stack.is(Items.DIAMOND)) {
                    actualDiamond += stack.getCount();
                }
            }
            check(actualIron == iron && actualGold == gold && actualDiamond == diamond,
                "Wrong item drops: iron=" + actualIron + " gold=" + actualGold + " diamond=" + actualDiamond);
        }

        private void expectHatchLink(boolean linked) {
            var hatch = hatch();
            if (linked) {
                check(controller().getBlockPos().equals(hatch.getControllerPos()),
                    "Hatch controller position is incorrect");
                var chamber = controller().getChamberForHatch(hatch.getBlockPos());
                check(chamber != null && hatch.getChamber() == chamber, "Hatch does not resolve its active chamber");
            } else {
                check(hatch.getControllerPos() == null, "Inactive hatch still has a controller position");
                check(hatch.getChamber() == null, "Inactive hatch resolves a chamber");
            }
        }

        private CompoundTag save() {
            return controller().saveWithoutMetadata(context.overworld().registryAccess());
        }

        private void load(CompoundTag data) {
            controller().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                context.overworld().registryAccess(), data));
            controller().setChanged();
        }

        private void roundTrip() {
            load(save());
        }

        private void seed(long... amounts) {
            Fluid[] fluids = new Fluid[amounts.length];
            Arrays.fill(fluids, fixtureFluid());
            seed(fluids, amounts);
        }

        private void seed(Fluid[] fluids, long... amounts) {
            CompoundTag data = save();
            var tags = new ArrayList<CompoundTag>();
            for (var entry : data.getListOrEmpty("Chambers")) {
                tags.add((CompoundTag) entry);
            }
            var ops = context.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            tags.sort(Comparator
                .comparingDouble(t -> ExtraCodecs.AABB_CODEC.parse(ops, t.get("bounding_box")).getOrThrow().minX));
            check(tags.size() == amounts.length,
                "Cannot seed: expected " + amounts.length + " chambers, got " + tags.size());
            check(data.getListOrEmpty("RetainedFluids").isEmpty(), "Unexpected reserves before seeding");
            for (int i = 0; i < amounts.length; i++) {
                check(amounts[i] <= tags.get(i).getLongOr("capacity", 0), "Seed exceeds capacity");
                var stack = new FluidStack(ResourceTypes.FLUID.of(fluids[i].builtInRegistryHolder()), amounts[i]);
                tags.get(i).put("fluid", FluidStack.CODEC.codec().encodeStart(ops, stack).getOrThrow());
            }
            load(data);
        }

        private Expected expected(int firstX, int lastX, long amount) {
            return new Expected(new AABB(context.origin().getX() + firstX, context.origin().getY() + 3,
                context.origin().getZ() + 1, context.origin().getX() + lastX + 1,
                context.origin().getY() + 5, context.origin().getZ() + 4), amount);
        }

        private void expect(long retainedAmount, Expected... expected) {
            CompoundTag data = save();
            var ops = context.overworld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var active = new ArrayList<Chamber>();
            for (var entry : data.getListOrEmpty("Chambers")) {
                var tag = (CompoundTag) entry;
                active.add(new Chamber(ExtraCodecs.AABB_CODEC.parse(ops, tag.get("bounding_box")).getOrThrow(),
                    tag.getLongOr("capacity", 0), FluidStack.CODEC.codec().parse(ops, tag.get("fluid")).getOrThrow()));
            }
            active.sort(Comparator.comparingDouble(c -> c.bounds().minX));
            long retained = 0;
            for (var entry : data.getListOrEmpty("RetainedFluids")) {
                var stack = FluidStack.CODEC.codec().parse(ops, ((CompoundTag) entry).get("fluid_stack")).getOrThrow();
                check(stack.amount() >= 0, "Negative reserve");
                check(stack.isEmpty() || stack.variant().value() == fixtureFluid(), "Reserve fluid type changed");
                retained += stack.amount();
            }
            LOGGER.info("[SmelterySuite] OBSERVED {}: active={} retainedAmount={}", name, active, retained);
            check(active.size() == expected.length,
                "Expected " + expected.length + " active chambers, got " + active.size());
            for (int i = 0; i < expected.length; i++) {
                var chamber = active.get(i);
                check(chamber.bounds().equals(expected[i].bounds()),
                    "Wrong bounds: expected " + expected[i].bounds() + ", got " + chamber.bounds());
                long capacity = (long) (chamber.bounds().getXsize() * chamber.bounds().getYsize()
                    * chamber.bounds().getZsize()) * BLOCK;
                check(chamber.capacity() == capacity, "Wrong capacity: " + chamber.capacity());
                check(chamber.fluid().amount() == expected[i].amount(),
                    "Wrong active amount: expected " + expected[i].amount() + ", got " + chamber.fluid().amount());
                check(chamber.fluid().amount() >= 0 && chamber.fluid().amount() <= capacity,
                    "Active storage exceeds capacity");
                check(chamber.fluid().isEmpty() || chamber.fluid().variant().value() == fixtureFluid(),
                    "Active fluid type changed");
            }
            check(retained == retainedAmount,
                "Wrong retained amount: expected " + retainedAmount + ", got " + retained);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
