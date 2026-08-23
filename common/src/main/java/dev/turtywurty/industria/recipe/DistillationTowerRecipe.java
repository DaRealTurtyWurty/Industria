package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.DistillationTowerRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record DistillationTowerRecipe(FluidStack inputFluid, FluidStack primaryOutputFluid,
                                      FluidStack secondaryOutputFluid, int processTime, int energyCost)
        implements Recipe<DistillationTowerRecipeInput> {
    @Override
    public boolean matches(DistillationTowerRecipeInput input, Level world) {
        return input.fluidStack().matches(this.inputFluid);
    }

    @Override
    public ItemStack assemble(DistillationTowerRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<DistillationTowerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.DISTILLATION_TOWER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DistillationTowerRecipeInput>> getType() {
        return ModRecipeTypes.DISTILLATION_TOWER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.DISTILLATION_TOWER.get();
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
        return List.of(new DistillationTowerRecipeDisplay(
                this.inputFluid,
                new SlotDisplay.ItemSlotDisplay(ModBlocks.DISTILLATION_TOWER.get().asItem()),
                this.primaryOutputFluid,
                this.secondaryOutputFluid,
                this.processTime,
                this.energyCost
        ));
    }

    @Override
    public String group() {
        return Industria.id("distillation_tower").toString();
    }

    public static class Type implements RecipeType<DistillationTowerRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("distillation_tower").toString();
        }
    }

    private static final MapCodec<DistillationTowerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(DistillationTowerRecipe::inputFluid),
            FluidStack.CODEC.fieldOf("primary_output_fluid").forGetter(DistillationTowerRecipe::primaryOutputFluid),
            FluidStack.CODEC.fieldOf("secondary_output_fluid").forGetter(DistillationTowerRecipe::secondaryOutputFluid),
            Codec.INT.fieldOf("process_time").orElse(200).forGetter(DistillationTowerRecipe::processTime),
            Codec.INT.fieldOf("energy_cost").orElse(40).forGetter(DistillationTowerRecipe::energyCost)
    ).apply(instance, DistillationTowerRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, DistillationTowerRecipe> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC, DistillationTowerRecipe::inputFluid,
            FluidStack.STREAM_CODEC, DistillationTowerRecipe::primaryOutputFluid,
            FluidStack.STREAM_CODEC, DistillationTowerRecipe::secondaryOutputFluid,
            ByteBufCodecs.INT, DistillationTowerRecipe::processTime,
            ByteBufCodecs.INT, DistillationTowerRecipe::energyCost,
            DistillationTowerRecipe::new
    );

    public static final RecipeSerializer<DistillationTowerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record DistillationTowerRecipeDisplay(FluidStack inputFluid, SlotDisplay craftingStation,
                                                 FluidStack primaryOutputFluid, FluidStack secondaryOutputFluid,
                                                 int processTime, int energyCost) implements RecipeDisplay {
        private static final MapCodec<DistillationTowerRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("input_fluid").forGetter(DistillationTowerRecipeDisplay::inputFluid),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(DistillationTowerRecipeDisplay::craftingStation),
                FluidStack.CODEC.fieldOf("primary_output_fluid").forGetter(DistillationTowerRecipeDisplay::primaryOutputFluid),
                FluidStack.CODEC.fieldOf("secondary_output_fluid").forGetter(DistillationTowerRecipeDisplay::secondaryOutputFluid),
                Codec.INT.fieldOf("process_time").forGetter(DistillationTowerRecipeDisplay::processTime),
                Codec.INT.fieldOf("energy_cost").forGetter(DistillationTowerRecipeDisplay::energyCost)
        ).apply(instance, DistillationTowerRecipeDisplay::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DistillationTowerRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                FluidStack.STREAM_CODEC, DistillationTowerRecipeDisplay::inputFluid,
                SlotDisplay.STREAM_CODEC, DistillationTowerRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, DistillationTowerRecipeDisplay::primaryOutputFluid,
                FluidStack.STREAM_CODEC, DistillationTowerRecipeDisplay::secondaryOutputFluid,
                ByteBufCodecs.INT, DistillationTowerRecipeDisplay::processTime,
                ByteBufCodecs.INT, DistillationTowerRecipeDisplay::energyCost,
                DistillationTowerRecipeDisplay::new
        );

        private static final RecipeDisplay.Type<DistillationTowerRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return SlotDisplay.Empty.INSTANCE;
        }

        @Override
        public RecipeDisplay.Type<DistillationTowerRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
