package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.InductionHeaterScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO
public class InductionHeaterBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("induction_heater");
    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SyncingFluidStorage> waterStorage = new WrappedFluidStorage<>();

    public InductionHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.INDUCTION_HEATER.get(), ModBlockEntityTypes.INDUCTION_HEATER.get(), pos, state);

        this.waterStorage.addStorage(new InputFluidStorage(this, FluidAmounts.BUCKET * 10, variant -> variant.value() == Fluids.WATER));
        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        InputFluidStorage waterStorage = getWaterStorage();

        return List.of(energyStorage, waterStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

    }

    @Override
    public void endTick() {

    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new InductionHeaterScreenHandler(syncId, this);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "WaterStorage", this.waterStorage);
        ViewUtils.putChild(view, "EnergyStorage", this.energyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "WaterStorage", this.waterStorage);
        ViewUtils.readChild(view, "EnergyStorage", this.energyStorage);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction side) {
        return this.energyStorage.getStorage(side);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) getEnergyProvider(null);
    }


    public InputFluidStorage getWaterStorage() {
        return (InputFluidStorage) this.waterStorage.getStorage(null);
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidProvider(Direction side) {
        return this.waterStorage.getStorage(null);
    }
}
