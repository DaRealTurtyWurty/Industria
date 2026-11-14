package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.block.AlloyFurnaceBlock;
import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.menu.AlloyFurnaceScreenHandler;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class AlloyFurnaceValidationTestWorld {
    private AlloyFurnaceValidationTestWorld() {
    }

    public static void start(TestWorldContext context) {
        int chunkX = context.origin().getX() >> 4, chunkZ = context.origin().getZ() >> 4;
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                context.overworld().setChunkForced(x, z, true);
            }
        }
        TestWorldScheduler.schedule(context, 40, test -> {
            try {
                validate(test.at(0, 8, 0));
                LogUtils.getLogger().info("[AlloyFurnaceSuite] PASS placement, every part/facing, drops, creative, replacement, obstruction and stocked fixture");
            } catch (Throwable failure) {
                LogUtils.getLogger().error("[AlloyFurnaceSuite] FAIL", failure);
            } finally {
                for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                    for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                        test.overworld().setChunkForced(x, z, false);
                    }
                }
                test.server().halt(false);
            }
        });
    }

    private static void validate(TestWorldContext context) {
        var level = context.overworld();
        var block = ModBlocks.ALLOY_FURNACE.get();
        var item = (BlockItem) block.asItem();
        var player = context.createFakePlayer(6, 0, 6, new GameProfile(UUID.randomUUID(), "AlloyTest"));
        player.setGameMode(GameType.SURVIVAL);
        BlockPos origin = context.origin();
        context.fillAir(-4, 0, -4, 4, 4, 4);
        context.fill(-4, -1, -4, 4, -1, 4, Blocks.STONE);

        for (Direction facing : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            player.setYRot(facing.getOpposite().toYRot());
            for (int brokenPart = 0; brokenPart <= 8; brokenPart++) {
                clearDrops(context);
                ItemStack stack = new ItemStack(item, 2);
                var placement = placement(player, stack, origin);
                check(item.place(placement).consumesAction(), "Placement failed: " + facing);
                check(stack.getCount() == 1, "Placement must consume exactly one furnace");
                for (int part = 0; part <= 8; part++) {
                    var pos = origin.offset(AlloyFurnaceBlock.getPartOffset(part, facing));
                    var state = level.getBlockState(pos);
                    check(state.is(block) && state.getValue(AlloyFurnaceBlock.PART) == part,
                        "Missing or wrong part " + part + " facing " + facing);
                    check(state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing, "Incorrect facing");
                    check(AlloyFurnaceBlock.getControllerPos(pos, state).equals(origin), "Incorrect controller link");
                    check((level.getBlockEntity(pos) instanceof AlloyFurnaceBlockEntity) == (part == 0),
                        "Only the controller should store inventory and tick");
                    check(!state.getShape(level, pos).isEmpty(), "Part has no hitbox");
                    if (brokenPart == 0) {
                        check(state.useWithoutItem(level, player,
                            new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false)).consumesAction(),
                            "Part did not handle interaction");
                        var controller = (AlloyFurnaceBlockEntity) level.getBlockEntity(AlloyFurnaceBlock.getControllerPos(pos, state));
                        var menu = controller.createMenu(1, player.getInventory(), player);
                        check(menu instanceof AlloyFurnaceScreenHandler,
                            "Part did not resolve the furnace menu");
                        menu.removed(player);
                        check(TransferService.get().findBlock(StorageKeys.ITEM, level, pos, Direction.EAST) != null,
                            "Part did not expose controller inventory");
                    }
                }
                var furnace = (AlloyFurnaceBlockEntity) level.getBlockEntity(origin);
                furnace.getWrappedContainerStorage().getInventory(0).setItem(0, new ItemStack(Items.IRON_INGOT, 3));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
                check(player.hasCorrectToolForDrops(level.getBlockState(origin)), "Pickaxe cannot harvest furnace");
                check(player.gameMode.destroyBlock(origin.offset(AlloyFurnaceBlock.getPartOffset(brokenPart, facing))),
                    "Could not break part " + brokenPart);
                expectGone(context, facing);
                check(countDrops(context, item) == 1, "Breaking part " + brokenPart + " must drop one furnace, found "
                    + countDrops(context, item) + "; all drops=" + level.getEntitiesOfClass(ItemEntity.class,
                        new AABB(origin).inflate(5)).stream().map(ItemEntity::getItem).toList());
                check(countDrops(context, Items.IRON_INGOT) == 3, "Inventory must drop exactly once");
            }
        }

        player.setYRot(Direction.SOUTH.toYRot());
        for (int part = 1; part <= 8; part++) {
            var obstruction = origin.offset(AlloyFurnaceBlock.getPartOffset(part, Direction.NORTH));
            level.setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
            ItemStack stack = new ItemStack(item);
            check(!item.place(placement(player, stack, origin)).consumesAction(), "Accepted obstructed part " + part);
            check(stack.getCount() == 1 && level.getBlockState(origin).isAir(), "Rejected placement changed the world/item");
            check(level.getBlockState(obstruction).is(Blocks.STONE), "Overwrote obstruction");
            level.removeBlock(obstruction, false);
        }

        var tooHigh = new BlockPos(origin.getX(), level.getMaxY() - 1, origin.getZ());
        level.setBlockAndUpdate(tooHigh.below(), Blocks.STONE.defaultBlockState());
        check(block.getStateForPlacement(placement(player, new ItemStack(item), tooHigh)) == null,
            "Accepted placement with chimney outside build height");
        level.removeBlock(tooHigh.below(), false);

        clearDrops(context);
        player.setGameMode(GameType.CREATIVE);
        check(item.place(placement(player, new ItemStack(item), origin)).consumesAction(), "Creative placement failed");
        player.gameMode.destroyBlock(origin.offset(AlloyFurnaceBlock.getPartOffset(8, Direction.NORTH)));
        expectGone(context, Direction.NORTH);
        check(countDrops(context, item) == 0, "Creative breaking must not drop a furnace");

        check(item.place(placement(player, new ItemStack(item), origin)).consumesAction(), "First adjacent fixture failed");
        BlockPos adjacent = origin.offset(2, 0, 0);
        check(item.place(placement(player, new ItemStack(item), adjacent)).consumesAction(), "Second adjacent fixture failed");
        player.gameMode.destroyBlock(origin);
        expectGone(context, Direction.NORTH);
        for (int part = 0; part <= 8; part++) {
            check(level.getBlockState(adjacent.offset(AlloyFurnaceBlock.getPartOffset(part, Direction.NORTH))).is(block),
                "Breaking furnace damaged adjacent furnace");
        }
        player.gameMode.destroyBlock(adjacent);

        check(item.place(placement(player, new ItemStack(item), origin)).consumesAction(), "Replacement fixture failed");
        var replaced = origin.offset(AlloyFurnaceBlock.getPartOffset(5, Direction.NORTH));
        level.setBlockAndUpdate(replaced, Blocks.STONE.defaultBlockState());
        expectGone(context, Direction.NORTH);
        check(level.getBlockState(replaced).is(Blocks.STONE), "Cleanup removed the replacement block");
        level.removeBlock(replaced, false);

        var fixture = context.at(8, 0, 0);
        AlloyFurnaceTestWorld.place(fixture);
        var furnace = fixture.requireBlockEntity(0, 0, 0, AlloyFurnaceBlockEntity.class);
        for (int part = 0; part <= 8; part++) {
            check(level.getBlockState(fixture.origin().offset(AlloyFurnaceBlock.getPartOffset(part, Direction.NORTH))).is(block),
                "Stocked fixture is missing part " + part);
        }
        for (int tick = 0; tick < 403; tick++) {
            furnace.tick();
        }
        check(furnace.getBlockState().getValue(BlockStateProperties.LIT), "Stocked fixture did not start burning");
        var output = furnace.getWrappedContainerStorage().getInventory(AlloyFurnaceBlockEntity.OUTPUT_SLOT).getItem(0);
        check(output.is(ModItems.BRONZE_INGOT.get()) && output.getCount() == 4,
            "Stocked fixture did not smelt its first batch of bronze");
    }

    private static BlockPlaceContext placement(ServerPlayer player, ItemStack stack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack,
            new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, 0.5, 0), Direction.UP, pos.below(), false));
    }

    private static void expectGone(TestWorldContext context, Direction facing) {
        for (int part = 0; part <= 8; part++) {
            var pos = context.origin().offset(AlloyFurnaceBlock.getPartOffset(part, facing));
            check(!context.overworld().getBlockState(pos).is(ModBlocks.ALLOY_FURNACE.get()), "Orphaned part " + part);
            check(!(context.overworld().getBlockEntity(pos) instanceof AlloyFurnaceBlockEntity), "Orphaned inventory");
        }
    }

    private static int countDrops(TestWorldContext context, net.minecraft.world.item.Item item) {
        return context.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(context.origin()).inflate(5))
            .stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static void clearDrops(TestWorldContext context) {
        context.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(context.origin()).inflate(5))
            .forEach(ItemEntity::discard);
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
