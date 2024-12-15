package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.DamageTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import dev.turtywurty.industria.util.enums.StringRepresentable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
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
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrillBlockEntity extends UpdatableBlockEntity implements ExtendedScreenHandlerFactory<BlockPosPayload>, SyncableTickableBlockEntity, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("drill");

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final List<ItemStack> overflowStacks = new ArrayList<>(); // Only used if overflowMethod is set to PAUSE
    private boolean drilling = false;
    private float drillYOffset = 1.0F;
    private boolean retracting = false;
    private int ticks = 0;
    private OverflowMethod overflowMethod = OverflowMethod.VOID;
    private float currentRotationSpeed = 0.0F, targetRotationSpeed = 0.75F;
    private boolean isPaused;
    private Box drillHeadAABB;

    // Only used client side
    private DrillRenderData renderData;
    public float clientMotorRotation;

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.DRILL, pos, state);

        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.getItem() instanceof DrillHeadable) {
            @Override
            public int getMaxCountPerStack() {
                return 1;
            }
        }, Direction.UP);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.isOf(BlockInit.MOTOR.asItem())) {
            @Override
            public int getMaxCountPerStack() {
                return 1;
            }
        }, Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.DOWN);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 3, (stack, slot) -> stack.getItem() instanceof BlockItem), Direction.SOUTH);

        getDrillHeadInventory().addListener(inv -> {
            if (this.world != null && this.world.isClient) {
                ItemStack stack = inv.getStack(0);
                setRenderData(stack.getItem() instanceof DrillHeadable drillHeadable ? drillHeadable.createRenderData() : null);
            }
        });

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var drillHeadInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        var motorInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
        var outputInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(2);
        var placeableBlockInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(3);
        var energyStorage = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(drillHeadInventory, motorInventory, outputInventory, placeableBlockInventory, energyStorage);
    }

    @Override
    public void onTick() {
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

        {
            boolean currentDrilling = this.drilling;
            boolean currentRetracting = this.retracting;
            if (!hasMotor()) {
                this.drilling = false;
                this.retracting = false;

                if (currentDrilling || currentRetracting) {
                    update();
                }

                return;
            }
        }

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
        float previousRotationSpeed = this.currentRotationSpeed;
        long previousEnergy = energyStorage.getAmount();

        if ((this.drilling || this.retracting) && energyStorage.getAmount() > 0) {
            long energyRequired = MotorBlockEntity.calculateEnergyForRotation(this.currentRotationSpeed, this.targetRotationSpeed);
            if (energyStorage.getAmount() < energyRequired) {
                this.currentRotationSpeed = MotorBlockEntity.calculateRotationSpeed(energyStorage.getAmount());
                energyStorage.amount = 0;
            } else {
                energyStorage.amount -= energyRequired;
                this.currentRotationSpeed = this.targetRotationSpeed;
            }
        } else {
            this.currentRotationSpeed = MathHelper.clamp(this.currentRotationSpeed - 0.01F, 0.0F, this.targetRotationSpeed);
        }

        if (this.currentRotationSpeed > 0.0F) {
            this.isPaused = false;
        } else {
            boolean currentPaused = this.isPaused;
            this.isPaused = true;

            if (!currentPaused)
                update();
        }

        if (this.isPaused)
            return;

        if (previousRotationSpeed != this.currentRotationSpeed || previousEnergy != energyStorage.getAmount())
            update();

        DrillHeadable drillHeadable = (DrillHeadable) getDrillStack().getItem();

        // Do stuff
        float currentDrillYOffset = this.drillYOffset;
        if (this.drilling) {
            this.drillYOffset = drillHeadable.updateDrill(this, this.drillYOffset);
        } else if (this.retracting) {
            this.drillYOffset = drillHeadable.updateRetracting(this, this.drillYOffset);
        }

        if (!this.drilling && !this.retracting && this.drillYOffset != 1.0F && this.overflowMethod == OverflowMethod.PAUSE && this.overflowStacks.isEmpty()) {
            this.drilling = true;
            update();
        }

        if (this.drillYOffset != currentDrillYOffset) {
            this.drillHeadAABB = new Box(this.pos.getX(), this.pos.getY() + 1 + this.drillYOffset, this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + this.drillYOffset, this.pos.getZ() + 1);
            update();
        }

        if(this.drilling) {
            for (LivingEntity entity : this.world.getEntitiesByClass(LivingEntity.class, this.drillHeadAABB, LivingEntity::isAlive)) {
                entity.damage((ServerWorld) this.world, DamageTypeInit.drillDamageSource(this.world.getDamageSources()), 5.0F);
            }
        }
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

        if (nbt.contains("Retracting", NbtElement.BYTE_TYPE)) {
            this.retracting = nbt.getBoolean("Retracting");
        }

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE)) {
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);
        }

        if (nbt.contains("Energy", NbtElement.LIST_TYPE)) {
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registryLookup);
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

        if (nbt.contains("Paused", NbtElement.BYTE_TYPE)) {
            this.isPaused = nbt.getBoolean("Paused");
        }

        if (nbt.contains("CurrentRotationSpeed", NbtElement.FLOAT_TYPE)) {
            this.currentRotationSpeed = nbt.getFloat("CurrentRotationSpeed");
        }

        if (nbt.contains("TargetRotationSpeed", NbtElement.FLOAT_TYPE)) {
            this.targetRotationSpeed = nbt.getFloat("TargetRotationSpeed");
        }

        if (nbt.contains("DrillHeadAABB", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("DrillHeadAABB", NbtElement.DOUBLE_TYPE);
            this.drillHeadAABB = new Box(list.getDouble(0), list.getDouble(1), list.getDouble(2), list.getDouble(3), list.getDouble(4), list.getDouble(5));
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
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registryLookup));
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

        nbt.putBoolean("Paused", this.isPaused);
        nbt.putFloat("CurrentRotationSpeed", this.currentRotationSpeed);
        nbt.putFloat("TargetRotationSpeed", this.targetRotationSpeed);

        if (this.drillHeadAABB != null) {
            var list = new NbtList();
            list.add(NbtDouble.of(this.drillHeadAABB.minX));
            list.add(NbtDouble.of(this.drillHeadAABB.minY));
            list.add(NbtDouble.of(this.drillHeadAABB.minZ));
            list.add(NbtDouble.of(this.drillHeadAABB.maxX));
            list.add(NbtDouble.of(this.drillHeadAABB.maxY));
            list.add(NbtDouble.of(this.drillHeadAABB.maxZ));
            nbt.put("DrillHeadAABB", list);
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
    public MultiblockType<?> type() {
        return MultiblockTypeInit.DRILL;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    public InventoryStorage getInventoryStorage(Vec3i offsetFromPrimary, @Nullable Direction direction) {
        if (offsetFromPrimary.getY() == 2) {
            return this.wrappedInventoryStorage.getStorage(Direction.SOUTH);
        } else if (offsetFromPrimary.getY() == 0 && (offsetFromPrimary.getX() != 0 || offsetFromPrimary.getZ() != 0)) {
            return this.wrappedInventoryStorage.getStorage(Direction.DOWN);
        } else if (offsetFromPrimary.getY() == 1 && offsetFromPrimary.getX() == -1 && offsetFromPrimary.getZ() == 0) {
            return this.wrappedInventoryStorage.getStorage(Direction.UP);
        } else if (offsetFromPrimary.getY() == 1 && offsetFromPrimary.getX() == 1 && offsetFromPrimary.getZ() == 0) {
            return this.wrappedInventoryStorage.getStorage(Direction.NORTH);
        }

        return null;
    }

    @Override
    public EnergyStorage getEnergyStorage(Vec3i offsetFromPrimary, @Nullable Direction direction) {
        return (offsetFromPrimary.getY() == 0 && (offsetFromPrimary.getX() != 0 || offsetFromPrimary.getZ() != 0)) ?
                this.wrappedEnergyStorage.getStorage(null) : null;
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

    public SimpleInventory getPlaceableBlockInventory() {
        return this.wrappedInventoryStorage.getInventory(3);
    }

    public EnergyStorage getEnergyStorage() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public boolean isDrilling() {
        return this.drilling;
    }

    public boolean isRetracting() {
        return this.retracting;
    }

    public boolean hasMotor() {
        return !getMotorInventory().isEmpty();
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

    public void setTargetRotationSpeed(float targetRotationSpeed) {
        this.targetRotationSpeed = MathHelper.clamp(targetRotationSpeed, 0.0F, 1.0F);
    }

    public float getRotationSpeed() {
        return this.currentRotationSpeed;
    }

    public float getTargetRotationSpeed() {
        return this.targetRotationSpeed;
    }

    public Box getDrillHeadAABB() {
        return this.drillHeadAABB;
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
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return;

        List<ItemStack> drops = new ArrayList<>(state.getDroppedStacks(new LootWorldContext.Builder(serverWorld)
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

        drops.removeIf(ItemStack::isEmpty);

        switch (this.overflowMethod) {
            case VOID:
                break;
            case PAUSE:
                if (!drops.isEmpty()) {
                    this.drilling = false;
                    this.retracting = false;
                    this.overflowStacks.addAll(drops);
                    update();
                }

                break;
            case SPILLAGE:
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ItemScatterer.spawn(world, this.pos.getX(), this.pos.getY() + 3.5, this.pos.getZ(), drop);
                    }
                }

                break;
        }

        world.breakBlock(pos, false);
    }

    public void setOverflowMethod(OverflowMethod overflowMethod) {
        this.overflowMethod = overflowMethod;
    }

    public boolean isPaused() {
        return this.isPaused;
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
