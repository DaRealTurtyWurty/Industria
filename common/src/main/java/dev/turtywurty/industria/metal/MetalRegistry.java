package dev.turtywurty.industria.metal;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.*;

public final class MetalRegistry {
    private static final Map<Identifier, MetalDefinition> METALS = new LinkedHashMap<>();

    private MetalRegistry() {
    }

    public static MetalDefinition register(MetalDefinition definition) {
        Objects.requireNonNull(definition, "definition cannot be null");
        if (METALS.putIfAbsent(definition.id(), definition) != null)
            throw new IllegalArgumentException("Metal with id " + definition.id() + " is already registered");

        return definition;
    }

    public static Optional<MetalDefinition> get(Identifier id) {
        return Optional.ofNullable(METALS.get(id));
    }

    public static List<MetalDefinition> getAll() {
        return List.copyOf(METALS.values());
    }

    public static Optional<MetalDefinition> findBy(MetalForm form, Item item) {
        Objects.requireNonNull(form, "form cannot be null");
        Objects.requireNonNull(item, "item cannot be null");

        return METALS.values().stream()
            .filter(definition -> definition.getItem(form).map(i -> i == item).orElse(false))
            .findFirst();
    }

    public static Optional<MetalDefinition> findBy(Block storageBlock) {
        Objects.requireNonNull(storageBlock, "storageBlock cannot be null");

        return METALS.values().stream()
            .filter(definition -> definition.storageBlock() != null && definition.storageBlock().get() == storageBlock)
            .findFirst();
    }

    public static Optional<MetalDefinition> findBy(Fluid fluid) {
        Objects.requireNonNull(fluid, "fluid cannot be null");

        return METALS.values().stream()
            .filter(definition -> definition.moltenFluid().still().get() == fluid
                || definition.moltenFluid().flowing().get() == fluid)
            .findFirst();
    }
}
