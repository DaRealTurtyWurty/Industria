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
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record ClarifierRecipe(FluidStack inputFluid, FluidStack outputFluidStack, OutputItemStack outputItemStack,
                              int processTime) implements Recipe<ClarifierRecipeInput> {
    @Override
    public boolean matches(ClarifierRecipeInput input, World world) {
        return input.fluidStack().matches(inputFluid);
    }

    @Override
    public ItemStack craft(ClarifierRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return outputItemStack().createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.CLARIFIER;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new ClarifierRecipeDisplay(
                this.inputFluid,
                new SlotDisplay.ItemSlotDisplay(BlockInit.CLARIFIER.asItem()),
                this.outputFluidStack,
                this.outputItemStack,
                this.processTime
        ));
    }

    @Override
    public String getGroup() {
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

        private static final PacketCodec<RegistryByteBuf, ClarifierRecipe> PACKET_CODEC =
                PacketCodec.tuple(
                        FluidStack.PACKET_CODEC, ClarifierRecipe::inputFluid,
                        FluidStack.PACKET_CODEC, ClarifierRecipe::outputFluidStack,
                        OutputItemStack.PACKET_CODEC, ClarifierRecipe::outputItemStack,
                        PacketCodecs.INTEGER, ClarifierRecipe::processTime,
                        ClarifierRecipe::new);

        @Override
        public MapCodec<ClarifierRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ClarifierRecipe> packetCodec() {
            return PACKET_CODEC;
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

        private static final PacketCodec<RegistryByteBuf, ClarifierRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                FluidStack.PACKET_CODEC, ClarifierRecipeDisplay::inputFluid,
                SlotDisplay.PACKET_CODEC, ClarifierRecipeDisplay::craftingStation,
                FluidStack.PACKET_CODEC, ClarifierRecipeDisplay::outputFluid,
                OutputItemStack.PACKET_CODEC, ClarifierRecipeDisplay::outputItem,
                PacketCodecs.INTEGER, ClarifierRecipeDisplay::processTime,
                ClarifierRecipeDisplay::new);

        public static final Serializer<ClarifierRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return SlotDisplay.EmptySlotDisplay.INSTANCE;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
