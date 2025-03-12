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
import net.minecraft.world.World;

import java.util.List;

public record DigesterRecipe(SlurryStack inputSlurry, FluidStack outputFluid,
                             int processTime) implements Recipe<DigesterRecipeInput> {
    @Override
    public boolean matches(DigesterRecipeInput input, World world) {
        return input.slurryStack().matches(this.inputSlurry);
    }

    @Override
    public ItemStack craft(DigesterRecipeInput input, RegistryWrapper.WrapperLookup registries) {
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.DIGESTER;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new DigesterRecipeDisplay(
                this.inputSlurry,
                new SlotDisplay.ItemSlotDisplay(BlockInit.DIGESTER.asItem()),
                this.outputFluid,
                this.processTime
        ));
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public String getGroup() {
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

        private static final PacketCodec<RegistryByteBuf, DigesterRecipe> PACKET_CODEC =
                PacketCodec.tuple(
                        SlurryStack.PACKET_CODEC, DigesterRecipe::inputSlurry,
                        FluidStack.PACKET_CODEC, DigesterRecipe::outputFluid,
                        PacketCodecs.INTEGER, DigesterRecipe::processTime,
                        DigesterRecipe::new);

        @Override
        public MapCodec<DigesterRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, DigesterRecipe> packetCodec() {
            return PACKET_CODEC;
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

        public static final PacketCodec<RegistryByteBuf, DigesterRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                SlurryStack.PACKET_CODEC, DigesterRecipeDisplay::inputSlurry,
                SlotDisplay.PACKET_CODEC, DigesterRecipeDisplay::craftingStation,
                FluidStack.PACKET_CODEC, DigesterRecipeDisplay::outputFluid,
                PacketCodecs.INTEGER, DigesterRecipeDisplay::processTime,
                DigesterRecipeDisplay::new);

        public static final Serializer<DigesterRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

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
