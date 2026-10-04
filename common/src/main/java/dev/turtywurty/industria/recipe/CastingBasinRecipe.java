package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.CastingBasinRecipeInput;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CastingBasinRecipe(FluidStack inputFluid, OutputItemStack output, int coolingTime)
        implements Recipe<CastingBasinRecipeInput> {
    @Override
    public boolean matches(CastingBasinRecipeInput input, Level world) {
        return input.fluidStack().testForRecipe(this.inputFluid);
    }

    @Override
    public ItemStack assemble(CastingBasinRecipeInput input) {
        return this.output.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
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
        return Industria.id("casting_basin").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CastingBasinRecipeInput>> getSerializer() {
        return ModRecipeSerializers.CASTING_BASIN.get();
    }

    @Override
    public RecipeType<? extends Recipe<CastingBasinRecipeInput>> getType() {
        return ModRecipeTypes.CASTING_BASIN.get();
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
        return ModRecipeBookCategories.CASTING_BASIN.get();
    }

    public static class Type implements RecipeType<CastingBasinRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("casting_basin").toString();
        }
    }

    private static final MapCodec<CastingBasinRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(CastingBasinRecipe::inputFluid),
            OutputItemStack.CODEC.fieldOf("output").forGetter(CastingBasinRecipe::output),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("cooling_time").forGetter(CastingBasinRecipe::coolingTime)
    ).apply(instance, CastingBasinRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CastingBasinRecipe> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, CastingBasinRecipe::inputFluid,
            OutputItemStack.STREAM_CODEC, CastingBasinRecipe::output,
            ByteBufCodecs.INT, CastingBasinRecipe::coolingTime,
            CastingBasinRecipe::new
    );

    public static final RecipeSerializer<CastingBasinRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
