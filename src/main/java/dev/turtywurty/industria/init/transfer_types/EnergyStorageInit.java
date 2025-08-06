package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.minecraft.server.world.ServerWorld;
import team.reborn.energy.api.EnergyStorage;

public class EnergyStorageInit {
    public static void init() {
        EnergyStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        EnergyStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getEnergyProvider, BlockEntityTypeInit.BATTERY);
        EnergyStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);
        EnergyStorage.SIDED.registerForBlockEntity(SolarPanelBlockEntity::getEnergyProvider, BlockEntityTypeInit.SOLAR_PANEL);
        EnergyStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getEnergyProvider, BlockEntityTypeInit.CRUSHER);
        EnergyStorage.SIDED.registerForBlockEntity(WindTurbineBlockEntity::getEnergyProvider, BlockEntityTypeInit.WIND_TURBINE);
        EnergyStorage.SIDED.registerForBlockEntity(MotorBlockEntity::getEnergyProvider, BlockEntityTypeInit.MOTOR);
        EnergyStorage.SIDED.registerForBlockEntity(ElectricFurnaceBlockEntity::getEnergyProvider, BlockEntityTypeInit.ELECTRIC_FURNACE);
        EnergyStorage.SIDED.registerForBlockEntity(InductionHeaterBlockEntity::getEnergyProvider, BlockEntityTypeInit.INDUCTION_HEATER);
        EnergyStorage.SIDED.registerForBlockEntity(FluidPumpBlockEntity::getEnergyProvider, BlockEntityTypeInit.FLUID_PUMP);
        EnergyStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getEnergyProvider, BlockEntityTypeInit.MIXER);
        EnergyStorage.SIDED.registerForBlockEntity(ElectrolyzerBlockEntity::getEnergyProvider, BlockEntityTypeInit.ELECTROLYZER);
        EnergyStorage.SIDED.registerForBlockEntity(OilPumpJackBlockEntity::getEnergyProvider, BlockEntityTypeInit.OIL_PUMP_JACK);
        EnergyStorage.SIDED.registerForBlockEntity(DrillBlockEntity::getEnergyProvider, BlockEntityTypeInit.DRILL);
        EnergyStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getEnergyProvider, BlockEntityTypeInit.DIGESTER);
        EnergyStorage.SIDED.registerForBlockEntity(ShakingTableBlockEntity::getEnergyProvider, BlockEntityTypeInit.SHAKING_TABLE);

        EnergyStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerWorld serverWorld) {
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.ENERGY, pos);
            }

            return null;
        }, BlockInit.CABLE);
    }
}
