package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.ClarifierRecipeInput;
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

public record ClarifierRecipe(FluidStack inputFluid, FluidStack outputFluidStack, OutputItemStack outputItemStack,
                              int processTime) implements Recipe<ClarifierRecipeInput> {
    @Override
    public boolean matches(ClarifierRecipeInput input, Level world) {
        return input.fluidStack().matches(inputFluid);
    }

    @Override
    public ItemStack assemble(ClarifierRecipeInput input, HolderLookup.Provider registries) {
        return outputItemStack().createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<ClarifierRecipeInput>> getSerializer() {
        return RecipeSerializerInit.CLARIFIER;
    }

    @Override
    public RecipeType<? extends Recipe<ClarifierRecipeInput>> getType() {
        return RecipeTypeInit.CLARIFIER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.CLARIFIER;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ClarifierRecipeDisplay(
                this.inputFluid,
                new SlotDisplay.ItemSlotDisplay(BlockInit.CLARIFIER.asItem()),
                this.outputFluidStack,
                this.outputItemStack,
                this.processTime
        ));
    }

    @Override
    public String group() {
        return Industria.id("clarifier").toString();
    }

    public static class Type implements RecipeType<ClarifierRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("clarifier").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<ClarifierRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<ClarifierRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("input_fluid").forGetter(ClarifierRecipe::inputFluid),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ClarifierRecipe::outputFluidStack),
                OutputItemStack.CODEC.fieldOf("output_item").forGetter(ClarifierRecipe::outputItemStack),
                Codec.INT.fieldOf("process_time").forGetter(ClarifierRecipe::processTime)
        ).apply(instance, ClarifierRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ClarifierRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        FluidStack.STREAM_CODEC, ClarifierRecipe::inputFluid,
                        FluidStack.STREAM_CODEC, ClarifierRecipe::outputFluidStack,
                        OutputItemStack.STREAM_CODEC, ClarifierRecipe::outputItemStack,
                        ByteBufCodecs.INT, ClarifierRecipe::processTime,
                        ClarifierRecipe::new);

        @Override
        public MapCodec<ClarifierRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClarifierRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record ClarifierRecipeDisplay(FluidStack inputFluid, SlotDisplay craftingStation, FluidStack outputFluid,
                                         OutputItemStack outputItem, int processTime) implements RecipeDisplay {
        private static final MapCodec<ClarifierRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("input_fluid").forGetter(ClarifierRecipeDisplay::inputFluid),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ClarifierRecipeDisplay::craftingStation),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ClarifierRecipeDisplay::outputFluid),
                OutputItemStack.CODEC.fieldOf("output_item").forGetter(ClarifierRecipeDisplay::outputItem),
                Codec.INT.fieldOf("process_time").forGetter(ClarifierRecipeDisplay::processTime)
        ).apply(instance, ClarifierRecipeDisplay::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ClarifierRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                FluidStack.STREAM_CODEC, ClarifierRecipeDisplay::inputFluid,
                SlotDisplay.STREAM_CODEC, ClarifierRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, ClarifierRecipeDisplay::outputFluid,
                OutputItemStack.STREAM_CODEC, ClarifierRecipeDisplay::outputItem,
                ByteBufCodecs.INT, ClarifierRecipeDisplay::processTime,
                ClarifierRecipeDisplay::new);

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
