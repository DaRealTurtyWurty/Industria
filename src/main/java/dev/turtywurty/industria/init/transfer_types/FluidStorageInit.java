package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.server.world.ServerWorld;

public class FluidStorageInit {
    public static void init() {
        FluidStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getFluidProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        FluidStorage.SIDED.registerForBlockEntity(FractionalDistillationControllerBlockEntity::getFluidProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        FluidStorage.SIDED.registerForBlockEntity(FractionalDistillationTowerBlockEntity::getFluidProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER);
        FluidStorage.SIDED.registerForBlockEntity(InductionHeaterBlockEntity::getFluidProvider, BlockEntityTypeInit.INDUCTION_HEATER);
        FluidStorage.SIDED.registerForBlockEntity(FluidPumpBlockEntity::getFluidProvider, BlockEntityTypeInit.FLUID_PUMP);
        FluidStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getFluidProvider, BlockEntityTypeInit.MIXER);
        FluidStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getFluidProvider, BlockEntityTypeInit.DIGESTER);
        FluidStorage.SIDED.registerForBlockEntity(ClarifierBlockEntity::getFluidProvider, BlockEntityTypeInit.CLARIFIER);
        FluidStorage.SIDED.registerForBlockEntity(CrystallizerBlockEntity::getFluidProvider, BlockEntityTypeInit.CRYSTALLIZER);
        FluidStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getFluidProvider, BlockEntityTypeInit.ELECTROLYZER);
        FluidStorage.SIDED.registerForBlockEntity(FluidTankBlockEntity::getFluidProvider, BlockEntityTypeInit.FLUID_TANK);
        FluidStorage.SIDED.registerForBlockEntity(WellheadBlockEntity::getFluidProvider, BlockEntityTypeInit.WELLHEAD);
        FluidStorage.SIDED.registerForBlockEntity(ShakingTableBlockEntity::getFluidProvider, BlockEntityTypeInit.SHAKING_TABLE);
        FluidStorage.SIDED.registerForBlockEntity(CentrifugalConcentratorBlockEntity::getFluidProvider, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR);
        FluidStorage.SIDED.registerForBlockEntity(ArcFurnaceBlockEntity::getFluidProvider, BlockEntityTypeInit.ARC_FURNACE);

        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerWorld serverWorld) {
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.FLUID, pos);
            }

            return null;
        }, BlockInit.FLUID_PIPE);
    }
}
