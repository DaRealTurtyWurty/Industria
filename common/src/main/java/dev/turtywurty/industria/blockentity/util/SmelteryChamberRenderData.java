package dev.turtywurty.industria.blockentity.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public record SmelteryChamberRenderData(
    AABB bounds,
    FluidStack fluid,
    long capacity,
    BlockState solidBlockState,
    boolean molten
) {
    public static final Codec<SmelteryChamberRenderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.AABB_CODEC.fieldOf("bounds").forGetter(SmelteryChamberRenderData::bounds),
        FluidStack.CODEC.fieldOf("fluid").forGetter(SmelteryChamberRenderData::fluid),
        Codec.LONG.fieldOf("capacity").forGetter(SmelteryChamberRenderData::capacity),
        BlockState.CODEC.fieldOf("solidBlockState").forGetter(SmelteryChamberRenderData::solidBlockState),
        Codec.BOOL.fieldOf("molten").forGetter(SmelteryChamberRenderData::molten))
        .apply(instance, SmelteryChamberRenderData::new));
}
