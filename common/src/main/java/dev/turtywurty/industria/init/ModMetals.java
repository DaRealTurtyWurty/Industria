package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.fluid.FluidRegistryObject;
import dev.turtywurty.industria.metal.MetalDefinition;
import dev.turtywurty.industria.metal.MetalForm;
import dev.turtywurty.industria.metal.MetalRegistry;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public final class ModMetals {
    private ModMetals() {
    }

    public static final MetalDefinition ALUMINIUM = register("aluminium",
        660.323f,
        ModBlocks.ALUMINIUM_BLOCK,
        ModFluids.MOLTEN_ALUMINIUM,
        Map.of(
            MetalForm.INGOT, ModItems.ALUMINIUM_INGOT,
            MetalForm.NUGGET, ModItems.ALUMINIUM_NUGGET,
            MetalForm.PLATE, ModItems.ALUMINIUM_PLATE,
            MetalForm.POWDER, ModItems.ALUMINIUM_POWDER));

    public static final MetalDefinition SILVER = register("silver",
        961.78f,
        ModBlocks.SILVER_BLOCK,
        ModFluids.MOLTEN_SILVER,
        Map.of(
            MetalForm.INGOT, ModItems.SILVER_INGOT,
            MetalForm.NUGGET, ModItems.SILVER_NUGGET));

    public static final MetalDefinition LEAD = register("lead",
        327.462f,
        ModBlocks.LEAD_BLOCK,
        ModFluids.MOLTEN_LEAD,
        Map.of(
            MetalForm.INGOT, ModItems.LEAD_INGOT,
            MetalForm.NUGGET, ModItems.LEAD_NUGGET));

    public static final MetalDefinition TITANIUM = register("titanium",
        1670f,
        ModBlocks.TITANIUM_BLOCK,
        ModFluids.MOLTEN_TITANIUM,
        Map.of(
            MetalForm.INGOT, ModItems.TITANIUM_INGOT,
            MetalForm.NUGGET, ModItems.TITANIUM_NUGGET,
            MetalForm.PLATE, ModItems.TITANIUM_PLATE));

    public static final MetalDefinition ZINC = register("zinc",
        419.527f,
        ModBlocks.ZINC_BLOCK,
        ModFluids.MOLTEN_ZINC,
        Map.of(
            MetalForm.INGOT, ModItems.ZINC_INGOT,
            MetalForm.NUGGET, ModItems.ZINC_NUGGET));

    public static final MetalDefinition COBALT = register("cobalt",
        1495f,
        ModBlocks.COBALT_BLOCK,
        ModFluids.MOLTEN_COBALT,
        Map.of(
            MetalForm.INGOT, ModItems.COBALT_INGOT,
            MetalForm.NUGGET, ModItems.COBALT_NUGGET));

    public static final MetalDefinition LITHIUM = register("lithium",
        180.5f,
        null,
        ModFluids.MOLTEN_LITHIUM,
        Map.of(
            MetalForm.INGOT, ModItems.LITHIUM_INGOT,
            MetalForm.NUGGET, ModItems.LITHIUM_NUGGET));

    public static final MetalDefinition NICKEL = register("nickel",
        1455f,
        ModBlocks.NICKEL_BLOCK,
        ModFluids.MOLTEN_NICKEL,
        Map.of(
            MetalForm.INGOT, ModItems.NICKEL_INGOT,
            MetalForm.NUGGET, ModItems.NICKEL_NUGGET));

    public static final MetalDefinition IRIDIUM = register("iridium",
        2446f,
        ModBlocks.IRIDIUM_BLOCK,
        ModFluids.MOLTEN_IRIDIUM,
        Map.of(
            MetalForm.INGOT, ModItems.IRIDIUM_INGOT,
            MetalForm.NUGGET, ModItems.IRIDIUM_NUGGET));

    public static final MetalDefinition SILICON = register("silicon",
        1414f,
        null,
        ModFluids.MOLTEN_SILICON,
        Map.of(
            MetalForm.INGOT, ModItems.SILICON_INGOT,
            MetalForm.ROD, ModItems.SILICON_ROD));

    public static final MetalDefinition TIN = register("tin",
        231.928f,
        ModBlocks.TIN_BLOCK,
        ModFluids.MOLTEN_TIN,
        Map.of(
            MetalForm.INGOT, ModItems.TIN_INGOT,
            MetalForm.NUGGET, ModItems.TIN_NUGGET));

    public static final MetalDefinition STEEL = register("steel",
        1500f,
        ModBlocks.STEEL_BLOCK,
        ModFluids.MOLTEN_STEEL,
        Map.of(
            MetalForm.INGOT, ModItems.STEEL_INGOT,
            MetalForm.NUGGET, ModItems.STEEL_NUGGET));

    public static final MetalDefinition IRON = register("iron",
        1538f,
        () -> Blocks.IRON_BLOCK,
        ModFluids.MOLTEN_IRON,
        Map.of(
            MetalForm.INGOT, () -> Items.IRON_INGOT,
            MetalForm.NUGGET, () -> Items.IRON_NUGGET));

    public static final MetalDefinition COPPER = register("copper",
        1084.62f,
        () -> Blocks.COPPER_BLOCK.weathering().unaffected(),
        ModFluids.MOLTEN_COPPER,
        Map.of(
            MetalForm.INGOT, () -> Items.COPPER_INGOT,
            MetalForm.NUGGET, () -> Items.COPPER_NUGGET));

    public static final MetalDefinition GOLD = register("gold",
        1064.18f,
        () -> Blocks.GOLD_BLOCK,
        ModFluids.MOLTEN_GOLD,
        Map.of(
            MetalForm.INGOT, () -> Items.GOLD_INGOT,
            MetalForm.NUGGET, () -> Items.GOLD_NUGGET));

    public static final MetalDefinition NETHERITE = register("netherite",
        2000f,
        () -> Blocks.NETHERITE_BLOCK,
        ModFluids.MOLTEN_NETHERITE,
        Map.of(
            MetalForm.INGOT, () -> Items.NETHERITE_INGOT));

    private static MetalDefinition register(
        String name,
        float meltingTemperature,
        @Nullable Supplier<? extends Block> block,
        FluidRegistryObject<?, ?> fluid,
        Map<MetalForm, Supplier<? extends Item>> forms
    ) {
        return MetalRegistry.register(new MetalDefinition(
            Industria.id(name),
            meltingTemperature,
            block,
            fluid,
            forms));
    }

    public static void init() {
    }
}
