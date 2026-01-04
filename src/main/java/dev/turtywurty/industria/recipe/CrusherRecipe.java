package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CrusherRecipe(IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, Level world) {
        return this.input.testForRecipe(input.getItem(0));
    }

    public Tuple<ItemStack, ItemStack> assemble(RecipeSimpleInventory input, RandomSource random) {
        return new Tuple<>(this.outputA.createStack(random), this.outputB.createStack(random));
    }

    @Override
    public ItemStack assemble(RecipeSimpleInventory input, HolderLookup.Provider lookup) {
        ItemStack stack = input.getItem(0);
        stack.shrink(this.input.stackData().count());
        input.setItem(0, stack);
        return this.outputA.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return RecipeSerializerInit.CRUSHER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return RecipeTypeInit.CRUSHER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.CRUSHER;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CrusherRecipeDisplay(
                input().toDisplay(),
                new SlotDisplay.Composite(List.of(this.outputA().toDisplay(), this.outputB().toDisplay())),
                new SlotDisplay.ItemSlotDisplay(BlockInit.CRUSHER.asItem()),
                processTime()
        ));
    }

    @Override
    public String group() {
        return Industria.id("crusher").toString();
    }

    public static class Type implements RecipeType<CrusherRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {}

        @Override
        public String toString() {
            return Industria.id("crusher").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<CrusherRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(CrusherRecipe::input),
                OutputItemStack.CODEC.fieldOf("output_a").forGetter(CrusherRecipe::outputA),
                OutputItemStack.CODEC.fieldOf("output_b").forGetter(CrusherRecipe::outputB),
                Codec.INT.fieldOf("process_time").forGetter(CrusherRecipe::processTime)
        ).apply(instance, CrusherRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> STREAM_CODEC =
                StreamCodec.composite(IndustriaIngredient.STREAM_CODEC, CrusherRecipe::input,
                        OutputItemStack.STREAM_CODEC, CrusherRecipe::outputA,
                        OutputItemStack.STREAM_CODEC, CrusherRecipe::outputB,
                        ByteBufCodecs.INT, CrusherRecipe::processTime,
                        CrusherRecipe::new);

        @Override
        public MapCodec<CrusherRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record CrusherRecipeDisplay(SlotDisplay input, SlotDisplay output, SlotDisplay craftingStation, int processTime) implements RecipeDisplay {
        public static final MapCodec<CrusherRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.fieldOf("input").forGetter(CrusherRecipeDisplay::input),
                        SlotDisplay.CODEC.fieldOf("output").forGetter(CrusherRecipeDisplay::output),
                        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(CrusherRecipeDisplay::craftingStation),
                        Codec.INT.fieldOf("process_time").forGetter(CrusherRecipeDisplay::processTime)
                ).apply(instance, CrusherRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, CrusherRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, CrusherRecipeDisplay::output,
                SlotDisplay.STREAM_CODEC, CrusherRecipeDisplay::craftingStation,
                ByteBufCodecs.INT, CrusherRecipeDisplay::processTime,
                CrusherRecipeDisplay::new);

        public static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
