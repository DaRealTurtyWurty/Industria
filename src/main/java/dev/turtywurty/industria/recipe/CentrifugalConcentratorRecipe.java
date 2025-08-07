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

public record CentrifugalConcentratorRecipe(IndustriaIngredient input, OutputItemStack output, SlurryStack outputSlurry,
                                            int processTime, int rpm) implements Recipe<CentrifugalConcentratorRecipeInput> {
    @Override
    public boolean matches(CentrifugalConcentratorRecipeInput input, World world) {
        return this.input.testForRecipe(input.recipeInventory().getStackInSlot(0)) &&
                input.waterAmount() >= FluidConstants.BUCKET * 2;
    }

    @Override
    public ItemStack craft(CentrifugalConcentratorRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return this.output.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new CentrifugalConcentratorRecipeDisplay(
                this.input, new SlotDisplay.ItemSlotDisplay(BlockInit.CENTRIFUGAL_CONCENTRATOR.asItem()),
                this.output, this.outputSlurry,
                this.processTime, this.rpm));
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public String getGroup() {
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

        private static final PacketCodec<RegistryByteBuf, CentrifugalConcentratorRecipe> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, CentrifugalConcentratorRecipe::input,
                        OutputItemStack.PACKET_CODEC, CentrifugalConcentratorRecipe::output,
                        SlurryStack.PACKET_CODEC, CentrifugalConcentratorRecipe::outputSlurry,
                        PacketCodecs.INTEGER, CentrifugalConcentratorRecipe::processTime,
                        PacketCodecs.INTEGER, CentrifugalConcentratorRecipe::rpm,
                        CentrifugalConcentratorRecipe::new);

        @Override
        public MapCodec<CentrifugalConcentratorRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CentrifugalConcentratorRecipe> packetCodec() {
            return PACKET_CODEC;
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

        public static final PacketCodec<RegistryByteBuf, CentrifugalConcentratorRecipeDisplay> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, CentrifugalConcentratorRecipeDisplay::input,
                        SlotDisplay.PACKET_CODEC, CentrifugalConcentratorRecipeDisplay::craftingStation,
                        OutputItemStack.PACKET_CODEC, CentrifugalConcentratorRecipeDisplay::output,
                        SlurryStack.PACKET_CODEC, CentrifugalConcentratorRecipeDisplay::outputSlurry,
                        PacketCodecs.INTEGER, CentrifugalConcentratorRecipeDisplay::processTime,
                        PacketCodecs.INTEGER, CentrifugalConcentratorRecipeDisplay::rpm,
                        CentrifugalConcentratorRecipeDisplay::new);

        public static final Serializer<CentrifugalConcentratorRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

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
