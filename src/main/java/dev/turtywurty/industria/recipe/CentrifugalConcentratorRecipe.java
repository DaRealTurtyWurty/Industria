package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.CentrifugalConcentratorRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
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

public record CentrifugalConcentratorRecipe(IndustriaIngredient input, OutputItemStack output, SlurryStack outputSlurry,
                                            int processTime, int rpm) implements Recipe<CentrifugalConcentratorRecipeInput> {
    @Override
    public boolean matches(CentrifugalConcentratorRecipeInput input, Level world) {
        return this.input.testForRecipe(input.recipeInventory().getItem(0)) &&
                input.waterAmount() >= FluidConstants.BUCKET * 2;
    }

    @Override
    public ItemStack assemble(CentrifugalConcentratorRecipeInput input, HolderLookup.Provider registries) {
        return this.output.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<CentrifugalConcentratorRecipeInput>> getSerializer() {
        return RecipeSerializerInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public RecipeType<? extends Recipe<CentrifugalConcentratorRecipeInput>> getType() {
        return RecipeTypeInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CentrifugalConcentratorRecipeDisplay(
                this.input, new SlotDisplay.ItemSlotDisplay(BlockInit.CENTRIFUGAL_CONCENTRATOR.asItem()),
                this.output, this.outputSlurry,
                this.processTime, this.rpm));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public String group() {
        return Industria.id("centrifugal_concentrator").toString();
    }

    public static class Type implements RecipeType<CentrifugalConcentratorRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("centrifugal_concentrator").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<CentrifugalConcentratorRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        private static final MapCodec<CentrifugalConcentratorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(CentrifugalConcentratorRecipe::input),
                OutputItemStack.CODEC.fieldOf("output").forGetter(CentrifugalConcentratorRecipe::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(CentrifugalConcentratorRecipe::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(CentrifugalConcentratorRecipe::processTime),
                Codec.INT.fieldOf("rpm").forGetter(CentrifugalConcentratorRecipe::rpm)
        ).apply(instance, CentrifugalConcentratorRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CentrifugalConcentratorRecipe> STREAM_CODEC =
                StreamCodec.composite(IndustriaIngredient.STREAM_CODEC, CentrifugalConcentratorRecipe::input,
                        OutputItemStack.STREAM_CODEC, CentrifugalConcentratorRecipe::output,
                        SlurryStack.STREAM_CODEC, CentrifugalConcentratorRecipe::outputSlurry,
                        ByteBufCodecs.INT, CentrifugalConcentratorRecipe::processTime,
                        ByteBufCodecs.INT, CentrifugalConcentratorRecipe::rpm,
                        CentrifugalConcentratorRecipe::new);

        @Override
        public MapCodec<CentrifugalConcentratorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CentrifugalConcentratorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record CentrifugalConcentratorRecipeDisplay(IndustriaIngredient input, SlotDisplay craftingStation,
                                                       OutputItemStack output, SlurryStack outputSlurry,
                                                       int processTime, int rpm) implements RecipeDisplay {
        public static final MapCodec<CentrifugalConcentratorRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(CentrifugalConcentratorRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(CentrifugalConcentratorRecipeDisplay::craftingStation),
                OutputItemStack.CODEC.fieldOf("output").forGetter(CentrifugalConcentratorRecipeDisplay::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(CentrifugalConcentratorRecipeDisplay::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(CentrifugalConcentratorRecipeDisplay::processTime),
                Codec.INT.fieldOf("rpm").forGetter(CentrifugalConcentratorRecipeDisplay::rpm)
        ).apply(instance, CentrifugalConcentratorRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugalConcentratorRecipeDisplay> STREAM_CODEC =
                StreamCodec.composite(IndustriaIngredient.STREAM_CODEC, CentrifugalConcentratorRecipeDisplay::input,
                        SlotDisplay.STREAM_CODEC, CentrifugalConcentratorRecipeDisplay::craftingStation,
                        OutputItemStack.STREAM_CODEC, CentrifugalConcentratorRecipeDisplay::output,
                        SlurryStack.STREAM_CODEC, CentrifugalConcentratorRecipeDisplay::outputSlurry,
                        ByteBufCodecs.INT, CentrifugalConcentratorRecipeDisplay::processTime,
                        ByteBufCodecs.INT, CentrifugalConcentratorRecipeDisplay::rpm,
                        CentrifugalConcentratorRecipeDisplay::new);

        public static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return craftingStation;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
