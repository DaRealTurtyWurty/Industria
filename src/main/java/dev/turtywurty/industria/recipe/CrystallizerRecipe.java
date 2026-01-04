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
import dev.turtywurty.industria.recipe.input.CrystallizerRecipeInput;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.core.HolderLookup;
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

public record CrystallizerRecipe(FluidStack waterFluid, FluidStack crystalFluid, IndustriaIngredient catalyst,
                                 OutputItemStack output, OutputItemStack byProduct,
                                 boolean requiresCatalyst, int catalystUses, int processTime) implements Recipe<CrystallizerRecipeInput> {
    @Override
    public boolean matches(CrystallizerRecipeInput input, Level world) {
        FluidStack waterFluid = input.waterFluid();
        FluidStack crystalFluid = input.crystalFluid();
        ItemStack catalyst = input.catalyst();

        return waterFluid.matches(this.waterFluid) && crystalFluid.matches(this.crystalFluid) &&
                (this.catalyst.testForRecipe(catalyst) || !this.requiresCatalyst);
    }

    @Override
    public ItemStack assemble(CrystallizerRecipeInput input, HolderLookup.Provider registries) {
        return createOutput(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    public ItemStack createOutput(RandomSource random) {
        return this.output.createStack(random);
    }

    public ItemStack createByProduct(RandomSource random) {
        return this.byProduct.createStack(random);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<CrystallizerRecipeInput>> getSerializer() {
        return RecipeSerializerInit.CRYSTALLIZER;
    }

    @Override
    public RecipeType<? extends Recipe<CrystallizerRecipeInput>> getType() {
        return RecipeTypeInit.CRYSTALLIZER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new CrystallizerRecipeDisplay(this.waterFluid, this.crystalFluid, this.catalyst.toDisplay(),
                        new SlotDisplay.ItemSlotDisplay(BlockInit.CRYSTALLIZER.asItem()),
                        this.output.toDisplay(), this.byProduct.toDisplay(), this.requiresCatalyst, this.catalystUses, this.processTime));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.CRYSTALLIZER;
    }

    @Override
    public String group() {
        return Industria.id("crystallizer").toString();
    }

    public static class Type implements RecipeType<CrystallizerRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("crystallizer").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<CrystallizerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<CrystallizerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("water_fluid").forGetter(CrystallizerRecipe::waterFluid),
                FluidStack.CODEC.fieldOf("crystal_fluid").forGetter(CrystallizerRecipe::crystalFluid),
                IndustriaIngredient.CODEC.fieldOf("catalyst").forGetter(CrystallizerRecipe::catalyst),
                OutputItemStack.CODEC.fieldOf("output").forGetter(CrystallizerRecipe::output),
                OutputItemStack.CODEC.fieldOf("by_product").forGetter(CrystallizerRecipe::byProduct),
                Codec.BOOL.fieldOf("requires_catalyst").forGetter(CrystallizerRecipe::requiresCatalyst),
                Codec.INT.fieldOf("catalyst_uses").forGetter(CrystallizerRecipe::catalystUses),
                Codec.INT.fieldOf("process_time").forGetter(CrystallizerRecipe::processTime)
        ).apply(instance, CrystallizerRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipe> STREAM_CODEC =
                StreamCodec.composite(FluidStack.STREAM_CODEC, CrystallizerRecipe::waterFluid,
                        FluidStack.STREAM_CODEC, CrystallizerRecipe::crystalFluid,
                        IndustriaIngredient.STREAM_CODEC, CrystallizerRecipe::catalyst,
                        OutputItemStack.STREAM_CODEC, CrystallizerRecipe::output,
                        OutputItemStack.STREAM_CODEC, CrystallizerRecipe::byProduct,
                        ByteBufCodecs.BOOL, CrystallizerRecipe::requiresCatalyst,
                        ByteBufCodecs.INT, CrystallizerRecipe::catalystUses,
                        ByteBufCodecs.INT, CrystallizerRecipe::processTime,
                        CrystallizerRecipe::new);

        @Override
        public MapCodec<CrystallizerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record CrystallizerRecipeDisplay(FluidStack waterFluid, FluidStack crystalFluid,
                                            SlotDisplay catalyst, SlotDisplay craftingStation,
                                            SlotDisplay output, SlotDisplay byProduct,
                                            boolean requiresCatalyst, int catalystUses, int processTime) implements RecipeDisplay {
        private static final MapCodec<CrystallizerRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("water_fluid").forGetter(CrystallizerRecipeDisplay::waterFluid),
                FluidStack.CODEC.fieldOf("crystal_fluid").forGetter(CrystallizerRecipeDisplay::crystalFluid),
                SlotDisplay.CODEC.fieldOf("catalyst").forGetter(CrystallizerRecipeDisplay::catalyst),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(CrystallizerRecipeDisplay::craftingStation),
                SlotDisplay.CODEC.fieldOf("output").forGetter(CrystallizerRecipeDisplay::output),
                SlotDisplay.CODEC.fieldOf("by_product").forGetter(CrystallizerRecipeDisplay::byProduct),
                Codec.BOOL.fieldOf("requires_catalyst").forGetter(CrystallizerRecipeDisplay::requiresCatalyst),
                Codec.INT.fieldOf("catalyst_uses").forGetter(CrystallizerRecipeDisplay::catalystUses),
                Codec.INT.fieldOf("process_time").forGetter(CrystallizerRecipeDisplay::processTime)
        ).apply(instance, CrystallizerRecipeDisplay::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipeDisplay> STREAM_CODEC =
                ExtraPacketCodecs.tuple(FluidStack.STREAM_CODEC, CrystallizerRecipeDisplay::waterFluid,
                        FluidStack.STREAM_CODEC, CrystallizerRecipeDisplay::crystalFluid,
                        SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::catalyst,
                        SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::craftingStation,
                        SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::output,
                        SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::byProduct,
                        ByteBufCodecs.BOOL, CrystallizerRecipeDisplay::requiresCatalyst,
                        ByteBufCodecs.INT, CrystallizerRecipeDisplay::catalystUses,
                        ByteBufCodecs.INT, CrystallizerRecipeDisplay::processTime,
                        CrystallizerRecipeDisplay::new);

        private static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return new SlotDisplay.Composite(List.of(output, byProduct));
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
