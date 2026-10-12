package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.fluid.FluidRegistryObject;
import dev.turtywurty.industria.fluid.IndustriaFluid;
import dev.turtywurty.industria.fluid.MoltenFluid;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModFluids {
    private static final RegistryService REGISTRIES = RegistryService.get();
    private static final Map<String, FluidRegistryObject<?, ?>> MOLTEN_FLUIDS = new LinkedHashMap<>();

    public static final FluidRegistryObject<?, ?> CRUDE_OIL = registerFluid("crude_oil");
    public static final FluidRegistryObject<?, ?> DIRTY_SODIUM_ALUMINATE = registerFluid("dirty_sodium_aluminate");
    public static final FluidRegistryObject<?, ?> SODIUM_ALUMINATE = registerFluid("sodium_aluminate");
    public static final FluidRegistryObject<?, ?> LATEX = registerFluid("latex");
    public static final FluidRegistryObject<?, ?> METHANOL = registerFluid("methanol");
    public static final FluidRegistryObject<?, ?> FORMIC_ACID = registerFluid("formic_acid");
    public static final FluidRegistryObject<?, ?> DILUTED_FORMIC_ACID = registerFluid("diluted_formic_acid");

    // Molten Fluids
    public static final FluidRegistryObject<?, ?> MOLTEN_ALUMINIUM = registerMoltenFluid("molten_aluminium");
    public static final FluidRegistryObject<?, ?> MOLTEN_SILVER = registerMoltenFluid("molten_silver");
    public static final FluidRegistryObject<?, ?> MOLTEN_LEAD = registerMoltenFluid("molten_lead");
    public static final FluidRegistryObject<?, ?> MOLTEN_TITANIUM = registerMoltenFluid("molten_titanium");
    public static final FluidRegistryObject<?, ?> MOLTEN_ZINC = registerMoltenFluid("molten_zinc");
    public static final FluidRegistryObject<?, ?> MOLTEN_COBALT = registerMoltenFluid("molten_cobalt");
    public static final FluidRegistryObject<?, ?> MOLTEN_LITHIUM = registerMoltenFluid("molten_lithium");
    public static final FluidRegistryObject<?, ?> MOLTEN_NICKEL = registerMoltenFluid("molten_nickel");
    public static final FluidRegistryObject<?, ?> MOLTEN_IRIDIUM = registerMoltenFluid("molten_iridium");
    public static final FluidRegistryObject<?, ?> MOLTEN_SILICON = registerMoltenFluid("molten_silicon");
    public static final FluidRegistryObject<?, ?> MOLTEN_TIN = registerMoltenFluid("molten_tin");
    public static final FluidRegistryObject<?, ?> MOLTEN_STEEL = registerMoltenFluid("molten_steel");
    public static final FluidRegistryObject<?, ?> MOLTEN_IRON = registerMoltenFluid("molten_iron");
    public static final FluidRegistryObject<?, ?> MOLTEN_COPPER = registerMoltenFluid("molten_copper");
    public static final FluidRegistryObject<?, ?> MOLTEN_GOLD = registerMoltenFluid("molten_gold");
    public static final FluidRegistryObject<?, ?> MOLTEN_NETHERITE = registerMoltenFluid("molten_netherite");
    public static final FluidRegistryObject<?, ?> MOLTEN_CRYOLITE = registerMoltenFluid("molten_cryolite");

    public static FluidRegistryObject<MoltenFluid.Still, MoltenFluid.Flowing> registerMoltenFluid(String name) {
        var fluid = registerFluid(
            name,
            MoltenFluid.Still::new,
            MoltenFluid.Flowing::new);
        MOLTEN_FLUIDS.put(name, fluid);
        return fluid;
    }

    public static Map<String, FluidRegistryObject<?, ?>> getMoltenFluids() {
        return Collections.unmodifiableMap(MOLTEN_FLUIDS);
    }

    public static FluidRegistryObject<IndustriaFluid.Still, IndustriaFluid.Flowing> registerFluid(String name) {
        return registerFluid(
            name,
            IndustriaFluid.Still::new,
            IndustriaFluid.Flowing::new);
    }

    public static <S extends IndustriaFluid, F extends IndustriaFluid> FluidRegistryObject<S, F> registerFluid(
        String name,
        FluidRegistryObject.IndustriaFluidFactory<S> stillFactory,
        FluidRegistryObject.IndustriaFluidFactory<F> flowingFactory
    ) {
        AtomicReference<RegistrationHandle<Fluid, S>> stillRef = new AtomicReference<>();
        AtomicReference<RegistrationHandle<Fluid, F>> flowingRef = new AtomicReference<>();
        AtomicReference<RegistrationHandle<Item, BucketItem>> bucketRef = new AtomicReference<>();
        AtomicReference<RegistrationHandle<Block, LiquidBlock>> blockRef = new AtomicReference<>();

        var still = REGISTRIES.registerFluid(
            Industria.id(name),
            () -> stillFactory.create(
                () -> stillRef.get().get(),
                () -> flowingRef.get().get(),
                () -> bucketRef.get().get(),
                () -> blockRef.get().get()));
        stillRef.set(still);

        var flowing = REGISTRIES.registerFluid(
            Industria.id("flowing_" + name),
            () -> flowingFactory.create(
                () -> stillRef.get().get(),
                () -> flowingRef.get().get(),
                () -> bucketRef.get().get(),
                () -> blockRef.get().get()));
        flowingRef.set(flowing);

        RegistrationHandle<Item, BucketItem> bucket = REGISTRIES.registerItem(
            Industria.id(name + "_bucket"),
            () -> new BucketItem(
                still.get(),
                new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BUCKET)
                    .setId(bucketRef.get().key())));
        bucketRef.set(bucket);

        RegistrationHandle<Block, LiquidBlock> block = REGISTRIES.registerBlock(
            Industria.id(name),
            () -> new RegisteredLiquidBlock(
                still.get(),
                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                    .setId(blockRef.get().key())));
        blockRef.set(block);

        return new FluidRegistryObject<>(still, flowing, bucket, block);
    }

    public static void init() {
    }

    private static final class RegisteredLiquidBlock extends LiquidBlock {
        private RegisteredLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
            super(fluid, properties);
        }
    }
}
