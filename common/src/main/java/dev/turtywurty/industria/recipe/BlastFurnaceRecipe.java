package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record BlastFurnaceRecipe(
        IndustriaIngredient input,
        IndustriaIngredient flux,
        FluidStack output,
        ItemStackTemplate byproduct,
        int processTime
) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory inventory, Level level) {
        return findOreSlot(inventory) >= 0;
    }

    public int findOreSlot(RecipeSimpleInventory inventory) {
        if (inventory.size() < 2)
            return -1;

        if (this.input.testForRecipe(inventory.getItem(0))
                && this.flux.testForRecipe(inventory.getItem(1)))
            return 0;
        else if (this.input.testForRecipe(inventory.getItem(1))
                && this.flux.testForRecipe(inventory.getItem(0)))
            return 1;
        else
            return -1;
    }

    @Override
    public ItemStack assemble(RecipeSimpleInventory itemStacks) {
        return this.byproduct.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return ModRecipeSerializers.BLAST_FURNACE.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return ModRecipeTypes.BLAST_FURNACE.get();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return Industria.id("blast_furnace").toString();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.BLAST_FURNACE.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    public static class Type implements RecipeType<BlastFurnaceRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("blast_furnace").toString();
        }
    }

    private static final MapCodec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IndustriaIngredient.CODEC.fieldOf("input").forGetter(BlastFurnaceRecipe::input),
            IndustriaIngredient.CODEC.fieldOf("flux").forGetter(BlastFurnaceRecipe::flux),
            FluidStack.CODEC.fieldOf("output").forGetter(BlastFurnaceRecipe::output),
            ItemStackTemplate.CODEC.fieldOf("byproduct").forGetter(BlastFurnaceRecipe::byproduct),
            Codec.INT.fieldOf("process_time").forGetter(BlastFurnaceRecipe::processTime)
    ).apply(instance, BlastFurnaceRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> STREAM_CODEC = StreamCodec.composite(
            IndustriaIngredient.STREAM_CODEC, BlastFurnaceRecipe::input,
            IndustriaIngredient.STREAM_CODEC, BlastFurnaceRecipe::flux,
            FluidStack.STREAM_CODEC, BlastFurnaceRecipe::output,
            ItemStackTemplate.STREAM_CODEC, BlastFurnaceRecipe::byproduct,
            ByteBufCodecs.VAR_INT, BlastFurnaceRecipe::processTime,
            BlastFurnaceRecipe::new
    );

    public static final RecipeSerializer<BlastFurnaceRecipe> SERIALIZER =
            new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
