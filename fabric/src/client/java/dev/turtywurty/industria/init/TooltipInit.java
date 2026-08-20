package dev.turtywurty.industria.init;

import dev.turtywurty.industria.item.MobJarItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.Comparator;
import java.util.Map;

public class TooltipInit {
    private static final Component ENTITY_DATA_HINT = Component.literal("Hold Shift for entity data").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component ENTITY_DATA_HEADER = Component.literal("Entity Data").withStyle(ChatFormatting.GRAY);

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltipFlag, lines) -> {
            if (!(stack.getItem() instanceof MobJarItem)) {
                return;
            }

            TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            if (!display.shows(DataComponents.ENTITY_DATA)) {
                return;
            }

            TypedEntityData<EntityType<?>> typedEntityData = stack.get(DataComponents.ENTITY_DATA);
            if (typedEntityData == null) {
                return;
            }

            CompoundTag entityData = typedEntityData.copyTagWithoutId();
            if (entityData.isEmpty()) {
                return;
            }

            if (!Minecraft.getInstance().hasShiftDown()) {
                lines.add(ENTITY_DATA_HINT);
                return;
            }

            lines.add(ENTITY_DATA_HEADER);
            entityData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                    .map(entry -> Component.literal(entry.getKey() + ": " + entry.getValue()).withStyle(ChatFormatting.DARK_GRAY))
                    .forEach(lines::add);
        });
    }
}
