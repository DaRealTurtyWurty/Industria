package dev.turtywurty.industria.init;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.consumeeffect.DestroyStomachConsumeEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

public final class ConsumeEffectTypeInit {
    private ConsumeEffectTypeInit() {
    }

    public static final ConsumeEffect.Type<DestroyStomachConsumeEffect> DESTROY_STOMACH =
            register("destroy_stomach", DestroyStomachConsumeEffect.CODEC, DestroyStomachConsumeEffect.STREAM_CODEC);

    public static <T extends ConsumeEffect> ConsumeEffect.Type<T> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return Registry.register(BuiltInRegistries.CONSUME_EFFECT_TYPE, Industria.id(name), new ConsumeEffect.Type<>(codec, streamCodec));
    }

    public static void init() {
    }
}
