package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.*;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.multiblock.MultiblockType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;

public class MultiblockTypeInit {
    public static final MultiblockType<OilPumpJackBlockEntity> OIL_PUMP_JACK = register("oil_pump_jack",
            new MultiblockType.Builder<OilPumpJackBlockEntity>(123)
                    .setHasDirectionProperty(true));

    public static final MultiblockType<DrillBlockEntity> DRILL = register("drill",
            new MultiblockType.Builder<DrillBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true));

    public static final MultiblockType<UpgradeStationBlockEntity> UPGRADE_STATION = register("upgrade_station",
            new MultiblockType.Builder<UpgradeStationBlockEntity>(12)
                    .setHasDirectionProperty(true));

    public static final MultiblockType<MixerBlockEntity> MIXER = register("mixer",
            new MultiblockType.Builder<MixerBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true)
                    .shapes(MixerBlock.VOXEL_SHAPE));

    public static final MultiblockType<DigesterBlockEntity> DIGESTER = register("digester",
            new MultiblockType.Builder<DigesterBlockEntity>(44) // 3x3x4
                    .setHasDirectionProperty(true)
                    .shapes(DigesterBlock.VOXEL_SHAPE));

    public static final MultiblockType<ClarifierBlockEntity> CLARIFIER = register("clarifier",
            new MultiblockType.Builder<ClarifierBlockEntity>(17) // 3x3x2
                    .setHasDirectionProperty(true)
                    .shapes(ClarifierBlock.VOXEL_SHAPE));

    public static final MultiblockType<CrystallizerBlockEntity> CRYSTALLIZER = register("crystallizer",
            new MultiblockType.Builder<CrystallizerBlockEntity>(35) // 3x3x4
                    .setHasDirectionProperty(true)
                    .shapes(CrystallizerBlock.VOXEL_SHAPE));

    public static final MultiblockType<RotaryKilnControllerBlockEntity> ROTARY_KILN_CONTROLLER = register("rotary_kiln_controller",
            new MultiblockType.Builder<RotaryKilnControllerBlockEntity>(24) // 5x5x1
                    .setHasDirectionProperty(true)
                    .shapeFactory(RotaryKilnControllerBlock::getVoxelShape));

    public static final MultiblockType<RotaryKilnBlockEntity> ROTARY_KILN = register("rotary_kiln",
            new MultiblockType.Builder<RotaryKilnBlockEntity>(24) // 5x5x1
                    .setHasDirectionProperty(true)
                    .shapeFactory(RotaryKilnBlock::getVoxelShape));

    public static final MultiblockType<ElectrolyzerBlockEntity> ELECTROLYZER = register("electrolyzer",
            new MultiblockType.Builder<ElectrolyzerBlockEntity>(11) // 3x2x2
                    .setHasDirectionProperty(true)
                    .shapes(ElectrolyzerBlock.VOXEL_SHAPE));

    public static final MultiblockType<ShakingTableBlockEntity> SHAKING_TABLE = register("shaking_table",
            new MultiblockType.Builder<ShakingTableBlockEntity>(29) // 5x3x2
                    .setHasDirectionProperty(true)
                    .shapes(ShakingTableBlock.VOXEL_SHAPE));

    public static final MultiblockType<CentrifugalConcentratorBlockEntity> CENTRIFUGAL_CONCENTRATOR = register("centrifugal_concentrator",
            new MultiblockType.Builder<CentrifugalConcentratorBlockEntity>(26) // 3x3x3
                    .setHasDirectionProperty(true)
                    .shapes(CentrifugalConcentratorBlock.VOXEL_SHAPE));

    public static final MultiblockType<ArcFurnaceBlockEntity> ARC_FURNACE = register("arc_furnace",
            new MultiblockType.Builder<ArcFurnaceBlockEntity>(79) // 4x4x5
                    .setHasDirectionProperty(true)
                    .shapes(ArcFurnaceBlock.VOXEL_SHAPE));

    public static <T extends BlockEntity> MultiblockType<T> register(String name, MultiblockType.Builder<T> builder) {
        return Registry.register(IndustriaRegistries.MULTIBLOCK_TYPES, Industria.id(name), builder.build());
    }

    public static void init() {
    }
}
