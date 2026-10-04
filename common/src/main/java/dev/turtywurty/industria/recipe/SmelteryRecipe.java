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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record SmelteryRecipe(IndustriaIngredient input, FluidStack output, int requiredTemperature,
                             int preferredTemperature, float maxSpeedMultiplier, int processTime)
        implements Recipe<SingleItemStackRecipeInput> {
    @Override
    public boolean matches(SingleItemStackRecipeInput input, Level world) {
        return this.input.testForRecipe(input.stack());
    }

    @Override
    public ItemStack assemble(SingleItemStackRecipeInput input) {
        return ItemStack.EMPTY;
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
        return Industria.id("smeltery").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleItemStackRecipeInput>> getSerializer() {
        return ModRecipeSerializers.SMELTERY.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleItemStackRecipeInput>> getType() {
        return ModRecipeTypes.SMELTERY.get();
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
        return ModRecipeBookCategories.SMELTERY.get();
    }

    public double getSpeedMultiplier(float temperature) {
        if(temperature < this.requiredTemperature)
            return 0.0;

        double fraction = Math.clamp((temperature - (double)this.requiredTemperature)
                / (this.preferredTemperature - (double) this.requiredTemperature), 0.0, 1.0);

        return 1.0 + fraction * (this.maxSpeedMultiplier - 1.0);
    }

    public static class Type implements RecipeType<SmelteryRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("smeltery").toString();
        }
    }

    private static final MapCodec<SmelteryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IndustriaIngredient.CODEC.fieldOf("input").forGetter(SmelteryRecipe::input),
            FluidStack.CODEC.fieldOf("output").forGetter(SmelteryRecipe::output),
            Codec.INT.fieldOf("required_temperature").forGetter(SmelteryRecipe::requiredTemperature),
            Codec.INT.fieldOf("preferred_temperature").forGetter(SmelteryRecipe::preferredTemperature),
            Codec.FLOAT.fieldOf("max_speed_multiplier").forGetter(SmelteryRecipe::maxSpeedMultiplier),
            Codec.INT.fieldOf("process_time").forGetter(SmelteryRecipe::processTime)
    ).apply(instance, SmelteryRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> STREAM_CODEC = StreamCodec.composite(
            IndustriaIngredient.STREAM_CODEC, SmelteryRecipe::input,
            FluidStack.STREAM_CODEC, SmelteryRecipe::output,
            ByteBufCodecs.INT, SmelteryRecipe::requiredTemperature,
            ByteBufCodecs.INT, SmelteryRecipe::preferredTemperature,
            ByteBufCodecs.FLOAT, SmelteryRecipe::maxSpeedMultiplier,
            ByteBufCodecs.INT, SmelteryRecipe::processTime,
            SmelteryRecipe::new
    );

    public static final RecipeSerializer<SmelteryRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
