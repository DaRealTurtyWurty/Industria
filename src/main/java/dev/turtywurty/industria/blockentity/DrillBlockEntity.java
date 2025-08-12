package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
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
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class DrillBlockEntity extends IndustriaBlockEntity implements BlockEntityWithGui<BlockPosPayload>, SyncableTickableBlockEntity, Multiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("drill");

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final List<ItemStack> overflowStacks = new ArrayList<>(); // Only used if overflowMethod is set to PAUSE
    public float clientMotorRotation;
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

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DRILL, BlockEntityTypeInit.DRILL, pos, state);

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

        if (this.drilling) {
            for (LivingEntity entity : this.world.getEntitiesByClass(LivingEntity.class, this.drillHeadAABB, LivingEntity::isAlive)) {
                entity.damage((ServerWorld) this.world, DamageTypeInit.drillDamageSource(this.world.getDamageSources()), 5.0F);
            }
        }
    }

    @Override
    protected void readData(ReadView view) {
        Multiblockable.read(this, view.getReadView("MultiblockPositions"));

        this.drilling = view.getBoolean("Drilling", false);

        this.retracting = view.getBoolean("Retracting", false);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        this.drillYOffset = view.getFloat("DrillYOffset", 0.0F);
        this.overflowMethod = view.read("OverflowMethod", OverflowMethod.CODEC)
                .orElse(OverflowMethod.VOID);

        this.overflowStacks.clear();
        for (ItemStack stack : view.getTypedListView("OverflowStacks", ItemStack.CODEC)) {
            this.overflowStacks.add(stack);
        }

        this.isPaused = view.getBoolean("Paused", false);

        this.currentRotationSpeed = view.getFloat("CurrentRotationSpeed", 0.0F);

        this.targetRotationSpeed = view.getFloat("TargetRotationSpeed", 0.0F);

        this.drillHeadAABB = view.read("DrillHeadAABB", ExtraCodecs.BOX_CODEC)
                .orElseGet(() -> Box.from(Vec3d.of(this.pos)));

        if (this.world != null && this.world.isClient && this.renderData == null && getDrillStack().getItem() instanceof DrillHeadable drillHeadable) {
            setRenderData(drillHeadable.createRenderData());
        }
    }

    @Override
    protected void writeData(WriteView view) {
        Multiblockable.write(this, view);
        view.putBoolean("Drilling", this.drilling);
        view.putBoolean("Retracting", this.retracting);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        view.putFloat("DrillYOffset", this.drillYOffset);
        view.put("OverflowMethod", OverflowMethod.CODEC, this.overflowMethod);

        if (!this.overflowStacks.isEmpty()) {
            var overflowStacks = view.getListAppender("OverflowStacks", ItemStack.CODEC);
            for (ItemStack stack : this.overflowStacks) {
                if (stack.isEmpty())
                    continue;

                overflowStacks.add(stack);
            }
        }

        view.putBoolean("Paused", this.isPaused);
        view.putFloat("CurrentRotationSpeed", this.currentRotationSpeed);
        view.putFloat("TargetRotationSpeed", this.targetRotationSpeed);

        if (this.drillHeadAABB != null) {
            view.put("DrillHeadAABB", ExtraCodecs.BOX_CODEC, this.drillHeadAABB);
        }
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.DRILL;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    public InventoryStorage getInventoryProvider(@Nullable Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    public EnergyStorage getEnergyProvider(@Nullable Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, MultiblockIOPort> ports = new EnumMap<>(Direction.class);
        if (offsetFromPrimary.getY() == 2) {
            ports.put(Direction.SOUTH, new MultiblockIOPort(Direction.SOUTH, TransferType.ITEM));
        } else if (offsetFromPrimary.getY() == 0 && (offsetFromPrimary.getX() != 0 || offsetFromPrimary.getZ() != 0)) {
            ports.put(Direction.DOWN, new MultiblockIOPort(Direction.DOWN, TransferType.ITEM));
        } else if (offsetFromPrimary.getY() == 1 && offsetFromPrimary.getX() == -1 && offsetFromPrimary.getZ() == 0) {
            ports.put(Direction.UP, new MultiblockIOPort(Direction.UP, TransferType.ITEM));
        } else if (offsetFromPrimary.getY() == 1 && offsetFromPrimary.getX() == 1 && offsetFromPrimary.getZ() == 0) {
            ports.put(Direction.NORTH, new MultiblockIOPort(Direction.NORTH, TransferType.ITEM));
        }

        if (Multiblockable.isCenterColumn(offsetFromPrimary) && offsetFromPrimary.getY() == 2) {
            ports.put(Direction.DOWN, new MultiblockIOPort(Direction.DOWN, TransferType.ENERGY));
        }

        return ports;
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
        return new DrillScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
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

    public void setDrilling(boolean drilling) {
        this.drilling = drilling;
    }

    public boolean isRetracting() {
        return this.retracting;
    }

    public void setRetracting(boolean retracting) {
        this.retracting = retracting;
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

    public float getRotationSpeed() {
        return this.currentRotationSpeed;
    }

    public float getTargetRotationSpeed() {
        return this.targetRotationSpeed;
    }

    public void setTargetRotationSpeed(float targetRotationSpeed) {
        this.targetRotationSpeed = MathHelper.clamp(targetRotationSpeed, 0.0F, 1.0F);
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

    public void setOverflowMethod(OverflowMethod overflowMethod) {
        this.overflowMethod = overflowMethod;
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

    public boolean isPaused() {
        return this.isPaused;
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    public enum OverflowMethod implements IndustriaEnum<OverflowMethod> {
        VOID, PAUSE, SPILLAGE;

        public static final Codec<OverflowMethod> CODEC = Codec.STRING.xmap(str -> OverflowMethod.valueOf(str.toUpperCase(Locale.ROOT)), OverflowMethod::getSerializedName);

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
