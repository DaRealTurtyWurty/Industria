package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.blockentity.SteamBoilerBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.multiblocklib.MultiblockPlacement;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public final class SteamBoilerTestWorld {
    private SteamBoilerTestWorld() {
    }

    public static void place(TestWorldContext context) {
        var definition = MultiblockLib.DEFINITION_MANAGER.get(Industria.id("steam_boiler"));
        BlockPos controller = definition.controllerPos();
        BlockPos controllerPos = context.origin().offset(controller);
        var level = context.overworld();
        if (!(level.getBlockEntity(controllerPos) instanceof SteamBoilerBlockEntity existing && existing.isFormed())) {
            context.constructMultiblockPattern(0, 0, 0, definition);
            MultiblockPlacement.tryForm(level, controllerPos, new ItemStack(ModItems.WRENCH.get()));
        }

        var boiler = context.requireBlockEntity(
            controller.getX(), controller.getY(), controller.getZ(), SteamBoilerBlockEntity.class);
        if (!boiler.isFormed())
            throw new IllegalStateException("Could not form the test-world steam boiler at " + controllerPos);

        context.placeHopperFeed(context.pos(1, 0, -1), Direction.SOUTH, new ItemStack(Items.COAL, 8));

        // Run water up from a filled, refillable tank to the top edge of the boiler.
        if (!level.getBlockState(context.pos(1, 0, -3)).is(ModBlocks.FLUID_TANK.get())) {
            context.setBlock(1, 0, -3, ModBlocks.FLUID_TANK);
            context.configureBlockEntity(1, 0, -3, FluidTankBlockEntity.class, tank -> {
                var storage = tank.getFluidTank();
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    storage.insertInternal(ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()),
                        storage.getCapacity(), transaction);
                    transaction.commit();
                }
                tank.setExtractMode(true);
            });
        }
        context.runPipe(List.of(context.pos(1, 1, -3), context.pos(1, 3, -3), context.pos(1, 3, -1)));

        // Leave the steam line accessible from ground level for inspection or further connections.
        context.runPipe(List.of(context.pos(1, 4, 1), context.pos(4, 4, 1), context.pos(4, 0, 1)),
            ModBlocks.GAS_PIPE);
    }
}
