package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.LatexBlock;
import dev.turtywurty.industria.block.TreeTapBlock;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.set;

public class TreeTapBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity {
    private final WrappedFluidStorage<SyncingFluidStorage> fluidStorage = new WrappedFluidStorage<>();

    private static final int EXTRACTION_TIME = 20;
    private int progress = 0;
    private final Set<BlockPos> cachedLatexSources = new HashSet<>();
    private boolean latexNetworkDirty = true;

    public TreeTapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TREE_TAP.get(), ModBlockEntityTypes.TREE_TAP.get(), pos, state);
        this.fluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BLOCK,
                variant -> variant.value().is(TagList.Fluids.LATEX)));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getFluidStorage());
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        OutputFluidStorage storage = getFluidStorage();
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        if (!storage.getResource().isBlank()) {
            ResourceVariant<Fluid> variant = storage.getResource();
            BlockPos outputPos = this.worldPosition.relative(facing);
            ResourceStorage<ResourceVariant<Fluid>> outputStorage = TransferService.get().findBlock(StorageKeys.FLUID, this.level, outputPos, facing.getOpposite());
            if (outputStorage != null) {
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    long amountToExtract = Math.min(storage.getAmount(), FluidAmounts.INGOT);
                    long extracted = storage.extract(variant, amountToExtract, transaction);
                    long accepted = outputStorage.insert(variant, extracted, transaction);
                    if (accepted < extracted) {
                        storage.insert(variant, extracted - accepted, transaction);
                    }

                    if (accepted > 0) {
                        transaction.commit();
                        update();
                    }
                }
            }
        }

        if (storage.getAmount() + FluidAmounts.INGOT >= storage.getCapacity())
            return;

        if (progress++ < EXTRACTION_TIME)
            return;

        progress = 0;

        LatexEntry source = findBestLatexSource(facing);
        if (source == null)
            return;

        BlockState stateAfterExtraction = source.latexBlock().extractLatex(source.state());
        if (stateAfterExtraction != null) {
            this.level.setBlock(source.pos(), stateAfterExtraction, Block.UPDATE_ALL);
            set(storage, ResourceTypes.FLUID.of(ModFluids.LATEX.still().get().builtInRegistryHolder()),
                    storage.getAmount() + FluidAmounts.INGOT);
            update();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", this.progress);
        ViewUtils.putChild(output, "FluidStorage", this.fluidStorage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progress = input.getIntOr("Progress", 0);
        this.cachedLatexSources.clear();
        this.latexNetworkDirty = true;
        ViewUtils.readChild(input, "FluidStorage", this.fluidStorage);
    }

    public OutputFluidStorage getFluidStorage() {
        return (OutputFluidStorage) this.fluidStorage.getStorage(null);
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidProvider(Direction side) {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (side == facing)
            return getFluidStorage();

        return null;
    }

    public void markLatexNetworkDirty() {
        this.latexNetworkDirty = true;
    }

    private @Nullable LatexEntry findBestLatexSource(Direction facing) {
        if (this.level == null)
            return null;

        Set<BlockPos> latexSources = getCachedLatexSources(facing);
        if (latexSources.isEmpty())
            return null;

        Comparator<LatexEntry> topDownComparator = Comparator
                .comparingInt((LatexEntry entry) -> entry.pos().getY())
                .thenComparingInt(entry -> -entry.pos().distManhattan(this.worldPosition))
                .thenComparingInt(entry -> entry.pos().getX())
                .thenComparingInt(entry -> entry.pos().getZ());

        return latexSources.stream()
                .map(pos -> {
                    BlockState state = this.level.getBlockState(pos);
                    LatexBlock latexBlock = state.getBlock() instanceof LatexBlock block ? block : null;
                    return new LatexEntry(pos, state, latexBlock);
                })
                .filter(entry -> entry.latexBlock != null)
                .filter(entry -> entry.latexBlock().hasLatex(entry.state()))
                .max(topDownComparator)
                .orElse(null);
    }

    private Set<BlockPos> getCachedLatexSources(Direction facing) {
        if (this.level == null)
            return Collections.emptySet();

        if (this.latexNetworkDirty || !isCachedLatexNetworkValid(this.level)) {
            rebuildLatexNetwork(facing);
        }

        return this.cachedLatexSources;
    }

    private boolean isCachedLatexNetworkValid(Level level) {
        if (this.cachedLatexSources.isEmpty())
            return false;

        for (BlockPos sourcePos : this.cachedLatexSources) {
            if (!(level.getBlockState(sourcePos).getBlock() instanceof LatexBlock))
                return false;
        }

        return true;
    }

    private void rebuildLatexNetwork(Direction facing) {
        this.cachedLatexSources.clear();
        this.latexNetworkDirty = false;

        if (this.level == null)
            return;

        BlockPos attachedPos = this.worldPosition.relative(facing.getOpposite());
        if (!(this.level.getBlockState(attachedPos).getBlock() instanceof LatexBlock))
            return;

        this.cachedLatexSources.addAll(TreeTapBlock.findLatexSources(this.level, attachedPos, Collections.emptySet(), new HashSet<>()));
    }

    private record LatexEntry(BlockPos pos, BlockState state, LatexBlock latexBlock) {
    }
}
