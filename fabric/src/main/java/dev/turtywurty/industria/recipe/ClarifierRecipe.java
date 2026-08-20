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

public record ClarifierRecipe(FluidStack inputFluid, FluidStack outputFluidStack, OutputItemStack outputItemStack, int processTime)
        implements Recipe<ClarifierRecipeInput> {
    @Override
    public boolean matches(ClarifierRecipeInput input, Level world) {
        return input.fluidStack().matches(inputFluid);
    }

    @Override
    public ItemStack assemble(ClarifierRecipeInput input) {
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
    public boolean showNotification() {
        return true;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ClarifierRecipeDisplay(
                this.inputFluid,
                new SlotDisplay.ItemSlotDisplay(BlockInit.CLARIFIER.asItem()),
                this.outputFluidStack,
                this.outputItemStack.toDisplay(),
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

    private static final MapCodec<ClarifierRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(ClarifierRecipe::inputFluid),
            FluidStack.CODEC.fieldOf("output_fluid").forGetter(ClarifierRecipe::outputFluidStack),
            OutputItemStack.CODEC.fieldOf("output_item").forGetter(ClarifierRecipe::outputItemStack),
            Codec.INT.fieldOf("process_time").forGetter(ClarifierRecipe::processTime)
    ).apply(instance, ClarifierRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ClarifierRecipe> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, ClarifierRecipe::inputFluid,
            FluidStack.STREAM_CODEC, ClarifierRecipe::outputFluidStack,
            OutputItemStack.STREAM_CODEC, ClarifierRecipe::outputItemStack,
            ByteBufCodecs.INT, ClarifierRecipe::processTime,
            ClarifierRecipe::new
    );

    public static final RecipeSerializer<ClarifierRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record ClarifierRecipeDisplay(FluidStack inputFluid, SlotDisplay craftingStation, FluidStack outputFluid,
                                         SlotDisplay outputItem, int processTime) implements RecipeDisplay {
        private static final MapCodec<ClarifierRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("input_fluid").forGetter(ClarifierRecipeDisplay::inputFluid),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ClarifierRecipeDisplay::craftingStation),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ClarifierRecipeDisplay::outputFluid),
                SlotDisplay.CODEC.fieldOf("output_item").forGetter(ClarifierRecipeDisplay::outputItem),
                Codec.INT.fieldOf("process_time").forGetter(ClarifierRecipeDisplay::processTime)
        ).apply(instance, ClarifierRecipeDisplay::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ClarifierRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                FluidStack.STREAM_CODEC, ClarifierRecipeDisplay::inputFluid,
                SlotDisplay.STREAM_CODEC, ClarifierRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, ClarifierRecipeDisplay::outputFluid,
                SlotDisplay.STREAM_CODEC, ClarifierRecipeDisplay::outputItem,
                ByteBufCodecs.INT, ClarifierRecipeDisplay::processTime,
                ClarifierRecipeDisplay::new
        );

        public static final RecipeDisplay.Type<ClarifierRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.outputItem;
        }

        @Override
        public RecipeDisplay.Type<ClarifierRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
