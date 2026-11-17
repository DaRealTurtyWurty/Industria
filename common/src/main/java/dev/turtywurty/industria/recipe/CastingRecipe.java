package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.CastingRecipeInput;
import dev.turtywurty.industria.molding.MoldPiece;
import dev.turtywurty.industria.util.OutputItemStack;
import dev.turtywurty.industria.util.enums.StringRepresentable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public record CastingRecipe(
    BlockType blockType,
    FluidStack inputFluid,
    OutputItemStack output,
    int coolingTime,
    Optional<Identifier> moldId,
    Optional<MoldCreation> moldCreation
) implements Recipe<CastingRecipeInput> {
    public CastingRecipe(BlockType blockType, FluidStack inputFluid, OutputItemStack output, int coolingTime,
                         Optional<Identifier> moldId) {
        this(blockType, inputFluid, output, coolingTime, moldId, Optional.empty());
    }

    @Override
    public boolean matches(CastingRecipeInput input, Level world) {
        if (input.blockType() != this.blockType || !input.fluidStack().testForRecipe(this.inputFluid))
            return false;

        if (this.moldCreation.isPresent()) {
            return this.blockType == BlockType.TABLE && input.moldId().isEmpty()
                && this.moldCreation.get().template().test(input.template());
        }

        return input.template().isEmpty() && this.moldId.equals(input.moldId());
    }

    @Override
    public ItemStack assemble(CastingRecipeInput input) {
        return assemble(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
    }

    public ItemStack assemble(RandomSource random) {
        ItemStack result = this.output.createStack(random);
        this.moldCreation.ifPresent(creation -> {
            if (!result.isEmpty()) {
                result.set(ModDataComponentTypes.MOLD.get(), new MoldComponent(creation.pattern(), MoldPiece.WHOLE));
            }
        });
        return result;
    }

    public record MoldCreation(Ingredient template, Identifier pattern) {
        public static final Codec<MoldCreation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("template").forGetter(MoldCreation::template),
            Identifier.CODEC.fieldOf("pattern").forGetter(MoldCreation::pattern)).apply(instance, MoldCreation::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MoldCreation> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MoldCreation::template,
            Identifier.STREAM_CODEC, MoldCreation::pattern,
            MoldCreation::new);
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
        return Industria.id("casting").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CastingRecipeInput>> getSerializer() {
        return ModRecipeSerializers.CASTING.get();
    }

    @Override
    public RecipeType<? extends Recipe<CastingRecipeInput>> getType() {
        return ModRecipeTypes.CASTING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.CASTING.get();
    }

    public static class Type implements RecipeType<CastingRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("casting").toString();
        }
    }

    private static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BlockType.CODEC.fieldOf("block_type").forGetter(CastingRecipe::blockType),
        FluidStack.CODEC.fieldOf("input_fluid").forGetter(CastingRecipe::inputFluid),
        OutputItemStack.CODEC.fieldOf("output").forGetter(CastingRecipe::output),
        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("cooling_time").forGetter(CastingRecipe::coolingTime),
        Identifier.CODEC.optionalFieldOf("mold").forGetter(CastingRecipe::moldId),
        MoldCreation.CODEC.optionalFieldOf("mold_creation").forGetter(CastingRecipe::moldCreation))
        .apply(instance, CastingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.composite(
        BlockType.STREAM_CODEC, CastingRecipe::blockType,
        FluidStack.STREAM_CODEC, CastingRecipe::inputFluid,
        OutputItemStack.STREAM_CODEC, CastingRecipe::output,
        ByteBufCodecs.INT, CastingRecipe::coolingTime,
        ByteBufCodecs.optional(Identifier.STREAM_CODEC), CastingRecipe::moldId,
        ByteBufCodecs.optional(MoldCreation.STREAM_CODEC), CastingRecipe::moldCreation,
        CastingRecipe::new);

    public static final RecipeSerializer<CastingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public enum BlockType implements StringRepresentable {
        BASIN,
        TABLE;

        public static final Codec<BlockType> CODEC = StringRepresentable.fromEnum(BlockType.class);
        public static final StreamCodec<ByteBuf, BlockType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
