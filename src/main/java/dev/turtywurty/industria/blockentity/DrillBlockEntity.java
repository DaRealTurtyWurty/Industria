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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.DamageTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrillBlockEntity extends IndustriaBlockEntity implements BlockEntityWithGui<BlockPosPayload>, SyncableTickableBlockEntity, AutoMultiblockable, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("drill");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 2)
                    .on(LocalDirection.BACK)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && (p.x() != 0 || p.z() != 0))
                    .on(LocalDirection.DOWN)
                    .types(PortType.output(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 1 && p.x() == -1 && p.z() == 0)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 1 && p.x() == 1 && p.z() == 0)
                    .on(LocalDirection.FRONT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.isCenterColumn() && p.y() == 2)
                    .on(LocalDirection.DOWN)
                    .types(PortType.input(TransferType.ENERGY))
                    .build()
    );

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
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
    private AABB drillHeadAABB;

    // Only used client side
    public float clockwiseRotation, counterClockwiseRotation;

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DRILL, BlockEntityTypeInit.DRILL, pos, state);

        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.getItem() instanceof DrillHeadable) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, Direction.UP);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1, (stack, slot) -> stack.is(BlockInit.MOTOR.asItem())) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.DOWN);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 3, (stack, slot) -> stack.getItem() instanceof BlockItem), Direction.SOUTH);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var drillHeadInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        var motorInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
        var outputInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(2);
        var placeableBlockInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(3);
        var energyStorage = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(drillHeadInventory, motorInventory, outputInventory, placeableBlockInventory, energyStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.ticks++ == 0)
            update();

        if (getDrillStack().isEmpty())
            return;

        if (!this.overflowStacks.isEmpty()) {
            this.overflowStacks.replaceAll(stack -> getOutputInventory().addItem(stack));
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
            this.currentRotationSpeed = Mth.clamp(this.currentRotationSpeed - 0.01F, 0.0F, this.targetRotationSpeed);
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
            this.drillHeadAABB = new AABB(this.worldPosition.getX(), this.worldPosition.getY() + 1 + this.drillYOffset, this.worldPosition.getZ(), this.worldPosition.getX() + 1, this.worldPosition.getY() + this.drillYOffset, this.worldPosition.getZ() + 1);
            update();
        }

        if (this.drilling) {
            for (LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, this.drillHeadAABB, LivingEntity::isAlive)) {
                entity.hurtServer((ServerLevel) this.level, DamageTypeInit.drillDamageSource(this.level.damageSources()), 5.0F);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        Multiblockable.read(this, view);

        this.drilling = view.getBooleanOr("Drilling", false);

        this.retracting = view.getBooleanOr("Retracting", false);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        this.drillYOffset = view.getFloatOr("DrillYOffset", 0.0F);
        this.overflowMethod = view.read("OverflowMethod", OverflowMethod.CODEC)
                .orElse(OverflowMethod.VOID);

        this.overflowStacks.clear();
        for (ItemStack stack : view.listOrEmpty("OverflowStacks", ItemStack.CODEC)) {
            this.overflowStacks.add(stack);
        }

        this.isPaused = view.getBooleanOr("Paused", false);

        this.currentRotationSpeed = view.getFloatOr("CurrentRotationSpeed", 0.0F);

        this.targetRotationSpeed = view.getFloatOr("TargetRotationSpeed", 0.0F);

        this.drillHeadAABB = view.read("DrillHeadAABB", ExtraCodecs.BOX_CODEC)
                .orElseGet(() -> AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(this.worldPosition)));
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        Multiblockable.write(this, view);
        view.putBoolean("Drilling", this.drilling);
        view.putBoolean("Retracting", this.retracting);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        view.putFloat("DrillYOffset", this.drillYOffset);
        view.store("OverflowMethod", OverflowMethod.CODEC, this.overflowMethod);

        if (!this.overflowStacks.isEmpty()) {
            var overflowStacks = view.list("OverflowStacks", ItemStack.CODEC);
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
            view.store("DrillHeadAABB", ExtraCodecs.BOX_CODEC, this.drillHeadAABB);
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

    public ContainerStorage getInventoryProvider(@Nullable Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public EnergyStorage getEnergyProvider(@Nullable Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
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

                    positions.add(this.worldPosition.offset(x, y, z));
                }
            }
        }

        return positions;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new DrillScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    public SimpleContainer getDrillHeadInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public SimpleContainer getMotorInventory() {
        return this.wrappedContainerStorage.getInventory(1);
    }

    public SimpleContainer getPlaceableBlockInventory() {
        return this.wrappedContainerStorage.getInventory(3);
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
        return getDrillHeadInventory().getItem(0);
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
        this.targetRotationSpeed = Mth.clamp(targetRotationSpeed, 0.0F, 1.0F);
    }

    public AABB getDrillHeadAABB() {
        return this.drillHeadAABB;
    }

    public OverflowMethod getOverflowMethod() {
        return this.overflowMethod;
    }

    public void setOverflowMethod(OverflowMethod overflowMethod) {
        this.overflowMethod = overflowMethod;
    }

    public SimpleContainer getOutputInventory() {
        return this.wrappedContainerStorage.getInventory(2);
    }

    public void handleBlockBreak(BlockPos pos, BlockState state) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return;

        List<ItemStack> drops = new ArrayList<>(state.getDrops(new LootParams.Builder(serverWorld)
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance())
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, this)));
        SimpleContainer inventory = getOutputInventory();
        for (int index = 0; index < drops.size(); index++) {
            ItemStack drop = drops.get(index);
            if (drop.isEmpty())
                continue;

            drops.set(index, inventory.addItem(drop));
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
                        Containers.dropItemStack(level, this.worldPosition.getX(), this.worldPosition.getY() + 3.5, this.worldPosition.getZ(), drop);
                    }
                }

                break;
        }

        level.destroyBlock(pos, false);
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public enum OverflowMethod implements IndustriaEnum<OverflowMethod> {
        VOID, PAUSE, SPILLAGE;

        public static final Codec<OverflowMethod> CODEC = Codec.STRING.xmap(str -> OverflowMethod.valueOf(str.toUpperCase(Locale.ROOT)), OverflowMethod::getSerializedName);

        private final Component text = Component.translatable("industria.overflow_method." + getSerializedName());

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
        public Component getAsText() {
            return this.text;
        }
    }
}