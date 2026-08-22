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

public class ModFluids {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final FluidRegistryObject<?, ?> CRUDE_OIL = registerFluid("crude_oil");
    public static final FluidRegistryObject<?, ?> DIRTY_SODIUM_ALUMINATE = registerFluid("dirty_sodium_aluminate");
    public static final FluidRegistryObject<?, ?> SODIUM_ALUMINATE = registerFluid("sodium_aluminate");
    public static final FluidRegistryObject<?, ?> MOLTEN_ALUMINIUM = registerMoltenFluid("molten_aluminium");
    public static final FluidRegistryObject<?, ?> MOLTEN_CRYOLITE = registerMoltenFluid("molten_cryolite");
    public static final FluidRegistryObject<?, ?> LATEX = registerFluid("latex");
    public static final FluidRegistryObject<?, ?> METHANOL = registerFluid("methanol");
    public static final FluidRegistryObject<?, ?> FORMIC_ACID = registerFluid("formic_acid");
    public static final FluidRegistryObject<?, ?> DILUTED_FORMIC_ACID = registerFluid("diluted_formic_acid");

    public static FluidRegistryObject<MoltenFluid.Still, MoltenFluid.Flowing> registerMoltenFluid(String name) {
        return registerFluid(
                name,
                MoltenFluid.Still::new,
                MoltenFluid.Flowing::new
        );
    }

    public static FluidRegistryObject<IndustriaFluid.Still, IndustriaFluid.Flowing> registerFluid(String name) {
        return registerFluid(
                name,
                IndustriaFluid.Still::new,
                IndustriaFluid.Flowing::new
        );
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
                        () -> blockRef.get().get()
                )
        );
        stillRef.set(still);

        var flowing = REGISTRIES.registerFluid(
                Industria.id("flowing_" + name),
                () -> flowingFactory.create(
                        () -> stillRef.get().get(),
                        () -> flowingRef.get().get(),
                        () -> bucketRef.get().get(),
                        () -> blockRef.get().get()
                )
        );
        flowingRef.set(flowing);

        RegistrationHandle<Item, BucketItem> bucket =
                REGISTRIES.registerItem(
                        Industria.id(name + "_bucket"),
                        () -> new BucketItem(
                                still.get(),
                                new Item.Properties()
                                        .stacksTo(1)
                                        .craftRemainder(Items.BUCKET)
                                        .setId(bucketRef.get().key())
                        )
                );
        bucketRef.set(bucket);

        RegistrationHandle<Block, LiquidBlock> block =
                REGISTRIES.registerBlock(
                        Industria.id(name),
                        () -> new RegisteredLiquidBlock(
                                still.get(),
                                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                                        .setId(blockRef.get().key())
                        )
                );
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
