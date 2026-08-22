package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.pipe.ConnectionModelSet;
import dev.turtywurty.industria.pipe.PipeConnectionModelApi;
import dev.turtywurty.industria.pipe.PipeConnectionModelRegistry;
import dev.turtywurty.multiblocklib.MultiblockLib;
import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ModPipeConnectionModels {
    private static final ConnectionModelSet CABLE = ConnectionModelSet.horizontalAndVertical(
            Industria.id("default/cable"),
            Industria.id("block/cable_connection"),
            Industria.id("block/cable_connection_up"),
            Industria.id("block/cable_connection_down")
    );
    private static final ConnectionModelSet FLUID_PIPE = ConnectionModelSet.horizontalAndVertical(
            Industria.id("default/fluid_pipe"),
            Industria.id("block/fluid_pipe_connection"),
            Industria.id("block/fluid_pipe_connection_up"),
            Industria.id("block/fluid_pipe_connection_down")
    );
    private static final ConnectionModelSet SLURRY_PIPE = ConnectionModelSet.horizontalAndVertical(
            Industria.id("default/slurry_pipe"),
            Industria.id("block/slurry_pipe_connection"),
            Industria.id("block/slurry_pipe_connection_up"),
            Industria.id("block/slurry_pipe_connection_down")
    );
    private static final ConnectionModelSet GAS_PIPE = ConnectionModelSet.horizontalAndVertical(
            Industria.id("default/gas_pipe"),
            Industria.id("block/gas_pipe_connection"),
            Industria.id("block/gas_pipe_connection_up"),
            Industria.id("block/gas_pipe_connection_down")
    );
    private static final ConnectionModelSet MIXER_FLUID_PIPE = ConnectionModelSet.forDirection(
            Industria.id("mixer_fluid_pipe"),
            Direction.DOWN,
            Industria.id("block/mixer_fluid_pipe_connection"),
            Variant.SimpleModelState.DEFAULT.withY(Quadrant.R180)
    );
    private static final ConnectionModelSet MIXER_SLURRY_PIPE = ConnectionModelSet.rotatedFromNorth(
            Industria.id("mixer_slurry_pipe"),
            Industria.id("block/mixer_slurry_pipe_connection")
    );
    private static final ConnectionModelSet SOLAR_PANEL_CABLE = ConnectionModelSet.horizontal(
            Industria.id("solar_panel_cable"),
            Industria.id("block/solar_panel_cable_connection")
    );

    public static void init() {
    }

    public static void register() {
        PipeConnectionModelRegistry.registerDefault(
                ModBlocks.CABLE.get(),
                CABLE
        );

        PipeConnectionModelRegistry.registerDefault(
                ModBlocks.FLUID_PIPE.get(),
                FLUID_PIPE
        );

        PipeConnectionModelRegistry.registerDefault(
                ModBlocks.SLURRY_PIPE.get(),
                SLURRY_PIPE
        );

        PipeConnectionModelRegistry.registerDefault(
                ModBlocks.GAS_PIPE.get(),
                GAS_PIPE
        );

        PipeConnectionModelApi.register(
                Industria.id("mixer_fluid_pipe"),
                ModBlocks.FLUID_PIPE.get(),
                MultiblockLib.MULTIBLOCK_PART,
                (level, targetPos, _, targetFace) -> isMixerPort(level, targetPos, targetFace, true),
                MIXER_FLUID_PIPE,
                100
        );

        PipeConnectionModelApi.register(
                Industria.id("mixer_slurry_pipe"),
                ModBlocks.SLURRY_PIPE.get(),
                MultiblockLib.MULTIBLOCK_PART,
                (level, targetPos, _, targetFace) -> isMixerPort(level, targetPos, targetFace, false),
                MIXER_SLURRY_PIPE,
                100
        );

        PipeConnectionModelApi.register(
                Industria.id("solar_panel_cable"),
                ModBlocks.CABLE.get(),
                ModBlocks.SOLAR_PANEL.get(),
                (state, targetFace) -> {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return targetFace == facing.getClockWise() || targetFace == facing.getCounterClockWise();
                },
                SOLAR_PANEL_CABLE,
                0
        );

        PipeConnectionModelApi.register(
                Industria.id("advanced_solar_panel_cable"),
                ModBlocks.CABLE.get(),
                ModBlocks.ADVANCED_SOLAR_PANEL.get(),
                (state, targetFace) -> {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return targetFace == facing.getClockWise() || targetFace == facing.getCounterClockWise();
                },
                SOLAR_PANEL_CABLE,
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
