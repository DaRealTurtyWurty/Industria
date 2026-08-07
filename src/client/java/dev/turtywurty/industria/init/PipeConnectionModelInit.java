package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.ConnectionModelSet;
import dev.turtywurty.industria.pipe.PipeConnectionModelApi;
import dev.turtywurty.industria.pipe.PipeConnectionModelRegistry;
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
}
