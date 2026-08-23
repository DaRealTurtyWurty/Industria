package dev.turtywurty.industria.init;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.consumeeffect.DestroyStomachConsumeEffect;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

public final class ModConsumeEffectTypes {
    private ModConsumeEffectTypes() {
    }

    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<ConsumeEffect.Type<?>, ConsumeEffect.Type<DestroyStomachConsumeEffect>> DESTROY_STOMACH =
            register("destroy_stomach", DestroyStomachConsumeEffect.CODEC, DestroyStomachConsumeEffect.STREAM_CODEC);

    public static <T extends ConsumeEffect> RegistrationHandle<ConsumeEffect.Type<?>, ConsumeEffect.Type<T>> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return REGISTRIES.registerConsumeEffectType(Industria.id(name), () -> new ConsumeEffect.Type<>(codec, streamCodec));
    }

    public static void init() {
    }
}
