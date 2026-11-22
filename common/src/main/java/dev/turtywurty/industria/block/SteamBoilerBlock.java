package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.SteamBoilerBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class SteamBoilerBlock extends IndustriaBlock {
    private static final Map<Direction, VoxelShape> FORMED_SHAPES = Shapes.rotateHorizontal(Shapes.or(
            formedBox(0, 0, 0, 16, 2, 16),
            formedBox(1, 2, 1, 15, 7, 15),
            formedBox(5, 2, 0, 11, 6, 1),
            formedBox(0.5, 7, 0.5, 15.5, 8, 15.5),
            formedBox(5.5, 8, 2, 10.5, 11.5, 14),
            formedBox(2, 8, 5.5, 14, 11.5, 10.5),
            formedBox(5.5, 11.5, 5.5, 10.5, 12.5, 10.5),
            formedBox(6.5, 12.5, 6.5, 9.5, 16, 9.5),
            formedBox(7, 12, 0, 9, 13.5, 7),
            formedBox(6.5, 12, 0, 9.5, 14, 1)).optimize());

    public SteamBoilerBlock(Properties settings) {
        super(settings.dynamicShape(), new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .hasLitProperty()
                .shapeFactory((state, level, pos, _) ->
                        level.getBlockEntity(pos) instanceof SteamBoilerBlockEntity boiler && boiler.isFormed()
                                ? FORMED_SHAPES.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                                : Shapes.block())
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.STEAM_BOILER)
                        .shouldTick()
                        .dropContentsOnBreak()
                        .rightClickToOpenGui()));
    }

    // Convert the placeholder model's pixel coordinates into the formed shape around the controller.
    private static VoxelShape formedBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1 * 3 - 16, y1 * 4, z1 * 3 - 16, x2 * 3 - 16, y2 * 4, z2 * 3 - 16);
    }
}
