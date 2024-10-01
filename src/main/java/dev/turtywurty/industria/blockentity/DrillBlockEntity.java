package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrillBlockEntity extends UpdatableBlockEntity implements ExtendedScreenHandlerFactory<BlockPosPayload>, TickableBlockEntity, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("drill");

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private boolean drilling = false;
    private float drillYOffset = 1.0F;
    private boolean retracting = false;
    private int ticks = 0;
    private DrillRenderData renderData; // Only used client side

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.DRILL, pos, state);

        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.getItem() instanceof DrillHeadable));
        getDrillHeadInventory().addListener(inv -> {
            if(this.world != null && this.world.isClient) {
                ItemStack stack = inv.getStack(0);
                setRenderData(stack.getItem() instanceof DrillHeadable drillHeadable ? drillHeadable.createRenderData() : null);
            }
        });
    }

    @Override
    public void tick() {
        if(this.world == null || this.world.isClient)
            return;

        if(this.ticks++ == 0)
            update();

        // Do stuff
        if (isDrilling()) {
            BlockPos down = this.pos.up(MathHelper.ceil(this.drillYOffset));
            BlockState state = this.world.getBlockState(down);
            float hardness = state.getHardness(this.world, pos);
            this.drillYOffset -= (hardness == -1 || hardness == 0) ? 0.01F : (1F / (hardness + 5));

            if(!state.getFluidState().isEmpty()) {
                this.drilling = false;
                this.retracting = true;
                update();
                return;
            } else if(state.isAir()) {
                this.drillYOffset -= 0.1F;
            }

            boolean isThis = false;
            if(state.isOf(BlockInit.MULTIBLOCK_BLOCK) || state.isOf(BlockInit.DRILL)) {
                this.drillYOffset -= 0.01F;
                isThis = true;
            }

            //System.out.println(this.drillYOffset);

            if(this.world.getBlockState(down).getHardness(this.world, pos) == -1F || this.drillYOffset < this.world.getBottomY() - this.pos.getY()) {
                this.drilling = false;
                this.retracting = true;
                this.drillYOffset += 0.01F;
            }

            System.out.println(this.drillYOffset - Math.floor(this.drillYOffset));
            if (!isRetracting() && (this.drillYOffset - Math.floor(this.drillYOffset) > 0.75F) && !isThis && !state.isAir()) {
                this.world.breakBlock(down, false);
                this.drillYOffset += 0.01F;
            }

            update();
        } else if (this.retracting) {
            this.drillYOffset += 0.05F;
            if(this.drillYOffset >= 1.0F) {
                this.retracting = false;
                this.drillYOffset = 1.0F;
            }

            update();
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        ItemStack previousStack = getDrillStack();

        if(nbt.contains("MultiblockPositions", NbtElement.LIST_TYPE)) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MultiblockPositions", NbtElement.INT_ARRAY_TYPE));
        }

        if(nbt.contains("Drilling", NbtElement.BYTE_TYPE)) {
            this.drilling = nbt.getBoolean("Drilling");
        }

        if(nbt.contains("Inventory", NbtElement.LIST_TYPE)) {
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);
        }

        if (nbt.contains("DrillYOffset", NbtElement.FLOAT_TYPE)) {
            this.drillYOffset = nbt.getFloat("DrillYOffset");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
        nbt.putBoolean("Drilling", this.drilling);
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registryLookup));
        nbt.putFloat("DrillYOffset", this.drillYOffset);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    @Override
    public MultiblockType type() {
        return MultiblockType.DRILL;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    // 3x3x3 Multiblock
    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        List<BlockPos> positions = new ArrayList<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = 0; y <= 2; y++) {
                for(int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    positions.add(this.pos.add(x, y, z));
                }
            }
        }

        return positions;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DrillScreenHandler(syncId, playerInventory, this);
    }

    public SimpleInventory getDrillHeadInventory() {
        return this.wrappedInventoryStorage.getInventory(0);
    }

    public boolean isDrilling() {
        return this.drilling;
    }

    public boolean isRetracting() {
        return this.retracting;
    }

    public ItemStack getDrillStack() {
        return getDrillHeadInventory().getStack(0);
    }

    public float getDrillYOffset() {
        return this.drillYOffset;
    }

    public void setDrilling(boolean drilling) {
        this.drilling = drilling;
        this.retracting = !this.drilling;
        update();
    }

    @Override
    @Nullable
    public DrillRenderData getRenderData() {
        return renderData;
    }

    public void setRenderData(DrillRenderData renderData) {
        if(this.world == null || !this.world.isClient)
            return;

        this.renderData = renderData;
    }
}
