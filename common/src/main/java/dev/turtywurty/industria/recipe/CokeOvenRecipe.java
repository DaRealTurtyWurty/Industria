package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record CokeOvenRecipe(
    IndustriaIngredient input,
    ItemStackTemplate outputTemplate,
    FluidStack outputFluid,
    int requiredTemperature,
    int processTime
) implements Recipe<SingleItemStackRecipeInput> {
    @Override
    public boolean matches(SingleItemStackRecipeInput input, Level world) {
        return this.input.testForRecipe(input.stack());
    }

    @Override
    public ItemStack assemble(SingleItemStackRecipeInput input) {
        return this.outputTemplate.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return Industria.id("coke_oven").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleItemStackRecipeInput>> getSerializer() {
        return ModRecipeSerializers.COKE_OVEN.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleItemStackRecipeInput>> getType() {
        return ModRecipeTypes.COKE_OVEN.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.COKE_OVEN.get();
    }

    public static class Type implements RecipeType<CokeOvenRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("coke_oven").toString();
        }
    }

    private static final MapCodec<CokeOvenRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        IndustriaIngredient.CODEC.fieldOf("input").forGetter(CokeOvenRecipe::input),
        ItemStackTemplate.CODEC.fieldOf("output_stack").forGetter(CokeOvenRecipe::outputTemplate),
        FluidStack.CODEC.fieldOf("output_fluid").forGetter(CokeOvenRecipe::outputFluid),
        Codec.INT.fieldOf("required_temperature").forGetter(CokeOvenRecipe::requiredTemperature),
        Codec.INT.fieldOf("process_time").forGetter(CokeOvenRecipe::processTime)).apply(instance, CokeOvenRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> STREAM_CODEC = StreamCodec.composite(
        IndustriaIngredient.STREAM_CODEC, CokeOvenRecipe::input,
        ItemStackTemplate.STREAM_CODEC, CokeOvenRecipe::outputTemplate,
        FluidStack.STREAM_CODEC, CokeOvenRecipe::outputFluid,
        ByteBufCodecs.INT, CokeOvenRecipe::requiredTemperature,
        ByteBufCodecs.INT, CokeOvenRecipe::processTime,
        CokeOvenRecipe::new);

    public static final RecipeSerializer<CokeOvenRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
