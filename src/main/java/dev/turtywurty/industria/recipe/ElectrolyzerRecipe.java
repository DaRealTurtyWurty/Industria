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

public record ElectrolyzerRecipe(IndustriaIngredient input,
                                 IndustriaIngredient anode, IndustriaIngredient cathode,
                                 IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                 FluidStack outputFluid, GasStack outputGas,
                                 int processTime, int energyCost,
                                 int temperature) implements Recipe<ElectrolyzerRecipeInput> {
    @Override
    public boolean matches(ElectrolyzerRecipeInput input, World world) {
        SingleFluidStorage fluidStorage = input.electrolyteFluidStorage();
        return this.input.testForRecipe(input.getStackInSlot(0)) &&
                this.anode.testForRecipeIgnoreComponents(input.getStackInSlot(1)) &&
                this.cathode.testForRecipe(input.getStackInSlot(2)) &&
                (this.electrolyteItem.testForRecipe(input.getStackInSlot(3)) ||
                        this.electrolyteFluid.testForRecipe(fluidStorage));
    }

    @Override
    public ItemStack craft(ElectrolyzerRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public String getGroup() {
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
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
    public RecipeBookCategory getRecipeBookCategory() {
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

        public static final PacketCodec<RegistryByteBuf, ElectrolyzerRecipe> PACKET_CODEC = ExtraPacketCodecs.tuple(
                IndustriaIngredient.PACKET_CODEC, ElectrolyzerRecipe::input,
                IndustriaIngredient.PACKET_CODEC, ElectrolyzerRecipe::anode,
                IndustriaIngredient.PACKET_CODEC, ElectrolyzerRecipe::cathode,
                IndustriaIngredient.PACKET_CODEC, ElectrolyzerRecipe::electrolyteItem,
                FluidStack.PACKET_CODEC, ElectrolyzerRecipe::electrolyteFluid,
                FluidStack.PACKET_CODEC, ElectrolyzerRecipe::outputFluid,
                GasStack.PACKET_CODEC, ElectrolyzerRecipe::outputGas,
                PacketCodecs.INTEGER, ElectrolyzerRecipe::processTime,
                PacketCodecs.INTEGER, ElectrolyzerRecipe::energyCost,
                PacketCodecs.INTEGER, ElectrolyzerRecipe::temperature,
                ElectrolyzerRecipe::new);

        @Override
        public MapCodec<ElectrolyzerRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ElectrolyzerRecipe> packetCodec() {
            return PACKET_CODEC;
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

        private static final PacketCodec<RegistryByteBuf, ElectrolyzerRecipeDisplay> PACKET_CODEC = ExtraPacketCodecs.tuple(
                SlotDisplay.PACKET_CODEC, ElectrolyzerRecipeDisplay::input,
                SlotDisplay.PACKET_CODEC, ElectrolyzerRecipeDisplay::anode,
                SlotDisplay.PACKET_CODEC, ElectrolyzerRecipeDisplay::cathode,
                SlotDisplay.PACKET_CODEC, ElectrolyzerRecipeDisplay::electrolyteItem,
                FluidStack.PACKET_CODEC, ElectrolyzerRecipeDisplay::electrolyteFluid,
                SlotDisplay.PACKET_CODEC, ElectrolyzerRecipeDisplay::craftingStation,
                FluidStack.PACKET_CODEC, ElectrolyzerRecipeDisplay::outputFluid,
                GasStack.PACKET_CODEC, ElectrolyzerRecipeDisplay::outputGas,
                PacketCodecs.INTEGER, ElectrolyzerRecipeDisplay::processTime,
                PacketCodecs.INTEGER, ElectrolyzerRecipeDisplay::energyCost,
                PacketCodecs.INTEGER, ElectrolyzerRecipeDisplay::temperature,
                ElectrolyzerRecipeDisplay::new);

        private static final Serializer<ElectrolyzerRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

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
