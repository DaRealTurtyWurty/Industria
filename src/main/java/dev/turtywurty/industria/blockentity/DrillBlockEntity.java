package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
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
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import dev.turtywurty.industria.util.enums.StringRepresentable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrillBlockEntity extends UpdatableBlockEntity implements ExtendedScreenHandlerFactory<BlockPosPayload>, TickableBlockEntity, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("drill");

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private boolean drilling = false;
    private float drillYOffset = 1.0F;
    private boolean retracting = false;
    private int ticks = 0;
    private OverflowMethod overflowMethod = OverflowMethod.VOID;
    private final List<ItemStack> overflowStacks = new ArrayList<>(); // Only used if overflowMethod is set to PAUSE

    private DrillRenderData renderData; // Only used client side

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.DRILL, pos, state);

        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.getItem() instanceof DrillHeadable), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.isOf(BlockInit.MOTOR.asItem())), Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.DOWN);

        getDrillHeadInventory().addListener(inv -> {
            if (this.world != null && this.world.isClient) {
                ItemStack stack = inv.getStack(0);
                setRenderData(stack.getItem() instanceof DrillHeadable drillHeadable ? drillHeadable.createRenderData() : null);
            }
        });
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient)
            return;

        if (this.ticks++ == 0)
            update();

        if (getDrillStack().isEmpty())
            return;

        if (!this.overflowStacks.isEmpty()) {
            this.overflowStacks.replaceAll(stack -> getOutputInventory().addStack(stack));
            this.overflowStacks.removeIf(ItemStack::isEmpty);
        }

        DrillHeadable drillHeadable = (DrillHeadable) getDrillStack().getItem();

        // Do stuff
        float currentDrillYOffset = this.drillYOffset;
        if (isDrilling()) {
            this.drillYOffset = drillHeadable.updateDrill(this, this.drillYOffset);
        } else if (this.retracting) {
            this.drillYOffset = drillHeadable.updateRetracting(this, this.drillYOffset);
        }

        if (!this.drilling && !this.retracting && this.drillYOffset != 1.0F && this.overflowMethod == OverflowMethod.PAUSE && this.overflowStacks.isEmpty()) {
            this.drilling = true;
            update();
        }

        if (this.drillYOffset != currentDrillYOffset)
            update();
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("MultiblockPositions", NbtElement.LIST_TYPE)) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MultiblockPositions", NbtElement.INT_ARRAY_TYPE));
        }

        if (nbt.contains("Drilling", NbtElement.BYTE_TYPE)) {
            this.drilling = nbt.getBoolean("Drilling");
        }

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE)) {
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);
        }

        if (nbt.contains("DrillYOffset", NbtElement.FLOAT_TYPE)) {
            this.drillYOffset = nbt.getFloat("DrillYOffset");
        }

        if (nbt.contains("OverflowMethod", NbtElement.STRING_TYPE)) {
            this.overflowMethod = StringRepresentable.getEnumByName(OverflowMethod.values(), nbt.getString("OverflowMethod").toLowerCase(Locale.ROOT));
        }

        if (nbt.contains("OverflowStacks", NbtElement.LIST_TYPE)) {
            this.overflowStacks.clear();
            for (NbtElement element : nbt.getList("OverflowStacks", NbtElement.COMPOUND_TYPE)) {
                this.overflowStacks.add(ItemStack.fromNbtOrEmpty(registryLookup, (NbtCompound) element));
            }

            this.overflowStacks.removeIf(ItemStack::isEmpty);
        }

        if (this.world != null && this.world.isClient && this.renderData == null && getDrillStack().getItem() instanceof DrillHeadable drillHeadable) {
            setRenderData(drillHeadable.createRenderData());
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
        nbt.putBoolean("Drilling", this.drilling);
        nbt.putBoolean("Retracting", this.retracting);
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registryLookup));
        nbt.putFloat("DrillYOffset", this.drillYOffset);
        nbt.putString("OverflowMethod", this.overflowMethod.getSerializedName());

        if (!this.overflowStacks.isEmpty()) {
            var overflowStacks = new NbtList();
            for (ItemStack stack : this.overflowStacks) {
                if (stack.isEmpty())
                    continue;

                overflowStacks.add(stack.toNbt(registryLookup));
            }

            nbt.put("OverflowStacks", overflowStacks);
        }
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

        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
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

    public SimpleInventory getMotorInventory() {
        return this.wrappedInventoryStorage.getInventory(1);
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
    }

    public void setRetracting(boolean retracting) {
        this.retracting = retracting;
    }

    @Override
    @Nullable
    public DrillRenderData getRenderData() {
        return renderData;
    }

    public void setRenderData(DrillRenderData renderData) {
        if (this.world == null || !this.world.isClient)
            return;

        this.renderData = renderData;
    }

    public OverflowMethod getOverflowMethod() {
        return this.overflowMethod;
    }

    public SimpleInventory getOutputInventory() {
        return this.wrappedInventoryStorage.getInventory(2);
    }

    public void handleBlockBreak(BlockPos pos, BlockState state) {
        if (this.world == null || this.world.isClient)
            return;

        List<ItemStack> drops = new ArrayList<>(state.getDroppedStacks(new LootWorldContext.Builder((ServerWorld) world)
                .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                .add(LootContextParameters.BLOCK_STATE, state)
                .add(LootContextParameters.TOOL, Items.DIAMOND_PICKAXE.getDefaultStack())
                .addOptional(LootContextParameters.BLOCK_ENTITY, this)));
        SimpleInventory inventory = getOutputInventory();
        for (int index = 0; index < drops.size(); index++) {
            ItemStack drop = drops.get(index);
            if (drop.isEmpty())
                continue;

            drops.set(index, inventory.addStack(drop));
        }

        switch (this.overflowMethod) {
            case VOID:
                break;
            case PAUSE:
                if (!drops.isEmpty()) {
                    this.drilling = false;
                }

                break;
            case SPILLAGE:
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ItemScatterer.spawn(world, pos.getX(), pos.getY() + 2, pos.getZ(), drop);
                    }
                }
                break;
        }

        world.breakBlock(pos, false);
    }

    public void setOverflowMethod(OverflowMethod overflowMethod) {
        this.overflowMethod = overflowMethod;
    }

    public enum OverflowMethod implements IndustriaEnum<OverflowMethod> {
        VOID, PAUSE, SPILLAGE;

        private final Text text = Text.translatable("industria.overflow_method." + getSerializedName());

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public OverflowMethod next() {
            return values()[(ordinal() + 1) % values().length];
        }

        @Override
        public OverflowMethod previous() {
            return values()[(ordinal() - 1 + values().length) % values().length];
        }

        @Override
        public OverflowMethod[] getValues() {
            return values();
        }

        @Override
        public Text getAsText() {
            return this.text;
        }
    }
}
