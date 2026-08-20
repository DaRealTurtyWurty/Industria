package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.consumeeffect.DestroyStomachConsumeEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

public final class FoodList {
    private FoodList() {
    }

    public static final FoodProperties FORMIC_ACID_FOOD = new FoodProperties.Builder().alwaysEdible().build();
    public static final Consumable FORMIC_ACID_CONSUMABLE = Consumable.builder()
            .animation(ItemUseAnimation.DRINK)
            .onConsume(new DestroyStomachConsumeEffect())
            .build();
}
