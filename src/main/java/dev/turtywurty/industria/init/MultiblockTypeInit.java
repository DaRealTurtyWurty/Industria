package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.multiblock.MultiblockType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;

public class MultiblockTypeInit {
    public static final MultiblockType<OilPumpJackBlockEntity> OIL_PUMP_JACK = register("oil_pump_jack",
            new MultiblockType.Builder<OilPumpJackBlockEntity>(123)
                    .setHasDirectionProperty(true)
                    .setOnPrimaryBlockUse((world, player, hitResult, pos) -> {
                        if (world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJackBlockEntity) {
                            oilPumpJackBlockEntity.type().onPrimaryBlockUse(world, player, hitResult, pos);
                        }
                    })
                    .setOnMultiblockBreak((world, pos) -> {
                        if (world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJackBlockEntity) {
                            oilPumpJackBlockEntity.type().onMultiblockBreak(world, pos);
                        }
                    })
                    .setEnergyProvider((blockEntity, direction) -> {
                        return null;
                    })
                    .setInventoryProvider((blockEntity, direction) -> {
                        return null;
                    })
                    .setFluidProvider((blockEntity, direction) -> {
                        return null;
                    }));

    public static final MultiblockType<DrillBlockEntity> DRILL = register("drill",
            new MultiblockType.Builder<DrillBlockEntity>(26)
                    .setHasDirectionProperty(true)
                    .setOnPrimaryBlockUse((world, player, hitResult, pos) -> {
                        if (world.getBlockEntity(pos) instanceof DrillBlockEntity drillBlockEntity) {
                            drillBlockEntity.type().onPrimaryBlockUse(world, player, hitResult, pos);
                        }
                    })
                    .setOnMultiblockBreak((world, pos) -> {
                        if (world.getBlockEntity(pos) instanceof DrillBlockEntity drillBlockEntity) {
                            drillBlockEntity.type().onMultiblockBreak(world, pos);
                        }
                    })
                    .setEnergyProvider((blockEntity, direction) -> {
                        return null;
                    })
                    .setInventoryProvider((blockEntity, direction) -> {
                        return null;
                    })
                    .setFluidProvider((blockEntity, direction) -> {
                        return null;
                    }));

    public static <T extends BlockEntity> MultiblockType<T> register(String name, MultiblockType.Builder<T> builder) {
        return Registry.register(IndustriaRegistries.MULTIBLOCK_TYPES, Industria.id(name), builder.build());
    }

    public static void init() {}
}
