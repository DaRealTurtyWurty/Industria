package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.util.AgitatorPortType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AgitatorPortStack(AgitatorPortType type, ItemStack item, FluidStack fluid, GasStack gas, SlurryStack slurry) {
    public static final AgitatorPortStack EMPTY = new AgitatorPortStack(
            AgitatorPortType.ITEM,
            ItemStack.EMPTY,
            FluidStack.EMPTY,
            GasStack.EMPTY,
            SlurryStack.EMPTY
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorPortStack> STREAM_CODEC = StreamCodec.composite(
            AgitatorPortType.STREAM_CODEC, AgitatorPortStack::type,
            ItemStack.STREAM_CODEC, AgitatorPortStack::item,
            FluidStack.STREAM_CODEC, AgitatorPortStack::fluid,
            GasStack.STREAM_CODEC, AgitatorPortStack::gas,
            SlurryStack.STREAM_CODEC, AgitatorPortStack::slurry,
            AgitatorPortStack::new
    );

    public AgitatorPortStack {
        if (type == null)
            type = AgitatorPortType.ITEM;

        if (item == null)
            item = ItemStack.EMPTY;

        if (fluid == null)
            fluid = FluidStack.EMPTY;

        if (gas == null)
            gas = GasStack.EMPTY;

        if (slurry == null)
            slurry = SlurryStack.EMPTY;
    }

    public boolean isEmpty() {
        return switch (this.type) {
            case ITEM -> this.item.isEmpty();
            case FLUID -> this.fluid.isEmpty();
            case GAS -> this.gas.isEmpty();
            case SLURRY -> this.slurry.isEmpty();
        };
    }
}
