package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.network.RotaryKilnControllerRemovedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RotaryKilnControllerBlock extends IndustriaBlock {
    public static final Map<Direction, VoxelShape[]> SHAPES = Util.make(new HashMap<>(), map -> {
        VoxelShape[] segments = new VoxelShape[16];

        segments[0] = VoxelShapes.cuboid(-2, 0, 0, 3, 5, 1);
        segments[1] = VoxelShapes.union(
                VoxelShapes.cuboid(-2, 4, -1, 0, 5, 0),
                VoxelShapes.cuboid(-2, 1, -1, -0.125, 4, 0),
                VoxelShapes.cuboid(-2, 0, -1, 3, 1, 0),
                VoxelShapes.cuboid(1.125, 1, -1, 3, 4, 0),
                VoxelShapes.cuboid(-0.125, 1, -0.0625, 1.125, 5, 0.0625),
                VoxelShapes.cuboid(1, 4, -1, 3, 5, 0),
                VoxelShapes.cuboid(0, 2.375, -1, 1, 4, -0.875),
                VoxelShapes.cuboid(-0.125, 1.1875, -1, 0, 4, -0.0625),
                VoxelShapes.cuboid(1, 1.1875, -1, 1.125, 4, -0.0625),
                VoxelShapes.cuboid(-0.125, 1, -1, 1.125, 1.1875, -0.0625)
        );
        segments[2] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -2, 3, 4, -1),
                VoxelShapes.cuboid(-2, 0, -2, 3, 1, -1),
                VoxelShapes.cuboid(-2, 1, -2, -1, 4, -1),
                VoxelShapes.cuboid(-2, 4, -2, 3, 5, -1)
        );
        segments[3] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -3, 3, 4, -2),
                VoxelShapes.cuboid(-2, 0, -3, 3, 1, -2),
                VoxelShapes.cuboid(-2, 1, -3, -1, 4, -2),
                VoxelShapes.cuboid(-2, 4, -3, 3, 5, -2)
        );
        segments[4] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -4, 3, 4, -3),
                VoxelShapes.cuboid(-2, 0, -4, 3, 1, -3),
                VoxelShapes.cuboid(-2, 1, -4, -1, 4, -3),
                VoxelShapes.cuboid(-2, 4, -4, 3, 5, -3)
        );
        segments[5] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -5, 3, 4, -4),
                VoxelShapes.cuboid(-2, 0, -5, 3, 1, -4),
                VoxelShapes.cuboid(-2, 1, -5, -1, 4, -4),
                VoxelShapes.cuboid(-2, 4, -5, 3, 5, -4)
        );
        segments[6] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -6, 3, 4, -5),
                VoxelShapes.cuboid(-2, 0, -6, 3, 1, -5),
                VoxelShapes.cuboid(-2, 1, -6, -1, 4, -5),
                VoxelShapes.cuboid(-2, 4, -6, 3, 5, -5)
        );
        segments[7] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -7, 3, 4, -6),
                VoxelShapes.cuboid(-2, 0, -7, 3, 1, -6),
                VoxelShapes.cuboid(-2, 1, -7, -1, 4, -6),
                VoxelShapes.cuboid(-2, 4, -7, 3, 5, -6)
        );
        segments[8] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -8, 3, 4, -7),
                VoxelShapes.cuboid(-2, 0, -8, 3, 1, -7),
                VoxelShapes.cuboid(-2, 1, -8, -1, 4, -7),
                VoxelShapes.cuboid(-2, 4, -8, 3, 5, -7)
        );
        segments[9] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -9, 3, 4, -8),
                VoxelShapes.cuboid(-2, 0, -9, 3, 1, -8),
                VoxelShapes.cuboid(-2, 1, -9, -1, 4, -8),
                VoxelShapes.cuboid(-2, 4, -9, 3, 5, -8)
        );
        segments[10] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -10, 3, 4, -9),
                VoxelShapes.cuboid(-2, 0, -10, 3, 1, -9),
                VoxelShapes.cuboid(-2, 1, -10, -1, 4, -9),
                VoxelShapes.cuboid(-2, 4, -10, 3, 5, -9)
        );
        segments[11] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -11, 3, 4, -10),
                VoxelShapes.cuboid(-2, 0, -11, 3, 1, -10),
                VoxelShapes.cuboid(-2, 1, -11, -1, 4, -10),
                VoxelShapes.cuboid(-2, 4, -11, 3, 5, -10)
        );
        segments[12] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -12, 3, 4, -11),
                VoxelShapes.cuboid(-2, 0, -12, 3, 1, -11),
                VoxelShapes.cuboid(-2, 1, -12, -1, 4, -11),
                VoxelShapes.cuboid(-2, 4, -12, 3, 5, -11)
        );
        segments[13] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -13, 3, 4, -12),
                VoxelShapes.cuboid(-2, 0, -13, 3, 1, -12),
                VoxelShapes.cuboid(-2, 1, -13, -1, 4, -12),
                VoxelShapes.cuboid(-2, 4, -13, 3, 5, -12)
        );
        segments[14] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -14, 3, 4, -13),
                VoxelShapes.cuboid(-2, 0, -14, 3, 1, -13),
                VoxelShapes.cuboid(-2, 1, -14, -1, 4, -13),
                VoxelShapes.cuboid(-2, 4, -14, 3, 5, -13)
        );
        segments[15] = VoxelShapes.union(
                VoxelShapes.cuboid(2, 1, -15, 3, 4, -14),
                VoxelShapes.cuboid(-2, 0, -15, 3, 1, -14),
                VoxelShapes.cuboid(-2, 1, -15, -1, 4, -14),
                VoxelShapes.cuboid(-2, 4, -15, 3, 5, -14)
        );

        for (Direction direction : Direction.Type.HORIZONTAL) {
            VoxelShape[] rotatedShapes = new VoxelShape[16];
            for (int i = 0; i < segments.length; i++) {
                rotatedShapes[i] = IndustriaBlock.BlockProperties.calculateShape(direction, segments[i]);
            }

            map.put(direction, rotatedShapes);
        }
    });

    public RotaryKilnControllerBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .shapeFactory((state, world, pos, context) ->
                        getVoxelShape(world, pos, state.get(Properties.HORIZONTAL_FACING)))
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ROTARY_KILN_CONTROLLER)
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.ROTARY_KILN_CONTROLLER).build()));
    }

    public static VoxelShape getVoxelShape(BlockView world, BlockPos pos, Direction direction) {
        VoxelShape currentShape = SHAPES.get(direction)[0];
        if (world.getBlockEntity(pos) instanceof RotaryKilnControllerBlockEntity blockEntity) {
            int size = blockEntity.getKilnSegments().size();
            for (int i = 1; i < size + 1; i++) {
                VoxelShape segmentShape = SHAPES.get(direction)[i];
                currentShape = VoxelShapes.union(currentShape, segmentShape);
            }
        }

        return currentShape;
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if(world instanceof ServerWorld serverWorld) {
            var payload = new RotaryKilnControllerRemovedPayload(pos);
            for (ServerPlayerEntity sPlayer : serverWorld.getPlayers()) {
                ServerPlayNetworking.send(sPlayer, payload);
            }
        }
    }
}
