package dev.turtywurty.industria.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.MultiblockExportSelectionComponent;
import dev.turtywurty.industria.init.ComponentTypeInit;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MultiblockExportItem extends Item {
    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public MultiblockExportItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            return InteractionResult.PASS;

        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel) || player == null)
            return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        if (!stack.is(this))
            return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            clearSelection(stack);
            player.sendSystemMessage(Component.literal("Cleared multiblock selection."));
            return InteractionResult.SUCCESS;
        }

        MultiblockExportSelectionComponent selection = getSelection(stack);
        if (selection.positions().size() >= 3) {
            player.sendSystemMessage(Component.literal("Selection is complete. Sneak-right-click air to export, or sneak-right-click any block to clear."));
            return InteractionResult.PASS;
        }

        List<BlockPos> positions = new ArrayList<>(selection.positions());
        positions.add(context.getClickedPos());
        stack.set(ComponentTypeInit.MULTIBLOCK_EXPORT_SELECTION, new MultiblockExportSelectionComponent(positions));

        if (positions.size() == 1) {
            player.sendSystemMessage(Component.literal("Selected controller/base position 1/3."));
        } else if (positions.size() == 2) player.sendSystemMessage(Component.literal("Selected footprint corner 2/3."));
        else {
            player.sendSystemMessage(Component.literal("Selected height corner 3/3. Sneak-right-click air to export."));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(this))
            return InteractionResult.PASS;

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (!player.isShiftKeyDown())
            return InteractionResult.PASS;

        MultiblockExportSelectionComponent selection = getSelection(stack);
        if (selection.positions().size() < 3) {
            clearSelection(stack);
            player.sendSystemMessage(Component.literal("Cleared multiblock selection."));
            return InteractionResult.SUCCESS;
        }

        return exportSelection((ServerLevel) level, stack, (ServerPlayer) player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Right-click 3 positions to define a multiblock."));
        tooltip.accept(Component.literal("1: controller/base, 2: footprint corner, 3: height corner."));
        tooltip.accept(Component.literal("Sneak-right-click air to export the selection."));
        tooltip.accept(Component.literal("Sneak-right-click any block to clear."));
    }

    public static MultiblockExportSelectionComponent getSelection(ItemStack stack) {
        return stack.getOrDefault(ComponentTypeInit.MULTIBLOCK_EXPORT_SELECTION, MultiblockExportSelectionComponent.EMPTY);
    }

    public static int getSelectionStage(ItemStack stack) {
        return getSelection(stack).positions().size();
    }

    public static void clearSelection(ItemStack stack) {
        stack.set(ComponentTypeInit.MULTIBLOCK_EXPORT_SELECTION, MultiblockExportSelectionComponent.EMPTY);
    }

    public static SelectionBounds getSelectionBounds(ItemStack stack) {
        MultiblockExportSelectionComponent selection = getSelection(stack);
        List<BlockPos> positions = selection.positions();
        if (positions.isEmpty())
            return null;

        BlockPos controllerPos = positions.get(0);
        if (positions.size() == 1)
            return SelectionBounds.singleBlock(controllerPos);

        BlockPos footprintCorner = positions.get(1);
        if (positions.size() == 2)
            return SelectionBounds.footprint(controllerPos, footprintCorner);

        BlockPos heightCorner = positions.get(2);
        return SelectionBounds.full(controllerPos, footprintCorner, heightCorner);
    }

    private static InteractionResult exportSelection(ServerLevel level, ItemStack stack, ServerPlayer player) {
        SelectionBounds bounds = getSelectionBounds(stack);
        if (bounds == null) {
            player.sendSystemMessage(Component.literal("No selection to export."));
            return InteractionResult.FAIL;
        }

        BlockState controllerState = level.getBlockState(bounds.controller());
        Block controllerBlock = controllerState.getBlock();
        if (controllerState.isAir()) {
            player.sendSystemMessage(Component.literal("The controller position is air. Select a real controller block first."));
            return InteractionResult.FAIL;
        }

        var root = new JsonObject();
        root.addProperty("trigger_item", Industria.id("wrench").toString());
        root.addProperty("controller_block", BuiltInRegistries.BLOCK.getKey(controllerBlock).toString());

        var allowedRotations = new JsonArray();
        allowedRotations.add("none");
        allowedRotations.add("cw_90");
        allowedRotations.add("cw_180");
        allowedRotations.add("cw_270");
        root.add("allowed_rotations", allowedRotations);
        root.addProperty("allow_mirror", false);

        var controller = new JsonArray();
        controller.add(bounds.controller().getX() - bounds.minX());
        controller.add(bounds.controller().getY() - bounds.minY());
        controller.add(bounds.controller().getZ() - bounds.minZ());
        root.add("controller", controller);

        var pattern = new JsonObject();
        pattern.addProperty("type", "multiblocklib:grid");
        var layers = new JsonArray();

        for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
            var layer = new JsonArray();
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                var row = new JsonArray();
                for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    row.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                }

                layer.add(row);
            }

            layers.add(layer);
        }

        pattern.add("layers", layers);
        root.add("pattern", pattern);

        Path exportDir = FabricLoader.getInstance().getGameDir().resolve("industria_multiblock_exports");
        String fileName = sanitizeFileName(BuiltInRegistries.BLOCK.getKey(controllerBlock).getPath()) + "_" + FILE_NAME_FORMAT.format(LocalDateTime.now()) + ".json";
        Path exportPath = exportDir.resolve(fileName);

        try {
            Files.createDirectories(exportDir);
            Files.writeString(exportPath, Industria.GSON.toJson(root));
        } catch (IOException exception) {
            Industria.LOGGER.error("Failed to export multiblock selection to {}", exportPath, exception);
            player.sendSystemMessage(Component.literal("Failed to export multiblock: " + exception.getMessage()));
            return InteractionResult.FAIL;
        }

        clearSelection(stack);
        player.sendSystemMessage(Component.literal("Exported multiblock selection to " + exportPath.toAbsolutePath()));
        return InteractionResult.SUCCESS;
    }

    private static String sanitizeFileName(String value) {
        var builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isLetterOrDigit(character) || character == '-' || character == '_') {
                builder.append(character);
            } else {
                builder.append('_');
            }
        }

        return builder.toString();
    }

    public record SelectionBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos controller) {
        public static SelectionBounds singleBlock(BlockPos pos) {
            return new SelectionBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(), pos);
        }

        public static SelectionBounds footprint(BlockPos controllerPos, BlockPos footprintCorner) {
            int minX = Math.min(controllerPos.getX(), footprintCorner.getX());
            int minZ = Math.min(controllerPos.getZ(), footprintCorner.getZ());
            int maxX = Math.max(controllerPos.getX(), footprintCorner.getX());
            int maxZ = Math.max(controllerPos.getZ(), footprintCorner.getZ());
            int y = controllerPos.getY();
            return new SelectionBounds(minX, y, minZ, maxX, y, maxZ, controllerPos);
        }

        public static SelectionBounds full(BlockPos controllerPos, BlockPos footprintCorner, BlockPos heightCorner) {
            int minX = Math.min(controllerPos.getX(), footprintCorner.getX());
            int minZ = Math.min(controllerPos.getZ(), footprintCorner.getZ());
            int maxX = Math.max(controllerPos.getX(), footprintCorner.getX());
            int maxZ = Math.max(controllerPos.getZ(), footprintCorner.getZ());
            int minY = Math.min(controllerPos.getY(), heightCorner.getY());
            int maxY = Math.max(controllerPos.getY(), heightCorner.getY());
            return new SelectionBounds(minX, minY, minZ, maxX, maxY, maxZ, controllerPos);
        }
    }
}
