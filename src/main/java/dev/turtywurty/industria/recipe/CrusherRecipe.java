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
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CrusherRecipe(IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, World world) {
        return this.input.testForRecipe(input.getStack(0));
    }

    public Pair<ItemStack, ItemStack> assemble(RecipeSimpleInventory input, Random random) {
        return new Pair<>(this.outputA.createStack(random), this.outputB.createStack(random));
    }

    @Override
    public ItemStack craft(RecipeSimpleInventory input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack stack = input.getStack(0);
        stack.decrement(this.input.stackData().count());
        input.setStack(0, stack);
        return this.outputA.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.CRUSHER;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new CrusherRecipeDisplay(
                input().toDisplay(),
                new SlotDisplay.CompositeSlotDisplay(List.of(this.outputA().toDisplay(), this.outputB().toDisplay())),
                new SlotDisplay.ItemSlotDisplay(BlockInit.CRUSHER.asItem()),
                processTime()
        ));
    }

    @Override
    public String getGroup() {
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

        private static final PacketCodec<RegistryByteBuf, CrusherRecipe> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, CrusherRecipe::input,
                        OutputItemStack.PACKET_CODEC, CrusherRecipe::outputA,
                        OutputItemStack.PACKET_CODEC, CrusherRecipe::outputB,
                        PacketCodecs.INTEGER, CrusherRecipe::processTime,
                        CrusherRecipe::new);

        @Override
        public MapCodec<CrusherRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CrusherRecipe> packetCodec() {
            return PACKET_CODEC;
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

        public static final PacketCodec<RegistryByteBuf, CrusherRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                SlotDisplay.PACKET_CODEC, CrusherRecipeDisplay::input,
                SlotDisplay.PACKET_CODEC, CrusherRecipeDisplay::output,
                SlotDisplay.PACKET_CODEC, CrusherRecipeDisplay::craftingStation,
                PacketCodecs.INTEGER, CrusherRecipeDisplay::processTime,
                CrusherRecipeDisplay::new);

        public static final Serializer<CrusherRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
