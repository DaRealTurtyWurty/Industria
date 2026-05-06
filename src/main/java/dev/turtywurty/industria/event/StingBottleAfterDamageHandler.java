package dev.turtywurty.industria.event;

import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public final class StingBottleAfterDamageHandler {
    private static final List<SlotRange> INSERT_SLOT_RANGES = List.of(
            Objects.requireNonNull(SlotRanges.nameToIds("container.*")),
            Objects.requireNonNull(SlotRanges.nameToIds("hotbar.*")),
            Objects.requireNonNull(SlotRanges.nameToIds("inventory.*")),
            Objects.requireNonNull(SlotRanges.nameToIds("mob.inventory.*"))
    );

    private StingBottleAfterDamageHandler() {
    }

    public static void handle(LivingEntity entity, DamageSource source) {
        Level level = entity.level();
        if (level.isClientSide() || !Objects.equals(source.typeHolder().unwrapKey().orElse(null), DamageTypes.STING))
            return;

        InteractionHand hand = getBottleHand(entity);
        if (hand == null)
            return;

        ItemStack inHand = entity.getItemInHand(hand);
        if (inHand.isEmpty())
            return;

        inHand.consume(1, entity);
        ItemStack bottle = ItemInit.BOTTLE_FORMIC_ACID.getDefaultInstance();
        if (bottle.isEmpty())
            return;

        if (!tryInsert(entity, bottle.copy())) {
            entity.spawnAtLocation((ServerLevel) level, bottle);
        }
    }

    private static InteractionHand getBottleHand(LivingEntity entity) {
        if (entity.getMainHandItem().is(Items.GLASS_BOTTLE))
            return InteractionHand.MAIN_HAND;

        if (entity.getOffhandItem().is(Items.GLASS_BOTTLE))
            return InteractionHand.OFF_HAND;

        return null;
    }

    private static boolean tryInsert(SlotProvider slotProvider, ItemStack stack) {
        for (SlotRange slotRange : INSERT_SLOT_RANGES) {
            for (int slotId : slotRange.slots()) {
                SlotAccess slotAccess = slotProvider.getSlot(slotId);
                if (slotAccess == null)
                    continue;

                ItemStack inSlot = slotAccess.get();
                if (inSlot.isEmpty()) {
                    if (slotAccess.set(stack))
                        return true;

                    continue;
                }

                if (ItemStack.isSameItemSameComponents(inSlot, stack)
                        && inSlot.getCount() + stack.getCount() <= inSlot.getMaxStackSize()) {
                    ItemStack copy = inSlot.copy();
                    copy.grow(stack.getCount());
                    if (slotAccess.set(copy))
                        return true;
                }
            }
        }

        return false;
    }
}
