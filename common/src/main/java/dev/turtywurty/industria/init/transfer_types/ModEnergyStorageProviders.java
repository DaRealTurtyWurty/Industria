package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.industria.block.FluidPumpBlock;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModEnergyStorageProviders {
    private ModEnergyStorageProviders() {
    }

    public static void init() {
        TransferService transfers = TransferService.get();
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.THERMAL_GENERATOR, ThermalGeneratorBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.BATTERY, BatteryBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.COMBUSTION_GENERATOR, CombustionGeneratorBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.SOLAR_PANEL, SolarPanelBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.CRUSHER, CrusherBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.WIND_TURBINE, WindTurbineBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.MOTOR, MotorBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.ELECTRIC_FURNACE, ElectricFurnaceBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.INDUCTION_HEATER, InductionHeaterBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.MIXER, MixerBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.ELECTROLYZER, ElectrolyzerBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.OIL_PUMP_JACK, OilPumpJackBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.DRILL, DrillBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.DIGESTER, DigesterBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.SHAKING_TABLE, ShakingTableBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.CENTRIFUGAL_CONCENTRATOR, CentrifugalConcentratorBlockEntity::getEnergyProvider);
        transfers.registerBlockEntityProvider(StorageKeys.ENERGY, ModBlockEntityTypes.ARC_FURNACE, ArcFurnaceBlockEntity::getEnergyProvider);
        transfers.registerBlockProvider(StorageKeys.ENERGY, (level, pos, state, _, side) -> {
            if (side != Direction.UP)
                return null;

            BlockPos controllerPos = FluidPumpBlock.getControllerPos(pos, state);
            BlockEntity controller = level.getBlockEntity(controllerPos);
            return controller instanceof FluidPumpBlockEntity fluidPump
                    ? fluidPump.getEnergyProvider(side)
                    : null;
        }, ModBlocks.FLUID_PUMP);
        transfers.registerBlockProvider(StorageKeys.ENERGY, (world, pos, _, _, _) -> {
            if (world instanceof ServerLevel serverWorld) {
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.ENERGY, pos);
            }

            return null;
        }, ModBlocks.CABLE);
    }
}
