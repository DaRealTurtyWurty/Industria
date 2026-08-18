package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.pipe.ConnectionModelSet;
import dev.turtywurty.industria.pipe.PipeConnectionModelApi;
import dev.turtywurty.industria.pipe.PipeConnectionModelRegistry;
import dev.turtywurty.multiblocklib.MultiblockLib;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class PipeConnectionModelInit {
    public static void init() {
        PipeConnectionModelRegistry.registerDefault(
                BlockInit.CABLE,
                ConnectionModelSet.horizontalAndVertical(
                        Industria.id("default/cable"),
                        Industria.id("block/cable_connection"),
                        Industria.id("block/cable_connection_up"),
                        Industria.id("block/cable_connection_down")
                )
        );

        PipeConnectionModelRegistry.registerDefault(
                BlockInit.FLUID_PIPE,
                ConnectionModelSet.horizontalAndVertical(
                        Industria.id("default/fluid_pipe"),
                        Industria.id("block/fluid_pipe_connection"),
                        Industria.id("block/fluid_pipe_connection_up"),
                        Industria.id("block/fluid_pipe_connection_down")
                )
        );

        PipeConnectionModelRegistry.registerDefault(
                BlockInit.SLURRY_PIPE,
                ConnectionModelSet.horizontalAndVertical(
                        Industria.id("default/slurry_pipe"),
                        Industria.id("block/slurry_pipe_connection"),
                        Industria.id("block/slurry_pipe_connection_up"),
                        Industria.id("block/slurry_pipe_connection_down")
                )
        );

        PipeConnectionModelRegistry.registerDefault(
                BlockInit.GAS_PIPE,
                ConnectionModelSet.horizontalAndVertical(
                        Industria.id("default/gas_pipe"),
                        Industria.id("block/gas_pipe_connection"),
                        Industria.id("block/gas_pipe_connection_up"),
                        Industria.id("block/gas_pipe_connection_down")
                )
        );

        PipeConnectionModelRegistry.registerDefault(
                BlockInit.HEAT_PIPE,
                ConnectionModelSet.rotatedFromNorth(
                        Industria.id("default/heat_pipe"),
                        Industria.id("block/heat_pipe_connected")
                )
        );

        PipeConnectionModelApi.register(
                Industria.id("mixer_fluid_pipe"),
                BlockInit.FLUID_PIPE,
                MultiblockLib.MULTIBLOCK_PART,
                (level, targetPos, _, targetFace) -> isMixerPort(level, targetPos, targetFace, true),
                ConnectionModelSet.forDirection(
                        Industria.id("mixer_fluid_pipe"),
                        Direction.DOWN,
                        Industria.id("block/mixer_fluid_pipe_connection")
                ),
                100
        );

        PipeConnectionModelApi.register(
                Industria.id("mixer_slurry_pipe"),
                BlockInit.SLURRY_PIPE,
                MultiblockLib.MULTIBLOCK_PART,
                (level, targetPos, _, targetFace) -> isMixerPort(level, targetPos, targetFace, false),
                ConnectionModelSet.rotatedFromNorth(
                        Industria.id("mixer_slurry_pipe"),
                        Industria.id("block/mixer_slurry_pipe_connection")
                ),
                100
        );

        ConnectionModelSet solarPanelCable = ConnectionModelSet.horizontal(
                Industria.id("solar_panel_cable"),
                Industria.id("block/solar_panel_cable_connection")
        );

        PipeConnectionModelApi.register(
                Industria.id("solar_panel_cable"),
                BlockInit.CABLE,
                BlockInit.SOLAR_PANEL,
                (state, targetFace) -> {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return targetFace == facing.getClockWise() || targetFace == facing.getCounterClockWise();
                },
                solarPanelCable,
                0
        );

        PipeConnectionModelApi.register(
                Industria.id("advanced_solar_panel_cable"),
                BlockInit.CABLE,
                BlockInit.ADVANCED_SOLAR_PANEL,
                (state, targetFace) -> {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return targetFace == facing.getClockWise() || targetFace == facing.getCounterClockWise();
                },
                solarPanelCable,
                0
        );
    }

    private static boolean isMixerPort(BlockAndTintGetter level, BlockPos targetPos, Direction targetFace, boolean fluid) {
        for (int blocksBelow = 0; blocksBelow <= 2; blocksBelow++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos controllerPos = targetPos.offset(offsetX, -blocksBelow, offsetZ);
                    if (!(level.getBlockEntity(controllerPos) instanceof MixerBlockEntity mixer))
                        continue;

                    boolean matchesPort = fluid ?
                            mixer.getFluidStorageForExternal(targetPos, targetFace) != null :
                            mixer.getSlurryStorageForExternal(targetPos, targetFace) != null;
                    if (matchesPort)
                        return true;
                }
            }
        }

        return false;
    }
}
