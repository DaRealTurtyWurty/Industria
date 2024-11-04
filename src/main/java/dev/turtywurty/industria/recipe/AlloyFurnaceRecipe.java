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

public record AlloyFurnaceRecipe(IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output,
                                 int smeltTime) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, World world) {
        ItemStack stackA = input.getStack(0);
        ItemStack stackB = input.getStack(1);
        return (this.inputA.testForRecipe(stackA) && this.inputB.testForRecipe(stackB)) ||
                (this.inputA.testForRecipe(stackB) && this.inputB.testForRecipe(stackA));
    }

    @Override
    public ItemStack craft(RecipeSimpleInventory inventory, RegistryWrapper.WrapperLookup lookup) {
        // extract the inventory stacks
        ItemStack stackA = this.inputA.testForRecipe(inventory.getStack(0)) ? inventory.getStack(0) : inventory.getStack(1);
        ItemStack stackB = this.inputB.testForRecipe(inventory.getStack(0)) ? inventory.getStack(0) : inventory.getStack(1);

        // remove the input stacks
        stackA.decrement(this.inputA.stackData().count());
        stackB.decrement(this.inputB.stackData().count());

        // set the stacks back into the inventory
        inventory.setStack(0, stackA);
        inventory.setStack(1, stackB);

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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategoryInit.ALLOY_FURNACE;
    }

    @Override
    public String getGroup() {
        return Industria.id("alloy_furnace").toString();
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new AlloyFurnaceRecipeDisplay(
                this.inputA.toDisplay(),
                this.inputB.toDisplay(),
                SlotDisplay.AnyFuelSlotDisplay.INSTANCE,
                new SlotDisplay.StackSlotDisplay(this.output),
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

        private static final PacketCodec<RegistryByteBuf, AlloyFurnaceRecipe> PACKET_CODEC =
                PacketCodec.tuple(IndustriaIngredient.PACKET_CODEC, AlloyFurnaceRecipe::inputA,
                        IndustriaIngredient.PACKET_CODEC, AlloyFurnaceRecipe::inputB,
                        ItemStack.PACKET_CODEC, AlloyFurnaceRecipe::output,
                        PacketCodecs.INTEGER, AlloyFurnaceRecipe::smeltTime,
                        AlloyFurnaceRecipe::new);

        @Override
        public MapCodec<AlloyFurnaceRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, AlloyFurnaceRecipe> packetCodec() {
            return PACKET_CODEC;
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

        public static final PacketCodec<RegistryByteBuf, AlloyFurnaceRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                SlotDisplay.PACKET_CODEC, AlloyFurnaceRecipeDisplay::inputA,
                SlotDisplay.PACKET_CODEC, AlloyFurnaceRecipeDisplay::inputB,
                SlotDisplay.PACKET_CODEC, AlloyFurnaceRecipeDisplay::fuel,
                SlotDisplay.PACKET_CODEC, AlloyFurnaceRecipeDisplay::result,
                SlotDisplay.PACKET_CODEC, AlloyFurnaceRecipeDisplay::craftingStation,
                PacketCodecs.INTEGER, AlloyFurnaceRecipeDisplay::processTime,
                AlloyFurnaceRecipeDisplay::new
        );

        public static final Serializer<AlloyFurnaceRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
