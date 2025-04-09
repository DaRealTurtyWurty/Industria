package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.fluid.FluidRegistryObject;
import dev.turtywurty.industria.fluid.IndustriaFluid;
import dev.turtywurty.industria.fluid.MoltenFluid;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.concurrent.atomic.AtomicReference;

public class FluidInit {
    public static final FluidRegistryObject<?, ?> CRUDE_OIL = registerFluid("crude_oil");
    public static final FluidRegistryObject<?, ?> DIRTY_SODIUM_ALUMINATE = registerFluid("dirty_sodium_aluminate");
    public static final FluidRegistryObject<?, ?> SODIUM_ALUMINATE = registerFluid("sodium_aluminate");
    public static final FluidRegistryObject<?, ?> MOLTEN_ALUMINIUM = registerMoltenFluid("molten_aluminium");
    public static final FluidRegistryObject<?, ?> MOLTEN_CRYOLITE = registerMoltenFluid("molten_cryolite");

    public static FluidRegistryObject<MoltenFluid.Still, MoltenFluid.Flowing> registerMoltenFluid(String name) {
        return registerFluid(name, MoltenFluid.Still::new, MoltenFluid.Flowing::new);
    }

    public static FluidRegistryObject<IndustriaFluid.Still, IndustriaFluid.Flowing> registerFluid(String name) {
        return registerFluid(name, IndustriaFluid.Still::new, IndustriaFluid.Flowing::new);
    }

    public static <S extends IndustriaFluid, F extends IndustriaFluid> FluidRegistryObject<S, F> registerFluid(String name, FluidRegistryObject.IndustriaFluidFactory<S> stillFactory, FluidRegistryObject.IndustriaFluidFactory<F> flowingFactory) {
        final AtomicReference<S> still = new AtomicReference<>();
        final AtomicReference<F> flowing = new AtomicReference<>();
        final AtomicReference<BucketItem> bucket = new AtomicReference<>();
        final AtomicReference<FluidBlock> block = new AtomicReference<>();

        still.set(register(name, stillFactory.create(still::get, flowing::get, bucket::get, block::get)));
        flowing.set(register("flowing_" + name, flowingFactory.create(still::get, flowing::get, bucket::get, block::get)));
        bucket.set(ItemInit.register(name + "_bucket", settings -> new BucketItem(still.get(), settings), settings -> settings.maxCount(1).recipeRemainder(Items.BUCKET)));
        block.set(BlockInit.registerWithCopy(name, settings -> new FluidBlock(still.get(), settings), Blocks.WATER, settings -> settings));

        return new FluidRegistryObject<>(still.get(), flowing.get(), bucket.get(), block.get());
    }

    public static <T extends Fluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, Industria.id(name), fluid);
    }

    public static void init() {}
}
