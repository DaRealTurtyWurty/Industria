package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.IndustriaIngredient;
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

public record AlloyFurnaceRecipe(IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output,
                                 int smeltTime) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, Level world) {
        ItemStack stackA = input.getItem(0);
        ItemStack stackB = input.getItem(1);
        return (this.inputA.testForRecipe(stackA) && this.inputB.testForRecipe(stackB)) ||
                (this.inputA.testForRecipe(stackB) && this.inputB.testForRecipe(stackA));
    }

    @Override
    public ItemStack assemble(RecipeSimpleInventory inventory, HolderLookup.Provider lookup) {
        // extract the inventory stacks
        ItemStack stackA = this.inputA.testForRecipe(inventory.getItem(0)) ? inventory.getItem(0) : inventory.getItem(1);
        ItemStack stackB = this.inputB.testForRecipe(inventory.getItem(0)) ? inventory.getItem(0) : inventory.getItem(1);

        // remove the input stacks
        stackA.shrink(this.inputA.stackData().count());
        stackB.shrink(this.inputB.stackData().count());

        // set the stacks back into the inventory
        inventory.setItem(0, stackA);
        inventory.setItem(1, stackB);

        return this.output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return RecipeSerializerInit.ALLOY_FURNACE;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return RecipeTypeInit.ALLOY_FURNACE;
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
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.ALLOY_FURNACE;
    }

    @Override
    public String group() {
        return Industria.id("alloy_furnace").toString();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new AlloyFurnaceRecipeDisplay(
                this.inputA.toDisplay(),
                this.inputB.toDisplay(),
                SlotDisplay.AnyFuel.INSTANCE,
                new SlotDisplay.ItemStackSlotDisplay(this.output),
                new SlotDisplay.ItemSlotDisplay(BlockInit.ALLOY_FURNACE.asItem()),
                this.smeltTime
        ));
    }

    public static class Type implements RecipeType<AlloyFurnaceRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("alloy_furnace").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyFurnaceRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<AlloyFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IndustriaIngredient.CODEC.fieldOf("inputA").forGetter(AlloyFurnaceRecipe::inputA),
                IndustriaIngredient.CODEC.fieldOf("inputB").forGetter(AlloyFurnaceRecipe::inputB),
                ItemStack.CODEC.fieldOf("output").forGetter(AlloyFurnaceRecipe::output),
                Codec.INT.fieldOf("smelt_time").forGetter(AlloyFurnaceRecipe::smeltTime)
        ).apply(instance, AlloyFurnaceRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AlloyFurnaceRecipe> STREAM_CODEC =
                StreamCodec.composite(IndustriaIngredient.STREAM_CODEC, AlloyFurnaceRecipe::inputA,
                        IndustriaIngredient.STREAM_CODEC, AlloyFurnaceRecipe::inputB,
                        ItemStack.STREAM_CODEC, AlloyFurnaceRecipe::output,
                        ByteBufCodecs.INT, AlloyFurnaceRecipe::smeltTime,
                        AlloyFurnaceRecipe::new);

        @Override
        public MapCodec<AlloyFurnaceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloyFurnaceRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record AlloyFurnaceRecipeDisplay(SlotDisplay inputA, SlotDisplay inputB, SlotDisplay fuel,
                                            SlotDisplay result, SlotDisplay craftingStation,
                                            int processTime) implements RecipeDisplay {
        public static final MapCodec<AlloyFurnaceRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.fieldOf("inputA").forGetter(AlloyFurnaceRecipeDisplay::inputA),
                        SlotDisplay.CODEC.fieldOf("inputB").forGetter(AlloyFurnaceRecipeDisplay::inputB),
                        SlotDisplay.CODEC.fieldOf("fuel").forGetter(AlloyFurnaceRecipeDisplay::fuel),
                        SlotDisplay.CODEC.fieldOf("result").forGetter(AlloyFurnaceRecipeDisplay::result),
                        SlotDisplay.CODEC.fieldOf("craftingStation").forGetter(AlloyFurnaceRecipeDisplay::craftingStation),
                        Codec.INT.fieldOf("processTime").forGetter(AlloyFurnaceRecipeDisplay::processTime)
                ).apply(instance, AlloyFurnaceRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyFurnaceRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::inputA,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::inputB,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::fuel,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::craftingStation,
                ByteBufCodecs.INT, AlloyFurnaceRecipeDisplay::processTime,
                AlloyFurnaceRecipeDisplay::new
        );

        public static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
