package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.DigesterRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record DigesterRecipe(SlurryStack inputSlurry, FluidStack outputFluid,
                             int processTime) implements Recipe<DigesterRecipeInput> {
    @Override
    public boolean matches(DigesterRecipeInput input, Level world) {
        return input.slurryStack().matches(this.inputSlurry);
    }

    @Override
    public ItemStack assemble(DigesterRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<DigesterRecipeInput>> getSerializer() {
        return RecipeSerializerInit.DIGESTER;
    }

    @Override
    public RecipeType<? extends Recipe<DigesterRecipeInput>> getType() {
        return RecipeTypeInit.DIGESTER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.DIGESTER;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new DigesterRecipeDisplay(
                this.inputSlurry,
                new SlotDisplay.ItemSlotDisplay(BlockInit.DIGESTER.asItem()),
                this.outputFluid,
                this.processTime
        ));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return Industria.id("digester").toString();
    }

    public static class Type implements RecipeType<DigesterRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("digester").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<DigesterRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<DigesterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlurryStack.CODEC.fieldOf("input_slurry").forGetter(DigesterRecipe::inputSlurry),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(DigesterRecipe::outputFluid),
                Codec.INT.fieldOf("process_time").forGetter(DigesterRecipe::processTime)
        ).apply(instance, DigesterRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DigesterRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        SlurryStack.STREAM_CODEC, DigesterRecipe::inputSlurry,
                        FluidStack.STREAM_CODEC, DigesterRecipe::outputFluid,
                        ByteBufCodecs.INT, DigesterRecipe::processTime,
                        DigesterRecipe::new);

        @Override
        public MapCodec<DigesterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DigesterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record DigesterRecipeDisplay(SlurryStack inputSlurry, SlotDisplay craftingStation, FluidStack outputFluid,
                                        int processTime) implements RecipeDisplay {
        public static final MapCodec<DigesterRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlurryStack.CODEC.fieldOf("input_slurry").forGetter(DigesterRecipeDisplay::inputSlurry),
                        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(DigesterRecipeDisplay::craftingStation),
                        FluidStack.CODEC.fieldOf("output_fluid").forGetter(DigesterRecipeDisplay::outputFluid),
                        Codec.INT.fieldOf("process_time").forGetter(DigesterRecipeDisplay::processTime)
                ).apply(instance, DigesterRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DigesterRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlurryStack.STREAM_CODEC, DigesterRecipeDisplay::inputSlurry,
                SlotDisplay.STREAM_CODEC, DigesterRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, DigesterRecipeDisplay::outputFluid,
                ByteBufCodecs.INT, DigesterRecipeDisplay::processTime,
                DigesterRecipeDisplay::new);

        public static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return SlotDisplay.Empty.INSTANCE;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
