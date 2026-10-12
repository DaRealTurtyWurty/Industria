package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.MoldingTableMenu;
import dev.turtywurty.industria.molding.MoldTableVisualState;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class MoldingTableBlockEntity extends IndustriaBlockEntity
    implements
        BlockEntityContentsDropper,
        BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("molding_table");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private @Nullable MoldTableVisualState visualState;

    public MoldingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MOLDING_TABLE.get(), ModBlockEntityTypes.MOLDING_TABLE.get(), pos, state);
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1) {
            @Override
            public void setChanged() {
                MoldTableVisualState visual = MoldingTableBlockEntity.this.visualState;
                if (visual != null && !visual.matches(getItem(0))) {
                    MoldingTableBlockEntity.this.visualState = null;
                }
                super.setChanged();
            }
        });
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
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new MoldingTableMenu(syncId, inventory, this, this.wrappedContainerStorage);
    }

    @Override
    public Block getBlock() {
        return ModBlocks.MOLDING_TABLE.get();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        if (this.visualState != null && this.visualState.matches(getInventory().getItem(0))) {
            output.store("MoldVisual", MoldTableVisualState.CODEC, this.visualState);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);
        this.visualState = input.read("MoldVisual", MoldTableVisualState.CODEC)
            .filter(visual -> visual.matches(getInventory().getItem(0)) && visual.isValid()).orElse(null);
    }

    public SimpleContainer getInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public @Nullable MoldTableVisualState getVisualState() {
        return this.visualState;
    }

    @Override
    public boolean shouldWaitForEndTick() {
        return false;
    }

    public void setVisualState(MoldTableVisualState visual) {
        if (visual.matches(getInventory().getItem(0)) && visual.isValid() && !visual.equals(this.visualState)) {
            this.visualState = visual;
            if (this.level != null && !this.level.isClientSide()) {
                update();
            }
        }
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }
}
