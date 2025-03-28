package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record RotaryKilnRecipe(IndustriaIngredient input, OutputItemStack output, int requiredTemperature) implements Recipe<SingleItemStackRecipeInput> {
    @Override
    public boolean matches(SingleItemStackRecipeInput input, World world) {
        return this.input.testForRecipe(input.stack());
    }

    @Override
    public ItemStack craft(SingleItemStackRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return this.output.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public String getGroup() {
        return Industria.id("rotary_kiln").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleItemStackRecipeInput>> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<? extends Recipe<SingleItemStackRecipeInput>> getType() {
        return null;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return null;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return Recipe.super.getDisplays();
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    public static class Serializer implements RecipeSerializer<RotaryKilnRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<RotaryKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(RotaryKilnRecipe::input),
                OutputItemStack.CODEC.fieldOf("output").forGetter(RotaryKilnRecipe::output),
                Codec.INT.fieldOf("required_temperature").forGetter(RotaryKilnRecipe::requiredTemperature)
        ).apply(instance, RotaryKilnRecipe::new));

        public static final PacketCodec<RegistryByteBuf, RotaryKilnRecipe> PACKET_CODEC = PacketCodec.tuple(
                IndustriaIngredient.PACKET_CODEC, RotaryKilnRecipe::input,
                OutputItemStack.PACKET_CODEC, RotaryKilnRecipe::output,
                PacketCodecs.INTEGER, RotaryKilnRecipe::requiredTemperature,
                RotaryKilnRecipe::new);

        @Override
        public MapCodec<RotaryKilnRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, RotaryKilnRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }

    public static class Type implements RecipeType<RotaryKilnRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("rotary_kiln").toString();
        }
    }
}
