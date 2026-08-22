package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.*;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class ModMultiblockTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<OilPumpJackBlockEntity>> OIL_PUMP_JACK = register("oil_pump_jack",
            () -> new MultiblockType.Builder<OilPumpJackBlockEntity>(123)
                    .setHasDirectionProperty(true));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<DrillBlockEntity>> DRILL = register("drill",
            () -> new MultiblockType.Builder<DrillBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<UpgradeStationBlockEntity>> UPGRADE_STATION = register("upgrade_station",
            () -> new MultiblockType.Builder<UpgradeStationBlockEntity>(12)
                    .setHasDirectionProperty(true));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<MixerBlockEntity>> MIXER = register("mixer",
            () -> new MultiblockType.Builder<MixerBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true)
                    .shapes(MixerBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<DigesterBlockEntity>> DIGESTER = register("digester",
            () -> new MultiblockType.Builder<DigesterBlockEntity>(44) // 3x3x4
                    .setHasDirectionProperty(true)
                    .shapes(DigesterBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<ClarifierBlockEntity>> CLARIFIER = register("clarifier",
            () -> new MultiblockType.Builder<ClarifierBlockEntity>(17) // 3x3x2
                    .setHasDirectionProperty(true)
                    .shapes(ClarifierBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<CrystallizerBlockEntity>> CRYSTALLIZER = register("crystallizer",
            () -> new MultiblockType.Builder<CrystallizerBlockEntity>(35) // 3x3x4
                    .setHasDirectionProperty(true)
                    .shapes(CrystallizerBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<RotaryKilnControllerBlockEntity>> ROTARY_KILN_CONTROLLER = register("rotary_kiln_controller",
            () -> new MultiblockType.Builder<RotaryKilnControllerBlockEntity>(24) // 5x5x1
                    .setHasDirectionProperty(true)
                    .shapeFactory(RotaryKilnControllerBlock::getVoxelShape));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<RotaryKilnBlockEntity>> ROTARY_KILN = register("rotary_kiln",
            () -> new MultiblockType.Builder<RotaryKilnBlockEntity>(24) // 5x5x1
                    .setHasDirectionProperty(true)
                    .shapeFactory(RotaryKilnBlock::getVoxelShape));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<ElectrolyzerBlockEntity>> ELECTROLYZER = register("electrolyzer",
            () -> new MultiblockType.Builder<ElectrolyzerBlockEntity>(11) // 3x2x2
                    .setHasDirectionProperty(true)
                    .shapes(ElectrolyzerBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<ShakingTableBlockEntity>> SHAKING_TABLE = register("shaking_table",
            () -> new MultiblockType.Builder<ShakingTableBlockEntity>(29) // 5x3x2
                    .setHasDirectionProperty(true)
                    .shapes(ShakingTableBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<CentrifugalConcentratorBlockEntity>> CENTRIFUGAL_CONCENTRATOR = register("centrifugal_concentrator",
            () -> new MultiblockType.Builder<CentrifugalConcentratorBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true)
                    .shapes(CentrifugalConcentratorBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<AgitatorBlockEntity>> AGITATOR = register("agitator",
            () -> new MultiblockType.Builder<AgitatorBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true)
                    .shapes(AgitatorBlock.VOXEL_SHAPE));

    public static final RegistrationHandle<MultiblockType<?>, MultiblockType<DistillationTowerBlockEntity>> DISTILLATION_TOWER = register("distillation_tower",
            () -> new MultiblockType.Builder<DistillationTowerBlockEntity>(63) // 3x3x7
                    .setHasDirectionProperty(true)
                    .shapes(DistillationTowerBlock.VOXEL_SHAPE));

    public static <T extends BlockEntity> RegistrationHandle<MultiblockType<?>, MultiblockType<T>> register(String name, Supplier<MultiblockType.Builder<T>> builder) {
        return REGISTRIES.register(IndustriaRegistries.MULTIBLOCK_TYPES_KEY, Industria.id(name), () -> builder.get().build());
    }

    public static void init() {
    }
}
