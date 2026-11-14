package dev.turtywurty.industria.recipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.molding.MoldPiece;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class ClayMoldSmeltingRecipe extends SmeltingRecipe {
    private static final MapCodec<SmeltingRecipe> CODEC = cookingMapCodec(ClayMoldSmeltingRecipe::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, SmeltingRecipe> NETWORK_CODEC = cookingStreamCodec(
        ClayMoldSmeltingRecipe::new);
    public static final RecipeSerializer<SmeltingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, NETWORK_CODEC);

    public ClayMoldSmeltingRecipe(
        CommonInfo commonInfo,
        CookingBookInfo bookInfo,
        Ingredient ingredient,
        ItemStackTemplate result,
        float experience,
        int cookingTime
    ) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        MoldComponent mold = input.item().get(ModDataComponentTypes.MOLD.get());
        return super.matches(input, level) && mold != null && MoldPatterns.find(mold.moldId())
            .map(pattern -> pattern.split() == (mold.piece() != MoldPiece.WHOLE))
            .orElse(false);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        MoldComponent mold = input.item().get(ModDataComponentTypes.MOLD.get());
        if (mold == null)
            return ItemStack.EMPTY;

        ItemStack result = super.assemble(input);
        result.set(ModDataComponentTypes.MOLD.get(), mold);
        return result;
    }

    @Override
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public List<RecipeDisplay> display() {
        List<RecipeDisplay> displays = new ArrayList<>();
        for (MoldPattern pattern : MoldPatterns.ALL) {
            MoldPiece[] pieces = pattern.split()
                ? new MoldPiece[]{MoldPiece.HALF_1, MoldPiece.HALF_2}
                : new MoldPiece[]{MoldPiece.WHOLE};
            for (MoldPiece piece : pieces) {
                DataComponentPatch patch = DataComponentPatch.builder()
                    .set(ModDataComponentTypes.MOLD.get(), new MoldComponent(pattern.id(), piece)).build();
                var clay = new ItemStackTemplate(ModItems.CLAY_MOLD.get(), patch);
                var ceramic = new ItemStackTemplate(result().item(), result().count(), patch);
                displays.add(new FurnaceRecipeDisplay(
                    new SlotDisplay.ItemStackSlotDisplay(clay),
                    SlotDisplay.AnyFuel.INSTANCE,
                    new SlotDisplay.ItemStackSlotDisplay(ceramic),
                    new SlotDisplay.ItemSlotDisplay(Items.FURNACE), cookingTime(), experience()));
            }
        }

        return List.copyOf(displays);
    }
}
