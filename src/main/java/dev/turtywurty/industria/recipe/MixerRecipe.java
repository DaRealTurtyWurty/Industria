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
import dev.turtywurty.industria.recipe.input.MixerRecipeInput;
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
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public record MixerRecipe(List<IndustriaIngredient> inputs, FluidStack inputFluid, int minTemp, int maxTemp,
                          OutputItemStack output, SlurryStack outputSlurry,
                          int processTime) implements Recipe<MixerRecipeInput> {
    @Override
    public boolean matches(MixerRecipeInput input, World world) {
        if(!input.fluidStack().matches(this.inputFluid) || input.temperature() < this.minTemp || input.temperature() > this.maxTemp)
            return false;

        List<IndustriaIngredient> remainingIngredients = new ArrayList<>(this.inputs);
        List<ItemStack> presentItems = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                presentItems.stream()
                        .filter(itemStack -> itemStack.isOf(stack.getItem()))
                        .findFirst()
                        .ifPresentOrElse(
                                stack1 -> stack1.copyWithCount(stack.getCount() + 1),
                                () -> presentItems.add(stack));
            }
        }

        if (presentItems.size() != this.inputs.size())
            return false;

        for (ItemStack stack : presentItems) {
            boolean matched = false;
            for (IndustriaIngredient ingredient : remainingIngredients) {
                if (ingredient.testForRecipe(stack)) {
                    remainingIngredients.remove(ingredient);
                    matched = true;
                    break;
                }
            }

            if (!matched)
                return false;
        }

        return remainingIngredients.isEmpty();
    }

    @Override
    public ItemStack craft(MixerRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        Map<IndustriaIngredient, Integer> ingredientCounts = this.inputs.stream()
                .collect(HashMap::new,
                        (map, ingredient) ->
                                map.put(ingredient, ingredient.stackData().count()),
                        Map::putAll);

        for (IndustriaIngredient ingredient : this.inputs) {
            for (int slot = 0; slot < input.size(); slot++) {
                ItemStack stack = input.getStackInSlot(slot);
                if (ingredient.test(stack, false, false)) {
                    int stackSize = stack.getCount();
                    int count = ingredientCounts.get(ingredient);
                    if (stackSize > count) {
                        stack.decrement(count);
                        input.recipeInventory().setStack(slot, stack);
                        ingredientCounts.put(ingredient, 0);
                    } else {
                        ingredientCounts.put(ingredient, count - stackSize);
                        input.recipeInventory().setStack(slot, ItemStack.EMPTY);
                    }
                }
            }
        }

        return this.output.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
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
        return RecipeBookCategoryInit.MIXER;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new MixerRecipeDisplay(
                this.inputs.stream().map(IndustriaIngredient::toDisplay).toList(),
                new SlotDisplay.ItemSlotDisplay(BlockInit.MIXER.asItem()),
                this.inputFluid,
                this.minTemp,
                this.maxTemp,
                this.output.toDisplay(),
                this.outputSlurry,
                this.processTime
        ));
    }

    @Override
    public RecipeSerializer<? extends Recipe<MixerRecipeInput>> getSerializer() {
        return RecipeSerializerInit.MIXER;
    }

    @Override
    public RecipeType<? extends Recipe<MixerRecipeInput>> getType() {
        return RecipeTypeInit.MIXER;
    }

    @Override
    public String getGroup() {
        return Industria.id("mixer").toString();
    }

    public static class Type implements RecipeType<MixerRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("mixer").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<MixerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<MixerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.listOf().fieldOf("inputs").forGetter(MixerRecipe::inputs),
                FluidStack.CODEC.fieldOf("input_fluid").forGetter(MixerRecipe::inputFluid),
                Codec.INT.fieldOf("min_temp").forGetter(MixerRecipe::minTemp),
                Codec.INT.fieldOf("max_temp").forGetter(MixerRecipe::maxTemp),
                OutputItemStack.CODEC.fieldOf("output").forGetter(MixerRecipe::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(MixerRecipe::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(MixerRecipe::processTime)
        ).apply(instance, MixerRecipe::new));

        private static final PacketCodec<RegistryByteBuf, MixerRecipe> PACKET_CODEC =
                PacketCodec.tuple(PacketCodecs.collection(ArrayList::new, IndustriaIngredient.PACKET_CODEC), MixerRecipe::inputs,
                        FluidStack.PACKET_CODEC, MixerRecipe::inputFluid,
                        PacketCodecs.INTEGER, MixerRecipe::minTemp,
                        PacketCodecs.INTEGER, MixerRecipe::maxTemp,
                        OutputItemStack.PACKET_CODEC, MixerRecipe::output,
                        SlurryStack.PACKET_CODEC, MixerRecipe::outputSlurry,
                        PacketCodecs.INTEGER, MixerRecipe::processTime,
                        MixerRecipe::new);

        @Override
        public MapCodec<MixerRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, MixerRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }

    public record MixerRecipeDisplay(List<SlotDisplay> inputs, SlotDisplay craftingStation,
                                     FluidStack fluid, int minTemp, int maxTemp,
                                     SlotDisplay output, SlurryStack outputSlurry,
                                     int processTime) implements RecipeDisplay {
        public static final MapCodec<MixerRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.listOf().fieldOf("inputs").forGetter(MixerRecipeDisplay::inputs),
                        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(MixerRecipeDisplay::craftingStation),
                        FluidStack.CODEC.fieldOf("input_fluid").forGetter(MixerRecipeDisplay::fluid),
                        Codec.INT.fieldOf("min_temp").forGetter(MixerRecipeDisplay::minTemp),
                        Codec.INT.fieldOf("max_temp").forGetter(MixerRecipeDisplay::maxTemp),
                        SlotDisplay.CODEC.fieldOf("output").forGetter(MixerRecipeDisplay::output),
                        SlurryStack.CODEC.fieldOf("output_slurry").forGetter(MixerRecipeDisplay::outputSlurry),
                        Codec.INT.fieldOf("process_time").forGetter(MixerRecipeDisplay::processTime)
                ).apply(instance, MixerRecipeDisplay::new)
        );

        public static final PacketCodec<RegistryByteBuf, MixerRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.collection(ArrayList::new, SlotDisplay.PACKET_CODEC), MixerRecipeDisplay::inputs,
                SlotDisplay.PACKET_CODEC, MixerRecipeDisplay::craftingStation,
                FluidStack.PACKET_CODEC, MixerRecipeDisplay::fluid,
                PacketCodecs.INTEGER, MixerRecipeDisplay::minTemp,
                PacketCodecs.INTEGER, MixerRecipeDisplay::maxTemp,
                SlotDisplay.PACKET_CODEC, MixerRecipeDisplay::output,
                SlurryStack.PACKET_CODEC, MixerRecipeDisplay::outputSlurry,
                PacketCodecs.INTEGER, MixerRecipeDisplay::processTime,
                MixerRecipeDisplay::new
        );

        public static final Serializer<MixerRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}