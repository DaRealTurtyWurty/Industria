package dev.turtywurty.industria.renderer.world;

import dev.turtywurty.industria.item.MultiblockExportItem;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

public class MultiblockExportSelectionRenderer implements IndustriaLevelRenderer {
    @Override
    public void render(LevelRenderContext context) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MultiblockExportItem)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof MultiblockExportItem))
                return;
        }

        MultiblockExportItem.SelectionBounds bounds = MultiblockExportItem.getSelectionBounds(stack);
        if (bounds == null)
            return;

        int stage = MultiblockExportItem.getSelectionStage(stack);
        AABB box = new AABB(
                bounds.minX(),
                bounds.minY(),
                bounds.minZ(),
                bounds.maxX() + 1.0D,
                bounds.maxY() + 1.0D,
                bounds.maxZ() + 1.0D
        );

        int color = switch (stage) {
            case 1 -> 0x80FFD54A;
            case 2 -> 0x8000E5FF;
            case 3 -> 0x8000FF66;
            default -> 0x80FFFFFF;
        };

        Gizmos.cuboid(box, GizmoStyle.stroke(color));
        BlockPos controller = bounds.controller();
        Gizmos.cuboid(new AABB(controller.getX(), controller.getY(), controller.getZ(), controller.getX() + 1.0D, controller.getY() + 1.0D, controller.getZ() + 1.0D),
                GizmoStyle.stroke(0xFFFF5555));
    }
}
