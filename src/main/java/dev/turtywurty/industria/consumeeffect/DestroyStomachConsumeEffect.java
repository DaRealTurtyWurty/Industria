package dev.turtywurty.industria.consumeeffect;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.ConsumeEffectTypeInit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public class DestroyStomachConsumeEffect implements ConsumeEffect {
    public static final MapCodec<DestroyStomachConsumeEffect> CODEC = MapCodec.unit(DestroyStomachConsumeEffect::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DestroyStomachConsumeEffect> STREAM_CODEC =
            StreamCodec.unit(new DestroyStomachConsumeEffect());

    public static final Identifier STOMACH_DESTRUCTION_HEALTH_MODIFIER = Industria.id("stomach_destruction_health");

    @Override
    public Type<? extends ConsumeEffect> getType() {
        return ConsumeEffectTypeInit.DESTROY_STOMACH;
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity user) {
        if (!(user instanceof Player player) || level.isClientSide())
            return false;

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null)
            return false;

        int destructionLevel = player.getAttachedOrElse(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, 0) + 1;
        player.setAttached(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, destructionLevel);

        int maxFood = 20 - destructionLevel * 2;
        if (maxFood <= 0) {
            player.hurtServer((ServerLevel) level, player.damageSources().starve(), Float.MAX_VALUE);
            return true;
        }

        maxHealth.removeModifier(STOMACH_DESTRUCTION_HEALTH_MODIFIER);

        double amount = -2.0 * destructionLevel;
        if (amount != 0.0) {
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(
                    STOMACH_DESTRUCTION_HEALTH_MODIFIER,
                    amount,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        if (player.getHealth() <= 0 || maxHealth.getBaseValue() <= 0) {
            player.hurtServer((ServerLevel) level, player.damageSources().starve(), Float.MAX_VALUE);
            return true;
        }

        return true;
    }
}
