package dev.turtywurty.industria.metal;

import dev.turtywurty.industria.fluid.FluidRegistryObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public record MetalDefinition(
    Identifier id,
    float meltingTemperature,
    @Nullable Supplier<? extends Block> storageBlock,
    FluidRegistryObject<?, ?> moltenFluid,
    Map<MetalForm, Supplier<? extends Item>> items
) {
    public MetalDefinition {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(moltenFluid, "moltenFluid cannot be null");
        Objects.requireNonNull(items, "items cannot be null");

        if (meltingTemperature <= 0 || Float.isNaN(meltingTemperature) || Float.isInfinite(meltingTemperature))
            throw new IllegalArgumentException("meltingTemperature must be a positive finite number");

        items = Map.copyOf(items);
    }

    public Optional<Item> getItem(MetalForm form) {
        return Optional.ofNullable(items.get(form)).map(Supplier::get);
    }
}
