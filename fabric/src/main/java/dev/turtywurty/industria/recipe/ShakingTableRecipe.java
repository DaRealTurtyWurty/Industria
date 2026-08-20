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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record ShakingTableRecipe(IndustriaIngredient input, OutputItemStack output, SlurryStack outputSlurry,
                                 int processTime, int frequency) implements Recipe<ShakingTableRecipeInput> {
    @Override
    public boolean matches(ShakingTableRecipeInput input, Level world) {
        return this.input.testForRecipe(input.recipeInventory().getItem(0))
                && input.waterAmount() >= FluidConstants.BUCKET * 2;
    }

    @Override
    public ItemStack assemble(ShakingTableRecipeInput input) {
        return this.output.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
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
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShakingTableRecipeDisplay(
                this.input.toDisplay(),
                new SlotDisplay.ItemSlotDisplay(BlockInit.SHAKING_TABLE.asItem()),
                this.output.toDisplay(),
                this.outputSlurry,
                this.processTime,
                this.frequency
        ));
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
        return RecipeBookCategoryInit.SHAKING_TABLE;
    }

    @Override
    public String group() {
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

    private static final MapCodec<ShakingTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IndustriaIngredient.CODEC.fieldOf("input").forGetter(ShakingTableRecipe::input),
            OutputItemStack.CODEC.fieldOf("output").forGetter(ShakingTableRecipe::output),
            SlurryStack.CODEC.fieldOf("output_slurry").forGetter(ShakingTableRecipe::outputSlurry),
            Codec.INT.fieldOf("process_time").forGetter(ShakingTableRecipe::processTime),
            Codec.INT.fieldOf("frequency").forGetter(ShakingTableRecipe::frequency)
    ).apply(instance, ShakingTableRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ShakingTableRecipe> STREAM_CODEC = StreamCodec.composite(
            IndustriaIngredient.STREAM_CODEC, ShakingTableRecipe::input,
            OutputItemStack.STREAM_CODEC, ShakingTableRecipe::output,
            SlurryStack.STREAM_CODEC, ShakingTableRecipe::outputSlurry,
            ByteBufCodecs.INT, ShakingTableRecipe::processTime,
            ByteBufCodecs.INT, ShakingTableRecipe::frequency,
            ShakingTableRecipe::new
    );

    public static final RecipeSerializer<ShakingTableRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record ShakingTableRecipeDisplay(SlotDisplay input, SlotDisplay craftingStation,
                                            SlotDisplay output, SlurryStack outputSlurry,
                                            int processTime, int frequency) implements RecipeDisplay {
        public static final MapCodec<ShakingTableRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf("input").forGetter(ShakingTableRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ShakingTableRecipeDisplay::craftingStation),
                SlotDisplay.CODEC.fieldOf("output").forGetter(ShakingTableRecipeDisplay::output),
                SlurryStack.CODEC.fieldOf("output_slurry").forGetter(ShakingTableRecipeDisplay::outputSlurry),
                Codec.INT.fieldOf("process_time").forGetter(ShakingTableRecipeDisplay::processTime),
                Codec.INT.fieldOf("frequency").forGetter(ShakingTableRecipeDisplay::frequency)
        ).apply(instance, ShakingTableRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShakingTableRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ShakingTableRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, ShakingTableRecipeDisplay::craftingStation,
                SlotDisplay.STREAM_CODEC, ShakingTableRecipeDisplay::output,
                SlurryStack.STREAM_CODEC, ShakingTableRecipeDisplay::outputSlurry,
                ByteBufCodecs.INT, ShakingTableRecipeDisplay::processTime,
                ByteBufCodecs.INT, ShakingTableRecipeDisplay::frequency,
                ShakingTableRecipeDisplay::new
        );

        public static final RecipeDisplay.Type<ShakingTableRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public RecipeDisplay.Type<ShakingTableRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
