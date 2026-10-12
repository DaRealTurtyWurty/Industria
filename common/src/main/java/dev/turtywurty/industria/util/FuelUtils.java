package dev.turtywurty.industria.util;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;

/** Resolves the reloadable fuel component that replaced the level fuel registry. */
public final class FuelUtils {
    private FuelUtils() {}

    public static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.COOKING_FUEL);
    }

    public static int burnDuration(Level level, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || !isFuel(stack)) return 0;
        LootParams params = new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
            .create(LootContextParamSets.COMMAND);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        return Math.max(0, ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0));
    }
}
