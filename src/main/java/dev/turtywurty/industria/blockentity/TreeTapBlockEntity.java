package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.LatexBlock;
import dev.turtywurty.industria.block.TreeTapBlock;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class TreeTapBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity {
    private final WrappedFluidStorage<SingleFluidStorage> fluidStorage = new WrappedFluidStorage<>();

    private static final int EXTRACTION_TIME = 20;
    private int progress = 0;
    private final Set<BlockPos> cachedLatexSources = new HashSet<>();
    private boolean latexNetworkDirty = true;

    public TreeTapBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.TREE_TAP, BlockEntityTypeInit.TREE_TAP, pos, state);
        this.fluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BLOCK,
                variant -> variant.is(TagList.Fluids.LATEX)));
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

        if (!storage.isResourceBlank()) {
            FluidVariant variant = storage.variant;
            BlockPos outputPos = this.worldPosition.relative(facing);
            Storage<FluidVariant> outputStorage = FluidStorage.SIDED.find(this.level, outputPos, facing.getOpposite());
            if (outputStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long amountToExtract = Math.min(storage.getAmount(), FluidConstants.INGOT);
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

        if (storage.getAmount() + FluidConstants.INGOT >= storage.getCapacity())
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
            storage.variant = FluidVariant.of(FluidInit.LATEX.still());
            storage.amount += FluidConstants.INGOT;
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

    public Storage<FluidVariant> getFluidProvider(Direction side) {
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
