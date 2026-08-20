package dev.turtywurty.industria.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.core.Direction;

import java.util.function.Consumer;

public class MobJarItem extends Item {
    public MobJarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        TypedEntityData<EntityType<?>> typedEntityData = stack.get(DataComponents.ENTITY_DATA);
        if (typedEntityData == null)
            return InteractionResult.PASS;

        EntityType<?> entityType = typedEntityData.type();
        if (!entityType.isAllowedInPeaceful() && level.getDifficulty() == Difficulty.PEACEFUL)
            return InteractionResult.FAIL;

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos spawnPos = level.getBlockState(clickedPos).getCollisionShape(level, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(clickedFace);
        boolean alignToSurface = !spawnPos.equals(clickedPos) && clickedFace == Direction.UP;

        if (entityType.spawn(serverLevel, stack, context.getPlayer(), spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, alignToSurface) == null)
            return InteractionResult.FAIL;

        ItemStackTemplate craftingRemainder = getCraftingRemainder();
        if (craftingRemainder == null)
            return InteractionResult.SUCCESS;

        return InteractionResult.SUCCESS.heldItemTransformedTo(craftingRemainder.create());
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
