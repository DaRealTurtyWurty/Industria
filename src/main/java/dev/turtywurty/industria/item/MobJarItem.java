package dev.turtywurty.industria.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class MobJarItem extends Item {
    public MobJarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            TypedEntityData<EntityType<?>> typedEntityData = stack.get(DataComponents.ENTITY_DATA);
            if (typedEntityData != null) {
                EntityType.loadEntityRecursive(
                        typedEntityData.copyTagWithoutId(),
                        level,
                        EntitySpawnReason.SPAWN_ITEM_USE,
                        EntityProcessor.NOP);

                ItemStackTemplate craftingRemainder = getCraftingRemainder();
                if (craftingRemainder == null)
                    return InteractionResult.PASS;

                return InteractionResult.SUCCESS.heldItemTransformedTo(craftingRemainder.create());
            } else {
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        TypedEntityData<EntityType<?>> typedEntityData = itemStack.get(DataComponents.ENTITY_DATA);
        if (typedEntityData != null && display.shows(DataComponents.ENTITY_DATA)) {
            builder.accept(typedEntityData.type().getDescription().copy());
        }
    }
}
