package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.DigesterRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record DigesterRecipe(SlurryStack inputSlurry, FluidStack outputFluid, int processTime)
        implements Recipe<DigesterRecipeInput> {
    @Override
    public boolean matches(DigesterRecipeInput input, Level world) {
        return input.slurryStack().matches(this.inputSlurry);
    }

    @Override
    public ItemStack assemble(DigesterRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<DigesterRecipeInput>> getSerializer() {
        return ModRecipeSerializers.DIGESTER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DigesterRecipeInput>> getType() {
        return ModRecipeTypes.DIGESTER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.DIGESTER.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new DigesterRecipeDisplay(
                this.inputSlurry,
                new SlotDisplay.ItemSlotDisplay(ModBlocks.DIGESTER.get().asItem()),
                this.outputFluid,
                this.processTime
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
    public String group() {
        return Industria.id("digester").toString();
    }

    public static class Type implements RecipeType<DigesterRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("digester").toString();
        }
    }

    private static final MapCodec<DigesterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SlurryStack.CODEC.fieldOf("input_slurry").forGetter(DigesterRecipe::inputSlurry),
            FluidStack.CODEC.fieldOf("output_fluid").forGetter(DigesterRecipe::outputFluid),
            Codec.INT.fieldOf("process_time").forGetter(DigesterRecipe::processTime)
    ).apply(instance, DigesterRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, DigesterRecipe> STREAM_CODEC = StreamCodec.composite(
            SlurryStack.STREAM_CODEC, DigesterRecipe::inputSlurry,
            FluidStack.STREAM_CODEC, DigesterRecipe::outputFluid,
            ByteBufCodecs.INT, DigesterRecipe::processTime,
            DigesterRecipe::new
    );

    public static final RecipeSerializer<DigesterRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record DigesterRecipeDisplay(SlurryStack inputSlurry, SlotDisplay craftingStation, FluidStack outputFluid, int processTime)
            implements RecipeDisplay {
        public static final MapCodec<DigesterRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlurryStack.CODEC.fieldOf("input_slurry").forGetter(DigesterRecipeDisplay::inputSlurry),
                        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(DigesterRecipeDisplay::craftingStation),
                        FluidStack.CODEC.fieldOf("output_fluid").forGetter(DigesterRecipeDisplay::outputFluid),
                        Codec.INT.fieldOf("process_time").forGetter(DigesterRecipeDisplay::processTime)
                ).apply(instance, DigesterRecipeDisplay::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, DigesterRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlurryStack.STREAM_CODEC, DigesterRecipeDisplay::inputSlurry,
                SlotDisplay.STREAM_CODEC, DigesterRecipeDisplay::craftingStation,
                FluidStack.STREAM_CODEC, DigesterRecipeDisplay::outputFluid,
                ByteBufCodecs.INT, DigesterRecipeDisplay::processTime,
                DigesterRecipeDisplay::new
        );

        public static final RecipeDisplay.Type<DigesterRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return SlotDisplay.Empty.INSTANCE;
        }

        @Override
        public RecipeDisplay.Type<DigesterRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
