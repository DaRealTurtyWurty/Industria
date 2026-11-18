package dev.turtywurty.industria.item;

import dev.turtywurty.industria.recipe.WireDrawplateRecipe;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.WireDrawplateClientCache;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.EntityStorageContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class WireDrawplateItem extends Item {
    public WireDrawplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        if (getUseDuration(player.getItemInHand(hand), player) <= 0)
            return InteractionResult.PASS;

        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel))
            return itemStack;

        ItemStack offHandStack = entity.getOffhandItem();
        if (offHandStack.isEmpty())
            return itemStack;

        Optional<RecipeHolder<WireDrawplateRecipe>> recipe = serverLevel.recipeAccess().getRecipeFor(
            WireDrawplateRecipe.Type.INSTANCE,
            new SingleItemStackRecipeInput(offHandStack),
            serverLevel);
        if (recipe.isEmpty())
            return itemStack;

        WireDrawplateRecipe wireDrawplateRecipe = recipe.get().value();
        ItemStack result = wireDrawplateRecipe.assemble(new SingleItemStackRecipeInput(offHandStack));
        if (entity instanceof Player player) {
            player.getInventory().placeItemBackInInventory(result, Prediction.SERVER_ONLY);
        } else {
            ResourceStorage<ResourceVariant<Item>> storage = TransferService.get()
                .findEntity(StorageKeys.ITEM, entity, EntityStorageContext.EMPTY);
            if (storage != null) {
                long inserted;
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    inserted = storage.insert(ResourceVariant.ofItem(result), result.getCount(), transaction);
                    transaction.commit();
                }

                result.shrink((int) inserted);
            } else {
                if (!result.isEmpty()) {
                    entity.spawnAtLocation(serverLevel, result);
                }
            }
        }

        offHandStack.consume(wireDrawplateRecipe.input().stackData().count(), entity);
        itemStack.hurtAndBreak(1, entity, InteractionHand.MAIN_HAND);

        return itemStack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        ItemStack input = user.getOffhandItem();

        if (user.level() instanceof ServerLevel level)
            return level.recipeAccess()
                .getRecipeFor(WireDrawplateRecipe.Type.INSTANCE,
                    new SingleItemStackRecipeInput(input), level)
                .map(holder -> holder.value().duration())
                .orElse(0);

        return WireDrawplateClientCache.findDuration(input);
    }
}
