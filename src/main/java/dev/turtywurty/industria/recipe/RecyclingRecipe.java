package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record RecyclingRecipe(IndustriaIngredient input, List<OutputItemStack> outputs, int processTime)
        implements Recipe<SingleRecipeInput> {
    @Override
    public boolean matches(SingleRecipeInput input, Level world) {
        return this.input.testForRecipe(input.getItem(0));
    }

    public List<ItemStack> assemble(RandomSource random) {
        return this.outputs.stream().map(output -> output.createStack(random)).toList();
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack stack = input.getItem(0);
        stack.shrink(this.input.stackData().count());
        return this.outputs.getFirst().createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return RecipeSerializerInit.RECYCLING;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return RecipeTypeInit.RECYCLING;
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
    public boolean showNotification() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.RECYCLING;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecyclingRecipeDisplay(
                input().toDisplay(),
                new SlotDisplay.Composite(this.outputs().stream().map(OutputItemStack::toDisplay).toList()),
                new SlotDisplay.ItemSlotDisplay(BlockInit.ARC_FURNACE.asItem()), // TODO: Something else(?)
                processTime()
        ));
    }

    @Override
    public String group() {
        return Industria.id("recycling").toString();
    }

    public static class Type implements RecipeType<RecyclingRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("recycling").toString();
        }
    }

    private static final MapCodec<RecyclingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IndustriaIngredient.CODEC.fieldOf("input").forGetter(RecyclingRecipe::input),
            ExtraCodecs.listOf(OutputItemStack.CODEC).fieldOf("outputs").forGetter(RecyclingRecipe::outputs),
            Codec.INT.fieldOf("process_time").forGetter(RecyclingRecipe::processTime)
    ).apply(instance, RecyclingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, RecyclingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    IndustriaIngredient.STREAM_CODEC, RecyclingRecipe::input,
                    ExtraStreamCodecs.listOf(OutputItemStack.STREAM_CODEC), RecyclingRecipe::outputs,
                    ByteBufCodecs.INT, RecyclingRecipe::processTime,
                    RecyclingRecipe::new
            );

    public static final RecipeSerializer<RecyclingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record RecyclingRecipeDisplay(SlotDisplay input, SlotDisplay output, SlotDisplay craftingStation, int processTime)
            implements RecipeDisplay {
        public static final MapCodec<RecyclingRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.fieldOf("input").forGetter(RecyclingRecipeDisplay::input),
                        SlotDisplay.CODEC.fieldOf("output").forGetter(RecyclingRecipeDisplay::output),
                        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(RecyclingRecipeDisplay::craftingStation),
                        Codec.INT.fieldOf("process_time").forGetter(RecyclingRecipeDisplay::processTime)
                ).apply(instance, RecyclingRecipeDisplay::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, RecyclingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, RecyclingRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, RecyclingRecipeDisplay::output,
                SlotDisplay.STREAM_CODEC, RecyclingRecipeDisplay::craftingStation,
                ByteBufCodecs.INT, RecyclingRecipeDisplay::processTime,
                RecyclingRecipeDisplay::new
        );

        public static final Type<RecyclingRecipeDisplay> SERIALIZER = new Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public Type<RecyclingRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
