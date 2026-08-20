package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.AgitatorPortStack;
import dev.turtywurty.industria.recipe.input.AgitatorRecipeInput;
import dev.turtywurty.industria.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record AgitatorRecipe(List<AgitatorInput> inputs, List<AgitatorOutput> outputs,
                             int processTime, int energyCost) implements Recipe<AgitatorRecipeInput> {
    public static final int INPUT_COUNT = 3;
    public static final int OUTPUT_COUNT = 2;

    public AgitatorRecipe {
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);

        if (inputs.size() != INPUT_COUNT)
            throw new IllegalArgumentException("Agitator recipes require exactly " + INPUT_COUNT + " inputs");

        if (outputs.size() != OUTPUT_COUNT)
            throw new IllegalArgumentException("Agitator recipes require exactly " + OUTPUT_COUNT + " outputs");
    }

    @Override
    public boolean matches(AgitatorRecipeInput input, Level level) {
        for (int index = 0; index < INPUT_COUNT; index++) {
            if (!this.inputs.get(index).matches(input.getPort(index)))
                return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(AgitatorRecipeInput input) {
        var random = new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
        for (AgitatorOutput output : this.outputs) {
            if (output.type() == AgitatorPortType.ITEM) {
                return output.item().createStack(random);
            }
        }

        return ItemStack.EMPTY;
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
        return RecipeBookCategoryInit.AGITATOR;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new AgitatorRecipeDisplay(
                this.inputs.stream().map(AgitatorInput::toDisplay).toList(),
                new SlotDisplay.ItemSlotDisplay(BlockInit.AGITATOR.asItem()),
                this.outputs.stream().map(AgitatorOutput::toDisplay).toList(),
                this.processTime,
                this.energyCost
        ));
    }

    @Override
    public RecipeSerializer<? extends Recipe<AgitatorRecipeInput>> getSerializer() {
        return RecipeSerializerInit.AGITATOR;
    }

    @Override
    public RecipeType<? extends Recipe<AgitatorRecipeInput>> getType() {
        return RecipeTypeInit.AGITATOR;
    }

    @Override
    public String group() {
        return Industria.id("agitator").toString();
    }

    public static class Type implements RecipeType<AgitatorRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("agitator").toString();
        }
    }

    private static final MapCodec<AgitatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.listOf(AgitatorInput.CODEC).fieldOf("inputs").forGetter(AgitatorRecipe::inputs),
            ExtraCodecs.listOf(AgitatorOutput.CODEC).fieldOf("outputs").forGetter(AgitatorRecipe::outputs),
            Codec.INT.fieldOf("process_time").orElse(200).forGetter(AgitatorRecipe::processTime),
            Codec.INT.fieldOf("energy_cost").orElse(1000).forGetter(AgitatorRecipe::energyCost)
    ).apply(instance, AgitatorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorRecipe> STREAM_CODEC = StreamCodec.composite(
            ExtraStreamCodecs.listOf(AgitatorInput.STREAM_CODEC), AgitatorRecipe::inputs,
            ExtraStreamCodecs.listOf(AgitatorOutput.STREAM_CODEC), AgitatorRecipe::outputs,
            ByteBufCodecs.INT, AgitatorRecipe::processTime,
            ByteBufCodecs.INT, AgitatorRecipe::energyCost,
            AgitatorRecipe::new
    );

    public static final RecipeSerializer<AgitatorRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record AgitatorInput(AgitatorPortType type, IndustriaIngredient item, FluidStack fluid, GasStack gas, SlurryStack slurry) {
        public static final MapCodec<AgitatorInput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AgitatorPortType.CODEC.fieldOf("type").forGetter(AgitatorInput::type),
                IndustriaIngredient.CODEC.optionalFieldOf("item", IndustriaIngredient.EMPTY).forGetter(AgitatorInput::item),
                FluidStack.CODEC.codec().optionalFieldOf("fluid", FluidStack.EMPTY).forGetter(AgitatorInput::fluid),
                GasStack.CODEC.codec().optionalFieldOf("gas", GasStack.EMPTY).forGetter(AgitatorInput::gas),
                SlurryStack.CODEC.codec().optionalFieldOf("slurry", SlurryStack.EMPTY).forGetter(AgitatorInput::slurry)
        ).apply(instance, AgitatorInput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorInput> STREAM_CODEC = StreamCodec.composite(
                AgitatorPortType.STREAM_CODEC, AgitatorInput::type,
                IndustriaIngredient.STREAM_CODEC, AgitatorInput::item,
                FluidStack.STREAM_CODEC, AgitatorInput::fluid,
                GasStack.STREAM_CODEC, AgitatorInput::gas,
                SlurryStack.STREAM_CODEC, AgitatorInput::slurry,
                AgitatorInput::new
        );

        public AgitatorInput {
            if (type == null)
                type = AgitatorPortType.ITEM;

            if (item == null)
                item = IndustriaIngredient.EMPTY;

            if (fluid == null)
                fluid = FluidStack.EMPTY;

            if (gas == null)
                gas = GasStack.EMPTY;

            if (slurry == null)
                slurry = SlurryStack.EMPTY;
        }

        public boolean matches(AgitatorPortStack stack) {
            if (stack == null)
                return false;

            if (isEmpty())
                return stack.isEmpty();

            if (stack.type() != this.type)
                return false;

            return switch (this.type) {
                case ITEM -> this.item.testForRecipe(stack.item());
                case FLUID -> stack.fluid().matches(this.fluid);
                case GAS -> stack.gas().matches(this.gas);
                case SLURRY -> stack.slurry().matches(this.slurry);
            };
        }

        public boolean isEmpty() {
            return switch (this.type) {
                case ITEM -> this.item.isEmpty();
                case FLUID -> this.fluid.isEmpty();
                case GAS -> this.gas.isEmpty();
                case SLURRY -> this.slurry.isEmpty();
            };
        }

        public SlotDisplay toDisplay() {
            if (isEmpty())
                return SlotDisplay.Empty.INSTANCE;

            return switch (this.type) {
                case ITEM -> this.item.toDisplay();
                default -> SlotDisplay.Empty.INSTANCE;
            };
        }
    }

    public record AgitatorOutput(AgitatorPortType type, OutputItemStack item, FluidStack fluid, GasStack gas, SlurryStack slurry) {
        public static final MapCodec<AgitatorOutput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AgitatorPortType.CODEC.fieldOf("type").forGetter(AgitatorOutput::type),
                OutputItemStack.CODEC.codec().optionalFieldOf("item", OutputItemStack.EMPTY).forGetter(AgitatorOutput::item),
                FluidStack.CODEC.codec().optionalFieldOf("fluid", FluidStack.EMPTY).forGetter(AgitatorOutput::fluid),
                GasStack.CODEC.codec().optionalFieldOf("gas", GasStack.EMPTY).forGetter(AgitatorOutput::gas),
                SlurryStack.CODEC.codec().optionalFieldOf("slurry", SlurryStack.EMPTY).forGetter(AgitatorOutput::slurry)
        ).apply(instance, AgitatorOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorOutput> STREAM_CODEC = StreamCodec.composite(
                AgitatorPortType.STREAM_CODEC, AgitatorOutput::type,
                OutputItemStack.STREAM_CODEC, AgitatorOutput::item,
                FluidStack.STREAM_CODEC, AgitatorOutput::fluid,
                GasStack.STREAM_CODEC, AgitatorOutput::gas,
                SlurryStack.STREAM_CODEC, AgitatorOutput::slurry,
                AgitatorOutput::new
        );

        public AgitatorOutput {
            if (type == null)
                type = AgitatorPortType.ITEM;

            if (item == null)
                item = OutputItemStack.EMPTY;

            if (fluid == null)
                fluid = FluidStack.EMPTY;

            if (gas == null)
                gas = GasStack.EMPTY;

            if (slurry == null)
                slurry = SlurryStack.EMPTY;
        }

        public SlotDisplay toDisplay() {
            return switch (this.type) {
                case ITEM -> this.item.toDisplay();
                default -> SlotDisplay.Empty.INSTANCE;
            };
        }
    }

    public record AgitatorRecipeDisplay(List<SlotDisplay> inputs, SlotDisplay craftingStation,
                                        List<SlotDisplay> outputs, int processTime, int energyCost) implements RecipeDisplay {
        public static final MapCodec<AgitatorRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.listOf().fieldOf("inputs").forGetter(AgitatorRecipeDisplay::inputs),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(AgitatorRecipeDisplay::craftingStation),
                SlotDisplay.CODEC.listOf().fieldOf("outputs").forGetter(AgitatorRecipeDisplay::outputs),
                Codec.INT.fieldOf("process_time").orElse(200).forGetter(AgitatorRecipeDisplay::processTime),
                Codec.INT.fieldOf("energy_cost").orElse(1000).forGetter(AgitatorRecipeDisplay::energyCost)
        ).apply(instance, AgitatorRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                ExtraStreamCodecs.listOf(SlotDisplay.STREAM_CODEC), AgitatorRecipeDisplay::inputs,
                SlotDisplay.STREAM_CODEC, AgitatorRecipeDisplay::craftingStation,
                ExtraStreamCodecs.listOf(SlotDisplay.STREAM_CODEC), AgitatorRecipeDisplay::outputs,
                ByteBufCodecs.INT, AgitatorRecipeDisplay::processTime,
                ByteBufCodecs.INT, AgitatorRecipeDisplay::energyCost,
                AgitatorRecipeDisplay::new
        );

        public static final RecipeDisplay.Type<AgitatorRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.outputs.stream()
                    .filter(display -> display != SlotDisplay.Empty.INSTANCE)
                    .findFirst()
                    .orElse(SlotDisplay.Empty.INSTANCE);
        }

        @Override
        public RecipeDisplay.Type<AgitatorRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
