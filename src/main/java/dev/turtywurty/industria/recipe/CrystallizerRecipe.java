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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CrystallizerRecipe(FluidStack waterFluid, FluidStack crystalFluid, IndustriaIngredient catalyst,
                                 OutputItemStack output, OutputItemStack byProduct,
                                 boolean requiresCatalyst, int catalystUses, int processTime) implements Recipe<CrystallizerRecipeInput> {
    @Override
    public boolean matches(CrystallizerRecipeInput input, World world) {
        FluidStack waterFluid = input.waterFluid();
        FluidStack crystalFluid = input.crystalFluid();
        ItemStack catalyst = input.catalyst();

        return waterFluid.matches(this.waterFluid) && crystalFluid.matches(this.crystalFluid) &&
                (this.catalyst.testForRecipe(catalyst) || !this.requiresCatalyst);
    }

    @Override
    public ItemStack craft(CrystallizerRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return createOutput(new LocalRandom(ThreadLocalRandom.current().nextLong()));
    }

    public ItemStack createOutput(Random random) {
        return this.output.createStack(random);
    }

    public ItemStack createByProduct(Random random) {
        return this.byProduct.createStack(random);
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(
                new CrystallizerRecipeDisplay(this.waterFluid, this.crystalFluid, this.catalyst.toDisplay(),
                        new SlotDisplay.ItemSlotDisplay(BlockInit.CRYSTALLIZER.asItem()),
                        this.output.toDisplay(), this.byProduct.toDisplay(), this.requiresCatalyst, this.catalystUses, this.processTime));
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.CRYSTALLIZER;
    }

    @Override
    public String getGroup() {
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

        private static final PacketCodec<RegistryByteBuf, CrystallizerRecipe> PACKET_CODEC =
                PacketCodec.tuple(FluidStack.PACKET_CODEC, CrystallizerRecipe::waterFluid,
                        FluidStack.PACKET_CODEC, CrystallizerRecipe::crystalFluid,
                        IndustriaIngredient.PACKET_CODEC, CrystallizerRecipe::catalyst,
                        OutputItemStack.PACKET_CODEC, CrystallizerRecipe::output,
                        OutputItemStack.PACKET_CODEC, CrystallizerRecipe::byProduct,
                        PacketCodecs.BOOL, CrystallizerRecipe::requiresCatalyst,
                        PacketCodecs.INTEGER, CrystallizerRecipe::catalystUses,
                        PacketCodecs.INTEGER, CrystallizerRecipe::processTime,
                        CrystallizerRecipe::new);

        @Override
        public MapCodec<CrystallizerRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CrystallizerRecipe> packetCodec() {
            return PACKET_CODEC;
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

        private static final PacketCodec<RegistryByteBuf, CrystallizerRecipeDisplay> PACKET_CODEC =
                ExtraPacketCodecs.tuple(FluidStack.PACKET_CODEC, CrystallizerRecipeDisplay::waterFluid,
                        FluidStack.PACKET_CODEC, CrystallizerRecipeDisplay::crystalFluid,
                        SlotDisplay.PACKET_CODEC, CrystallizerRecipeDisplay::catalyst,
                        SlotDisplay.PACKET_CODEC, CrystallizerRecipeDisplay::craftingStation,
                        SlotDisplay.PACKET_CODEC, CrystallizerRecipeDisplay::output,
                        SlotDisplay.PACKET_CODEC, CrystallizerRecipeDisplay::byProduct,
                        PacketCodecs.BOOL, CrystallizerRecipeDisplay::requiresCatalyst,
                        PacketCodecs.INTEGER, CrystallizerRecipeDisplay::catalystUses,
                        PacketCodecs.INTEGER, CrystallizerRecipeDisplay::processTime,
                        CrystallizerRecipeDisplay::new);

        private static final Serializer<CrystallizerRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return new SlotDisplay.CompositeSlotDisplay(List.of(output, byProduct));
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
