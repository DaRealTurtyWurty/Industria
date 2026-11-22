package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.BlastFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlastFurnaceBlock extends IndustriaBlock {
    public BlastFurnaceBlock(Properties settings) {
        super(settings.dynamicShape(), new BlockProperties()
            .hasHorizontalFacing()
            .hasLitProperty()
            .hasBlockEntityRenderer()
            .shapeFactory((state, level, pos, _) -> level.getBlockEntity(pos) instanceof BlastFurnaceBlockEntity furnace
                && furnace.isFormed()
                    ? BlastFurnaceGeometry.SHAPES.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                    : Shapes.block())
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.BLAST_FURNACE)
                .shouldTick()
                .rightClickToOpenGui()
                .dropContentsOnBreak()));
    }

    /**
     * Pixel coordinates from the bottom corner of the formed furnace. The grate opens toward +Z.
     */
    public static final class BlastFurnaceGeometry {
        public enum Material {
            FIREBRICK,
            STEEL,
            CAST_IRON,
            SOOT,
            FIREBED
        }

        public record Box(Material material, int x, int y, int z, int width, int height, int depth) {
        }

        public static final List<Box> BOXES = build();
        public static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(buildShape());

        public static final int CONTROLLER_X = 16, CONTROLLER_Y = 16, CONTROLLER_Z = 32;

        private BlastFurnaceGeometry() {
        }

        private static List<Box> build() {
            List<Box> parts = new ArrayList<Box>();
            parts.add(new Box(Material.CAST_IRON, 1, 0, 1, 46, 3, 46));
            parts.add(new Box(Material.FIREBRICK, 3, 3, 3, 42, 53, 42));
            for (int x : new int[]{2, 41}) {
                for (int z : new int[]{2, 41}) {
                    parts.add(new Box(Material.STEEL, x, 3, z, 5, 52, 5));
                }
            }

            for (int y : new int[]{13, 34, 54}) {
                parts.add(new Box(Material.STEEL, 1, y, 1, 46, 3, 46));
                for (int x : new int[]{3, 43}) {
                    parts.add(new Box(Material.CAST_IRON, x, y, 47, 2, 2, 1));
                }
            }

            parts.add(new Box(Material.CAST_IRON, 22, 40, 45, 4, 14, 1));
            parts.add(new Box(Material.STEEL, 9, 38, 45, 30, 2, 2));

            // Raised charging hatch with a recessed dark throat.
            parts.add(new Box(Material.CAST_IRON, 11, 57, 11, 26, 2, 26));
            parts.add(new Box(Material.SOOT, 17, 59, 17, 14, 1, 14));
            parts.add(new Box(Material.STEEL, 14, 59, 14, 20, 5, 3));
            parts.add(new Box(Material.STEEL, 14, 59, 31, 20, 5, 3));
            parts.add(new Box(Material.STEEL, 14, 59, 17, 3, 5, 14));
            parts.add(new Box(Material.STEEL, 31, 59, 17, 3, 5, 14));

            // Grate and tapping mouth on the front.
            parts.add(new Box(Material.FIREBED, 13, 18, 45, 22, 13, 1));
            parts.add(new Box(Material.CAST_IRON, 10, 16, 45, 28, 3, 3));
            parts.add(new Box(Material.CAST_IRON, 10, 31, 45, 28, 3, 3));
            for (int x : new int[]{10, 35}) {
                parts.add(new Box(Material.CAST_IRON, x, 19, 45, 3, 12, 3));
            }

            for (int x : new int[]{17, 23, 29}) {
                parts.add(new Box(Material.STEEL, x, 19, 46, 2, 12, 2));
            }

            parts.add(new Box(Material.FIREBED, 20, 4, 45, 8, 6, 1));
            parts.add(new Box(Material.STEEL, 18, 3, 45, 2, 9, 3));
            parts.add(new Box(Material.STEEL, 28, 3, 45, 2, 9, 3));
            parts.add(new Box(Material.STEEL, 20, 10, 45, 8, 2, 3));
            parts.add(new Box(Material.CAST_IRON, 20, 3, 45, 8, 2, 3));

            // Coke, air and slag connections align with the three side-wall cells.
            for (int y : new int[]{3, 19, 36}) {
                parts.add(new Box(Material.CAST_IRON, 1, y, 17, 3, 11, 14));
                parts.add(new Box(Material.STEEL, 0, y + 1, 18, 1, 2, 12));
                parts.add(new Box(Material.STEEL, 0, y + 8, 18, 1, 2, 12));
                parts.add(new Box(Material.STEEL, 0, y + 3, 18, 1, 5, 2));
                parts.add(new Box(Material.STEEL, 0, y + 3, 28, 1, 5, 2));
            }

            return List.copyOf(parts);
        }

        private static VoxelShape buildShape() {
            VoxelShape shape = Shapes.empty();
            for (Box box : BOXES) {
                int x = box.x - CONTROLLER_X, y = box.y - CONTROLLER_Y, z = box.z - CONTROLLER_Z;
                shape = Shapes.or(shape, Block.box(x, y, z, x + box.width, y + box.height, z + box.depth));
            }

            return shape.optimize();
        }
    }
}
