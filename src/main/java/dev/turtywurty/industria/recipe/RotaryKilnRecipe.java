package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record RotaryKilnRecipe(IndustriaIngredient input, OutputItemStack output,
                               int requiredTemperature) implements Recipe<SingleItemStackRecipeInput> {
    @Override
    public boolean matches(SingleItemStackRecipeInput input, Level world) {
        return this.input.testForRecipe(input.stack());
    }

    @Override
    public ItemStack assemble(SingleItemStackRecipeInput input, HolderLookup.Provider registries) {
        return this.output.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return Industria.id("rotary_kiln").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleItemStackRecipeInput>> getSerializer() {
        return RecipeSerializerInit.ROTARY_KILN;
    }

    @Override
    public RecipeType<? extends Recipe<SingleItemStackRecipeInput>> getType() {
        return RecipeTypeInit.ROTARY_KILN;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new RotaryKilnRecipeDisplay(
                        input.toDisplay(),
                        new SlotDisplay.ItemSlotDisplay(BlockInit.ROTARY_KILN_CONTROLLER.asItem()),
                        output.toDisplay(),
                        requiredTemperature)
        );
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.ROTARY_KILN;
    }

    public static class Serializer implements RecipeSerializer<RotaryKilnRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<RotaryKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(RotaryKilnRecipe::input),
                OutputItemStack.CODEC.fieldOf("output").forGetter(RotaryKilnRecipe::output),
                Codec.INT.fieldOf("required_temperature").forGetter(RotaryKilnRecipe::requiredTemperature)
        ).apply(instance, RotaryKilnRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RotaryKilnRecipe> STREAM_CODEC = StreamCodec.composite(
                IndustriaIngredient.STREAM_CODEC, RotaryKilnRecipe::input,
                OutputItemStack.STREAM_CODEC, RotaryKilnRecipe::output,
                ByteBufCodecs.INT, RotaryKilnRecipe::requiredTemperature,
                RotaryKilnRecipe::new);

        @Override
        public MapCodec<RotaryKilnRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RotaryKilnRecipe> streamCodec() {
            return STREAM_CODEC;
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

    public record RotaryKilnRecipeDisplay(SlotDisplay input, SlotDisplay craftingStation, SlotDisplay result,
                                          int requiredTemperature) implements RecipeDisplay {
        private static final MapCodec<RotaryKilnRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf("input").forGetter(RotaryKilnRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(RotaryKilnRecipeDisplay::craftingStation),
                SlotDisplay.CODEC.fieldOf("result").forGetter(RotaryKilnRecipeDisplay::result),
                Codec.INT.fieldOf("required_temperature").forGetter(RotaryKilnRecipeDisplay::requiredTemperature)
        ).apply(instance, RotaryKilnRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RotaryKilnRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, RotaryKilnRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, RotaryKilnRecipeDisplay::craftingStation,
                SlotDisplay.STREAM_CODEC, RotaryKilnRecipeDisplay::result,
                ByteBufCodecs.INT, RotaryKilnRecipeDisplay::requiredTemperature,
                RotaryKilnRecipeDisplay::new);

        private static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
