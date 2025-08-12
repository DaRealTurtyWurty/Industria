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
import dev.turtywurty.industria.recipe.input.ShakingTableRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
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

public record ShakingTableRecipe(IndustriaIngredient input, OutputItemStack output, SlurryStack outputSlurry,
                                 int processTime, int frequency) implements Recipe<ShakingTableRecipeInput> {
    @Override
    public boolean matches(ShakingTableRecipeInput input, World world) {
        return this.input.testForRecipe(input.recipeInventory().getStackInSlot(0)) &&
                input.waterAmount() >= FluidConstants.BUCKET * 2;
    }

    @Override
    public ItemStack craft(ShakingTableRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return this.output.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<ShakingTableRecipeInput>> getSerializer() {
        return RecipeSerializerInit.SHAKING_TABLE;
    }

    @Override
    public RecipeType<? extends Recipe<ShakingTableRecipeInput>> getType() {
        return RecipeTypeInit.SHAKING_TABLE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new ShakingTableRecipeDisplay(
                this.input, new SlotDisplay.ItemSlotDisplay(BlockInit.SHAKING_TABLE.asItem()),
                this.output, this.outputSlurry,
                this.processTime, this.frequency));
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.SHAKING_TABLE;
    }

    @Override
    public String getGroup() {
        return Industria.id("shaking_table").toString();
    }

    public static class Type implements RecipeType<ShakingTableRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("shaking_table").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<ShakingTableRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<ShakingTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(ShakingTableRecipe::input),
                OutputItemStack.CODEC.fieldOf("output").forGetter(ShakingTableRecipe::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(ShakingTableRecipe::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(ShakingTableRecipe::processTime),
                Codec.INT.fieldOf("frequency").forGetter(ShakingTableRecipe::frequency)
        ).apply(instance, ShakingTableRecipe::new));
        private static final PacketCodec<RegistryByteBuf, ShakingTableRecipe> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, ShakingTableRecipe::input,
                        OutputItemStack.PACKET_CODEC, ShakingTableRecipe::output,
                        SlurryStack.PACKET_CODEC, ShakingTableRecipe::outputSlurry,
                        PacketCodecs.INTEGER, ShakingTableRecipe::processTime,
                        PacketCodecs.INTEGER, ShakingTableRecipe::frequency,
                        ShakingTableRecipe::new);

        private Serializer() {
        }

        @Override
        public MapCodec<ShakingTableRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShakingTableRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }

    public record ShakingTableRecipeDisplay(IndustriaIngredient input, SlotDisplay craftingStation,
                                            OutputItemStack output, SlurryStack outputSlurry,
                                            int processTime, int frequency) implements RecipeDisplay {
        public static final MapCodec<ShakingTableRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(ShakingTableRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ShakingTableRecipeDisplay::craftingStation),
                OutputItemStack.CODEC.fieldOf("output").forGetter(ShakingTableRecipeDisplay::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(ShakingTableRecipeDisplay::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(ShakingTableRecipeDisplay::processTime),
                Codec.INT.fieldOf("frequency").forGetter(ShakingTableRecipeDisplay::frequency)
        ).apply(instance, ShakingTableRecipeDisplay::new));

        public static final PacketCodec<RegistryByteBuf, ShakingTableRecipeDisplay> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, ShakingTableRecipeDisplay::input,
                        SlotDisplay.PACKET_CODEC, ShakingTableRecipeDisplay::craftingStation,
                        OutputItemStack.PACKET_CODEC, ShakingTableRecipeDisplay::output,
                        SlurryStack.PACKET_CODEC, ShakingTableRecipeDisplay::outputSlurry,
                        PacketCodecs.INTEGER, ShakingTableRecipeDisplay::processTime,
                        PacketCodecs.INTEGER, ShakingTableRecipeDisplay::frequency,
                        ShakingTableRecipeDisplay::new);

        public static final Serializer<ShakingTableRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return craftingStation;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
