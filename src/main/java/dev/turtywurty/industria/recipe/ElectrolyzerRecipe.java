package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.recipe.input.ElectrolyzerRecipeInput;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record ElectrolyzerRecipe(IndustriaIngredient input,
                                 IndustriaIngredient anode, IndustriaIngredient cathode,
                                 IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                 FluidStack outputFluid, GasStack outputGas,
                                 int processTime, int energyCost,
                                 int temperature) implements Recipe<ElectrolyzerRecipeInput> {
    @Override
    public boolean matches(ElectrolyzerRecipeInput input, Level world) {
        SingleFluidStorage fluidStorage = input.electrolyteFluidStorage();
        return this.input.testForRecipe(input.getItem(0)) &&
                this.anode.testForRecipeIgnoreComponents(input.getItem(1)) &&
                this.cathode.testForRecipe(input.getItem(2)) &&
                (this.electrolyteItem.testForRecipe(input.getItem(3)) ||
                        this.electrolyteFluid.testForRecipe(fluidStorage));
    }

    @Override
    public ItemStack assemble(ElectrolyzerRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return Industria.id("electrolyzer").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<ElectrolyzerRecipeInput>> getSerializer() {
        return RecipeSerializerInit.ELECTROLYZER;
    }

    @Override
    public RecipeType<? extends Recipe<ElectrolyzerRecipeInput>> getType() {
        return RecipeTypeInit.ELECTROLYZER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ElectrolyzerRecipeDisplay(
                this.input.toDisplay(),
                this.anode.toDisplay(), this.cathode.toDisplay(),
                this.electrolyteItem.toDisplay(), this.electrolyteFluid,
                new SlotDisplay.ItemSlotDisplay(BlockInit.ELECTROLYZER.asItem()),
                this.outputFluid, this.outputGas,
                this.processTime, this.energyCost, this.temperature
        ));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.ELECTROLYZER;
    }

    public static class Type implements RecipeType<ElectrolyzerRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("electrolyzer").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<ElectrolyzerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<ElectrolyzerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("input").forGetter(ElectrolyzerRecipe::input),
                IndustriaIngredient.CODEC.fieldOf("anode").forGetter(ElectrolyzerRecipe::anode),
                IndustriaIngredient.CODEC.fieldOf("cathode").forGetter(ElectrolyzerRecipe::cathode),
                IndustriaIngredient.CODEC.fieldOf("electrolyte_item").forGetter(ElectrolyzerRecipe::electrolyteItem),
                FluidStack.CODEC.fieldOf("electrolyte_fluid").forGetter(ElectrolyzerRecipe::electrolyteFluid),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ElectrolyzerRecipe::outputFluid),
                GasStack.CODEC.fieldOf("output_gas").forGetter(ElectrolyzerRecipe::outputGas),
                Codec.INT.fieldOf("process_time").orElse(200).forGetter(ElectrolyzerRecipe::processTime),
                Codec.INT.fieldOf("energy_cost").orElse(1000).forGetter(ElectrolyzerRecipe::energyCost),
                Codec.INT.fieldOf("temperature").orElse(300).forGetter(ElectrolyzerRecipe::temperature)
        ).apply(instance, ElectrolyzerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> STREAM_CODEC = ExtraPacketCodecs.tuple(
                IndustriaIngredient.STREAM_CODEC, ElectrolyzerRecipe::input,
                IndustriaIngredient.STREAM_CODEC, ElectrolyzerRecipe::anode,
                IndustriaIngredient.STREAM_CODEC, ElectrolyzerRecipe::cathode,
                IndustriaIngredient.STREAM_CODEC, ElectrolyzerRecipe::electrolyteItem,
                FluidStack.STREAM_CODEC, ElectrolyzerRecipe::electrolyteFluid,
                FluidStack.STREAM_CODEC, ElectrolyzerRecipe::outputFluid,
                GasStack.STREAM_CODEC, ElectrolyzerRecipe::outputGas,
                ByteBufCodecs.INT, ElectrolyzerRecipe::processTime,
                ByteBufCodecs.INT, ElectrolyzerRecipe::energyCost,
                ByteBufCodecs.INT, ElectrolyzerRecipe::temperature,
                ElectrolyzerRecipe::new);

        @Override
        public MapCodec<ElectrolyzerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record ElectrolyzerRecipeDisplay(
            SlotDisplay input,
            SlotDisplay anode, SlotDisplay cathode,
            SlotDisplay electrolyteItem, FluidStack electrolyteFluid,
            SlotDisplay craftingStation,
            FluidStack outputFluid, GasStack outputGas,
            int processTime, int energyCost, int temperature
    ) implements RecipeDisplay {
        private static final MapCodec<ElectrolyzerRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf("input").forGetter(ElectrolyzerRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("anode").forGetter(ElectrolyzerRecipeDisplay::anode),
                SlotDisplay.CODEC.fieldOf("cathode").forGetter(ElectrolyzerRecipeDisplay::cathode),
                SlotDisplay.CODEC.fieldOf("electrolyte_item").forGetter(ElectrolyzerRecipeDisplay::electrolyteItem),
                FluidStack.CODEC.fieldOf("electrolyte_fluid").forGetter(ElectrolyzerRecipeDisplay::electrolyteFluid),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ElectrolyzerRecipeDisplay::craftingStation),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(ElectrolyzerRecipeDisplay::outputFluid),
                GasStack.CODEC.fieldOf("output_gas").forGetter(ElectrolyzerRecipeDisplay::outputGas),
                Codec.INT.fieldOf("process_time").orElse(200).forGetter(ElectrolyzerRecipeDisplay::processTime),
                Codec.INT.fieldOf("energy_cost").orElse(1000).forGetter(ElectrolyzerRecipeDisplay::energyCost),
                Codec.INT.fieldOf("temperature").orElse(300).forGetter(ElectrolyzerRecipeDisplay::temperature)
        ).apply(instance, ElectrolyzerRecipeDisplay::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipeDisplay> STREAM_CODEC = ExtraPacketCodecs.tuple(
                SlotDisplay.STREAM_CODEC, ElectrolyzerRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, ElectrolyzerRecipeDisplay::anode,
                SlotDisplay.STREAM_CODEC, ElectrolyzerRecipeDisplay::cathode,
                SlotDisplay.STREAM_CODEC, ElectrolyzerRecipeDisplay::electrolyteItem,
                FluidStack.STREAM_CODEC, ElectrolyzerRecipeDisplay::electrolyteFluid,
                SlotDisplay.STREAM_CODEC, ElectrolyzerRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, ElectrolyzerRecipeDisplay::outputFluid,
                GasStack.STREAM_CODEC, ElectrolyzerRecipeDisplay::outputGas,
                ByteBufCodecs.INT, ElectrolyzerRecipeDisplay::processTime,
                ByteBufCodecs.INT, ElectrolyzerRecipeDisplay::energyCost,
                ByteBufCodecs.INT, ElectrolyzerRecipeDisplay::temperature,
                ElectrolyzerRecipeDisplay::new);

        private static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return SlotDisplay.Empty.INSTANCE;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
